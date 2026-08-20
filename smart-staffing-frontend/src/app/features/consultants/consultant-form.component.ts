import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Availability, ConsultantPayload } from '../../core/models';
import { ConsultantService } from '../../core/consultant.service';

@Component({
  selector: 'app-consultant-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './consultant-form.component.html',
  styleUrl: './consultant-form.component.scss'
})
export class ConsultantFormComponent implements OnInit {
  id?: number;
  saving = false;
  error = '';
  readonly form: FormGroup;

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly api: ConsultantService
  ) {
    this.form = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phone: [''],
      yearsOfExperience: [0, [Validators.required, Validators.min(0)]],
      availability: ['AVAILABLE' as Availability, Validators.required],
      currentMission: [''],
      location: [''],
      languagesText: [''],
      skills: this.fb.array([this.skillGroup()])
    });
  }

  get skills(): FormArray {
    return this.form.get('skills') as FormArray;
  }

  ngOnInit(): void {
    const paramId = Number(this.route.snapshot.paramMap.get('id'));
    if (paramId) {
      this.id = paramId;
      this.api.get(paramId).subscribe({
        next: consultant => {
          this.skills.clear();
          if (consultant.skills && consultant.skills.length > 0) {
            consultant.skills.forEach(s => {
              this.skills.push(this.fb.group({
                name: [s.name, Validators.required],
                category: [s.category || 'Technical']
              }));
            });
          } else {
            this.skills.push(this.skillGroup());
          }

          this.form.patchValue({
            name: consultant.name,
            email: consultant.email,
            phone: consultant.phone || '',
            yearsOfExperience: consultant.yearsOfExperience,
            availability: consultant.availability,
            currentMission: consultant.currentMission || '',
            location: consultant.location || '',
            languagesText: (consultant.languages || []).join(', ')
          });
        },
        error: () => {
          this.error = 'Consultant details could not be loaded.';
        }
      });
    }
  }

  skillGroup(): FormGroup {
    return this.fb.group({
      name: [''],
      category: ['Technical']
    });
  }

  addSkill(): void {
    this.skills.push(this.skillGroup());
  }

  removeSkill(index: number): void {
    if (this.skills.length > 0) {
      this.skills.removeAt(index);
    }
  }

  submit(): void {
    this.error = '';

    // Remove empty skill entries before validating the main form
    for (let i = this.skills.length - 1; i >= 0; i--) {
      const skillName = (this.skills.at(i).get('name')?.value || '').trim();
      if (!skillName && this.skills.length > 1) {
        this.skills.removeAt(i);
      }
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.error = 'Please fill out all required fields marked in the form.';
      return;
    }

    this.saving = true;
    const value = this.form.getRawValue();

    const rawSkills = (value.skills as Array<{ name: string; category: string }>);
    const validSkills = rawSkills
      .filter(s => s.name && s.name.trim().length > 0)
      .map(s => ({
        name: s.name.trim(),
        category: (s.category && s.category.trim()) || 'Technical'
      }));

    const rawLanguages = (value.languagesText || '').split(',').map((x: string) => x.trim()).filter(Boolean);

    const payload: ConsultantPayload = {
      name: value.name.trim(),
      email: value.email.trim(),
      phone: value.phone ? value.phone.trim() : '',
      yearsOfExperience: Number(value.yearsOfExperience),
      availability: value.availability as Availability,
      currentMission: value.currentMission ? value.currentMission.trim() : '',
      location: value.location ? value.location.trim() : '',
      languages: rawLanguages,
      skills: validSkills
    };

    const request$ = this.id
      ? this.api.update(this.id, payload)
      : this.api.create(payload);

    request$.subscribe({
      next: consultant => {
        this.saving = false;
        this.router.navigate(['/consultants', consultant.id]);
      },
      error: err => {
        this.saving = false;
        if (err.status === 409) {
          this.error = 'A consultant with this email address already exists.';
        } else if (err.error && err.error.message) {
          this.error = err.error.message;
        } else {
          this.error = 'Could not save consultant. Please verify your inputs.';
        }
      }
    });
  }
}
