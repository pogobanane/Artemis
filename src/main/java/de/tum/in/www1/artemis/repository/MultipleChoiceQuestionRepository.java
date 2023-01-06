package de.tum.in.www1.artemis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.tum.in.www1.artemis.domain.quiz.MultipleChoiceQuestion;

/**
 * Spring Data JPA repository for the MultipleChoiceQuestion entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MultipleChoiceQuestionRepository extends JpaRepository<MultipleChoiceQuestion, Long> {

    @Query("""
            SELECT DISTINCT mc
            FROM MultipleChoiceQuestion mc
                LEFT JOIN FETCH mc.answerOptions
            WHERE mc.id = :questionId
            """)
    Optional<MultipleChoiceQuestion> findByIdWithAnswerOptions(@Param("questionId") Long questionId);
}
