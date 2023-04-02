import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Lecture } from 'app/entities/lecture.model';

@Component({
    selector: 'jhi-lecture-update-wizard-period',
    templateUrl: './lecture-wizard-period.component.html',
})
export class LectureUpdateWizardPeriodComponent {
    @Input() currentStep: number;
    @Input() lecture: Lecture;
    @Input() validateDatesFunction: () => void;
    @Input() isInvalidDate: boolean;
    @Output() invalidChange = new EventEmitter();

    constructor() {}
    update() {
        this.invalidChange.emit();
    }
}
