import { Component, Input, OnInit } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { CommitInfo } from 'app/entities/programming-submission.model';
import { GitDiffReportModalComponent } from 'app/exercises/programming/hestia/git-diff-report/git-diff-report-modal.component';
import { ProgrammingExerciseGitDiffReport } from 'app/entities/hestia/programming-exercise-git-diff-report.model';
import { ProgrammingExerciseParticipationService } from 'app/exercises/programming/manage/services/programming-exercise-participation.service';
import dayjs from 'dayjs';

@Component({
    selector: 'jhi-commits-info',
    templateUrl: './commits-info.component.html',
    styleUrls: ['./commits-info.component.scss'],
})
export class CommitsInfoComponent implements OnInit {
    @Input() commits?: CommitInfo[];
    @Input() activeCommitHash?: string;
    @Input() participationId?: number;

    constructor(private programmingExerciseParticipationService: ProgrammingExerciseParticipationService) {}

    ngOnInit(): void {
        if (!this.commits) {
            if (this.participationId) {
                this.programmingExerciseParticipationService.retrieveCommitsInfoForParticipation(this.participationId).subscribe((commits) => {
                    this.commits = this.sortCommitsByTimestampAsc(commits);
                });
            }
        }
    }
    sortCommitsByTimestampAsc(commitInfos?: CommitInfo[]) {
        return commitInfos?.sort((a, b) => dayjs(a.timestamp!).unix() - dayjs(b.timestamp!).unix())!;
    }
}
