import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { LoginComponent } from './features/login/login.component';
import { HrShellComponent } from './features/shell/hr-shell.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { ConsultantListComponent } from './features/consultants/consultant-list.component';
import { ConsultantFormComponent } from './features/consultants/consultant-form.component';
import { ConsultantDetailsComponent } from './features/consultants/consultant-details.component';
import { CvUploadComponent } from './features/consultants/cv-upload.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: '', component: HrShellComponent, canActivate: [authGuard], children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'consultants', component: ConsultantListComponent },
      { path: 'consultants/new', component: ConsultantFormComponent },
      { path: 'consultants/:id', component: ConsultantDetailsComponent },
      { path: 'consultants/:id/edit', component: ConsultantFormComponent },
      { path: 'consultants/:id/cv', component: CvUploadComponent }
    ]
  },
  { path: '**', redirectTo: '' }
];
