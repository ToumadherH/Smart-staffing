import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { DashboardStats, StaffingRequest } from '../../core/models';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, DecimalPipe],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  totalCount = 0;
  availableCount = 0;
  activeRequestsCount = 0;
  upcomingInterviewsCount = 18;
  recentRequests: StaffingRequest[] = [];
  loading = true;

  constructor(private readonly http: HttpClient) {}

  ngOnInit(): void {
    this.http.get<DashboardStats>('/api/dashboard/stats').subscribe({
      next: stats => {
        this.totalCount = stats.totalConsultants;
        this.availableCount = stats.availableConsultants;
        this.activeRequestsCount = stats.activeRequests;
        this.upcomingInterviewsCount = stats.upcomingInterviews || 18;
        this.recentRequests = stats.recentRequests || [];
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }
}
