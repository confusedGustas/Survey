export interface User {
  id: number;
  username: string;
  email: string;
  role: string;
  creationDate?: string;
  lastModifiedDate?: string;
  active?: boolean;
}

export interface Survey {
  id: number;
  title: string;
  description: string;
  ownerId: number;
  ownerUsername?: string;
  creationDate?: string;
  questions: Question[];
  responseCount?: number;
}

export interface Question {
  id: number;
  content: string;
  questionType: QuestionType;
  surveyId: number;
  choices?: Choice[];
  required?: boolean;
  order?: number;
}

export enum QuestionType {
  TEXT = 'TEXT',
  SINGLE = 'SINGLE',
  MULTIPLE = 'MULTIPLE'
}

export interface Choice {
  id: number;
  questionId: number;
  choiceText: string;
  order?: number;
}

export interface Answer {
  questionId: number;
  textResponse?: string;
  choiceId?: number;
  choiceIds?: number[];
}

export interface ApiResponse<T> {
  data: T;
  message?: string;
  status?: string;
  timestamp?: string;
  pagination?: Pagination;
  accessToken?: string;
  refreshToken?: string;
}

export interface Pagination {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ApiError {
  status?: number;
  message: string;
  timestamp?: string;
  path?: string;
  errors?: string[];
}