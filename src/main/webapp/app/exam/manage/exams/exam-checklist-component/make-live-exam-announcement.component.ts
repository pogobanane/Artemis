import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
    selector: 'jhi-make-live-exam-announcement-modal',
    templateUrl: './make-live-exam-announcement.component.html',
})
export class MakeLiveExamAnnouncementModalComponent {
    announcementText: string;
    constructor(private activeModal: NgbActiveModal) {}

    /**
     * Closes the modal by dismissing it
     */
    clear() {
        this.activeModal.dismiss('cancel');
    }
}
