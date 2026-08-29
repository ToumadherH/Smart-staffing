import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { StaffingRequestService } from '../../core/staffing-request.service';
import { Skill, StaffingRequestStatus } from '../../core/models';

@Component({
  selector: 'app-staffing-request-form',
  imports: [RouterLink, FormsModule],
  templateUrl: './staffing-request-form.component.html',
  styleUrl: './staffing-request-form.component.scss'
})
export class StaffingRequestFormComponent {
  title = '';
  clientName = '';
  location = '';
  yearsOfExperienceRequired: number | null = 3;
  description = '';
  status: StaffingRequestStatus = 'OPEN';

  newSkillName = '';
  newSkillCategory = 'Technical';
  skills: Skill[] = [];

  submitting = false;
  errorMessage = '';

  constructor(
    private readonly staffingRequestService: StaffingRequestService,
    private readonly router: Router
  ) {}

  addSkill(): void {
    const trimmed = this.newSkillName.trim();
    if (!trimmed) return;
    if (this.skills.some(s => s.name.toLowerCase() === trimmed.toLowerCase())) {
      this.newSkillName = '';
      return;
    }
    this.skills.push({ name: trimmed, category: this.newSkillCategory });
    this.newSkillName = '';
  }

  removeSkill(index: number): void {
    this.skills.splice(index, 1);
  }

  save(): void {
    if (!this.title.trim() || !this.clientName.trim()) {
      this.errorMessage = 'Please provide both Position Title and Client Name.';
      return;
    }

    this.submitting = true;
    this.errorMessage = '';

    const payload = {
      title: this.title.trim(),
      clientName: this.clientName.trim(),
      location: this.location.trim(),
      yearsOfExperienceRequired: this.yearsOfExperienceRequired ?? undefined,
      description: this.description.trim(),
      status: this.status,
      requiredSkills: this.skills
    };

    this.staffingRequestService.create(payload).subscribe({
      next: () => {
        this.submitting = false;
        this.router.navigate(['/staffing-requests']);
      },
      error: err => {
        this.submitting = false;
        this.errorMessage = err?.error?.message || 'Could not save staffing request. Please check required fields.';
      }
    });
  }
}
