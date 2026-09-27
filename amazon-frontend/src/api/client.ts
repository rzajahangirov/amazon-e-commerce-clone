import type { ResponseDto } from './types';

export const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/v1/api';

const TOKEN_STORAGE_KEY = 'admin_access_token';

export class ApiError extends Error {
  status: number;

  constructor(status: number, message: string) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

export function getAccessToken(): string {
  const fromStorage = localStorage.getItem(TOKEN_STORAGE_KEY);
  if (fromStorage) {
    return fromStorage;
  }
  const fromEnv = import.meta.env.VITE_ADMIN_ACCESS_TOKEN;
  if (fromEnv) {
    return fromEnv;
  }
  return 'mock-admin-development-token';
}

export function setAccessToken(token: string): void {
  localStorage.setItem(TOKEN_STORAGE_KEY, token);
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
  const response = await fetch(buildUrl(path, query), {
    ...init,
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      Authorization: `Bearer ${getAccessToken()}`,
      ...headers,
    },
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
