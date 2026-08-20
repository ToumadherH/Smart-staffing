import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, map, tap, throwError } from 'rxjs';

const API_URL = 'http://localhost:8080/api';
const AUTH_KEY = 'smart-staffing-basic-auth';

@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(private readonly http: HttpClient) {}

  login(email: string, password: string): Observable<void> {
    const authorization = `Basic ${btoa(`${email}:${password}`)}`;
    return this.http.get<unknown[]>(`${API_URL}/consultants`, { headers: { Authorization: authorization } }).pipe(
      tap(() => sessionStorage.setItem(AUTH_KEY, authorization)),
      map(() => undefined),
      catchError(() => throwError(() => new Error('Invalid email or password.')))
    );
  }

  logout(): void {
    sessionStorage.removeItem(AUTH_KEY);
  }

  get authorization(): string | null {
    return sessionStorage.getItem(AUTH_KEY);
  }

  get isAuthenticated(): boolean {
    return !!this.authorization;
  }
}
