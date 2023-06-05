import { Route } from '@angular/router';
import { ArtemisSecurityCenterComponent } from 'app/admin/artemis-security-center/artemis-security-center.component';

export const artemisSecurityCenterRoute: Route = {
    path: 'artemis-security-center',
    component: ArtemisSecurityCenterComponent,
    data: {
        pageTitle: 'artemisApp.artemisSecurityCenter.title',
    },
};
