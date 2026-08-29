import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InterviewService } from '../../core/interview.service';
import { ConsultantService } from '../../core/consultant.service';
import { StaffingRequestService } from '../../core/staffing-request.service';
import { Consultant, Interview, InterviewRequest, InterviewStatus, StaffingRequest } from '../../core/models';

interface CalendarDay {
  date: Date;
  dateStr: string;
  dayNumber: number;
  isCurrentMonth: boolean;
  isToday: boolean;
  interviews: Interview[];
}

@Component({
  selector: 'app-interviews',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './interviews.component.html',
  styleUrl: './interviews.component.scss'
})
export class InterviewsComponent implements OnInit {
  currentDate = new Date();
  viewMode: 'month' | 'week' = 'month';

  interviews: Interview[] = [];
  pendingConsultants: Consultant[] = [];
  allConsultants: Consultant[] = [];
  staffingRequests: StaffingRequest[] = [];

  calendarDays: CalendarDay[] = [];
  weekDaysHeader = ['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT'];

  loading = true;
  error: string | null = null;

  // Modal State
  showModal = false;
  isEditing = false;
  selectedInterviewId: number | null = null;

  formConsultantId: number | null = null;
  formStaffingRequestId: number | null = null;
  formDate = '';
  formTime = '10:00';
  formLocation = 'Google Meet';
  formStatus: InterviewStatus = 'SCHEDULED';
  formNotes = '';
  saving = false;
  modalError: string | null = null;

  constructor(
    private readonly interviewService: InterviewService,
    private readonly consultantService: ConsultantService,
    private readonly staffingRequestService: StaffingRequestService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.interviewService.list().subscribe({
      next: interviews => {
        this.interviews = interviews;
        this.generateCalendar();
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load interview schedules.';
        this.loading = false;
      }
    });

    this.interviewService.listPendingConsultants().subscribe({
      next: pending => this.pendingConsultants = pending,
      error: () => {}
    });

    this.consultantService.list().subscribe({
      next: consultants => this.allConsultants = consultants,
      error: () => {}
    });

    this.staffingRequestService.list().subscribe({
      next: requests => this.staffingRequests = requests,
      error: () => {}
    });
  }

  get monthYearLabel(): string {
    return this.currentDate.toLocaleString('default', { month: 'long', year: 'numeric' });
  }

