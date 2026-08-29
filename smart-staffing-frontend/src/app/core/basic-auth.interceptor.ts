import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';

export const basicAuthInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const authorization = auth.authorization;
  const isApiRequest = request.url.includes('/api/') || request.url.startsWith('/api');
  const authenticatedRequest = authorization && isApiRequest
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
