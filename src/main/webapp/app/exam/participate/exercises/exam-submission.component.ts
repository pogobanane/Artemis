import { Submission } from 'app/entities/submission.model';
import { Exercise } from 'app/entities/exercise.model';
import { ExamPageComponent } from 'app/exam/participate/exercises/exam-page.component';
import { Directive, Input } from '@angular/core';
import { SubmissionVersion } from 'app/entities/submission-version.model';

@Directive()
export abstract class ExamSubmissionComponent extends ExamPageComponent {
    /**
     * checks whether the component has unsaved changes.
     * It is called in the periodic update timer to determine, if the component needs an update
     */
    abstract hasUnsavedChanges(): boolean;

    /**
     * updates the submission with the values from the displayed content.
     * This is called when the submission is saved, so that the latest state is synchronized with the server
     */
    abstract updateSubmissionFromView(): void;

    /**
     * takes the current values from the submission and displays them
     * This is called when a component is initialized, because we want to display the state of the submission.
     * In case the submission has not been edited it is an empty submission.
     */
    abstract updateViewFromSubmission(): void;
    abstract updateViewFromSubmissionVersion(): void;

    abstract getSubmission(): Submission | undefined;
    abstract getExercise(): Exercise;
    @Input() readonly = false;
    submissionVersion: SubmissionVersion;
    submission: Submission;
    @Input() examTimeline = false;
}
