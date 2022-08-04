package de.tum.in.www1.artemis.web.rest;

import static de.tum.in.www1.artemis.web.rest.GitServerResourceEndpoints.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.PacketLineIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/*
 * REST controller for providing access to the local git server via the Git Http Protocol.
 */
@RestController
@RequestMapping("/git")
public class GitServerResource {

    private final Logger log = LoggerFactory.getLogger(GitServerResource.class);

    @Value("${artemis.version-control.default-branch:main}")
    protected String defaultBranch;

    private String localGitPath = "../../TestGitRepos/";

    enum GitServiceOption {

        UPLOADPACK("git-upload-pack"), RECEIVEPACK("git-receive-pack");

        private final String service;

        GitServiceOption(final String service) {
            this.service = service;
        }

        @Override
        public String toString() {
            return service;
        }
    }

    public GitServerResource() {

    }

    /*
     * GET $GIT_URL/info/refs?service=git-upload-pack Necessary for git fetch and push. $GIT_URL for now has the form "http://localhost:8080/git/:projectKey/:repoName" Discover the
     * capabilities/references available on the remote repository.
     */
    @RequestMapping(value = "/{projectKey}/{repoName}/info/refs", method = RequestMethod.GET, produces = "application/x-git-upload-pack-advertisement")
    public ResponseEntity<byte[]> getCapabilities(@PathVariable("projectKey") String projectKey, @PathVariable("repoName") String repoName,
            @RequestParam("service") String serviceName, @RequestHeader(value = "Git-Protocol", required = false) String gitProtocolVersion) {
        log.debug("REST request to get capabilites for repo {} in project {} using protocol {}.", repoName, projectKey, gitProtocolVersion);

        // If the requested service name is not "git-upload-pack" the server must
        // respond with the 403 Forbidden HTTP status code.
        if (!(serviceName.equals(GitServiceOption.UPLOADPACK.toString()))) {
            return new ResponseEntity<byte[]>(HttpStatus.FORBIDDEN);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/x-git-upload-pack-advertisement"));

        String result = "";

        if ("version=2".equals(gitProtocolVersion)) {
            result += "000eversion 2\n0000";
            return new ResponseEntity<byte[]>(result.getBytes(), headers, HttpStatus.OK);
        }

        // Using protocol version 1

        // Try to find the repository stated in the URL locally.
        String repoPath = localGitPath + projectKey + "/" + repoName;
        if (!Files.exists(Paths.get(repoPath))) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Open the repository using JGit.
        Git git;

        try {
            git = Git.open(new File(repoPath));
        }
        catch (IOException e) {
            log.warn("Cannot open existing repository by local path {}", repoPath, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        List<Ref> refs = Collections.emptyList();

        try {
            // For now only return branches as tags are not needed for fetch and pull.
            refs = git.branchList().call();
        }
        catch (GitAPIException e) {
            log.warn("Something went wrong trying to get the references from the repository {}.", repoName, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        result += "001e# service=git-upload-pack\n0000";

        if (refs.isEmpty()) {
            // Construct an empty_list = PKT-LINE(zero-id SP "capabilities^{}" NUL cap-list
            // LF)
            // result += "003f0000000000000000000000000000000000000000
            // capabilities^{}\0\n0000";
            result += "0000";
            log.debug("Returning result {}", result);
            return new ResponseEntity<byte[]>(result.getBytes(), HttpStatus.OK);
        }

        for (int i = 0; i < refs.size(); i++) {
            log.debug("Ref found: {}", refs.get(i).getName() + " " + refs.get(i).getObjectId().getName());
            if (i == 0) {
                // First response entry is constructed differently.
                String refOutput = refs.get(i).getObjectId().getName() + " " + refs.get(i).getName() + "\0\n";
                result += pktLineFromString(refOutput);
            }
            else {
                String refOutput = refs.get(i).getObjectId().getName() + " " + refs.get(i).getName() + "\n";
                result += pktLineFromString(refOutput);
            }
        }

        result += "0000";

        log.debug("Returning result {}", result);

        return new ResponseEntity<byte[]>(result.getBytes(), headers, HttpStatus.OK);

    }

    List<String> wantedList = new ArrayList<String>();

    /*
     * POST $GIT_URL/git-upload-pack Disc
     */
    @RequestMapping(value = "/{projectKey}/{repoName}/git-upload-pack", method = RequestMethod.POST, produces = "application/x-git-upload-pack-result")
    public ResponseEntity<byte[]> getACKs(@PathVariable("projectKey") String projectKey, @PathVariable("repoName") String repoName,
            @RequestHeader(value = "Git-Protocol", required = false) String gitProtocolVersion, @RequestBody String wantsAndHaves) {
        log.debug("REST request to get ACKs for repo {} in project {} using protocol {} for the following wants: {}", repoName, projectKey, gitProtocolVersion, wantsAndHaves);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/x-git-upload-pack-result"));

        String result = "";

        // Try to find the repository stated in the URL locally.
        String repoPath = localGitPath + projectKey + "/" + repoName;
        if (!Files.exists(Paths.get(repoPath))) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Open the repository using JGit.
        Git git;
        try {
            git = Git.open(new File(repoPath));
        }
        catch (IOException e) {
            log.warn("Cannot open existing repository by local path {}", repoPath, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // This endpoint will respond with a Packfile only if "done" was sent in the
        // request.
        Boolean sendPackFile = false;

        // Find available refs.
        Set<String> refsSet = new HashSet<>();

        try {
            List<Ref> refsList = git.branchList().call();
            for (Ref ref : refsList) {
                refsSet.add(ref.getObjectId().getName());
            }
        }
        catch (GitAPIException e) {
            log.warn("Something went wrong trying to get the references from the repository {}.", repoName, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // For now "haves" are not considered.
        // Walk through pkt-lines with "wants", check if the reference is available, and
        // add them to a "wanted" array. When the client sends a "done", all "wants" are
        // fulfilled.
        PacketLineIn packetLineIn = new PacketLineIn(new ByteArrayInputStream(wantsAndHaves.getBytes()));
        try {
            String nextPacketLine = packetLineIn.readString();
            while (!PacketLineIn.isEnd(nextPacketLine)) {
                if (nextPacketLine.startsWith("want")) {
                    String want = nextPacketLine.substring(5, nextPacketLine.length());
                    if (refsSet.contains(want)) {
                        wantedList.add(want);
                    }
                    else {
                        log.warn("Requesting unavailable reference: {}", want);
                        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                    }
                }
                nextPacketLine = packetLineIn.readString();
            }
            // Check, if a "done" is sent
            try {
                nextPacketLine = packetLineIn.readString();
                if (nextPacketLine.equals("done")) {
                    sendPackFile = true;

                }
            }
            catch (IOException e) {
                log.debug("No more packet lines after flush packet.");
            }

        }
        catch (IOException e) {
            log.warn("Unable to read packet line from response body.", wantsAndHaves, e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        result += pktLineFromString("NAK\n");

        if (sendPackFile) {
            log.debug("Sending wants as pack file: {}", wantedList);
            // Get Packfiles for the respective repository and check if there is one
            // containing all the requested refs.
            File packDir = new File(repoPath + "/objects/pack");
            File[] packFiles = packDir.listFiles();
            if (packFiles == null) {
                log.warn("Unable to read packfiles in repository.");
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            for (File packFile : packFiles) {
                if (getExtensionByStringHandling(packFile.getAbsolutePath()).get() == "pack") {
                    // PackFile jGitPackFile = new PackFi
                    // Eclipse JGit 6.2.0 besitzt keine Methoden, um den Inhalt von Packfiles zu
                    // inspizieren.
                    // Ich werde das Packfile selber zusammenbasteln müssen.
                }
            }

        }

        return new ResponseEntity<byte[]>(result.getBytes(), headers, HttpStatus.OK);

    }

    private String pktLineFromString(String pktLine) {
        String lineLengthHex = Integer.toHexString(pktLine.length() + 4);
        String pktLen;
        switch (lineLengthHex.length()) {
            case 1:
                pktLen = "000" + lineLengthHex;
                break;
            case 2:
                pktLen = "00" + lineLengthHex;
                break;
            case 3:
                pktLen = "0" + lineLengthHex;
                break;
            default:
                pktLen = lineLengthHex;
                break;
        }
        return pktLen + pktLine;
    }

    private Optional<String> getExtensionByStringHandling(String filename) {
        return Optional.ofNullable(filename).filter(f -> f.contains(".")).map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }

}
