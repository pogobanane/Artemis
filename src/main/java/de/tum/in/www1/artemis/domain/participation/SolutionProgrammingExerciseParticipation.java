package de.tum.in.www1.artemis.domain.participation;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import de.tum.in.www1.artemis.domain.ProgrammingExercise;
import jakarta.persistence.Cacheable;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;

@Entity
@DiscriminatorValue(value = "SPEP")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SolutionProgrammingExerciseParticipation extends AbstractBaseProgrammingExerciseParticipation {

    @OneToOne(mappedBy = "solutionParticipation")
    @JsonIgnoreProperties("solutionParticipation")
    private ProgrammingExercise programmingExercise;

    @Override
    public ProgrammingExercise getProgrammingExercise() {
        return programmingExercise;
    }

    @Override
    public void setProgrammingExercise(ProgrammingExercise programmingExercise) {
        this.programmingExercise = programmingExercise;
    }

    @Override
    public Participation copyParticipationId() {
        var participation = new SolutionProgrammingExerciseParticipation();
        participation.setId(getId());
        return participation;
    }
}
