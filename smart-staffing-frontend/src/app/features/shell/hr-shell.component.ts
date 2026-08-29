import { Component } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-hr-shell',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './hr-shell.component.html',
  styleUrl: './hr-shell.component.scss'
})
export class HrShellComponent {
  searchQuery = '';
  showNotifications = false;

  notifications = [
    { title: 'New AI Match: Elena Rodriguez', sub: '94% Match for Senior SRE (Req #402)', time: '10m ago' },
    { title: 'Interview Scheduled: Marcus Chen', sub: 'Tomorrow at 10:00 AM via Google Meet', time: '1h ago' },
    { title: 'Staffing Request Created: REQ-2049', sub: 'Senior Frontend Engineer at Acme Corp', time: '2h ago' }
  ];

  constructor(private readonly auth: AuthService, private readonly router: Router) {}

  onSearch(): void {
    if (this.searchQuery.trim()) {
      this.router.navigate(['/consultants'], { queryParams: { q: this.searchQuery.trim() } });
    }
  }

  toggleNotifications(): void {
    this.showNotifications = !this.showNotifications;
  }

  logout(): void {
    this.auth.logout();
    this.router.navigateByUrl('/login');
  }
}
