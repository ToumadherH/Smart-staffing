import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { LoginComponent } from './features/login/login.component';
import { HrShellComponent } from './features/shell/hr-shell.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { ConsultantListComponent } from './features/consultants/consultant-list.component';
import { ConsultantFormComponent } from './features/consultants/consultant-form.component';
import { ConsultantDetailsComponent } from './features/consultants/consultant-details.component';
import { CvUploadComponent } from './features/consultants/cv-upload.component';
import { StaffingRequestListComponent } from './features/staffing-requests/staffing-request-list.component';
import { StaffingRequestFormComponent } from './features/staffing-requests/staffing-request-form.component';
import { InterviewsComponent } from './features/interviews/interviews.component';
import { AiMatchingComponent } from './features/ai-matching/ai-matching.component';
import { SkillGapAnalysisComponent } from './features/gap-analysis/skill-gap-analysis.component';
import { ReportsComponent } from './features/reports/reports.component';
import { SettingsComponent } from './features/settings/settings.component';

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
      { path: 'consultants/:id/cv', component: CvUploadComponent },
      { path: 'staffing-requests', component: StaffingRequestListComponent },
      { path: 'staffing-requests/new', component: StaffingRequestFormComponent },
      { path: 'staffing-requests/:id/matches', component: AiMatchingComponent },
      { path: 'ai-matching', component: AiMatchingComponent },
      { path: 'ai-matching/:id', component: AiMatchingComponent },
      { path: 'ai-matching/:requestId/gap/:consultantId', component: SkillGapAnalysisComponent },
      { path: 'matching/gap-analysis', component: SkillGapAnalysisComponent },
      { path: 'interviews', component: InterviewsComponent },
      { path: 'reports', component: ReportsComponent },
      { path: 'settings', component: SettingsComponent }
    ]
  },
  { path: '**', redirectTo: '' }
];
