import { Component } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({ selector: 'app-hr-shell', imports: [RouterOutlet, RouterLink, RouterLinkActive], templateUrl: './hr-shell.component.html', styleUrl: './hr-shell.component.scss' })
export class HrShellComponent {
  constructor(private readonly auth: AuthService, private readonly router: Router) {}
  logout(): void { this.auth.logout(); this.router.navigateByUrl('/login'); }
}
