import { AfterViewInit, ChangeDetectorRef, Component, OnInit, QueryList, ViewChild, ViewChildren } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { StudentExamService } from 'app/exam/manage/student-exams/student-exam.service';
import { StudentExam } from 'app/entities/student-exam.model';
import { Exercise, ExerciseType } from 'app/entities/exercise.model';
import { ExamPage } from 'app/entities/exam-page.model';
import { ExamPageComponent } from 'app/exam/participate/exercises/exam-page.component';
import { ExamSubmissionComponent } from 'app/exam/participate/exercises/exam-submission.component';
import { ExamNavigationBarComponent } from 'app/exam/participate/exam-navigation-bar/exam-navigation-bar.component';
import { SubmissionService } from 'app/exercises/shared/submission/submission.service';
import dayjs from 'dayjs/esm';
import { SubmissionVersion } from 'app/entities/submission-version.model';
import { Observable, map, merge, mergeMap, toArray } from 'rxjs';
import { ProgrammingSubmission } from 'app/entities/programming-submission.model';
import { Submission } from 'app/entities/submission.model';
import { ArtemisDatePipe } from 'app/shared/pipes/artemis-date.pipe';
import { ChangeContext, Options } from 'ngx-slider-v2';

@Component({
    selector: 'jhi-student-exam-timeline',
    templateUrl: './student-exam-timeline.component.html',
    styleUrls: ['./student-exam-timeline.component.scss'],
})
export class StudentExamTimelineComponent implements OnInit, AfterViewInit {
    readonly TEXT = ExerciseType.TEXT;
    readonly QUIZ = ExerciseType.QUIZ;
    readonly MODELING = ExerciseType.MODELING;
    readonly PROGRAMMING = ExerciseType.PROGRAMMING;
    readonly FILEUPLOAD = ExerciseType.FILE_UPLOAD;
    // determines if component was once drawn visited
    pageComponentVisited: boolean[];
    value: number;
    options: Options = {
        showTicks: true,
        showTicksValues: true,
        stepsArray: [{ value: 0 }],
        translate: (value: number): string => {
            return this.datePipe.transform(value, 'time', true);
        },
    };

    studentExam: StudentExam;
    exerciseIndex: number;
    activeExamPage = new ExamPage();
    courseId: number;
    submissionTimeStamps: dayjs.Dayjs[] = [];
    submissionVersions: SubmissionVersion[] = [];
    programmingSubmissions: ProgrammingSubmission[] = [];
    @ViewChildren(ExamSubmissionComponent) currentPageComponents: QueryList<ExamSubmissionComponent>;
    @ViewChild('examNavigationBar') examNavigationBarComponent: ExamNavigationBarComponent;
    readonly SubmissionVersion = SubmissionVersion;

    constructor(
        private studentExamService: StudentExamService,
        private activatedRoute: ActivatedRoute,
        private submissionService: SubmissionService,
        private datePipe: ArtemisDatePipe,
        private changeDetectorRef: ChangeDetectorRef,
    ) {}

    ngOnInit(): void {
        this.activatedRoute.data.subscribe(({ studentExam: studentExamWithGrade }) => {
            this.studentExam = studentExamWithGrade.studentExam;
            this.courseId = this.studentExam.exam?.course?.id!;
        });
        this.exerciseIndex = 0;
        this.pageComponentVisited = new Array(this.studentExam.exercises!.length).fill(false);
        this.retrieveSubmissionDataAndTimeStamps().subscribe((results) => {
            results.forEach((result) => {
                if (this.isSubmissionVersion(result)) {
                    const submissionVersion = result as SubmissionVersion;
                    console.log('submitted version at ' + submissionVersion.createdDate);
                    this.submissionVersions.push(submissionVersion);
                    this.submissionTimeStamps.push(submissionVersion.createdDate);
                } else {
                    const programmingSubmission = result as ProgrammingSubmission;
                    console.log('programming submission at ' + programmingSubmission.submissionDate!);

                    this.programmingSubmissions.push(programmingSubmission);
                    this.submissionTimeStamps.push(programmingSubmission.submissionDate!);
                }
            });
            this.sortTimeStamps();
            this.setupRangeSlider();
        });
    }

    private setupRangeSlider() {
        this.value = this.submissionTimeStamps[0]?.toDate().getTime();
        const newOptions: Options = Object.assign({}, this.options);
        newOptions.stepsArray = this.submissionTimeStamps.map((date) => {
            return {
                value: date.toDate().getTime(),
            };
        });
        this.options = newOptions;
    }

    private isSubmissionVersion(object: SubmissionVersion | Submission | null) {
        if (object === null) {
            return false;
        }
        const submissionVersion = object as SubmissionVersion;
        return submissionVersion.id && submissionVersion.createdDate && submissionVersion.content && submissionVersion.submission;
    }