  prevPeriod(): void {
    if (this.viewMode === 'month') {
      this.currentDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() - 1, 1);
    } else {
      this.currentDate = new Date(this.currentDate.getTime() - 7 * 24 * 60 * 60 * 1000);
    }
    this.generateCalendar();
  }

  nextPeriod(): void {
    if (this.viewMode === 'month') {
      this.currentDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() + 1, 1);
    } else {
      this.currentDate = new Date(this.currentDate.getTime() + 7 * 24 * 60 * 60 * 1000);
    }
    this.generateCalendar();
  }

  setViewMode(mode: 'month' | 'week'): void {
    this.viewMode = mode;
    this.generateCalendar();
  }

  generateCalendar(): void {
    const days: CalendarDay[] = [];
    const today = new Date();
    const todayStr = this.formatDateISO(today);

    if (this.viewMode === 'month') {
      const year = this.currentDate.getFullYear();
      const month = this.currentDate.getMonth();

      const firstDayOfMonth = new Date(year, month, 1);
      const startingDayOfWeek = firstDayOfMonth.getDay(); // 0 = Sun
      const daysInMonth = new Date(year, month + 1, 0).getDate();

      // Leading days from previous month
      const prevMonthLastDate = new Date(year, month, 0).getDate();
      for (let i = startingDayOfWeek - 1; i >= 0; i--) {
        const d = new Date(year, month - 1, prevMonthLastDate - i);
        const dStr = this.formatDateISO(d);
        days.push({
          date: d,
          dateStr: dStr,
          dayNumber: d.getDate(),
          isCurrentMonth: false,
          isToday: dStr === todayStr,
          interviews: this.getInterviewsForDate(dStr)
        });
      }

      // Current month days
      for (let day = 1; day <= daysInMonth; day++) {
        const d = new Date(year, month, day);
        const dStr = this.formatDateISO(d);
        days.push({
          date: d,
          dateStr: dStr,
          dayNumber: day,
          isCurrentMonth: true,
          isToday: dStr === todayStr,
          interviews: this.getInterviewsForDate(dStr)
        });
      }

      // Trailing days to fill 5 or 6 weeks (multiples of 7)
      const remaining = 35 - days.length > 0 ? 35 - days.length : 42 - days.length;
      for (let day = 1; day <= remaining; day++) {
        const d = new Date(year, month + 1, day);
        const dStr = this.formatDateISO(d);
        days.push({
          date: d,
          dateStr: dStr,
          dayNumber: day,
          isCurrentMonth: false,
          isToday: dStr === todayStr,
          interviews: this.getInterviewsForDate(dStr)
        });
      }
    } else {
      // Week mode: current week starting Sunday
      const dayOfWeek = this.currentDate.getDay();
      const startOfWeek = new Date(this.currentDate);
      startOfWeek.setDate(this.currentDate.getDate() - dayOfWeek);

      for (let i = 0; i < 7; i++) {
        const d = new Date(startOfWeek);
        d.setDate(startOfWeek.getDate() + i);
        const dStr = this.formatDateISO(d);
        days.push({
          date: d,
          dateStr: dStr,
          dayNumber: d.getDate(),
          isCurrentMonth: d.getMonth() === this.currentDate.getMonth(),
          isToday: dStr === todayStr,
          interviews: this.getInterviewsForDate(dStr)
        });
      }
    }

    this.calendarDays = days;
  }

  getInterviewsForDate(dateStr: string): Interview[] {
    return this.interviews.filter(i => i.date === dateStr);
  }

  formatDateISO(d: Date): string {
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  getRoleBadge(consultant: Consultant): string {
    if (consultant.skills && consultant.skills.length > 0) {
      const firstSkill = consultant.skills[0].name.toUpperCase();
      if (firstSkill.includes('JAVA') || firstSkill.includes('SPRING') || firstSkill.includes('DEV')) return 'SR. DEV';
      if (firstSkill.includes('DESIGN') || firstSkill.includes('FIGMA') || firstSkill.includes('CSS')) return 'DESIGNER';
      if (firstSkill.includes('DATA') || firstSkill.includes('PYTHON')) return 'DATA ENG';
      if (firstSkill.includes('CLOUD') || firstSkill.includes('AWS') || firstSkill.includes('KUBERNETES')) return 'CLOUD ARCH';
      return firstSkill.substring(0, 8);
    }
    return 'CANDIDATE';
  }

  openScheduleModal(day?: CalendarDay, consultant?: Consultant): void {
    this.isEditing = false;
    this.selectedInterviewId = null;
    this.modalError = null;

    this.formDate = day ? day.dateStr : this.formatDateISO(new Date());
    this.formTime = '10:00';
    this.formLocation = 'Google Meet';
    this.formStatus = 'SCHEDULED';
    this.formNotes = '';

    if (consultant) {
      this.formConsultantId = consultant.id;
    } else if (this.allConsultants.length > 0) {
      this.formConsultantId = this.allConsultants[0].id;
    } else {
      this.formConsultantId = null;
    }

    this.formStaffingRequestId = this.staffingRequests.length > 0 ? this.staffingRequests[0].id : null;
    this.showModal = true;
  }

  openEditModal(interview: Interview, event: Event): void {
    event.stopPropagation();
    this.isEditing = true;
    this.selectedInterviewId = interview.id;
    this.modalError = null;

    this.formConsultantId = interview.consultantId;
    this.formStaffingRequestId = interview.staffingRequestId || null;
    this.formDate = interview.date;
    this.formTime = interview.time;
    this.formLocation = interview.location || 'Google Meet';
    this.formStatus = interview.status;
    this.formNotes = interview.notes || '';

    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.modalError = null;
  }

  saveInterview(): void {
    if (!this.formConsultantId || !this.formDate || !this.formTime) {
      this.modalError = 'Consultant, date, and time are required.';
      return;
    }

    this.saving = true;
    this.modalError = null;

    const payload: InterviewRequest = {
      consultantId: this.formConsultantId,
      staffingRequestId: this.formStaffingRequestId || undefined,
      date: this.formDate,
      time: this.formTime,
      location: this.formLocation,
      status: this.formStatus,
      notes: this.formNotes
    };

    if (this.isEditing && this.selectedInterviewId) {
      this.interviewService.update(this.selectedInterviewId, payload).subscribe({
        next: () => {
          this.saving = false;
          this.closeModal();
          this.loadData();
        },
        error: () => {
          this.modalError = 'Failed to update interview.';
          this.saving = false;
        }
      });
    } else {
      this.interviewService.create(payload).subscribe({
        next: () => {
          this.saving = false;
          this.closeModal();
          this.loadData();
        },
        error: () => {
          this.modalError = 'Failed to schedule interview.';
          this.saving = false;
        }
      });
    }
  }

  deleteInterview(): void {
    if (!this.selectedInterviewId) return;
    if (confirm('Are you sure you want to cancel and delete this interview?')) {
      this.interviewService.delete(this.selectedInterviewId).subscribe({
        next: () => {
          this.closeModal();
          this.loadData();
        },
        error: () => {
          this.modalError = 'Failed to delete interview.';
        }
      });
    }
  }

  getChipClass(interview: Interview): string {
    if (interview.status === 'COMPLETED') return 'chip-completed';
    if (interview.status === 'CANCELLED') return 'chip-cancelled';
    const idMod = (interview.id || 0) % 3;
    if (idMod === 0) return 'chip-blue';
    if (idMod === 1) return 'chip-green';
    return 'chip-purple';
  }
}
