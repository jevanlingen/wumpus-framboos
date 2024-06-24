import { Routes } from '@angular/router';
import { AdminComponent } from './admin/admin.component';
import { CompetitionComponent } from './competition/competition.component';

export const routes: Routes = [
    {
        path: 'competitions/:id',
        component: CompetitionComponent
    },
    {
        path: 'admin',
        component: AdminComponent
    }
];
