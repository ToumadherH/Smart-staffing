import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Consultant, ConsultantPayload, Cv } from './models';

const API_URL = '/api/consultants';

@Injectable({ providedIn: 'root' })
export class ConsultantService {
  constructor(private readonly http: HttpClient) {}

  list(): Observable<Consultant[]> { return this.http.get<Consultant[]>(API_URL); }
  get(id: number): Observable<Consultant> { return this.http.get<Consultant>(`${API_URL}/${id}`); }
  create(payload: ConsultantPayload): Observable<Consultant> { return this.http.post<Consultant>(API_URL, payload); }
  update(id: number, payload: ConsultantPayload): Observable<Consultant> { return this.http.put<Consultant>(`${API_URL}/${id}`, payload); }
  delete(id: number): Observable<void> { return this.http.delete<void>(`${API_URL}/${id}`); }
  uploadCv(id: number, file: File): Observable<Cv> {
    const body = new FormData();
    body.append('file', file);
    return this.http.post<Cv>(`${API_URL}/${id}/cv`, body);
  }

  cvDownloadUrl(consultant: Consultant): string {
    return `${API_URL}/${consultant.id}/cv/download`;
  }
}
