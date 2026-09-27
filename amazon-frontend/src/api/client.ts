import type { ResponseDto } from './types';

// Read API base URL dynamically from environment (supports both root URL and /v1/api suffix)
const rawBaseUrl = (import.meta.env.VITE_API_BASE_URL || 'https://amazon-e-commerce-clone-q5pc.onrender.com').trim();
export const API_BASE_URL = rawBaseUrl.endsWith('/v1/api')
  ? rawBaseUrl.replace(/\/+$/, '')
  : `${rawBaseUrl.replace(/\/+$/, '')}/v1/api`;

export const CUSTOMER_TOKEN_KEY = 'customer_access_token';
export const ADMIN_TOKEN_KEY = 'admin_access_token';

export class ApiError extends Error {
  status: number;

  constructor(status: number, message: string) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

export function getAccessToken(): string {
  const customerToken = localStorage.getItem(CUSTOMER_TOKEN_KEY);
  if (customerToken) {
    return customerToken;
  }
  const adminToken = localStorage.getItem(ADMIN_TOKEN_KEY);
  if (adminToken) {
    return adminToken;
  }
  const generalToken = localStorage.getItem('access_token');
  if (generalToken) {
    return generalToken;
  }
  return '';
}

export function setAccessToken(token: string): void {
  localStorage.setItem(ADMIN_TOKEN_KEY, token);
}

export function setCustomerAccessToken(token: string): void {
  localStorage.setItem(CUSTOMER_TOKEN_KEY, token);
}

export function removeCustomerAccessToken(): void {
  localStorage.removeItem(CUSTOMER_TOKEN_KEY);
}

function buildUrl(path: string, query?: Record<string, string | number | boolean | undefined>): string {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;
  const url = new URL(`${API_BASE_URL}${normalizedPath}`);
  if (query) {
    for (const [key, value] of Object.entries(query)) {
      if (value !== undefined && value !== '') {
        url.searchParams.set(key, String(value));
      }
    }
  }
  return url.toString();
}

export async function apiRequest<T>(
  path: string,
  options: RequestInit & { query?: Record<string, string | number | boolean | undefined> } = {},
): Promise<ResponseDto<T>> {
  const { query, headers, ...init } = options;
  const token = getAccessToken();
  const requestHeaders: Record<string, string> = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
  };
  if (token) {
    requestHeaders['Authorization'] = `Bearer ${token}`;
  }
  if (headers) {
    if (headers instanceof Headers) {
      headers.forEach((val, key) => {
        requestHeaders[key] = val;
      });
    } else if (Array.isArray(headers)) {
      headers.forEach(([key, val]) => {
        requestHeaders[key] = val;
      });
    } else {
      Object.assign(requestHeaders, headers);
    }
  }

  const response = await fetch(buildUrl(path, query), {
    ...init,
    credentials: init.credentials ?? 'include',
    headers: requestHeaders,
  });

  let body: ResponseDto<T>;
  try {
    body = (await response.json()) as ResponseDto<T>;
  } catch {
    throw new ApiError(response.status, response.statusText || 'Invalid JSON response');
  }

  if (!response.ok) {
    throw new ApiError(response.status, body.message || response.statusText);
  }

  return body;
}

export async function apiGet<T>(
  path: string,
  query?: Record<string, string | number | boolean | undefined>,
): Promise<T> {
  const envelope = await apiRequest<T>(path, { method: 'GET', query });
  if (envelope.data === null) {
    throw new ApiError(500, envelope.message || 'Empty response data');
  }
  return envelope.data;
}
