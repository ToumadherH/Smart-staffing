export type Availability = 'AVAILABLE' | 'ASSIGNED' | 'ON_LEAVE';

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
