import { NgModule } from '@angular/core';
import { UserSettingsContainerComponent } from 'app/shared/user-settings/user-settings-container/user-settings-container.component';
import { AccountInformationComponent } from 'app/shared/user-settings/account-information/account-information.component';
import { NotificationSettingsComponent } from 'app/shared/user-settings/notification-settings/notification-settings.component';
import { ArtemisSharedModule } from 'app/shared/shared.module';
import { RouterModule } from '@angular/router';
import { userSettingsState } from 'app/shared/user-settings/user-settings.route';
import { DataExportComponent } from 'app/shared/user-settings/data-export/data-export.component';
import { ArtemisSharedComponentModule } from 'app/shared/components/shared-component.module';

@NgModule({
    imports: [RouterModule.forChild(userSettingsState), ArtemisSharedModule, ArtemisSharedComponentModule],
    declarations: [UserSettingsContainerComponent, AccountInformationComponent, NotificationSettingsComponent, DataExportComponent],
})
export class UserSettingsModule {}
