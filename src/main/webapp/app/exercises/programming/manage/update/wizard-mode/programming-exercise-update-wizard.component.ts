import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ProgrammingExercise, ProgrammingLanguage, ProjectType } from 'app/entities/programming-exercise.model';
import { AuxiliaryRepository } from 'app/entities/programming-exercise-auxiliary-repository-model';
import { ExerciseCategory } from 'app/entities/exercise-category.model';
import { ModePickerOption } from 'app/exercises/shared/mode-picker/mode-picker.component';
import { Observable } from 'rxjs';
import { ValidationReason } from 'app/entities/exercise.model';

export type InfoStepInputs = {
    titleNamePattern: string;
    shortNamePattern: RegExp;
    updateRepositoryName: (auxiliaryRepository: AuxiliaryRepository) => (newValue: any) => string | undefined;
    updateCheckoutDirectory: (editedAuxiliaryRepository: AuxiliaryRepository) => (newValue: any) => string | undefined;
    refreshAuxiliaryRepositoryChecks: () => void;
    exerciseCategories: ExerciseCategory[];
    existingCategories: ExerciseCategory[];
    updateCategories: (categories: ExerciseCategory[]) => void;

    // Auxiliary Repository settings
    auxiliaryRepositoriesSupported: boolean;
    auxiliaryRepositoryDuplicateDirectories: boolean;
    auxiliaryRepositoryDuplicateNames: boolean;
    checkoutSolutionRepositoryAllowed: boolean;
    invalidDirectoryNamePattern: RegExp;
    invalidRepositoryNamePattern: RegExp;
    isImportFromExistingExercise: boolean;
};

export type LanguageStepInputs = {
    appNamePatternForSwift: string;
    modePickerOptions: ModePickerOption<ProjectType>[];
    withDependencies: boolean;
    onWithDependenciesChanged: (withDependencies: boolean) => boolean;
    packageNameRequired: boolean;
    packageNamePattern: string;
    supportedLanguages: string[];
    selectedProgrammingLanguage: ProgrammingLanguage;
    onProgrammingLanguageChange: (language: ProgrammingLanguage) => ProgrammingLanguage;
    projectTypes: ProjectType[];
    selectedProjectType: ProjectType;
    onProjectTypeChange: (projectType: ProjectType) => ProjectType;
    staticCodeAnalysisAllowed: boolean;
    onStaticCodeAnalysisChanged: () => void;
    maxPenaltyPattern: string;
    sequentialTestRunsAllowed: boolean;
    testwiseCoverageAnalysisSupported: boolean;
};

// Currently there are no inputs, however this can be used like the other inputs as well
export type GradingStepInputs = object;

export type ProblemStepInputs = {
    problemStatementLoaded: boolean;
    templateParticipationResultLoaded: boolean;
    hasUnsavedChanges: boolean;
    rerenderSubject: Observable<void>;
    checkoutSolutionRepositoryAllowed: boolean;
    validIdeSelection: () => boolean | undefined;
    inProductionEnvironment: boolean;
    recreateBuildPlans: boolean;
    onRecreateBuildPlanOrUpdateTemplateChange: () => void;
    updateTemplate: boolean;
    selectedProjectType: ProjectType;
};

export type InfrastructureStepInputs = {
    // checkbox things
    checkoutSolutionRepositoryAllowed: boolean;
    isImportFromExistingExercise: boolean;
    publishBuildPlanUrlAllowed: boolean;
    recreateBuildPlanOrUpdateTemplateChange: () => void; // default false
    recreateBuildPlans: boolean;
    refreshAuxiliaryRepositoryChecks: () => void;
    selectedProjectType: ProjectType;
    updateCheckoutDirectory: (editedAuxiliaryRepository: AuxiliaryRepository) => (newValue: any) => string | undefined;
    updateRepositoryName: (auxiliaryRepository: AuxiliaryRepository) => (newValue: any) => string | undefined;
    updateTemplate: boolean;
    validIdeSelection: () => boolean | undefined;
};

@Component({
    selector: 'jhi-programming-exercise-update-wizard',
    templateUrl: './programming-exercise-update-wizard.component.html',
    styleUrls: ['./programming-exercise-update-wizard.component.scss'],
})
export class ProgrammingExerciseUpdateWizardComponent implements OnInit {
    programmingExercise: ProgrammingExercise;

    @Output() exerciseChange = new EventEmitter<ProgrammingExercise>();

    @Input()
    get exercise() {
        return this.programmingExercise;
    }

    set exercise(exercise: ProgrammingExercise) {
        this.programmingExercise = exercise;
        this.exerciseChange.emit(this.programmingExercise);
    }

    @Input() toggleMode: () => void;
    @Input() isSaving: boolean;
    @Input() currentStep: number;
    @Output() onNextStep: EventEmitter<any> = new EventEmitter();
    @Input() getInvalidReasons: () => ValidationReason[];
    @Input() isExamMode: boolean;
    @Input() isImportFromExistingExercise: boolean;

    @Input() infoStepInputs: InfoStepInputs;
    @Input() languageStepInputs: LanguageStepInputs;
    @Input() gradingStepInputs: GradingStepInputs;
    @Input() problemStepInputs: ProblemStepInputs;
    @Input() infrastructureStepInputs: InfrastructureStepInputs;
    @Input() auxiliaryRepositoriesSupported: boolean;

    constructor(protected activatedRoute: ActivatedRoute) {}

    ngOnInit() {
        this.isSaving = false;
    }

    nextStep() {
        this.onNextStep.emit();
    }
}
