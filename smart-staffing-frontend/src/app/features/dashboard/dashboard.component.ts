import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { ConsultantService } from '../../core/consultant.service';
import { Consultant } from '../../core/models';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, DecimalPipe],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  consultants: Consultant[] = [];
  totalCount = 1248;
  availableCount = 312;
  activeRequestsCount = 45;
  upcomingInterviewsCount = 18;

  constructor(private readonly consultantService: ConsultantService) {}

  ngOnInit(): void {
    this.consultantService.list().subscribe({
      next: data => {
        this.consultants = data;
        if (data.length > 0) {
          const avail = data.filter(c => c.availability === 'AVAILABLE').length;
          if (avail > 0) {
            this.availableCount = avail;
          }
        }
      },
      error: () => {
        // Fallback to default metrics if needed
      }
    });
  }
}
