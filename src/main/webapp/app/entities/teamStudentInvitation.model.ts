import { User } from 'app/core/user/user.model';
import { Team } from './team.model';

export class TeamStudentInvitation {
    public id: number;
    public student: User;
    public team?: Team;
    public accepted?: boolean;

    constructor(student: User) {
        this.student = student;
    }
}
