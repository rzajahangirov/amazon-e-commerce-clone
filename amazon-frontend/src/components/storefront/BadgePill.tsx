import React from 'react';
import type { BadgeTag } from '../../api/storefrontTypes';

interface BadgePillProps {
  tag: BadgeTag | string | null | undefined;
  size?: 'sm' | 'md';
}

export const BadgePill: React.FC<BadgePillProps> = ({ tag, size = 'sm' }) => {
  if (!tag) return null;

  const normalized = String(tag).toUpperCase();

  let label = String(tag).replace(/_/g, ' ');
  let bg = '#232f3e';
  let color = '#ffffff';
  let border = 'none';

  switch (normalized) {
    case 'BEST_SELLER':
      label = 'Best Seller';
      bg = '#e67a00';
      color = '#ffffff';
      break;
    case 'OVERALL_PICK':
      label = 'Overall Pick';
      bg = '#002f36';
      color = '#ffffff';
      break;
    case 'LIMITED_STOCK':
      label = 'Limited Stock';
      bg = '#fff0f0';
      color = '#b12704';
      border = '1px solid #ffd8d8';
      break;
    case 'DEAL_OF_THE_DAY':
      label = 'Deal of the Day';
      bg = '#cc0c39';
      color = '#ffffff';
      break;
    case 'ENTERPRISE_TIER':
      label = 'Enterprise Tier';
      bg = '#102a43';
      color = '#f0b429';
      border = '1px solid rgba(240, 180, 41, 0.4)';
      break;
    case 'INDUSTRIAL_CHOICE':
      label = 'Industrial Choice';
      bg = '#334e68';
      color = '#f0f4f8';
      break;
    case 'APPLE_MAG_OPTIMIZED':
      label = 'MagSafe Optimized';
      bg = '#24292e';
      color = '#58a6ff';
      break;
    default:
      label = label.charAt(0).toUpperCase() + label.slice(1).toLowerCase();
      bg = '#f0f2f2';
      color = '#0f1111';
      border = '1px solid #d5d9d9';
  }

  const isSmall = size === 'sm';

  return (
    <span
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        padding: isSmall ? '2px 8px' : '4px 10px',
        borderRadius: '3px',
        backgroundColor: bg,
        color: color,
        border: border,
        fontSize: isSmall ? '0.68rem' : '0.78rem',
        fontWeight: 700,
        letterSpacing: '0.2px',
        textTransform: 'none',
        lineHeight: 1.2,
      }}
    >
      {label}
    </span>
  );
};
