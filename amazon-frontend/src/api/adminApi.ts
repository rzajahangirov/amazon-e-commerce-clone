import { apiGet } from './client';
import type {
  AdminAnalytics,
  AdminOrder,
  AdminOrdersQuery,
  AdminUsersQuery,
  CategoryResponse,
  PaginationPayload,
  UserResponse,
} from './types';

export function fetchAdminAnalytics(): Promise<AdminAnalytics> {
  return apiGet<AdminAnalytics>('/admin/analytics');
}

export function fetchAdminUsers(
  query: AdminUsersQuery = {},
): Promise<PaginationPayload<UserResponse>> {
  return apiGet<PaginationPayload<UserResponse>>('/admin/users', {
    role: query.role,
    active: query.active,
    email: query.email,
    page: query.page ?? 0,
    size: query.size ?? 20,
  });
}

export function fetchAdminOrders(
  query: AdminOrdersQuery = {},
): Promise<PaginationPayload<AdminOrder>> {
  return apiGet<PaginationPayload<AdminOrder>>('/admin/orders', {
    status: query.status,
    startDate: query.startDate,
    endDate: query.endDate,
    page: query.page ?? 0,
    size: query.size ?? 20,
  });
}

export function fetchPendingCategories(): Promise<CategoryResponse[]> {
  return apiGet<CategoryResponse[]>('/admin/categories/pending');
}
