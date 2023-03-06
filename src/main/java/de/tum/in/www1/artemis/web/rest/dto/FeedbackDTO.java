package de.tum.in.www1.artemis.web.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import de.tum.in.www1.artemis.domain.Feedback;

/**
 * Feedback DTO that should have same attributes as feedbackItem in the client
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FeedbackDTO extends Feedback {

    private FeedbackDTOType feedbackDTOType;

    public FeedbackDTO feedbackDTOType(FeedbackDTOType feedbackDTOType) {
        this.feedbackDTOType = feedbackDTOType;
        return this;
    }

    public FeedbackDTOType getFeedbackDTOType() {
        return feedbackDTOType;
    }
}
