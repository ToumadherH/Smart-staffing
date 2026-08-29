import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './settings.component.html',
  styleUrl: './settings.component.scss'
})
export class SettingsComponent implements OnInit {
  orgName = 'Digital Power Consulting (DPC)';
  hrEmail = 'hr@dpc.com';
  notificationsEnabled = true;
  interviewReminders = true;

  // AI Matching settings
  aiModel = 'gpt-4o-mini';
  skillWeight = 60;
  experienceWeight = 20;
  availabilityWeight = 20;
  semanticFallback = true;

  savedMessage = false;

  constructor(public readonly authService: AuthService) {}

  ngOnInit(): void {
    if (this.authService.email) {
      this.hrEmail = this.authService.email;
    }
  }

  saveSettings(): void {
    this.savedMessage = true;
    setTimeout(() => this.savedMessage = false, 3000);
  }
}
