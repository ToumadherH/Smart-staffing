import { Component, HostListener } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { ConsultantService } from '../../core/consultant.service';
import { Consultant } from '../../core/models';

@Component({
  selector: 'app-cv-upload',
  imports: [RouterLink, DecimalPipe],
  templateUrl: './cv-upload.component.html',
  styleUrl: './cv-upload.component.scss'
})
export class CvUploadComponent {
  consultant?: Consultant;
  file?: File;
  error = '';
  success = '';
  uploading = false;
  readonly id: number;

  constructor(route: ActivatedRoute, private readonly router: Router, private readonly api: ConsultantService) {
    this.id = Number(route.snapshot.paramMap.get('id'));
    api.get(this.id).subscribe({
      next: consultant => this.consultant = consultant,
      error: () => this.error = 'Consultant could not be found.'
    });
  }

  select(event: Event): void {
    this.setFile((event.target as HTMLInputElement).files?.[0]);
  }

  @HostListener('drop', ['$event'])
  drop(event: DragEvent): void {
    event.preventDefault();
    this.setFile(event.dataTransfer?.files[0]);
  }

  @HostListener('dragover', ['$event'])
  dragover(event: DragEvent): void {
    event.preventDefault();
  }

  setFile(file?: File): void {
    this.error = '';
    if (!file) return;
    if (!['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'].includes(file.type)) {
      this.error = 'Select a PDF, DOC, or DOCX file.';
      return;
    }
    if (file.size > 10 * 1024 * 1024) {
      this.error = 'The CV must be no larger than 10MB.';
      return;
    }
    this.file = file;
  }

  upload(): void {
    if (!this.file) {
      this.error = 'Select a CV first.';
      return;
    }
    this.uploading = true;
    this.api.uploadCv(this.id, this.file).subscribe({
      next: () => {
        this.success = 'CV uploaded successfully.';
        this.uploading = false;
        setTimeout(() => this.router.navigate(['/consultants', this.id]), 700);
      },
      error: err => {
        this.error = err.error?.message || 'Could not upload this CV.';
        this.uploading = false;
      }
    });
  }
}
