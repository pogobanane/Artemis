import { Component, ViewEncapsulation } from '@angular/core';
import { User } from 'app/core/user/user.model';
import { BaseTeamCreationComponent } from './base-team-creation.component';

@Component({
    selector: 'jhi-team-update-dialog',
    templateUrl: './team-update-dialog.component.html',
    styleUrls: ['./base-team-creation.component.scss'],
    encapsulation: ViewEncapsulation.None,
})
export class TeamUpdateDialogComponent extends BaseTeamCreationComponent {
    private isStudentAlreadyInPendingTeam(student: User): boolean {
        return this.pendingTeam.students?.find((stud) => stud.id === student.id) !== undefined;
    }

    /**
     * Hook to indicate a student was added to a team
     * @param {User} student - Added user
     */
    onAddStudent(student: User) {
        if (!this.isStudentAlreadyInPendingTeam(student)) {
            this.pendingTeam.students!.push(student);
        }
    }

    /**
     * Hook to indicate a student was removed of a team
     * @param {User} student - removed user
     */
    onRemoveStudent(student: User) {
        this.pendingTeam.invitations = this.pendingTeam.invitations?.filter((invitation) => invitation.student.id !== student.id);
        this.resetStudentTeamConflict(student); // conflict might no longer exist when the student is added again
    }

    /**
     * Hook to indicate the team owner was selected
     * @param {User} owner - User to select as owner
     */
    onSelectOwner(owner: User) {
        this.pendingTeam.owner = owner;
    }

    get showIgnoreTeamSizeRecommendationOption(): boolean {
        return !this.recommendedTeamSize;
    }

    get teamSizeViolationUnconfirmed(): boolean {
        return this.showIgnoreTeamSizeRecommendationOption && !this.ignoreTeamSizeRecommendation;
    }

    private get recommendedTeamSize(): boolean {
        const pendingTeamSize = this.pendingTeam.students!.length;
        return pendingTeamSize >= this.config.minTeamSize! && pendingTeamSize <= this.config.maxTeamSize!;
    }
}
