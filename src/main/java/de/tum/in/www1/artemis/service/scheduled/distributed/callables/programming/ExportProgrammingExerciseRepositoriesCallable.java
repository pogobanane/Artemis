package de.tum.in.www1.artemis.service.scheduled.distributed.callables.programming;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;

import de.tum.in.www1.artemis.domain.ProgrammingExercise;
import de.tum.in.www1.artemis.service.archival.ArchivalReportEntry;
import de.tum.in.www1.artemis.service.programming.ProgrammingExerciseExportService;

public class ExportProgrammingExerciseRepositoriesCallable implements Callable<Path>, java.io.Serializable {

    private transient ProgrammingExerciseExportService programmingExerciseExportService;

    private final ProgrammingExercise programmingExercise;

    private final Boolean includingStudentRepos;

    private final Path outputDir;

    private final List<String> exportErrors;

    private final List<ArchivalReportEntry> reportData;

    public ExportProgrammingExerciseRepositoriesCallable(ProgrammingExercise programmingExercise, Boolean includingStudentRepos, Path outputDir, List<String> exportErrors,
            List<ArchivalReportEntry> reportData) {
        this.programmingExercise = programmingExercise;
        this.includingStudentRepos = includingStudentRepos;
        this.outputDir = outputDir;
        this.exportErrors = exportErrors;
        this.reportData = reportData;
    }

    @Override
    public Path call() {
        return programmingExerciseExportService.exportProgrammingExerciseRepositories(programmingExercise, true, outputDir, exportErrors, reportData);
    }

    @Autowired
    public void setProgrammingExerciseExportService(final ProgrammingExerciseExportService programmingExerciseExportService) {
        this.programmingExerciseExportService = programmingExerciseExportService;
    }
}
