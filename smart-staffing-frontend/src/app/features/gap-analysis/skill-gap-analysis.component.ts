import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ConsultantService } from '../../core/consultant.service';
import { StaffingRequestService } from '../../core/staffing-request.service';
import { InterviewService } from '../../core/interview.service';
import { Consultant, ConsultantMatch, InterviewRequest, Skill, StaffingRequest } from '../../core/models';

interface MappingItem {
  reqName: string;
  reqCategory: string;
  matchType: 'EXACT' | 'PARTIAL' | 'MISSING';
  profileSkillName?: string;
  profileProficiency?: string;
  missingNote?: string;
}

interface UpskillingCourse {
  title: string;
  description: string;
  priority: 'High Priority' | 'Medium Priority';
  duration: string;
  platform: 'Udemy' | 'Coursera' | 'Pluralsight';
  assigned: boolean;
}

@Component({
  selector: 'app-skill-gap-analysis',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './skill-gap-analysis.component.html',
  styleUrl: './skill-gap-analysis.component.scss'
})
export class SkillGapAnalysisComponent implements OnInit {
  requestId: number | null = null;
  consultantId: number | null = null;

  request: StaffingRequest | null = null;
  consultant: Consultant | null = null;
  matchResult: ConsultantMatch | null = null;

  score = 0;
  matchGrade = '';
  matchSummary = '';

  requiredCount = 0;
  matchedCount = 0;
  missingCount = 0;

  mappings: MappingItem[] = [];
  upskillingCourses: UpskillingCourse[] = [];

