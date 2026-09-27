export interface ResponseDto<T> {
  data: T | null;
  message: string;
}

export interface PaginationPayload<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface DailyGmvPoint {
  date: string;
  gmv: number;
}

export interface TopMetric {
  id: string;
  name: string;
  revenue: number;
}

export interface AdminAnalytics {
  totalGMV: number;
  totalOrdersCount: number;
  totalActiveUsers: number;
  totalActiveSellers: number;
  totalActiveBrands: number;
  totalPendingBrandApplications: number;
  topPerformingBrands: TopMetric[];
  topPerformingCategories: TopMetric[];
  dailyGmvTrajectory: DailyGmvPoint[];
}

export type UserStatus = 'ACTIVE' | 'SUSPENDED';

export interface UserResponse {
  id: string;
  fullName: string;
  email: string;
  phone?: string;
  avatarUrl?: string | null;
  lastActiveAt?: string | null;
  status: UserStatus;
  roles: string[];
}

export type OrderStatus =
  | 'PENDING'
  | 'CONFIRMED'
  | 'SHIPPED'
  | 'DELIVERED'
  | 'CANCELLED'
  | 'REFUNDED';

export interface AdminOrder {
  id: string;
  orderNumber: string;
  buyerId: string;
  buyerEmail: string;
  totalAmount: number;
  status: OrderStatus;
  placedAt: string;
}

export interface CategoryResponse {
  id: string;
  parentId?: string | null;
  name: string;
  slug: string;
  level: number;
  isApproved: boolean;
  rejectionReason?: string | null;
  commercialJustification?: string | null;
  subCategories?: CategoryResponse[];
  createdAt?: string;
}

export interface AdminUsersQuery {
  role?: string;
  active?: boolean;
  email?: string;
  page?: number;
  size?: number;
}

export interface AdminOrdersQuery {
  status?: OrderStatus;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
}
