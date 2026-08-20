import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { ConsultantService } from '../../core/consultant.service';
import { Consultant } from '../../core/models';

@Component({ selector: 'app-consultant-details', imports: [RouterLink, DatePipe], templateUrl: './consultant-details.component.html', styleUrl: './consultant-details.component.scss' })
export class ConsultantDetailsComponent implements OnInit {
  consultant?: Consultant; error = '';
  constructor(private readonly route: ActivatedRoute, private readonly router: Router, private readonly api: ConsultantService) {}
  ngOnInit(): void { this.api.get(Number(this.route.snapshot.paramMap.get('id'))).subscribe({ next: c => this.consultant = c, error: () => this.error = 'Consultant could not be found.' }); }
  delete(): void { if (this.consultant && confirm(`Delete ${this.consultant.name}?`)) this.api.delete(this.consultant.id).subscribe({ next: () => this.router.navigateByUrl('/consultants'), error: () => this.error = 'Could not delete this consultant.' }); }
  download(): void { if (this.consultant) window.open(this.api.cvDownloadUrl(this.consultant), '_blank'); }
}
