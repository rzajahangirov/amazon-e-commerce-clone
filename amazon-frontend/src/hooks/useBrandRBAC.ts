import { useBrandAuth } from '../context/BrandAuthContext';
import type { BrandRole } from '../api/brandTypes';

export function useBrandRBAC() {
  const { activeRole, detectedRole, permissions, loading, profile, currentMember } =
    useBrandAuth();

  const hasAnyRole = (roles: BrandRole[]): boolean => {
    return activeRole !== null && roles.includes(activeRole);
  };

  const hasRole = (role: BrandRole): boolean => {
    return activeRole === role;
  };

  const roleNameDisplay: Record<BrandRole, string> = {
    BRAND_OWNER: 'Brand Owner',
    BRAND_SUPER_ADMIN: 'Brand Super Admin',
    BRAND_ADMIN: 'Brand Admin',
    BRAND_SELLER: 'Brand Seller',
    BRAND_MARKETING_MEMBER: 'Marketing Member',
  };

  const roleTierDisplay: Record<BrandRole, string> = {
    BRAND_OWNER: 'Tier 1 • Maximum Authority',
    BRAND_SUPER_ADMIN: 'Tier 1 • Executive Admin',
    BRAND_ADMIN: 'Tier 2 • High Operational',
    BRAND_SELLER: 'Tier 3 • Operational Fulfillment',
    BRAND_MARKETING_MEMBER: 'Tier 4 • Creative Marketing',
  };

  return {
    activeRole,
    detectedRole,
    permissions,
    loading,
    profile,
    currentMember,
    hasAnyRole,
    hasRole,
    roleTitle: activeRole ? (roleNameDisplay[activeRole] || activeRole) : 'No Role',
    roleTier: activeRole ? (roleTierDisplay[activeRole] || 'Standard Access') : 'Standard Access',
  };
}