  loading = true;
  error: string | null = null;

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
    this.route.paramMap.subscribe(params => {
      const reqId = params.get('requestId');
      const consId = params.get('consultantId');

      if (reqId) this.requestId = +reqId;
      if (consId) this.consultantId = +consId;

      this.route.queryParamMap.subscribe(queryParams => {
        if (!this.requestId && queryParams.get('requestId')) {
          this.requestId = +queryParams.get('requestId')!;
        }
        if (!this.consultantId && queryParams.get('consultantId')) {
          this.consultantId = +queryParams.get('consultantId')!;
        }
        this.loadData();
      });
    });
  }

  loadData(): void {
    this.loading = true;
    this.error = null;

    if (!this.requestId || !this.consultantId) {
      this.staffingRequestService.list().subscribe({
        next: requests => {
          if (requests.length > 0) {
            this.request = this.requestId ? (requests.find(r => r.id === this.requestId) || requests[0]) : requests[0];
            this.requestId = this.request.id;
          }
          this.consultantService.list().subscribe({
            next: consultants => {
              if (consultants.length > 0) {
                this.consultant = this.consultantId ? (consultants.find(c => c.id === this.consultantId) || consultants[0]) : consultants[0];
                this.consultantId = this.consultant.id;
              }
              this.fetchMatchAndAnalyze();
            },
            error: () => {
              this.error = 'Failed to load consultant data.';
              this.loading = false;
            }
          });
        },
        error: () => {
          this.error = 'Failed to load staffing request.';
          this.loading = false;
        }
      });
      return;
    }

    forkJoin({
      request: this.staffingRequestService.get(this.requestId),
      consultant: this.consultantService.get(this.consultantId)
    }).subscribe({
      next: ({ request, consultant }) => {
        this.request = request;
        this.consultant = consultant;
        this.fetchMatchAndAnalyze();
      },
      error: () => {
        this.error = 'Failed to load staffing request or consultant.';
        this.loading = false;
      }
    });
  }

  private fetchMatchAndAnalyze(): void {
    if (!this.requestId || !this.consultantId || !this.request || !this.consultant) {
      this.loading = false;
      return;
    }

    this.staffingRequestService.getMatches(this.requestId).subscribe({
      next: matches => {
        this.matchResult = matches.find(m => m.consultant.id === this.consultantId) || null;
        this.computeAnalysis();
        this.loading = false;
      },
      error: () => {
        // Fallback to local computation if matching endpoint errors
        this.computeAnalysis();
        this.loading = false;
      }
    });
  }

  computeAnalysis(): void {
    if (!this.request || !this.consultant) return;

    const reqSkills: Skill[] = this.request.requiredSkills || [];
    const consultantSkills: Skill[] = this.consultant.skills || [];
    const matchedSkillNames = this.matchResult ? (this.matchResult.matchedSkills || []) : [];
    const missingSkillNames = this.matchResult ? (this.matchResult.missingSkills || []) : [];

    const items: MappingItem[] = [];
    let exactCount = 0;
    let partialCount = 0;
    let missingCount = 0;

    for (const reqSkill of reqSkills) {
      const nameLower = reqSkill.name.toLowerCase();
      
      // Determine if skill is matched according to AI matching result or direct skill comparison
      const isMatchedInResult = matchedSkillNames.some(s => s.toLowerCase() === nameLower);
      const isMissingInResult = missingSkillNames.some(s => s.toLowerCase() === nameLower);
      
      const exactSkill = consultantSkills.find(s => s.name.toLowerCase() === nameLower);
      const relatedSkill = this.findRelatedSkill(nameLower, consultantSkills);

      if (isMatchedInResult || (!isMissingInResult && (exactSkill || relatedSkill))) {
        if (exactSkill) {
          exactCount++;
          items.push({
            reqName: reqSkill.name,
            reqCategory: reqSkill.category || 'Required Skill',
            matchType: 'EXACT',
            profileSkillName: exactSkill.name,
            profileProficiency: `${this.consultant.yearsOfExperience >= 5 ? 'Advanced' : 'Proficient'} • ${this.consultant.yearsOfExperience > 0 ? this.consultant.yearsOfExperience + ' yrs exp' : 'Demonstrated in profile'}`
          });
        } else if (relatedSkill) {
          partialCount++;
          items.push({
            reqName: reqSkill.name,
            reqCategory: reqSkill.category || 'Required Skill',
            matchType: 'PARTIAL',
            profileSkillName: relatedSkill.name,
            profileProficiency: `Semantic match via ${relatedSkill.name}`
          });
        } else {
          exactCount++;
          items.push({
            reqName: reqSkill.name,
            reqCategory: reqSkill.category || 'Required Skill',
            matchType: 'EXACT',
            profileSkillName: reqSkill.name,
            profileProficiency: `Demonstrated in profile`
          });
        }
      } else {
        missingCount++;
        items.push({
          reqName: reqSkill.name,
          reqCategory: reqSkill.category || 'Required Skill',
          matchType: 'MISSING',
          missingNote: `Missing: ${reqSkill.name} Experience`
        });
      }
    }

    this.mappings = items;
    this.requiredCount = reqSkills.length;
    this.matchedCount = this.matchResult ? matchedSkillNames.length : (exactCount + partialCount);
    this.missingCount = this.matchResult ? missingSkillNames.length : missingCount;

    if (this.matchResult) {
      this.score = Math.round(this.matchResult.matchScore);
    } else {
      this.score = reqSkills.length > 0 ? Math.round(((exactCount * 1.0 + partialCount * 0.6) / reqSkills.length) * 100) : 100;
    }

    if (this.score >= 80) {
      this.matchGrade = 'Strong Match';
      this.matchSummary = `${this.consultant.name} meets or exceeds core requirements for this position.`;
    } else if (this.score >= 60) {
      this.matchGrade = 'Good Match';
      this.matchSummary = `${this.consultant.name} possesses core foundational skills with a few upskilling recommendations.`;
    } else {
      this.matchGrade = 'Partial Match';
      this.matchSummary = `${this.consultant.name} matches several requirements but has notable gaps for this specific position.`;
    }

    // Upskilling courses strictly generated for actual missing skills
    const actualMissing = this.matchResult ? this.matchResult.missingSkills : items.filter(m => m.matchType === 'MISSING').map(m => m.reqName);
    
    if (actualMissing && actualMissing.length > 0) {
      this.upskillingCourses = actualMissing.map((missingSkill, i) => ({
        title: `${missingSkill} Deep Dive & Best Practices`,
        description: `Comprehensive training covering ${missingSkill} architecture, core principles, and enterprise implementation.`,
        priority: (i === 0 ? 'High Priority' : 'Medium Priority') as 'High Priority' | 'Medium Priority',
        duration: `${8 + (i * 4)}h`,
        platform: (i % 2 === 0 ? 'Udemy' : 'Coursera') as 'Udemy' | 'Coursera',
        assigned: false
      }));
    } else {
      this.upskillingCourses = [];
    }
  }

  private findRelatedSkill(reqNameLower: string, consultantSkills: Skill[]): Skill | null {
    if (reqNameLower.includes('container') || reqNameLower.includes('docker')) {
      const found = consultantSkills.find(s => {
        const n = s.name.toLowerCase();
        return n.includes('docker') || n.includes('kubernetes') || n.includes('devops');
      });
      if (found) return found;
    }
    if (reqNameLower.includes('airflow')) {
      const found = consultantSkills.find(s => s.name.toLowerCase().includes('prefect'));
      if (found) return found;
    }
    if (reqNameLower.includes('react')) {
      const found = consultantSkills.find(s => s.name.toLowerCase().includes('vue') || s.name.toLowerCase().includes('angular') || s.name.toLowerCase().includes('javascript'));
      if (found) return found;
    }
    if (reqNameLower.includes('kubernetes')) {
      const found = consultantSkills.find(s => s.name.toLowerCase().includes('docker'));
      if (found) return found;
    }
    return null;
  }

  assignCourse(course: UpskillingCourse): void {
    course.assigned = !course.assigned;
  }

  exportReport(): void {
    window.print();
  }

  openScheduleModal(): void {
    this.interviewDate = new Date(Date.now() + 86400000 * 2).toISOString().split('T')[0];
    this.interviewTime = '10:00';
    this.interviewLocation = 'Google Meet';
    this.interviewNotes = `Skill gap interview for ${this.consultant?.name} regarding ${this.request?.title || 'Position'}`;
    this.showScheduleModal = true;
    this.scheduleSuccess = false;
  }

  closeScheduleModal(): void {
    this.showScheduleModal = false;
  }

  confirmSchedule(): void {
    if (!this.consultantId) return;
    this.scheduling = true;

    const payload: InterviewRequest = {
      consultantId: this.consultantId,
      staffingRequestId: this.requestId || undefined,
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
