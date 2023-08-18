import { NgModule } from '@angular/core';
import { CommitsInfoComponent } from 'app/exercises/programming/shared/commits-info/commits-info.component';
import { ArtemisSharedCommonModule } from 'app/shared/shared-common.module';

@NgModule({
    declarations: [CommitsInfoComponent],
    imports: [ArtemisSharedCommonModule],
    exports: [CommitsInfoComponent],
})
export class CommitsInfoModule {}
