import React, { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { brandApi } from '../api/brandApi';
import type { BrandMemberResponseDto, BrandProfileResponseDto, BrandRole } from '../api/brandTypes';

export interface BrandPermissions {
  canViewTeam: boolean;
  canManageTeam: boolean;
  canModifyBrandOwner: boolean;
  canManageCatalog: boolean;
  canRegisterAsin: boolean;
  canCreateMarketingPost: boolean;
  canProposeProfileUpdate: boolean;
  canProposeCategory: boolean;
  canDeleteProduct: boolean;
  isSeller: boolean;
  isMarketingOnly: boolean;
}

export interface BrandAuthContextType {
  profile: BrandProfileResponseDto | null;
  currentMember: BrandMemberResponseDto | null;
  detectedRole: BrandRole;
  activeRole: BrandRole;
  simulatedRole: BrandRole | null;
  setSimulatedRole: (role: BrandRole | null) => void;
  permissions: BrandPermissions;
  loading: boolean;
  error: string | null;
  refetchProfile: () => Promise<void>;
}

const BrandAuthContext = createContext<BrandAuthContextType | undefined>(undefined);

export const BrandAuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [profile, setProfile] = useState<BrandProfileResponseDto | null>(null);
  const [currentMember, setCurrentMember] = useState<BrandMemberResponseDto | null>(null);
  const [detectedRole, setDetectedRole] = useState<BrandRole>('BRAND_OWNER');
  const [simulatedRole, setSimulatedRoleState] = useState<BrandRole | null>(() => {
    const saved = localStorage.getItem('simulated_brand_role');
    return (saved as BrandRole) || null;
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchProfile = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await brandApi.getBrandProfile();
      setProfile(data);

      // Find current user's membership. If not matched, default to the first member or BRAND_OWNER
      if (data.teamMembers && data.teamMembers.length > 0) {
        // Pick primary owner or first member
        const owner = data.teamMembers.find((m) => m.brandRole === 'BRAND_OWNER') || data.teamMembers[0];
        setCurrentMember(owner);
        setDetectedRole(owner.brandRole);
      } else {
        setDetectedRole('BRAND_OWNER');
      }
    } catch (err) {
      console.error('Error loading brand profile:', err);
      setError(err instanceof Error ? err.message : 'Failed to load brand profile');
      setDetectedRole('BRAND_OWNER');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void fetchProfile();
  }, []);

  const setSimulatedRole = (role: BrandRole | null) => {
    if (role) {
      localStorage.setItem('simulated_brand_role', role);
    } else {
      localStorage.removeItem('simulated_brand_role');
    }
    setSimulatedRoleState(role);
  };

  const activeRole: BrandRole = simulatedRole || detectedRole;

  const permissions: BrandPermissions = useMemo(() => {
    switch (activeRole) {
      case 'BRAND_OWNER':
        return {
          canViewTeam: true,
          canManageTeam: true,
          canModifyBrandOwner: true,
          canManageCatalog: true,
          canRegisterAsin: true,
          canCreateMarketingPost: true,
          canProposeProfileUpdate: true,
          canProposeCategory: true,
          canDeleteProduct: true,
          isSeller: false,
          isMarketingOnly: false,
        };
      case 'BRAND_SUPER_ADMIN':
        return {
          canViewTeam: true,
          canManageTeam: true,
          canModifyBrandOwner: false, // Cannot modify or remove BRAND_OWNER
          canManageCatalog: true,
          canRegisterAsin: true,
          canCreateMarketingPost: true,
          canProposeProfileUpdate: true,
          canProposeCategory: true,
          canDeleteProduct: true,
          isSeller: false,
          isMarketingOnly: false,
        };
      case 'BRAND_ADMIN':
        return {
          canViewTeam: false, // Cannot manage team members
          canManageTeam: false,
          canModifyBrandOwner: false,
          canManageCatalog: true,
          canRegisterAsin: true,
          canCreateMarketingPost: true,
          canProposeProfileUpdate: true,
          canProposeCategory: false,
          canDeleteProduct: false,
          isSeller: false,
          isMarketingOnly: false,
        };
      case 'BRAND_SELLER':
        return {
          canViewTeam: false,
          canManageTeam: false,
          canModifyBrandOwner: false,
          canManageCatalog: false, // Read-only view
          canRegisterAsin: false,
          canCreateMarketingPost: false,
          canProposeProfileUpdate: false,
          canProposeCategory: false,
          canDeleteProduct: false,
          isSeller: true,
          isMarketingOnly: false,
        };
      case 'BRAND_MARKETING_MEMBER':
        return {
          canViewTeam: false,
          canManageTeam: false,
          canModifyBrandOwner: false,
          canManageCatalog: false,
          canRegisterAsin: false,
          canCreateMarketingPost: true,
          canProposeProfileUpdate: false,
          canProposeCategory: false,
          canDeleteProduct: false,
          isSeller: false,
          isMarketingOnly: true,
        };
      default:
        return {
          canViewTeam: false,
          canManageTeam: false,
          canModifyBrandOwner: false,
          canManageCatalog: false,
          canRegisterAsin: false,
          canCreateMarketingPost: false,
          canProposeProfileUpdate: false,
          canProposeCategory: false,
          canDeleteProduct: false,
          isSeller: false,
          isMarketingOnly: false,
        };
    }
  }, [activeRole]);

  return (
    <BrandAuthContext.Provider
      value={{
        profile,
        currentMember,
        detectedRole,
        activeRole,
        simulatedRole,
        setSimulatedRole,
        permissions,
        loading,
        error,
        refetchProfile: fetchProfile,
      }}
    >
      {children}
    </BrandAuthContext.Provider>
  );
};

export function useBrandAuth(): BrandAuthContextType {
  const context = useContext(BrandAuthContext);
  if (!context) {
    throw new Error('useBrandAuth must be used within a BrandAuthProvider');
  }
  return context;
}
