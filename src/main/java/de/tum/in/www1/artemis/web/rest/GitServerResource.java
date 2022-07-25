package de.tum.in.www1.artemis.web.rest;

import static de.tum.in.www1.artemis.web.rest.GitServerResourceEndpoints.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.tum.in.www1.artemis.service.connectors.localgit.dto.LocalGitProjectDTO;

/*
 * REST controller for managing local git server.
 */
@RestController
@RequestMapping(ROOT)
public class GitServerResource {

    private final Logger log = LoggerFactory.getLogger(GitServerResource.class);

    public GitServerResource() {

    }

    /*
     * POST /init-project :
     */

}
