import { Component, OnInit } from '@angular/core';

@Component({
    selector: 'jhi-artemis-security-center',
    templateUrl: './artemis-security-center.component.html',
    styles: ['.table {table-layout: fixed}'],
})
export class ArtemisSecurityCenterComponent implements OnInit {
    ngOnInit(): void {
        console.log('ASC');
    }
}
