package de.tum.in.www1.artemis.web.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Feedback DTO that should have same attributes as feedbackItem in the client
 *
 * @param type    the type of the feedback
 * @param name    a translation string for the name
 * @param title   usually feedback.text
 * @param credits
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record FeedbackDTO(FeedbackDTOType type, String name, String title, String text, Boolean positive, Double credits) {
}