    private retrieveSubmissionDataAndTimeStamps() {
        const submissionObservables: Observable<SubmissionVersion[] | Submission[]>[] = [];
        this.studentExam.exercises?.forEach((exercise) => {
            if (exercise.type !== this.PROGRAMMING) {
                submissionObservables.push(
                    this.submissionService.findAllSubmissionVersionsOfSubmission(exercise.studentParticipations![0].submissions![0].id!).pipe(
                        mergeMap((versions) => versions),
                        toArray(),
                    ),
                );
            } else {
                submissionObservables.push(this.submissionService.findAllSubmissionsOfParticipation(exercise.studentParticipations![0].id!).pipe(map(({ body }) => body!)));
            }
        });
        return merge(...submissionObservables);
    }

    private sortTimeStamps() {
        this.submissionTimeStamps = this.submissionTimeStamps.sort((date1, date2) => (date1.isAfter(date2) ? 1 : -1));
    }

    ngAfterViewInit(): void {
        this.examNavigationBarComponent.changePage(false, this.exerciseIndex, false);
    }

    onPageChange(exerciseChange: { overViewChange: boolean; exercise?: Exercise; forceSave: boolean }): void {
        const activeComponent = this.activePageComponent;
        if (activeComponent) {
            activeComponent.onDeactivate();
        }
        this.initializeExercise(exerciseChange.exercise!);
    }

    initializeExercise(exercise: Exercise) {
        this.activeExamPage.exercise = exercise;
        // set current exercise Index
        this.exerciseIndex = this.studentExam.exercises!.findIndex((exercise1) => exercise1.id === exercise.id);
        this.activateActiveComponent();
    }

    private activateActiveComponent() {
        this.pageComponentVisited[this.activePageIndex] = true;
        const activeComponent = this.activePageComponent;
        if (activeComponent) {
            activeComponent.onActivate();
        }
    }

    get activePageIndex(): number {
        return this.studentExam.exercises!.findIndex((examExercise) => examExercise.id === this.activeExamPage.exercise?.id);
    }

    get activePageComponent(): ExamPageComponent | undefined {
        // we have to find the current component based on the activeExercise because the queryList might not be full yet (e.g. only 2 of 5 components initialized)
        return this.currentPageComponents.find((submissionComponent) => (submissionComponent as ExamSubmissionComponent).getExercise().id === this.activeExamPage.exercise?.id);
    }

    onInputChange(changeContext: ChangeContext) {
        console.log('change');
        const submission = this.findCorrespondingSubmissionForTimestamp(changeContext.value);
        let exercise: Exercise | undefined;
        if (this.isSubmissionVersion(submission)) {
            const submissionVersion = submission as SubmissionVersion;
            exercise = submissionVersion.submission.participation?.exercise;
        } else {
            const programmingSubmission = submission as ProgrammingSubmission;
            exercise = programmingSubmission.participation?.exercise;
        }
        if (exercise) {
            console.log('exercise id: ' + exercise.id);
            const exerciseIndex = this.studentExam.exercises!.findIndex((examExercise) => examExercise.id === exercise?.id);

            this.examNavigationBarComponent.changePage(false, exerciseIndex, false);
            console.log(this.currentPageComponents);
            this.currentPageComponents.forEach((component) => {
                console.log('exercise id in submission component' + (component as ExamSubmissionComponent).getExercise().id);
            });
            const correspondingSubmissionComponent = this.currentPageComponents.find(
                (submissionComponent) => (submissionComponent as ExamSubmissionComponent).getExercise().id === exercise?.id,
            );
            if (!correspondingSubmissionComponent) {
                console.log('no corresponding submission component found');
            }
            if (exercise.type === ExerciseType.PROGRAMMING) {
                correspondingSubmissionComponent!.submission = submission as ProgrammingSubmission;
            } else {
                correspondingSubmissionComponent!.submissionVersion = submission as SubmissionVersion;
            }
        }
        // TODO find the corresponding submission for the timestamp instantiate the component with it and navigate to the respective page
    }

    private findCorrespondingSubmissionForTimestamp(timestamp: number): SubmissionVersion | ProgrammingSubmission | null {
        console.log('find submission for timestamp' + timestamp);
        for (let i = 0; i < this.submissionVersions.length; i++) {
            const comparisonObject = dayjs(timestamp);
            const submissionVersion = this.submissionVersions[i];
            if (submissionVersion.createdDate.isSame(comparisonObject)) {
                return submissionVersion;
            }
        }
        for (let i = 0; i < this.programmingSubmissions.length; i++) {
            const comparisonObject = dayjs(timestamp);
            const programmingSubmission = this.programmingSubmissions[i];
            if (programmingSubmission.submissionDate?.isSame(comparisonObject)) {
                return programmingSubmission;
            }
        }
        return null;
    }
}
