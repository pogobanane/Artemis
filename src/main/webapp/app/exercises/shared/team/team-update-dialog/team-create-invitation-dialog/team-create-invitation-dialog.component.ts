import { Component, Input, OnInit } from '@angular/core';
import { User } from 'app/core/user/user.model';
import { TeamStudentInvitation } from 'app/entities/teamStudentInvitation.model';
import { BaseTeamCreationComponent } from '../base-team-creation.component';

@Component({
    selector: 'jhi-team-create-invitation-dialog',
    templateUrl: './team-create-invitation-dialog.component.html',
    styleUrls: ['../base-team-creation.component.scss'],
})
export class TeamCreateInvitationDialogComponent extends BaseTeamCreationComponent implements OnInit {
    @Input() currentUser: User;

    ngOnInit(): void {
        super.ngOnInit();
        const initiallyAcceptedInvitation = new TeamStudentInvitation(this.currentUser);
        initiallyAcceptedInvitation.accepted = true;
        this.pendingTeam.invitations.push(initiallyAcceptedInvitation);
    }

    private isStudentAlreadyInPendingTeam(student: User): boolean {
        return this.pendingTeam.invitations?.find((invitation) => invitation.student.id === student.id) !== undefined;
    }

    /**
     * Hook to indicate a student was added to a team
     * @param {User} student - Added user
     */
    onAddStudent(student: User): void {
        if (!this.isStudentAlreadyInPendingTeam(student)) {
            this.pendingTeam.invitations!.push(new TeamStudentInvitation(student));
        }
    }

    /**
     * Hook to indicate a student was removed of a team
     * @param {User} student - removed user
     */
    onRemoveStudent(student: User): void {
        this.pendingTeam.invitations = this.pendingTeam.invitations?.filter((invitation) => invitation.student.id !== student.id);
        this.resetStudentTeamConflict(student); // conflict might no longer exist when the student is added again
    }

    protected get recommendedTeamSize(): boolean {
        const pendingTeamSize = this.pendingTeam.invitations!.length;
        return pendingTeamSize >= this.config.minTeamSize! && pendingTeamSize <= this.config.maxTeamSize!;
    }

    invitedStudents(): User[] {
        return this.pendingTeam.invitations?.map((invitation) => invitation.student) || [];
    }
}
