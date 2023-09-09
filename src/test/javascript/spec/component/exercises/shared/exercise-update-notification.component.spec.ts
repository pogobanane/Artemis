import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExerciseUpdateNotificationComponent } from 'app/exercises/shared/exercise-update-notification/exercise-update-notification.component';
import { Exercise } from 'app/entities/exercise.model';
import { FormsModule } from '@angular/forms';
import { MockModule } from 'ng-mocks';

describe('ExerciseUpdateNotificationComponent', () => {
    let component: ExerciseUpdateNotificationComponent;
    let fixture: ComponentFixture<ExerciseUpdateNotificationComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [MockModule(FormsModule)],
            declarations: [ExerciseUpdateNotificationComponent],
        });
        fixture = TestBed.createComponent(ExerciseUpdateNotificationComponent);
        component = fixture.componentInstance;
        component.exercise = { id: 1 } as Exercise;
        component.isImport = false;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
