export type Availability = 'AVAILABLE' | 'ASSIGNED' | 'ON_LEAVE';
export type StaffingRequestStatus = 'OPEN' | 'IN_PROGRESS' | 'FULFILLED' | 'CLOSED';

export interface Skill {
  id?: number;
  name: string;
  category: string;
}

export interface Cv {
  id: number;
  fileName: string;
  contentType: string;
  uploadedAt: string;
  downloadUrl: string;
  extractedText?: string;
  extractedEmail?: string;
  extractedPhone?: string;
  extractedSkillsText?: string;
}

export interface Consultant {
  id: number;
  name: string;
  email: string;
  phone?: string;
  yearsOfExperience: number;
  availability: Availability;
  currentMission?: string;
  location?: string;
  languages: string[];
  skills: Skill[];
  cv?: Cv | null;
}

export type ConsultantPayload = Omit<Consultant, 'id' | 'cv'>;

export interface StaffingRequest {
  id: number;
  title: string;
  clientName: string;
  location?: string;
  yearsOfExperienceRequired?: number;
  description?: string;
  status: StaffingRequestStatus;
  createdAt: string;
  requiredSkills: Skill[];
}

export interface StaffingRequestRequest {
  title: string;
  clientName: string;
  location?: string;
  yearsOfExperienceRequired?: number;
  description?: string;
  status: StaffingRequestStatus;
  requiredSkills: Skill[];
}

export interface DashboardStats {
  totalConsultants: number;
  availableConsultants: number;
  activeRequests: number;
  upcomingInterviews: number;
  recentRequests: StaffingRequest[];
}

export interface ConsultantMatch {
  consultant: Consultant;
  matchScore: number;
  matchedSkills: string[];
  missingSkills: string[];
  matchReason: string;
}

export type InterviewStatus = 'SCHEDULED' | 'COMPLETED' | 'CANCELLED';

export interface Interview {
  id: number;
  date: string;
  time: string;
  location?: string;
  status: InterviewStatus;
  notes?: string;
  consultantId: number;
  consultantName: string;
  staffingRequestId?: number;
  staffingRequestTitle?: string;
}

export interface InterviewRequest {
  date: string;
  time: string;
  location?: string;
  status?: InterviewStatus;
  notes?: string;
  consultantId: number;
  staffingRequestId?: number;
}

