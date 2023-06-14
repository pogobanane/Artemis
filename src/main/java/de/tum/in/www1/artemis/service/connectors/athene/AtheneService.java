package de.tum.in.www1.artemis.service.connectors.athene;

import static de.tum.in.www1.artemis.config.Constants.ATHENE_RESULT_API_PATH;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import de.tum.in.www1.artemis.domain.TextExercise;
import de.tum.in.www1.artemis.domain.TextSubmission;
import de.tum.in.www1.artemis.domain.enumeration.Language;
import de.tum.in.www1.artemis.exception.NetworkingError;
import de.tum.in.www1.artemis.repository.TextBlockRepository;
import de.tum.in.www1.artemis.repository.TextExerciseRepository;
import de.tum.in.www1.artemis.repository.TextSubmissionRepository;
import de.tum.in.www1.artemis.service.TextAssessmentQueueService;

@Service
@Profile("athene")
public class AtheneService {

    private final Logger log = LoggerFactory.getLogger(AtheneService.class);

    @Value("${server.url}")
    private String artemisServerUrl;

    @Value("${artemis.athene.url}")
    private String atheneUrl;

    private final TextAssessmentQueueService textAssessmentQueueService;

    private final TextBlockRepository textBlockRepository;

    private final TextExerciseRepository textExerciseRepository;

    private final TextSubmissionRepository textSubmissionRepository;

    private final AtheneConnector<RequestDTO, ResponseDTO> connector;

    // Contains tasks submitted to Athene and currently processing
    private final List<Long> runningAtheneTasks = new ArrayList<>();

    public AtheneService(TextSubmissionRepository textSubmissionRepository, TextBlockRepository textBlockRepository, TextExerciseRepository textExerciseRepository,
            TextAssessmentQueueService textAssessmentQueueService, @Qualifier("atheneRestTemplate") RestTemplate atheneRestTemplate) {
        this.textSubmissionRepository = textSubmissionRepository;
        this.textBlockRepository = textBlockRepository;
        this.textExerciseRepository = textExerciseRepository;
        this.textAssessmentQueueService = textAssessmentQueueService;
        connector = new AtheneConnector<>(log, atheneRestTemplate, ResponseDTO.class);
    }

    // region Request/Response DTOs
    private static class RequestDTO {

        public long courseId;

        public String callbackUrl;

        public List<TextSubmission> submissions;

        RequestDTO(@NotNull long courseId, @NotNull List<TextSubmission> submissions, @NotNull String callbackUrl) {
            this.courseId = courseId;
            this.callbackUrl = callbackUrl;
            this.submissions = createSubmissionDTOs(submissions);
        }

        /**
         * Converts TextSubmissions to DTO objects to prepare for sending them to Athene in a REST call.
         */
        @NotNull
        private static List<TextSubmission> createSubmissionDTOs(@NotNull List<TextSubmission> submissions) {
            return submissions.stream().map(textSubmission -> {
                final TextSubmission submission = new TextSubmission();
                submission.setText(textSubmission.getText());
                submission.setId(textSubmission.getId());
                return submission;
            }).toList();
        }
    }

    private static class ResponseDTO {

        public String detail;

    }
    // endregion

    /**
     * Register an Athene task for an exercise as running
     *
     * @param exerciseId the exerciseId which the Athene task is running for
     */
    public void startTask(Long exerciseId) {
        runningAtheneTasks.add(exerciseId);
    }

    /**
     * Delete an Athene task for an exercise from the running tasks
     *
     * @param exerciseId the exerciseId which the Athene task finished for
     */
    public void finishTask(Long exerciseId) {
        runningAtheneTasks.remove(exerciseId);
    }

    /**
     * Check whether an Athene task is running for the given exerciseId
     *
     * @param exerciseId the exerciseId to check for a running Athene task
     * @return true, if a task for the given exerciseId is running
     */
    public boolean isTaskRunning(Long exerciseId) {
        return runningAtheneTasks.contains(exerciseId);
    }

    /**
     * Calls the remote Athene service to submit a Job for calculating automatic feedback
     *
     * @param exercise the exercise the automatic assessments should be calculated for
     */
    public void submitJob(TextExercise exercise) {
        submitJob(exercise, 1);
    }

    /**
     * Calls the remote Athene service to submit a Job for calculating automatic feedback
     * Falls back to naive splitting for less than 10 submissions
     * Note: See `TextSubmissionService:getTextSubmissionsByExerciseId` for selection of Submissions.
     *
     * @param exercise   the exercise the automatic assessments should be calculated for
     * @param maxRetries number of retries before the request will be canceled
     */
    public void submitJob(TextExercise exercise, int maxRetries) {
        log.debug("Start Athene Service for Text Exercise '{}' (#{}).", exercise.getTitle(), exercise.getId());

        // Find all submissions for Exercise
        // We only support english languages so far, to prevent corruption of the clustering
        List<TextSubmission> textSubmissions = textSubmissionRepository.getTextSubmissionsWithTextBlocksByExerciseIdAndLanguage(exercise.getId(), Language.ENGLISH);

        // Athene only works with 10 or more submissions
        if (textSubmissions.size() < 10) {
            return;
        }

        log.info("Calling Remote Service to calculate automatic feedback for {} submissions.", textSubmissions.size());

        try {
            final RequestDTO request = new RequestDTO(exercise.getId(), textSubmissions, artemisServerUrl + ATHENE_RESULT_API_PATH + exercise.getId());
            ResponseDTO response = connector.invokeWithRetry(atheneUrl + "/submit", request, maxRetries);
            log.info("Remote Service to calculate automatic feedback responded: {}", response.detail);

            // Register task for exercise as running, AtheneResource calls finishTask on result receive
            startTask(exercise.getId());
        }
        catch (NetworkingError networkingError) {
            log.error("Error while calling Remote Service: {}", networkingError.getMessage());
        }
    }

}
