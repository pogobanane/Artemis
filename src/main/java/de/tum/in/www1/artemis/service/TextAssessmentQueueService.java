package de.tum.in.www1.artemis.service;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.tum.in.www1.artemis.domain.TextExercise;
import de.tum.in.www1.artemis.domain.TextSubmission;
import de.tum.in.www1.artemis.domain.enumeration.Language;
import de.tum.in.www1.artemis.repository.TextSubmissionRepository;

@Service
@Profile("athena")
public class TextAssessmentQueueService {

    private final TextSubmissionRepository textSubmissionRepository;

    public TextAssessmentQueueService(TextSubmissionRepository textSubmissionRepository) {
        this.textSubmissionRepository = textSubmissionRepository;
    }

    /**
     * Calculates the proposedTextSubmission for a given Text exercise. This means the Text exercise which should be assessed next
     *
     * @param textExercise the exercise for
     * @return a TextSubmission with the highest information Gain if there is one
     * @throws IllegalArgumentException if textExercise isn't automatically assessable
     */
    public Optional<TextSubmission> getProposedTextSubmission(TextExercise textExercise) {
        return getProposedTextSubmission(textExercise, null);
    }

    /**
     * Calculates the proposedTextSubmission for a given Text exercise
     *
     * @param textExercise the exercise for
     * @param languages    list of languages the submission which the returned submission should have if null all languages are allowed
     * @return a TextSubmission with the highest information Gain if there is one
     * @throws IllegalArgumentException if textExercise isn't automatically assessable
     */
    @Transactional(readOnly = true) // TODO: remove transactional
    public Optional<TextSubmission> getProposedTextSubmission(TextExercise textExercise, List<Language> languages) {
        if (!textExercise.isAutomaticAssessmentEnabled()) {
            throw new IllegalArgumentException("The TextExercise is not automatic assessable");
        }
        List<TextSubmission> textSubmissionList = getAllOpenTextSubmissions(textExercise);
        if (textSubmissionList.isEmpty()) {
            return Optional.empty();
        }
        // TODO: implement
        return textSubmissionList.stream().filter(textSubmission -> languages == null || languages.contains(textSubmission.getLanguage())).findFirst();
    }

    /**
     * Return all TextSubmission which are the latest TextSubmission of a Participation and doesn't have a Result so far
     * The corresponding TextBlocks and Participations are retrieved from the database
     *
     * @param exercise Exercise for which all assessed submissions should be retrieved
     * @return an unmodifiable list of all TextSubmission which aren't assessed at the Moment, but need assessment in the future.
     */
    public List<TextSubmission> getAllOpenTextSubmissions(TextExercise exercise) {
        // TODO: remove
        final List<TextSubmission> submissions = textSubmissionRepository.findByParticipation_ExerciseIdAndResultsIsNullAndSubmittedIsTrue(exercise.getId());

        return submissions.stream()
                .filter(submission -> submission.getParticipation().findLatestSubmission().isPresent() && submission == submission.getParticipation().findLatestSubmission().get())
                .toList();
    }
}
