const currencyFormatter = new Intl.NumberFormat('en-US', {
  style: 'currency',
  currency: 'USD',
  maximumFractionDigits: 2,
});

const compactCurrencyFormatter = new Intl.NumberFormat('en-US', {
  style: 'currency',
  currency: 'USD',
  notation: 'compact',
  maximumFractionDigits: 1,
});

const numberFormatter = new Intl.NumberFormat('en-US');

export function formatCurrency(value: number | undefined | null): string {
  if (value == null || Number.isNaN(value)) {
    return '$0.00';
  }
  return currencyFormatter.format(value);
}

export function formatCompactCurrency(value: number | undefined | null): string {
  if (value == null || Number.isNaN(value)) {
    return '$0';
  }
  return compactCurrencyFormatter.format(value);
}

export function formatCount(value: number | undefined | null): string {
  if (value == null) {
    return '0';
  }
  return numberFormatter.format(value);
}

export function formatChartDate(isoDate: string): string {
  const d = new Date(isoDate);
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}

export function formatRoleLabel(role: string): string {
  return role.replace(/^ROLE_/, '').replace(/_/g, ' ');
}

export function userInitials(fullName: string): string {
  const parts = fullName.trim().split(/\s+/);
  if (parts.length === 0) {
    return '?';
  }
  if (parts.length === 1) {
    return parts[0].slice(0, 2).toUpperCase();
  }
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
}
