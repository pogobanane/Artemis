import { Component, Input } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { CommitInfo } from 'app/entities/programming-submission.model';
import { GitDiffReportModalComponent } from 'app/exercises/programming/hestia/git-diff-report/git-diff-report-modal.component';
import { ProgrammingExerciseGitDiffReport } from 'app/entities/hestia/programming-exercise-git-diff-report.model';

@Component({
    selector: 'jhi-commits-info',
    templateUrl: './commits-info.component.html',
    styleUrls: ['./commits-info.component.scss'],
})
export class CommitsInfoComponent {
    @Input() commits: CommitInfo[];
    @Input() activeCommitHash?: string;
}
