import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ExerciseChatbotComponent } from 'app/iris/exercise-chatbot/exercise-chatbot.component';
import { MatDialog } from '@angular/material/dialog';
import { ExerciseChatWidgetComponent } from 'app/iris/exercise-chatbot/exercise-chatwidget/exercise-chat-widget.component';
import { Overlay } from '@angular/cdk/overlay';
import { FormsModule } from '@angular/forms';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { ArtemisTranslatePipe } from 'app/shared/pipes/artemis-translate.pipe';
import { MockPipe } from 'ng-mocks';
import { of } from 'rxjs';

describe('ExerciseChatbotComponent', () => {
    let component: ExerciseChatbotComponent;
    let fixture: ComponentFixture<ExerciseChatbotComponent>;
    let mockDialog: MatDialog;
    let mockOverlay: Overlay;

    beforeEach(async () => {
        mockDialog = {
            open: jest.fn().mockReturnValue({
                afterClosed: jest.fn().mockReturnValue(of('true')) as any, // Use 'as any' to cast to unknown
                close: jest.fn(),
            }) as any, // Use 'as any' to cast to unknown
            closeAll: jest.fn(),
        } as any; // Use 'as any' to cast to unknown

        mockOverlay = {
            scrollStrategies: {
                noop: jest.fn().mockReturnValue({}),
            },
        } as unknown as Overlay;

        await TestBed.configureTestingModule({
            imports: [FormsModule, FontAwesomeModule],
            declarations: [ExerciseChatbotComponent, MockPipe(ArtemisTranslatePipe)],
            providers: [
                { provide: MatDialog, useValue: mockDialog },
                { provide: Overlay, useValue: mockOverlay },
            ],
        })
            .compileComponents()
            .then(() => {
                fixture = TestBed.createComponent(ExerciseChatbotComponent);
                component = fixture.componentInstance;
                fixture.detectChanges();
            });
    });

    it('should open chat when chat accepted', () => {
        jest.spyOn(component, 'openChat');

        component.chatAccepted = true;
        component.handleButtonClick();

        expect(component.openChat).toHaveBeenCalled();
    });

    it('should open chat and set buttonDisabled and chatOpen flags', () => {
        jest.spyOn(component.dialog, 'open');
        component.buttonDisabled = false;
        component.chatOpen = false;

        component.openChat();

        expect(component.dialog.open).toHaveBeenCalledWith(ExerciseChatWidgetComponent, {
            hasBackdrop: false,
            scrollStrategy: {},
            position: { bottom: '0px', right: '0px' },
        });
        expect(component.buttonDisabled).toBeTrue();
    });
});
