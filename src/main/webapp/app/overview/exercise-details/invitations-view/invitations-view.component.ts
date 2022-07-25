import { Component, Input, OnInit } from '@angular/core';
import { faPlayCircle, faTrashAlt } from '@fortawesome/free-solid-svg-icons';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Exercise } from 'app/entities/exercise.model';
import { TeamStudentInvitation } from 'app/entities/teamStudentInvitation.model';
import { TeamService } from 'app/exercises/shared/team/team.service';

@Component({
    selector: 'jhi-invitations-view',
    templateUrl: './invitations-view.component.html',
    styleUrls: ['./invitations-view.component.scss'],
})
export class InvitationsViewComponent implements OnInit {
    @Input() exercise: Exercise;

    faPlayCircle = faPlayCircle;
    faTrashAlt = faTrashAlt;
    protected invitations: TeamStudentInvitation[];

    constructor(private teamService: TeamService, private activeModal: NgbActiveModal) {}

    ngOnInit(): void {
        this.teamService.getInvitationsForGroupExercise(this.exercise).subscribe((response) => {
            this.invitations = response.body!;
        });
    }

    onAccept(invitation: TeamStudentInvitation) {
        if (invitation.team && invitation.team.id) {
            this.teamService.acceptInvitation(this.exercise, invitation.team.id).subscribe({
                next: () => {
                    this.exercise.teamIdStudentAcceptedInvitationTo = invitation.team?.id;
                    this.activeModal.close();
                },
            });
        }
    }

    onDecline(declinedInvitation: TeamStudentInvitation) {
        if (declinedInvitation.team && declinedInvitation.team.id) {
            this.teamService.rejectInvitation(this.exercise, declinedInvitation.team.id).subscribe({
                next: (team) => {
                    this.exercise.teamsIdStudentIsInvitedTo = this.exercise.teamsIdStudentIsInvitedTo?.filter((id) => id !== team.body!.id);
                    this.invitations = this.invitations.filter((invitation) => invitation.id !== declinedInvitation.id);
                },
            });
        }
    }

    clear() {
        this.activeModal.dismiss();
    }
}
