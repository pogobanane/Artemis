package de.tum.in.www1.artemis.service;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

import de.tum.in.www1.artemis.domain.quiz.DragAndDropQuestion;
import de.tum.in.www1.artemis.domain.quiz.MultipleChoiceQuestion;
import de.tum.in.www1.artemis.domain.quiz.QuizExercise;
import de.tum.in.www1.artemis.domain.quiz.ShortAnswerQuestion;
import de.tum.in.www1.artemis.repository.*;
import de.tum.in.www1.artemis.web.rest.errors.EntityNotFoundException;

@Service
public class QuizQuestionService {

    private final MultipleChoiceQuestionRepository multipleChoiceQuestionRepository;

    private final DragAndDropQuestionRepository dragAndDropQuestionRepository;

    private final ShortAnswerQuestionRepository shortAnswerQuestionRepository;

    private final QuizExerciseRepository quizExerciseRepository;

    public QuizQuestionService(MultipleChoiceQuestionRepository multipleChoiceQuestionRepository, DragAndDropQuestionRepository dragAndDropQuestionRepository,
            ShortAnswerQuestionRepository shortAnswerQuestionRepository, QuizExerciseRepository quizExerciseRepository) {
        this.multipleChoiceQuestionRepository = multipleChoiceQuestionRepository;
        this.dragAndDropQuestionRepository = dragAndDropQuestionRepository;
        this.shortAnswerQuestionRepository = shortAnswerQuestionRepository;
        this.quizExerciseRepository = quizExerciseRepository;
    }

    public void loadQuestionWithDetailsIfNecessary(QuizExercise quizExercise) {
        if (quizExercise.getQuizQuestions() == null || !Hibernate.isInitialized(quizExercise.getQuizQuestions())) {
            var questions = quizExerciseRepository.findWithEagerQuestionsById(quizExercise.getId())
                    .orElseThrow(() -> new EntityNotFoundException("QuizExercise", quizExercise.getId())).getQuizQuestions();
            quizExercise.setQuizQuestions(questions);
        }
        for (int i = 0; i < quizExercise.getQuizQuestions().size(); i++) {
            var originalQuestion = quizExercise.getQuizQuestions().get(i);
            if (originalQuestion instanceof MultipleChoiceQuestion multipleChoiceQuestion) {
                if (multipleChoiceQuestion.getAnswerOptions() == null || !Hibernate.isInitialized(multipleChoiceQuestion.getAnswerOptions())) {
                    var questionWithDetails = multipleChoiceQuestionRepository.findByIdWithAnswerOptions(multipleChoiceQuestion.getId())
                            .orElseThrow(() -> new EntityNotFoundException("MultipleChoiceQuestion", multipleChoiceQuestion.getId()));
                    quizExercise.getQuizQuestions().set(i, questionWithDetails);
                }
            }
            else if (originalQuestion instanceof DragAndDropQuestion dragAndDropQuestion) {
                if (dragAndDropQuestion.getCorrectDragAndDropMappings() == null || !Hibernate.isInitialized(dragAndDropQuestion.getCorrectDragAndDropMappings())) {
                    var questionWithDetails = dragAndDropQuestionRepository.findByIdWithDetails(dragAndDropQuestion.getId())
                            .orElseThrow(() -> new EntityNotFoundException("DragAndDropQuestion", dragAndDropQuestion.getId()));
                    quizExercise.getQuizQuestions().set(i, questionWithDetails);
                }
            }
            else if (originalQuestion instanceof ShortAnswerQuestion shortAnswerQuestion) {
                if (shortAnswerQuestion.getCorrectShortAnswerMappings() == null || !Hibernate.isInitialized(shortAnswerQuestion.getCorrectShortAnswerMappings())) {
                    var questionWithDetails = shortAnswerQuestionRepository.findByIdWithDetails(shortAnswerQuestion.getId())
                            .orElseThrow(() -> new EntityNotFoundException("ShortAnswerQuestion", shortAnswerQuestion.getId()));
                    quizExercise.getQuizQuestions().set(i, questionWithDetails);
                }
            }
        }
    }
}
