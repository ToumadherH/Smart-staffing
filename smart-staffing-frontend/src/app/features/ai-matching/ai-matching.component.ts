import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { StaffingRequestService } from '../../core/staffing-request.service';
import { ConsultantMatch, StaffingRequest } from '../../core/models';

@Component({
  selector: 'app-ai-matching',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './ai-matching.component.html',
  styleUrl: './ai-matching.component.scss'
})
export class AiMatchingComponent implements OnInit {
  requestId: number | null = null;
  selectedRequest: StaffingRequest | null = null;
  allRequests: StaffingRequest[] = [];
  matches: ConsultantMatch[] = [];
  filteredMatches: ConsultantMatch[] = [];

  loading = true;
  error: string | null = null;

  // Filters & sorting
  skillMatchFilter = true;
  availabilityFilter = false;
  sortBy: 'score' | 'exp' | 'name' = 'score';
  sortAsc = false;
  searchQuery = '';

  // Pagination
  pageSize = 5;
  currentPage = 1;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly staffingRequestService: StaffingRequestService
  ) {}

  ngOnInit(): void {
    this.staffingRequestService.list().subscribe({
      next: requests => {
        this.allRequests = requests;
        this.route.paramMap.subscribe(params => {
          const idParam = params.get('id');
          if (idParam) {
            this.requestId = +idParam;
            this.selectedRequest = this.allRequests.find(r => r.id === this.requestId) || null;
            this.loadMatches(this.requestId);
          } else if (this.allRequests.length > 0) {
            this.requestId = this.allRequests[0].id;
            this.selectedRequest = this.allRequests[0];
            this.loadMatches(this.requestId);
          } else {
            this.loading = false;
          }
        });
      },
      error: () => {
        this.error = 'Failed to load staffing requests.';
        this.loading = false;
      }
    });
  }

  onRequestChange(requestId: number): void {
    this.requestId = requestId;
    this.selectedRequest = this.allRequests.find(r => r.id === requestId) || null;
    this.router.navigate(['/staffing-requests', requestId, 'matches']);
    this.loadMatches(requestId);
  }

  loadMatches(requestId: number): void {
    this.loading = true;
    this.error = null;
    this.staffingRequestService.getMatches(requestId).subscribe({
      next: matches => {
        this.matches = matches;
        this.applyFilters();
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to compute AI matches for this staffing request.';
        this.loading = false;
      }
    });
  }

  applyFilters(): void {
    let result = [...this.matches];

    if (this.searchQuery.trim()) {
      const q = this.searchQuery.toLowerCase();
      result = result.filter(m =>
        m.consultant.name.toLowerCase().includes(q) ||
        m.consultant.email.toLowerCase().includes(q) ||
        (m.consultant.skills && m.consultant.skills.some(s => s.name.toLowerCase().includes(q)))
      );
    }

    if (this.availabilityFilter) {
      result = result.filter(m => m.consultant.availability === 'AVAILABLE');
    }

    // Sort
    result.sort((a, b) => {
      if (this.sortBy === 'score') {
        return this.sortAsc ? a.matchScore - b.matchScore : b.matchScore - a.matchScore;
      } else if (this.sortBy === 'exp') {
        return this.sortAsc
          ? a.consultant.yearsOfExperience - b.consultant.yearsOfExperience
          : b.consultant.yearsOfExperience - a.consultant.yearsOfExperience;
      } else {
        return this.sortAsc
          ? a.consultant.name.localeCompare(b.consultant.name)
          : b.consultant.name.localeCompare(a.consultant.name);
      }
    });

    this.filteredMatches = result;
    this.currentPage = 1;
  }

  toggleSort(type: 'score' | 'exp' | 'name'): void {
    if (this.sortBy === type) {
      this.sortAsc = !this.sortAsc;
    } else {
      this.sortBy = type;
      this.sortAsc = false;
    }
    this.applyFilters();
  }

  toggleAvailabilityFilter(): void {
    this.availabilityFilter = !this.availabilityFilter;
    this.applyFilters();
  }

  toggleSkillMatchFilter(): void {
    this.skillMatchFilter = !this.skillMatchFilter;
    this.applyFilters();
  }

  get paginatedMatches(): ConsultantMatch[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredMatches.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredMatches.length / this.pageSize) || 1;
  }

  get pagesArray(): number[] {
    const pages = [];
    for (let i = 1; i <= this.totalPages; i++) {
      pages.push(i);
    }
    return pages;
  }

  setPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
    }
  }

  getRoleTitle(match: ConsultantMatch): string {
    const skills = match.consultant.skills || [];
    if (skills.some(s => s.name.toLowerCase().includes('react') || s.name.toLowerCase().includes('frontend'))) {
      return match.consultant.yearsOfExperience >= 7 ? 'Lead Frontend Developer' : 'Senior React Engineer';
    }
    if (skills.some(s => s.name.toLowerCase().includes('cloud') || s.name.toLowerCase().includes('kubernetes') || s.name.toLowerCase().includes('terraform'))) {
      return 'Senior Cloud Systems Architect';
    }
    if (skills.some(s => s.name.toLowerCase().includes('spark') || s.name.toLowerCase().includes('python') || s.name.toLowerCase().includes('airflow'))) {
      return 'Senior Data Engineer';
    }
    if (skills.some(s => s.name.toLowerCase().includes('java') || s.name.toLowerCase().includes('spring'))) {
      return 'Senior Java / Spring Architect';
    }
    if (skills.some(s => s.name.toLowerCase().includes('design') || s.name.toLowerCase().includes('figma'))) {
      return 'Frontend & Design System Specialist';
    }
    return match.consultant.yearsOfExperience >= 5 ? 'Senior Software Engineer' : 'Software Developer';
  }

  getAvailabilityLabel(availability: string): string {
    if (availability === 'AVAILABLE') return 'Immediate';
    if (availability === 'ASSIGNED') return 'In 2 Weeks';
    return 'Unavailable';
  }

  getAvailabilityClass(availability: string): string {
    if (availability === 'AVAILABLE') return 'avail-immediate';
    if (availability === 'ASSIGNED') return 'avail-soon';
    return 'avail-unavailable';
  }

  getScoreBarClass(score: number): string {
    if (score >= 90) return 'bar-green';
    if (score >= 80) return 'bar-teal';
    if (score >= 60) return 'bar-blue';
    return 'bar-amber';
  }
}
