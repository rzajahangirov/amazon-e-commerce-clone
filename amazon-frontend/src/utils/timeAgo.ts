import { formatDistanceToNow, parseISO } from 'date-fns';

export function formatTimeAgo(isoDate: string | null | undefined): string {
  if (!isoDate) {
    return 'Never';
  }
  try {
    return formatDistanceToNow(parseISO(isoDate), { addSuffix: true });
  } catch {
    return 'Unknown';
  }
}
