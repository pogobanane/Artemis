package de.tum.in.www1.artemis.service;

import static de.tum.in.www1.artemis.config.Constants.FILE_ENDING_PATTERN;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import de.tum.in.www1.artemis.domain.FileUploadExercise;
import de.tum.in.www1.artemis.repository.FileUploadExerciseRepository;
import de.tum.in.www1.artemis.service.notifications.GroupNotificationScheduleService;
import de.tum.in.www1.artemis.web.rest.errors.BadRequestAlertException;

@Service
public class FileUploadExerciseService {

    private final Logger log = LoggerFactory.getLogger(FileUploadExerciseService.class);

    private final FileUploadExerciseRepository fileUploadExerciseRepository;

    private final GroupNotificationScheduleService groupNotificationScheduleService;

    public FileUploadExerciseService(FileUploadExerciseRepository fileUploadExerciseRepository, GroupNotificationScheduleService groupNotificationScheduleService) {
        this.fileUploadExerciseRepository = fileUploadExerciseRepository;
        this.groupNotificationScheduleService = groupNotificationScheduleService;
    }

    private boolean isFilePatternValid(FileUploadExercise exercise) {
        // a file ending should consist of a comma separated list of 1-5 characters / digits
        // when an empty string "" is passed in the exercise the file-pattern is null when it arrives in the rest endpoint
        if (exercise.getFilePattern() == null) {
            return false;
        }
        var filePattern = exercise.getFilePattern().toLowerCase().replaceAll("\\s+", "");
        var allowedFileEndings = filePattern.split(",");
        var isValid = true;
        for (var allowedFileEnding : allowedFileEndings) {
            isValid = isValid && FILE_ENDING_PATTERN.matcher(allowedFileEnding).matches();
        }

        if (isValid) {
            // use the lowercase version without whitespaces
            exercise.setFilePattern(filePattern);
            return true;
        }
        return false;
    }

    /**
     * Creates a FileUploadExercise
     *
     * @param fileUploadExercise the FileUploadExercise to be created
     * @return the created FileUploadExercise
     */
    public FileUploadExercise createFileUploadExercise(FileUploadExercise fileUploadExercise) {
        log.debug("Request to create FileUploadExercise: {}", fileUploadExercise);
        if (fileUploadExercise.getId() != null) {
            throw new BadRequestAlertException("A new fileUploadExercise cannot already have an ID", FileUploadExercise.ENTITY_NAME, "idExists");
        }
        // validates general settings: points, dates
        fileUploadExercise.validateGeneralSettings();
        // Valid exercises have set either a course or an exerciseGroup
        fileUploadExercise.checkCourseAndExerciseGroupExclusivity(FileUploadExercise.ENTITY_NAME);
        // Validates file pattern
        if (!isFilePatternValid(fileUploadExercise)) {
            throw new BadRequestAlertException("The file pattern is invalid. Please use a comma separated list with actual file endings without dots (e.g. 'png, pdf').",
                    FileUploadExercise.ENTITY_NAME, "filepattern.invalid");
        }

        FileUploadExercise result = fileUploadExerciseRepository.save(fileUploadExercise);
        groupNotificationScheduleService.checkNotificationsForNewExercise(fileUploadExercise);

        return result;
    }
}
