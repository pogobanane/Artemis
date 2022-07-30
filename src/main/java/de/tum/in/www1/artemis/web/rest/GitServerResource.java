package de.tum.in.www1.artemis.web.rest;

import static de.tum.in.www1.artemis.web.rest.GitServerResourceEndpoints.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
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

        result += "001e# service=git-upload-pack\n0000";

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

        if (refs.isEmpty()) {
            // Construct an empty_list = PKT-LINE(zero-id SP "capabilities^{}" NUL cap-list
            // LF)
            result += "003f0000000000000000000000000000000000000000 capabilities^{}\0\n0000";
            return new ResponseEntity<byte[]>(result.getBytes(), HttpStatus.OK);
        }

        for (int i = 0; i < refs.size(); i++) {
            log.debug("Ref found: {}", refs.get(i).getName() + " " + refs.get(i).getObjectId().getName());
            if (i == 0) {
                // First response entry is constructed differently.
                String refOutput = refs.get(i).getObjectId().getName() + " " + refs.get(i).getName() + "\0\n";
                result += pktLineLengthFromString(refOutput) + refOutput;
            }
            else {
                String refOutput = refs.get(i).getObjectId().getName() + " " + refs.get(i).getName() + "\n";
                result += pktLineLengthFromString(refOutput) + refOutput;
            }
        }

        result += "0000";

        /*
         * Boolean refsAvailable = false; try { refsAvailable = !isEmpty(Paths.get(repoPath, "refs" + "/" + "heads")); } catch (IOException e) {
         * log.warn("Unable to access {}/refs/heads.", repoPath, e); return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); } if (!refsAvailable) { // Construct an
         * empty_list = PKT-LINE(zero-id SP "capabilities^{}" NUL cap-list // LF) result += "003f0000000000000000000000000000000000000000 capabilities^{}\0\n0000"; return new
         * ResponseEntity<byte[]>(result.getBytes(), HttpStatus.OK); }
         */
        // References are available. Iterate through refs and add them to the response.
        // Stream<Path> refs = Files.list(Paths.get(repoPath, "refs" + "/" + "heads"));

        /*
         * repoPath = localGitPath + projectKey + "/" + repoName; result = ""; try { if (Files.exists(Paths.get(repoPath))) { if (isEmpty(Paths.get(repoPath, "refs" + "/" +
         * "heads"))) { // If no refs are present, it is an empty repository and the response contains // an empty list of refs. result += "001e# service=git-upload-pack\n0000";
         * result += "003f0000000000000000000000000000000000000000 capabilities^{}\0-\n"; } else { // Iterate through refs and add them to the response. Stream<Path> refs =
         * Files.list(Paths.get(repoPath, "refs" + "/" + "heads")); // For the first one special formatting with NUL and cap_list refs.forEach(ref -> { try (BufferedReader reader =
         * Files.newBufferedReader(ref, Charset.forName("UTF-8"))) { String currentLine = null; while ((currentLine = reader.readLine()) != null) { // result += currentLine; } }
         * catch (IOException e) { log.error("Unable to read file at {}", ref.toString(), e); } // result += " refs/heads/" + ref.getFileName() + "\n"; }); refs.close(); } } else {
         * return new ResponseEntity<>(null, HttpStatus.NOT_FOUND); } } catch (IOException e) { log.error("Error reading path {}", repoPath, e); } result += "0000"; return new
         * ResponseEntity<>(result.getBytes(), HttpStatus.OK);
         */

        return new ResponseEntity<byte[]>(result.getBytes(), headers, HttpStatus.OK);

    }

    /*
     * GET $GIT_URL/info/refs?service=serviceName Necessary for git fetch and push. $GIT_URL for now has the form "http://localhost:8080/git/:projectKey/:repoName" Discover the
     * references available on the remote repository.
     */
    @RequestMapping(value = "/{projectKey}/{repoName}/info/refss", method = RequestMethod.GET, produces = "application/x-git-upload-pack-advertisement")
    public ResponseEntity<byte[]> getRefs(@PathVariable("projectKey") String projectKey, @PathVariable("repoName") String repoName, @RequestParam("service") String serviceName,
            @RequestHeader("Git-Protocol") String gitProtocolVersion) {
        log.debug("REST request to get refs for repo {} in project {}", repoName, projectKey);

        // If the server does not recognize the requested service name, the server MUST
        // respond with the 403 Forbidden HTTP status code.
        if (!Stream.of(GitServiceOption.values()).anyMatch(e -> e.toString().equals(serviceName))) {
            return new ResponseEntity<byte[]>(HttpStatus.FORBIDDEN);
        }

        // Check if the requested respository in the given project is there.
        String repoPath = localGitPath + projectKey + "/" + repoName;
        log.debug("Checking if the folder {} exists", repoPath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/x-" + serviceName + "-advertisement"));

        String result = "";

        result += "001e# service=" + serviceName + "\n0000";

        return new ResponseEntity<byte[]>(result.getBytes(), headers, HttpStatus.OK);

        // Try to find the repository stated in the URL locally and get the refs from it

        // empty_list = PKT-LINE(zero-id SP "capabilities^{}" NUL cap-list LF)

        /*
         * repoPath = localGitPath + projectKey + "/" + repoName; result = ""; try { if (Files.exists(Paths.get(repoPath))) { if (isEmpty(Paths.get(repoPath, "refs" + "/" +
         * "heads"))) { // If no refs are present, it is an empty repository and the response contains // an empty list of refs. result += "001e# service=git-upload-pack\n0000";
         * result += "003f0000000000000000000000000000000000000000 capabilities^{}\0-\n"; } else { // Iterate through refs and add them to the response. Stream<Path> refs =
         * Files.list(Paths.get(repoPath, "refs" + "/" + "heads")); // For the first one special formatting with NUL and cap_list refs.forEach(ref -> { try (BufferedReader reader =
         * Files.newBufferedReader(ref, Charset.forName("UTF-8"))) { String currentLine = null; while ((currentLine = reader.readLine()) != null) { // result += currentLine; } }
         * catch (IOException e) { log.error("Unable to read file at {}", ref.toString(), e); } // result += " refs/heads/" + ref.getFileName() + "\n"; }); refs.close(); } } else {
         * return new ResponseEntity<>(null, HttpStatus.NOT_FOUND); } } catch (IOException e) { log.error("Error reading path {}", repoPath, e); } result += "0000"; return new
         * ResponseEntity<>(result.getBytes(), HttpStatus.OK);
         */
    }

    private boolean isEmpty(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (Stream<Path> entries = Files.list(path)) {
                return !entries.findFirst().isPresent();
            }
        }
        return false;
    }

    private String pktLineLengthFromString(String pktLine) {
        String lineLengthHex = Integer.toHexString(pktLine.length());
        switch (lineLengthHex.length()) {
            case 1:
                return "000" + lineLengthHex;
            case 2:
                return "00" + lineLengthHex;
            case 3:
                return "0" + lineLengthHex;
            default:
                return lineLengthHex;
        }
    }

}
