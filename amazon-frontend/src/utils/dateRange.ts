import { format, subDays } from 'date-fns';

export function defaultOrderDateRange(days = 30): { startDate: string; endDate: string } {
  const end = new Date();
  const start = subDays(end, days - 1);
  return {
    startDate: format(start, 'yyyy-MM-dd'),
    endDate: format(end, 'yyyy-MM-dd'),
  };
}
