import React, { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { brandApi } from '../api/brandApi';
import type { BrandMemberResponseDto, BrandProfileResponseDto, BrandRole } from '../api/brandTypes';
import { useCustomerAuth } from './CustomerAuthContext';

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
  detectedRole: BrandRole | null;
  activeRole: BrandRole | null;
  hasBrand: boolean;
  permissions: BrandPermissions;
  loading: boolean;
  error: string | null;
  refetchProfile: () => Promise<void>;
}

const BrandAuthContext = createContext<BrandAuthContextType | undefined>(undefined);

export const BrandAuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, isAuthenticated } = useCustomerAuth();
  const [profile, setProfile] = useState<BrandProfileResponseDto | null>(null);
  const [currentMember, setCurrentMember] = useState<BrandMemberResponseDto | null>(null);
  const [detectedRole, setDetectedRole] = useState<BrandRole | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchProfile = async () => {
    if (!isAuthenticated) {
      setProfile(null);
      setCurrentMember(null);
      setDetectedRole(null);
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const data = await brandApi.getBrandProfile();

      if (!data || !data.brand) {
        setProfile(null);
        setCurrentMember(null);
        setDetectedRole(null);
        setError('No brand associated with this account');
        return;
      }

      setProfile(data);

      if (data.teamMembers && data.teamMembers.length > 0) {
        let matchedMember: BrandMemberResponseDto | undefined;
        if (user?.email) {
          matchedMember = data.teamMembers.find(
            (m) => m.userEmail?.toLowerCase() === user.email.toLowerCase(),
          );
        }

        const resolved =
          matchedMember ||
          data.teamMembers.find((m) => m.brandRole === 'BRAND_OWNER') ||
          data.teamMembers[0];

        setCurrentMember(resolved);
        setDetectedRole(resolved.brandRole);
      } else {
        setDetectedRole('BRAND_OWNER');
      }
    } catch (err) {
      setProfile(null);
      setCurrentMember(null);
      setDetectedRole(null);
      setError(err instanceof Error ? err.message : 'No brand profile associated with this account');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    let ignore = false;
    const run = async () => {
      if (!isAuthenticated) {
        if (!ignore) {
          setProfile(null);
          setCurrentMember(null);
          setDetectedRole(null);
          setLoading(false);
        }
        return;
      }

      try {
        setLoading(true);
        setError(null);
        const data = await brandApi.getBrandProfile();
        if (ignore) return;

        if (!data || !data.brand) {
          setProfile(null);
          setCurrentMember(null);
          setDetectedRole(null);
          setError('No brand associated with this account');
          return;
        }

        setProfile(data);

        if (data.teamMembers && data.teamMembers.length > 0) {
          let matchedMember: BrandMemberResponseDto | undefined;
          if (user?.email) {
            matchedMember = data.teamMembers.find(
              (m) => m.userEmail?.toLowerCase() === user.email.toLowerCase(),
            );
          }
          const resolved =
            matchedMember ||
            data.teamMembers.find((m) => m.brandRole === 'BRAND_OWNER') ||
            data.teamMembers[0];
          if (!ignore) {
            setCurrentMember(resolved);
            setDetectedRole(resolved.brandRole);
          }
        } else {
          if (!ignore) setDetectedRole('BRAND_OWNER');
        }
      } catch (err) {
        if (!ignore) {
          setProfile(null);
          setCurrentMember(null);
          setDetectedRole(null);
          setError(err instanceof Error ? err.message : 'No brand profile associated with this account');
        }
      } finally {
        if (!ignore) setLoading(false);
      }
    };
    void run();
    return () => {
      ignore = true;
    };
  }, [isAuthenticated, user?.email]);

  const activeRole: BrandRole | null = detectedRole;
  const hasBrand = Boolean(profile && profile.brand && profile.brand.id);

  const permissions: BrandPermissions = useMemo(() => {
    if (!activeRole) {
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
          canModifyBrandOwner: false,
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
          canViewTeam: true,
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
          canManageCatalog: true,
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
        hasBrand,
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

// oxlint-disable-next-line react/only-export-components
export function useBrandAuth(): BrandAuthContextType {
  const context = useContext(BrandAuthContext);
  if (!context) {
    throw new Error('useBrandAuth must be used within a BrandAuthProvider');
  }
  return context;
}
