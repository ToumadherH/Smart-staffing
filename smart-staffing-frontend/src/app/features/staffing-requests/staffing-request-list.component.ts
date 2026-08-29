import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NgClass, DecimalPipe } from '@angular/common';
import { StaffingRequestService } from '../../core/staffing-request.service';
import { ConsultantMatch, StaffingRequest } from '../../core/models';

@Component({
  selector: 'app-staffing-request-list',
  imports: [RouterLink, FormsModule, NgClass, DecimalPipe],
  templateUrl: './staffing-request-list.component.html',
  styleUrl: './staffing-request-list.component.scss'
})
export class StaffingRequestListComponent implements OnInit {
  requests: StaffingRequest[] = [];
  filtered: StaffingRequest[] = [];
  loading = true;
  error = '';
  search = '';
  statusFilter = '';

  selectedRequest: StaffingRequest | null = null;
  selectedRequestMatches: ConsultantMatch[] = [];
  loadingMatches = false;
  showMatchModal = false;

  constructor(private readonly staffingRequestService: StaffingRequestService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.staffingRequestService.list().subscribe({
      next: data => {
        this.requests = data;
        this.filter();
        this.loading = false;
      },
      error: () => {
        this.error = 'Could not load staffing requests.';
        this.loading = false;
      }
    });
  }

  filter(): void {
    const term = this.search.toLowerCase();
    this.filtered = this.requests.filter(r => {
      const matchSearch = !term || `${r.title} ${r.clientName} ${r.location} ${r.requiredSkills.map(s => s.name).join(' ')}`.toLowerCase().includes(term);
      const matchStatus = !this.statusFilter || r.status === this.statusFilter;
      return matchSearch && matchStatus;
    });
  }

  badgeClass(status: string): string {
    if (status === 'OPEN') return 'badge-available';
    if (status === 'IN_PROGRESS') return 'badge-assigned';
    if (status === 'FULFILLED') return 'badge-fulfilled';
    return 'badge-on-leave';
  }

  openMatches(request: StaffingRequest): void {
    this.selectedRequest = request;
    this.showMatchModal = true;
    this.loadingMatches = true;
    this.selectedRequestMatches = [];

    this.staffingRequestService.getMatches(request.id).subscribe({
      next: matches => {
        this.selectedRequestMatches = matches;
        this.loadingMatches = false;
      },
      error: () => {
        this.loadingMatches = false;
      }
    });
  }

  closeMatchModal(): void {
    this.showMatchModal = false;
    this.selectedRequest = null;
    this.selectedRequestMatches = [];
  }
}
