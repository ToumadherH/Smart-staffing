import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Consultant, Interview, InterviewRequest, InterviewStatus } from './models';

@Injectable({
  providedIn: 'root'
})
export class InterviewService {
  private readonly baseUrl = '/api/interviews';

  constructor(private readonly http: HttpClient) {}

  list(): Observable<Interview[]> {
    return this.http.get<Interview[]>(this.baseUrl);
  }

  listPendingConsultants(): Observable<Consultant[]> {
    return this.http.get<Consultant[]>(`${this.baseUrl}/pending-consultants`);
  }

  create(interview: InterviewRequest): Observable<Interview> {
    return this.http.post<Interview>(this.baseUrl, interview);
  }

  update(id: number, interview: InterviewRequest): Observable<Interview> {
    return this.http.put<Interview>(`${this.baseUrl}/${id}`, interview);
  }

  updateStatus(id: number, status: InterviewStatus): Observable<Interview> {
    return this.http.patch<Interview>(`${this.baseUrl}/${id}/status`, null, {
      params: { status }
    });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
