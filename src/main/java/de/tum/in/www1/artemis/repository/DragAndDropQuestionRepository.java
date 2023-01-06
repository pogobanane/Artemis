package de.tum.in.www1.artemis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import de.tum.in.www1.artemis.domain.quiz.DragAndDropQuestion;

public interface DragAndDropQuestionRepository extends JpaRepository<DragAndDropQuestion, Long> {

    @Query("""
            SELECT DISTINCT dnd
            FROM DragAndDropQuestion dnd
                LEFT JOIN FETCH dnd.correctDragAndDropMappings
                LEFT JOIN FETCH dnd.dragItems
                LEFT JOIN FETCH dnd.dropLocations
            WHERE dnd.id = :questionId
            """)
    Optional<DragAndDropQuestion> findByIdWithDetails(Long questionId);
}
