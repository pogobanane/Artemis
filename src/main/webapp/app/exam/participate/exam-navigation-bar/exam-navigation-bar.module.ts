import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ExamNavigationBarComponent } from 'app/exam/participate/exam-navigation-bar/exam-navigation-bar.component';
import { ArtemisSharedCommonModule } from 'app/shared/shared-common.module';
import { ArtemisExamTimerModule } from 'app/exam/participate/timer/exam-timer.module';

@NgModule({
    declarations: [ExamNavigationBarComponent],
    imports: [CommonModule, ArtemisSharedCommonModule, ArtemisExamTimerModule],
    exports: [ExamNavigationBarComponent],
})
export class ArtemisExamNavigationBarModule {}
