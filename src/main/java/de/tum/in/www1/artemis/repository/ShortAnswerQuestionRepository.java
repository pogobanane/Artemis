package de.tum.in.www1.artemis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import de.tum.in.www1.artemis.domain.quiz.ShortAnswerQuestion;

public interface ShortAnswerQuestionRepository extends JpaRepository<ShortAnswerQuestion, Long> {

    @Query("""
            SELECT DISTINCT sa
            FROM ShortAnswerQuestion sa
                LEFT JOIN FETCH sa.correctShortAnswerMappings
                LEFT JOIN FETCH sa.solutions
                LEFT JOIN FETCH sa.spots
            WHERE sa.id = :questionId
            """)
    Optional<ShortAnswerQuestion> findByIdWithDetails(Long questionId);
}
