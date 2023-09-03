package de.tum.in.www1.artemis.service.connectors.athena;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import de.tum.in.www1.artemis.domain.Exercise;
import de.tum.in.www1.artemis.domain.enumeration.RepositoryType;
import de.tum.in.www1.artemis.domain.participation.ProgrammingExerciseStudentParticipation;
import de.tum.in.www1.artemis.repository.*;
import de.tum.in.www1.artemis.service.FileService;
import de.tum.in.www1.artemis.service.programming.ProgrammingExerciseExportService;
import de.tum.in.www1.artemis.web.rest.dto.RepositoryExportOptionsDTO;
import de.tum.in.www1.artemis.web.rest.errors.AccessForbiddenException;
import de.tum.in.www1.artemis.web.rest.util.ResponseUtil;

/**
 * Service for exporting programming exercise repositories for Athena.
 */
@Service
@Profile("athena")
public class AthenaRepositoryExportService {

    private final Logger log = LoggerFactory.getLogger(AthenaRepositoryExportService.class);

    // The downloaded repos should be cloned into another path in order to not interfere with the repo used by the student
    // We reuse the same directory as the programming exercise export service for this.
    @Value("${artemis.repo-download-clone-path}")
    private Path repoDownloadClonePath;

    private final ProgrammingExerciseRepository programmingExerciseRepository;

    private final ProgrammingExerciseExportService programmingExerciseExportService;

    private final FileService fileService;

    private final ProgrammingSubmissionRepository programmingSubmissionRepository;

    public AthenaRepositoryExportService(ProgrammingExerciseRepository programmingExerciseRepository, ProgrammingExerciseExportService programmingExerciseExportService,
            FileService fileService, ProgrammingSubmissionRepository programmingSubmissionRepository) {
        this.programmingExerciseRepository = programmingExerciseRepository;
        this.programmingExerciseExportService = programmingExerciseExportService;
        this.fileService = fileService;
        this.programmingSubmissionRepository = programmingSubmissionRepository;
    }

    /**
     * Check if feedback suggestions are enabled for the given exercise, otherwise throw an exception.
     *
     * @param exercise the exercise to check
     * @throws AccessForbiddenException if the feedback suggestions are not enabled for the given exercise
     */
    private void checkFeedbackSuggestionsEnabledElseThrow(Exercise exercise) {
        if (!exercise.getFeedbackSuggestionsEnabled()) {
            log.error("Feedback suggestions are not enabled for exercise {}", exercise.getId());
            throw new AccessForbiddenException("Feedback suggestions are not enabled for exercise");
        }
    }

    /**
     * Export the repository for the given exercise and participation to a zip file.
     * The ZIP file will be deleted automatically after 15 minutes.
     *
     * @param exerciseId     the id of the exercise to export the repository for
     * @param submissionId   the id of the submission to export the repository for (only for student repository, otherwise pass null)
     * @param repositoryType the type of repository to export. Pass null to export the student repository.
     * @return 200 Ok if successful with the corresponding result as body
     * @throws IOException              if the export fails
     * @throws AccessForbiddenException if the feedback suggestions are not enabled for the given exercise
     */
    public ResponseEntity<Resource> exportRepository(long exerciseId, Long submissionId, RepositoryType repositoryType) throws IOException {
        log.debug("Exporting repository for exercise {}, submission {}", exerciseId, submissionId);

        var programmingExercise = programmingExerciseRepository.findByIdElseThrow(exerciseId);
        checkFeedbackSuggestionsEnabledElseThrow(programmingExercise);

        var exportOptions = new RepositoryExportOptionsDTO();
        exportOptions.setAnonymizeRepository(false);
        exportOptions.setExportAllParticipants(false);
        exportOptions.setFilterLateSubmissions(false);
        exportOptions.setFilterLateSubmissionsIndividualDueDate(false);

        if (!Files.exists(repoDownloadClonePath)) {
            Files.createDirectories(repoDownloadClonePath);
        }

        Path exportDir = fileService.getTemporaryUniquePath(repoDownloadClonePath, 15);
        Path zipFile = null;

        if (repositoryType == null) { // Export student repository
            var submission = programmingSubmissionRepository.findById(submissionId).orElseThrow();
            zipFile = programmingExerciseExportService.createZipForRepositoryWithParticipation(programmingExercise,
                    (ProgrammingExerciseStudentParticipation) submission.getParticipation(), exportOptions, exportDir, exportDir);
        }
        else {
            List<String> exportErrors = List.of();
            var exportFile = programmingExerciseExportService.exportInstructorRepositoryForExercise(programmingExercise.getId(), repositoryType, exportDir, exportErrors);
            if (exportFile.isPresent()) {
                zipFile = exportFile.get().toPath();
            }
        }

        if (zipFile == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseUtil.ok(zipFile.toFile());
    }
}
