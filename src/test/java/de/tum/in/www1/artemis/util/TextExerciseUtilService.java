package de.tum.in.www1.artemis.util;

import static org.apache.commons.codec.digest.DigestUtils.sha1Hex;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.tum.in.www1.artemis.domain.*;
import de.tum.in.www1.artemis.domain.enumeration.AssessmentType;
import de.tum.in.www1.artemis.domain.enumeration.Language;
import de.tum.in.www1.artemis.domain.participation.StudentParticipation;
import de.tum.in.www1.artemis.repository.*;

@Service
public class TextExerciseUtilService {

    @Autowired
    private TextSubmissionRepository textSubmissionRepository;

    @Autowired
    private StudentParticipationRepository participationRepository;

    @Autowired
    private TextExerciseRepository textExerciseRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Generate a set of specified size containing TextBlocks with dummy Text
     *
     * @param count expected size of TextBlock set
     * @return Set of dummy TextBlocks
     */
    public Set<TextBlock> generateTextBlocks(int count) {
        Set<TextBlock> textBlocks = new HashSet<>();
        TextBlock textBlock;
        for (int i = 0; i < count; i++) {
            textBlock = new TextBlock();
            textBlock.setText("TextBlock" + i);
            textBlocks.add(textBlock);
        }
        return textBlocks;
    }

    /**
     * Generate a set of specified size containing TextBlocks with the same text
     *
     * @param count expected size of TextBlock set
     * @return Set of TextBlocks with identical texts
     */
    public Set<TextBlock> generateTextBlocksWithIdenticalTexts(int count) {
        Set<TextBlock> textBlocks = new HashSet<>();
        TextBlock textBlock;
        String text = "TextBlock";

        for (int i = 0; i < count; i++) {
            String blockId = sha1Hex("id" + i + text);
            textBlock = new TextBlock();
            textBlock.setText(text);
            textBlock.setId(blockId);
            textBlock.automatic();
            textBlocks.add(textBlock);
        }
        return textBlocks;
    }

    public TextExercise createSampleTextExerciseWithSubmissions(Course course, List<TextBlock> textBlocks, int submissionCount, int submissionSize) {
        if (textBlocks.size() != submissionCount * submissionSize) {
            throw new IllegalArgumentException("number of textBlocks must be eqaul to submissionCount * submissionSize");
        }
        TextExercise textExercise = new TextExercise();
        textExercise.setCourse(course);
        textExercise.setTitle("Title");
        textExercise.setShortName("Shortname");
        textExercise.setAssessmentType(AssessmentType.SEMI_AUTOMATIC);
        textExercise = textExerciseRepository.save(textExercise);

        // submissions.length must be equal to studentParticipations.length;
        for (int i = 0; i < submissionCount; i++) {
            TextSubmission submission = new TextSubmission();
            StudentParticipation studentParticipation = new StudentParticipation();
            studentParticipation.setParticipant(userRepository.getUser());
            studentParticipation.setExercise(textExercise);
            studentParticipation = participationRepository.save(studentParticipation);

            submission.setParticipation(studentParticipation);
            submission.setLanguage(Language.ENGLISH);
            submission.setText("Test123");
            submission.setBlocks(new HashSet<>(textBlocks.subList(i * submissionSize, (i + 1) * submissionSize)));
            submission.setSubmitted(true);
            submission.setSubmissionDate(ZonedDateTime.now());
            textBlocks.subList(i * submissionSize, (i + 1) * submissionSize).forEach(textBlock -> textBlock.setSubmission(submission));

            studentParticipation.addSubmission(submission);
            textSubmissionRepository.save(submission);

            textExercise.getStudentParticipations().add(studentParticipation);
        }
        return textExercise;
    }
}
