package de.tum.in.www1.artemis.service.feedback;

import de.tum.in.www1.artemis.domain.Feedback;
import de.tum.in.www1.artemis.domain.enumeration.FeedbackType;
import de.tum.in.www1.artemis.web.rest.dto.FeedbackDTO;
import de.tum.in.www1.artemis.web.rest.dto.FeedbackDTOType;

public class ProgrammingFeedbackDTOService implements FeedbackDTOService {

    public FeedbackDTO createFromFeedback(Feedback feedback) {
        var feedbackDTO = (FeedbackDTO) feedback;
        if (feedback.isSubmissionPolicyFeedback()) {
            return feedbackDTO.feedbackDTOType(FeedbackDTOType.Submission_Policy);
        }
        else if (feedback.isStaticCodeAnalysisFeedback()) {
            return feedbackDTO.feedbackDTOType(FeedbackDTOType.Static_Code_Analysis);
        }
        else if (feedback.getType() == FeedbackType.AUTOMATIC) {
            return feedbackDTO.feedbackDTOType(FeedbackDTOType.Test);
        }
        else if ((feedback.getType() == FeedbackType.MANUAL || feedback.getType() == FeedbackType.MANUAL_UNREFERENCED) && feedback.getGradingInstruction() != null) {
            // TODO: check subsequent
            return feedbackDTO.feedbackDTOType(FeedbackDTOType.Grading_Instruction);
        }
        return feedbackDTO.feedbackDTOType(FeedbackDTOType.Reviewer);
    }
}
