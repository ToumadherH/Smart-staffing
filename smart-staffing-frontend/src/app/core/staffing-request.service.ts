import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ConsultantMatch, StaffingRequest, StaffingRequestRequest } from './models';

@Injectable({
  providedIn: 'root'
})
export class StaffingRequestService {
  private readonly baseUrl = '/api/staffing-requests';

  constructor(private readonly http: HttpClient) {}

  list(): Observable<StaffingRequest[]> {
    return this.http.get<StaffingRequest[]>(this.baseUrl);
  }

  get(id: number): Observable<StaffingRequest> {
    return this.http.get<StaffingRequest>(`${this.baseUrl}/${id}`);
  }

  getMatches(id: number): Observable<ConsultantMatch[]> {
    return this.http.get<ConsultantMatch[]>(`${this.baseUrl}/${id}/matches`);
  }

  create(request: StaffingRequestRequest): Observable<StaffingRequest> {
    return this.http.post<StaffingRequest>(this.baseUrl, request);
  }

  update(id: number, request: StaffingRequestRequest): Observable<StaffingRequest> {
    return this.http.put<StaffingRequest>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
