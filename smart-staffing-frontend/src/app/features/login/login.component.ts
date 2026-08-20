import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  error = '';
  loading = false;
  showPassword = false;
  readonly form;

  constructor(formBuilder: FormBuilder, private readonly auth: AuthService, private readonly router: Router) {
    this.form = formBuilder.nonNullable.group({
      email: ['hr@dpc.com', [Validators.required, Validators.email]],
      password: ['ChangeMe123!', Validators.required],
      remember: [true]
    });
  }

  toggleShowPassword(): void {
    this.showPassword = !this.showPassword;
  }

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading = true;
    this.error = '';
    const { email, password } = this.form.getRawValue();
    this.auth.login(email, password).subscribe({
      next: () => this.router.navigateByUrl('/dashboard'),
      error: error => { this.error = error.message; this.loading = false; }
    });
  }
}
