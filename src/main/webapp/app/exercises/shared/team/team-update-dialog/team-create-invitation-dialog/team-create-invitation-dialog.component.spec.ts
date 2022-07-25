import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TeamCreateInvitationDialogComponent } from './team-create-invitation-dialog.component';

describe('TeamCreateInvitationDialogComponent', () => {
    let component: TeamCreateInvitationDialogComponent;
    let fixture: ComponentFixture<TeamCreateInvitationDialogComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [TeamCreateInvitationDialogComponent],
        }).compileComponents();

        fixture = TestBed.createComponent(TeamCreateInvitationDialogComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
