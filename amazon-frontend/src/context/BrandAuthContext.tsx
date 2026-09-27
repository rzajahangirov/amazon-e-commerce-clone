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
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchProfile = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await brandApi.getBrandProfile();
      setProfile(data);

      if (data.teamMembers && data.teamMembers.length > 0) {
        // Automatically match authenticated user email if available
        let matchedMember: BrandMemberResponseDto | undefined;
        const rawUser = localStorage.getItem('amazon_customer_user_v1');
        if (rawUser) {
          try {
            const userObj = JSON.parse(rawUser);
            if (userObj.email) {
              matchedMember = data.teamMembers.find(
                (m) => m.userEmail?.toLowerCase() === userObj.email.toLowerCase()
              );
            }
          } catch {
            // ignore
          }
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
      console.error('Error loading brand profile:', err);
      setError(err instanceof Error ? err.message : 'Failed to load brand profile');
      setDetectedRole('BRAND_OWNER');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    let ignore = false;
    const run = async () => {
      try {
        setLoading(true);
        setError(null);
        const data = await brandApi.getBrandProfile();
        if (ignore) return;
        setProfile(data);

        if (data.teamMembers && data.teamMembers.length > 0) {
          let matchedMember: BrandMemberResponseDto | undefined;
          const rawUser = localStorage.getItem('amazon_customer_user_v1');
          if (rawUser) {
            try {
              const userObj = JSON.parse(rawUser);
              if (userObj.email) {
                matchedMember = data.teamMembers.find(
                  (m) => m.userEmail?.toLowerCase() === userObj.email.toLowerCase()
                );
              }
            } catch {
              // ignore
            }
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
          console.error('Error loading brand profile:', err);
          setError(err instanceof Error ? err.message : 'Failed to load brand profile');
          setDetectedRole('BRAND_OWNER');
        }
      } finally {
        if (!ignore) setLoading(false);
      }
    };
    void run();
    return () => { ignore = true; };
  }, []);

  const activeRole: BrandRole = detectedRole;

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
