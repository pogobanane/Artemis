package de.tum.in.www1.artemis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import de.tum.in.www1.artemis.domain.submissionpolicy.SubmissionPolicy;

/**
 * Spring Data repository for the SubmissionPolicy entity.
 */
@Repository
public interface SubmissionPolicyRepository extends JpaRepository<SubmissionPolicy, Long> {

    @Query("""
            SELECT sp FROM SubmissionPolicy sp
            WHERE sp.programmingExercise.id = :#{#exerciseId}""")
    SubmissionPolicy findByExerciseId(Long exerciseId);
}
