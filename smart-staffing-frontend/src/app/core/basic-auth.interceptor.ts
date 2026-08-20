import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';

export const basicAuthInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const authorization = auth.authorization;
  const authenticatedRequest = authorization && request.url.startsWith('http://localhost:8080/api')
    ? request.clone({ setHeaders: { Authorization: authorization } })
    : request;

  return next(authenticatedRequest).pipe(catchError(error => {
    if (error.status === 401 && !request.headers.has('Authorization')) {
      auth.logout();
      router.navigateByUrl('/login');
    }
    return throwError(() => error);
  }));
};
