package de.tum.in.www1.artemis.web.rest.dto;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import de.tum.in.www1.artemis.domain.enumeration.*;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ResultDTO(Long id, ZonedDateTime completionDate, Boolean successful, Double score, Boolean rated, SubmissionDTO submission, long participationId,
        List<FeedbackDTO> feedbacks, AssessmentType assessmentType, Boolean hasComplaint, Boolean exampleResult, Integer testCaseCount, Integer passedTestCaseCount,
        Integer codeIssueCount) {

    public record SubmissionDTO(Long id, Boolean submitted, SubmissionType type, Boolean exampleSubmission, ZonedDateTime submissionDate, String commitHash, Boolean buildFailed,
            Boolean buildArtifact) {
    }

    public record FeedbackDTO(String text, String detailText, boolean hasLongFeedbackText, String reference, Double credits, Boolean positive, FeedbackType type,
            Visibility visibility) {
    }
}
