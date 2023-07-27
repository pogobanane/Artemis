import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ArtemisTestModule } from '../../../test.module';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { MockComponent } from 'ng-mocks';
import { LearningPathProgressModalComponent } from 'app/course/learning-paths/learning-path-management/learning-path-progress-modal.component';
import { LearningPathGraphComponent } from 'app/course/learning-paths/learning-path-graph/learning-path-graph.component';
import { By } from '@angular/platform-browser';

describe('LearningPathProgressModalComponent', () => {
    let fixture: ComponentFixture<LearningPathProgressModalComponent>;
    let comp: LearningPathProgressModalComponent;
    let activeModal: NgbActiveModal;
    let closeStub: jest.SpyInstance;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [ArtemisTestModule, MockComponent(LearningPathGraphComponent)],
            declarations: [LearningPathProgressModalComponent],
            providers: [],
        })
            .compileComponents()
            .then(() => {
                fixture = TestBed.createComponent(LearningPathProgressModalComponent);
                comp = fixture.componentInstance;
                activeModal = TestBed.inject(NgbActiveModal);
                closeStub = jest.spyOn(activeModal, 'close');
                fixture.detectChanges();
            });
    });

    afterEach(() => {
        jest.restoreAllMocks();
    });

    it('should display learning path graph if id is present', () => {
        comp.learningPathId = 1;
        comp.courseId = 2;
        fixture.detectChanges();
        expect(fixture.debugElement.query(By.css('.graph')).nativeElement).toBeTruthy();
    });

    it('should correctly close modal', () => {
        comp.close();
        expect(closeStub).toHaveBeenCalledOnce();
    });
});
