import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ConsultantService } from '../../core/consultant.service';
import { StaffingRequestService } from '../../core/staffing-request.service';
import { InterviewService } from '../../core/interview.service';
import { Consultant, InterviewRequest, StaffingRequest } from '../../core/models';

interface CompetencyItem {
  name: string;
  score: number; // 1 to 4
  level: string;
  isGap?: boolean;
}

@Component({
  selector: 'app-consultant-details',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, DatePipe],
  templateUrl: './consultant-details.component.html',
  styleUrl: './consultant-details.component.scss'
})
export class ConsultantDetailsComponent implements OnInit {
  consultant?: Consultant;
  activeRequestId: number | null = null;
  activeRequest?: StaffingRequest;
  error = '';
  activeTab: 'overview' | 'skills' | 'projects' | 'resume' = 'overview';

  // AI Match data — computed from real backend matching result
  matchScore: number | null = null;
  matchedSkills: string[] = [];
  bonusSkills: string[] = [];
  missingSkills: string[] = [];

  // Competency Matrix — derived from consultant's actual skills
  competencies: CompetencyItem[] = [];

  // Schedule Interview Modal
  showScheduleModal = false;
  interviewDate = '';
  interviewTime = '10:00';
  interviewLocation = 'Google Meet';
  interviewNotes = '';
  scheduling = false;
  scheduleSuccess = false;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly consultantService: ConsultantService,
    private readonly staffingRequestService: StaffingRequestService,
    private readonly interviewService: InterviewService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.route.queryParamMap.subscribe(qp => {
      if (qp.get('requestId')) {
        this.activeRequestId = Number(qp.get('requestId'));
      }
    });

    this.consultantService.get(id).subscribe({
      next: c => {
        this.consultant = c;
        this.buildConsultantContext();
      },
      error: () => this.error = 'Consultant could not be found.'
    });
  }

  buildConsultantContext(): void {
    if (!this.consultant) return;

    // Build competency matrix from ACTUAL skills
    this.buildCompetencyMatrix();

    // Load active staffing request if specified
    if (this.activeRequestId) {
      this.staffingRequestService.get(this.activeRequestId).subscribe({
        next: req => {
          this.activeRequest = req;
          this.computeAiMatch();
        }
      });
    } else {
      this.staffingRequestService.list().subscribe({
        next: reqs => {
          if (reqs.length > 0) {
            this.activeRequest = reqs[0];
            this.activeRequestId = reqs[0].id;
            this.computeAiMatch();
          }
        }
      });
    }
  }

  buildCompetencyMatrix(): void {
    if (!this.consultant) return;

    const skills = (this.consultant.skills || []).map(s => s.name);
    const items: CompetencyItem[] = [];

    const categoryMap: Array<{ label: string; keywords: string[] }> = [
      { label: 'Frontend Development', keywords: ['React', 'Angular', 'Vue', 'TypeScript', 'JavaScript', 'HTML', 'CSS', 'SCSS', 'Tailwind', 'GraphQL'] },
      { label: 'Backend Development', keywords: ['Java', 'Spring Boot', 'Node.js', 'Django', 'Flask', 'Python', 'PHP', 'Go', 'Golang', '.NET', 'REST API', 'Microservices', 'Kafka'] },
      { label: 'Cloud & DevOps', keywords: ['Docker', 'Kubernetes', 'AWS', 'Azure', 'Terraform', 'CI/CD', 'DevOps', 'Prometheus', 'EKS', 'Git'] },
      { label: 'Data & Databases', keywords: ['PostgreSQL', 'MySQL', 'SQL', 'MongoDB', 'Redis', 'Apache Spark', 'Airflow', 'PySpark', 'Prefect', 'Redshift'] },
    ];

    for (const category of categoryMap) {
      const matched = skills.filter(s =>
        category.keywords.some(kw => s.toLowerCase().includes(kw.toLowerCase()))
      );
      if (matched.length > 0) {
        const score = Math.min(4, matched.length + 1);
        const level = score === 4 ? 'Expert' : score === 3 ? 'Advanced' : score === 2 ? 'Proficient' : 'Familiar';
        items.push({ name: category.label, score, level });
      }
    }

    this.competencies = items;
  }

  computeAiMatch(): void {
    if (!this.consultant || !this.activeRequest) return;

    // Use the backend's actual matching result — same endpoint as the ranking page
    this.staffingRequestService.getMatches(this.activeRequest.id).subscribe({
      next: matches => {
        const match = matches.find(m => m.consultant.id === this.consultant!.id);
        if (match) {
          this.matchScore = Math.round(match.matchScore);
          this.matchedSkills = match.matchedSkills || [];
          this.missingSkills = match.missingSkills || [];

          // Bonus = consultant skills that are not required
          const consSkills = (this.consultant!.skills || []).map(s => s.name);
          this.bonusSkills = consSkills.filter(c =>
            !this.matchedSkills.some(m => m.toLowerCase() === c.toLowerCase())
          ).slice(0, 3);
        } else {
          this.matchScore = null;
        }
      },
      error: () => {
        this.matchScore = null;
      }
    });
  }

  setTab(tab: 'overview' | 'skills' | 'projects' | 'resume'): void {
    this.activeTab = tab;
  }

  delete(): void {
    if (this.consultant && confirm(`Delete ${this.consultant.name}?`)) {
      this.consultantService.delete(this.consultant.id).subscribe({
        next: () => this.router.navigateByUrl('/consultants'),
        error: () => this.error = 'Could not delete this consultant.'
      });
    }
  }

  download(): void {
    if (this.consultant) {
      window.open(this.consultantService.cvDownloadUrl(this.consultant), '_blank');
    }
  }

  openScheduleModal(): void {
    this.interviewDate = new Date(Date.now() + 86400000 * 2).toISOString().split('T')[0];
    this.interviewTime = '10:00';
    this.interviewLocation = 'Google Meet';
    this.interviewNotes = `Technical interview for ${this.consultant?.name} regarding ${this.activeRequest?.title || 'Staffing Opportunity'}`;
    this.showScheduleModal = true;
    this.scheduleSuccess = false;
  }

  closeScheduleModal(): void {
    this.showScheduleModal = false;
  }

  confirmSchedule(): void {
    if (!this.consultant) return;
    this.scheduling = true;

    const payload: InterviewRequest = {
      consultantId: this.consultant.id,
      staffingRequestId: this.activeRequestId || undefined,
      date: this.interviewDate,
      time: this.interviewTime,
      location: this.interviewLocation,
      status: 'SCHEDULED',
      notes: this.interviewNotes
    };

    this.interviewService.create(payload).subscribe({
      next: () => {
        this.scheduling = false;
        this.scheduleSuccess = true;
        setTimeout(() => {
          this.closeScheduleModal();
          this.router.navigate(['/interviews']);
        }, 1200);
      },
      error: () => {
        this.scheduling = false;
      }
    });
  }
}
