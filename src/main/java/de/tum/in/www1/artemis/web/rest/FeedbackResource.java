package de.tum.in.www1.artemis.web.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.tum.in.www1.artemis.domain.Result;
import de.tum.in.www1.artemis.domain.participation.Participation;
import de.tum.in.www1.artemis.domain.participation.ProgrammingExerciseParticipation;
import de.tum.in.www1.artemis.domain.participation.StudentParticipation;
import de.tum.in.www1.artemis.repository.ResultRepository;
import de.tum.in.www1.artemis.service.AuthorizationCheckService;
import de.tum.in.www1.artemis.service.FeedbackService;
import de.tum.in.www1.artemis.service.programming.ProgrammingExerciseParticipationService;
import de.tum.in.www1.artemis.web.rest.dto.FeedbackDTO;
import de.tum.in.www1.artemis.web.rest.errors.AccessForbiddenException;
import de.tum.in.www1.artemis.web.rest.errors.BadRequestAlertException;

@RestController
@RequestMapping("/api")
public class FeedbackResource {

    private final Logger log = LoggerFactory.getLogger(ResultResource.class);

    private final ResultRepository resultRepository;

    private final AuthorizationCheckService authCheckService;

    private final FeedbackService feedbackService;

    private final ProgrammingExerciseParticipationService programmingExerciseParticipationService;

    public FeedbackResource(ResultRepository resultRepository, AuthorizationCheckService authCheckService,
            ProgrammingExerciseParticipationService programmingExerciseParticipationService, FeedbackService feedbackService) {
        this.resultRepository = resultRepository;
        this.authCheckService = authCheckService;
        this.feedbackService = feedbackService;
        this.programmingExerciseParticipationService = programmingExerciseParticipationService;
    }

    @GetMapping("participations/{participationId}/results/resultId/items")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<FeedbackDTO>> getByResult(@PathVariable Long participationId, @PathVariable Long resultId) {
        log.debug("REST request to get feedback items for result : {}", resultId);

        Result result = resultRepository.findByIdWithEagerFeedbacksElseThrow(resultId);
        Participation participation = result.getParticipation();
        if (!participation.getId().equals(participationId)) {
            throw new BadRequestAlertException("participationId of the path doesnt match the participationId of the participation corresponding to the result " + resultId + " !",
                    "participationId", "400");
        }

        // The permission check depends on the participation type (normal participations vs. programming exercise participations).
        if (participation instanceof StudentParticipation) {
            if (!authCheckService.canAccessParticipation((StudentParticipation) participation)) {
                throw new AccessForbiddenException("participation", participationId);
            }
        }
        else if (participation instanceof ProgrammingExerciseParticipation) {
            if (!programmingExerciseParticipationService.canAccessParticipation((ProgrammingExerciseParticipation) participation)) {
                throw new AccessForbiddenException("participation", participationId);
            }
        }
        else {
            // This would be the case that a new participation type is introduced, without this the user would have access to it regardless of the permissions.
            throw new AccessForbiddenException("participation", participationId);
        }

        return new ResponseEntity<>(feedbackService.getByResult(result), HttpStatus.OK);

    }
}
