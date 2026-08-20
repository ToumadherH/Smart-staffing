import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NgClass } from '@angular/common';
import { ConsultantService } from '../../core/consultant.service';
import { Consultant } from '../../core/models';

@Component({
  selector: 'app-consultant-list',
  imports: [RouterLink, FormsModule, NgClass],
  templateUrl: './consultant-list.component.html',
  styleUrl: './consultant-list.component.scss'
})
export class ConsultantListComponent implements OnInit {
  consultants: Consultant[] = [];
  filtered: Consultant[] = [];
  loading = true;
  error = '';
  search = '';
  skillFilter = '';
  availabilityFilter = '';
  locationFilter = '';

  constructor(private readonly consultantsApi: ConsultantService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.consultantsApi.list().subscribe({
      next: data => {
        this.consultants = data;
        this.filter();
        this.loading = false;
      },
      error: () => {
        this.error = 'Could not load consultants.';
        this.loading = false;
      }
    });
  }

  filter(): void {
    const term = this.search.toLowerCase();
    const loc = this.locationFilter.toLowerCase();

    this.filtered = this.consultants.filter(c => {
      const matchSearch = !term || `${c.name} ${c.email} ${c.location} ${c.currentMission} ${c.skills.map(s => s.name).join(' ')}`.toLowerCase().includes(term);
      const matchLocation = !loc || (c.location && c.location.toLowerCase().includes(loc));
      const matchAvailability = !this.availabilityFilter || c.availability === this.availabilityFilter;

      return matchSearch && matchLocation && matchAvailability;
    });
  }

  availabilityLabel(value: string): string {
    if (value === 'AVAILABLE') return 'Available';
    if (value === 'ASSIGNED') return 'On Mission';
    if (value === 'ON_LEAVE') return 'Training / Leave';
    return value;
  }

  badgeClass(value: string): string {
    if (value === 'AVAILABLE') return 'badge-available';
    if (value === 'ASSIGNED') return 'badge-assigned';
    return 'badge-on-leave';
  }

  getScore(id: number): number {
    return 70 + ((id * 7) % 28);
  }
}
