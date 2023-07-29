package de.tum.in.www1.artemis.repository;

import static org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.LOAD;

import java.util.Optional;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.tum.in.www1.artemis.domain.competency.LearningPath;
import de.tum.in.www1.artemis.web.rest.errors.EntityNotFoundException;

@Repository
public interface LearningPathRepository extends JpaRepository<LearningPath, Long> {

    Optional<LearningPath> findByCourseIdAndUserId(long courseId, long userId);

    default LearningPath findByCourseIdAndUserIdElseThrow(long courseId, long userId) {
        return findByCourseIdAndUserId(courseId, userId).orElseThrow(() -> new EntityNotFoundException("LearningPath"));
    }

    @EntityGraph(type = LOAD, attributePaths = { "competencies" })
    Optional<LearningPath> findWithEagerCompetenciesByCourseIdAndUserId(long courseId, long userId);

    default LearningPath findWithEagerCompetenciesByCourseIdAndUserIdElseThrow(long courseId, long userId) {
        return findWithEagerCompetenciesByCourseIdAndUserId(courseId, userId).orElseThrow(() -> new EntityNotFoundException("LearningPath"));
    }

    @Query("""
            SELECT lp
            FROM LearningPath lp
            WHERE lp.course.id = :courseId
            """)
    Set<LearningPath> findAllForCourse(@Param("courseId") long courseId);

    @Query("""
            SELECT lp
            FROM LearningPath lp
            WHERE (lp.course.id = :courseId) AND (lp.user.login LIKE %:searchTerm% OR CONCAT(lp.user.firstName, ' ', lp.user.lastName) LIKE %:searchTerm%)
            """)
    Page<LearningPath> findByLoginOrNameInCourse(@Param("searchTerm") String searchTerm, @Param("courseId") long courseId, Pageable pageable);

    @EntityGraph(type = LOAD, attributePaths = { "competencies" })
    Optional<LearningPath> findWithEagerCompetenciesById(long learningPathId);

    @NotNull
    default LearningPath findWithEagerCompetenciesByIdElseThrow(long learningPathId) {
        return findWithEagerCompetenciesById(learningPathId).orElseThrow(() -> new EntityNotFoundException("LearningPath", learningPathId));
    }

    @EntityGraph(type = LOAD, attributePaths = { "competencies", "competencies.lectureUnits", "competencies.exercises" })
    Optional<LearningPath> findWithEagerCompetenciesAndLearningObjectsById(long learningPathId);

    @NotNull
    default LearningPath findWithEagerCompetenciesAndLearningObjectsByIdElseThrow(long learningPathId) {
        return findWithEagerCompetenciesAndLearningObjectsById(learningPathId).orElseThrow(() -> new EntityNotFoundException("LearningPath", learningPathId));
    }

    @EntityGraph(type = LOAD, attributePaths = { "competencies", "competencies.lectureUnits", "competencies.lectureUnits.completedUsers", "competencies.exercises",
            "competencies.exercises.studentParticipations" })
    Optional<LearningPath> findWithEagerCompetenciesAndLearningObjectsAndCompletedUsersById(long learningPathId);

    @NotNull
    default LearningPath findWithEagerCompetenciesAndLearningObjectsAndCompletedUsersByIdElseThrow(long learningPathId) {
        return findWithEagerCompetenciesAndLearningObjectsAndCompletedUsersById(learningPathId).orElseThrow(() -> new EntityNotFoundException("LearningPath", learningPathId));
    }

    @Query("""
            SELECT COUNT (learningPath)
            FROM LearningPath learningPath
            WHERE learningPath.course.id = :#{#courseId}
            """)
    Long countByCourseId(@Param("courseId") long courseId);
}
