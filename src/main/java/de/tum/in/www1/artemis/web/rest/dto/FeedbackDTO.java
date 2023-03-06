package de.tum.in.www1.artemis.web.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

enum FeedbackDTOType {
    Test, Static_Code_Analysis, Reviewer, Subsequent, Submission_Policy
}

/**
 * Feedback DTO that should have same attributes as feedbackItem in the client
 *
 * @param name
 * @param credits
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record FeedbackDTO(String name, Double credits, FeedbackDTOType type) {
}
