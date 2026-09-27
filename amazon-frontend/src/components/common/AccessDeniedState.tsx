import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCustomerAuth } from '../../context/CustomerAuthContext';
import { BrandOnboardingModal } from './BrandOnboardingModal';
import { SellerOnboardingModal } from './SellerOnboardingModal';

export interface AccessDeniedStateProps {
  portalType: 'brand' | 'seller' | 'admin';
  customTitle?: string;
  customMessage?: string;
}

export const AccessDeniedState: React.FC<AccessDeniedStateProps> = ({
  portalType,
  customTitle,
  customMessage,
}) => {
  const navigate = useNavigate();
  const { isAuthenticated, openAuthModal } = useCustomerAuth();

  const [brandModalOpen, setBrandModalOpen] = useState(false);
  const [sellerModalOpen, setSellerModalOpen] = useState(false);

  const getDetails = () => {
    switch (portalType) {
      case 'brand':
        return {
          title: customTitle || '403 Access Denied • Brand Registry Account Required',
          description:
            customMessage ||
            'Access to the Brand Operations & Registry Portal requires an authorized Brand Owner or Team Member role. Your account is not currently associated with any verified enterprise brand.',
          badge: 'Brand Registry RBAC Guard',
          primaryActionText: 'Register / Create Brand',
          onPrimaryAction: () => setBrandModalOpen(true),
        };
      case 'seller':
        return {
          title: customTitle || '403 Access Denied • Seller Merchant Account Required',
          description:
            customMessage ||
            'Access to Seller Central (ApexMerchant) requires an active 3P Merchant profile. Your account does not currently hold merchant selling privileges.',
          badge: 'Seller Central RBAC Guard',
          primaryActionText: 'Register as a Seller',
          onPrimaryAction: () => setSellerModalOpen(true),
        };
      case 'admin':
        return {
          title: customTitle || '403 Access Denied • Administrator Clearance Required',
          description:
            customMessage ||
            'Global Console access is restricted exclusively to authorized Platform Supervisors holding the ROLE_ADMIN cryptographic authority.',
          badge: 'Security Governance RBAC Guard',
          primaryActionText: null,
          onPrimaryAction: null,
        };
    }
  };

  const details = getDetails();

  return (
    <div
      style={{
        minHeight: '75vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '2rem 1rem',
        backgroundColor: '#f8fafc',
      }}
    >
      <div
        style={{
          maxWidth: '580px',
          width: '100%',
          backgroundColor: '#ffffff',
          borderRadius: '16px',
          border: '1px solid #e2e8f0',
          boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.05), 0 8px 10px -6px rgba(0, 0, 0, 0.01)',
          padding: '2.5rem',
          textAlign: 'center',
        }}
      >
        {/* Security Shield Icon */}
        <div
          style={{
            width: '68px',
            height: '68px',
            borderRadius: '50%',
            backgroundColor: '#fee2e2',
            color: '#dc2626',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            margin: '0 auto 1.5rem',
            boxShadow: '0 0 0 8px #fef2f2',
          }}
        >
          <svg width="34" height="34" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
            <path d="M9 12l2 2 4-4" />
          </svg>
        </div>

        {/* Guard Tag */}
        <div style={{ marginBottom: '0.75rem' }}>
          <span
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '6px',
              padding: '0.25rem 0.75rem',
              borderRadius: '9999px',
              fontSize: '0.725rem',
              fontWeight: 700,
              textTransform: 'uppercase',
              letterSpacing: '0.06em',
              backgroundColor: '#fef2f2',
              color: '#b91c1c',
              border: '1px solid #fecaca',
            }}
          >
            <span style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: '#ef4444' }} />
            {details.badge}
          </span>
        </div>

        {/* Title */}
        <h2
          style={{
            fontSize: '1.45rem',
            fontWeight: 800,
            color: '#0f172a',
            margin: '0 0 0.75rem',
            lineHeight: 1.3,
          }}
        >
          {details.title}
        </h2>

        {/* Description */}
        <p
          style={{
            fontSize: '0.925rem',
            color: '#64748b',
            lineHeight: 1.6,
            margin: '0 0 2rem',
          }}
        >
          {details.description}
        </p>

        {/* Action Buttons */}
        <div
          style={{
            display: 'flex',
            flexDirection: 'column',
            gap: '0.75rem',
            justifyContent: 'center',
            alignItems: 'center',
          }}
        >
          {!isAuthenticated ? (
            <button
              type="button"
              onClick={() => openAuthModal('signin', `Please sign in to access ${portalType} operations`)}
              style={{
                width: '100%',
                maxWidth: '320px',
                padding: '0.75rem 1.5rem',
                backgroundColor: '#2563eb',
                color: '#ffffff',
                border: 'none',
                borderRadius: '8px',
                fontWeight: 700,
                fontSize: '0.925rem',
                cursor: 'pointer',
                transition: 'all 0.15s ease',
              }}
            >
              Sign In to Your Account
            </button>
          ) : (
            details.primaryActionText &&
            details.onPrimaryAction && (
              <button
                type="button"
                onClick={details.onPrimaryAction}
                style={{
                  width: '100%',
                  maxWidth: '320px',
                  padding: '0.75rem 1.5rem',
                  backgroundColor: '#2563eb',
                  color: '#ffffff',
                  border: 'none',
                  borderRadius: '8px',
                  fontWeight: 700,
                  fontSize: '0.925rem',
                  cursor: 'pointer',
                  transition: 'all 0.15s ease',
                }}
              >
                {details.primaryActionText}
              </button>
            )
          )}

          <button
            type="button"
            onClick={() => navigate('/')}
            style={{
              width: '100%',
              maxWidth: '320px',
              padding: '0.7rem 1.5rem',
              backgroundColor: '#ffffff',
              color: '#334155',
              border: '1px solid #cbd5e1',
              borderRadius: '8px',
              fontWeight: 600,
              fontSize: '0.9rem',
              cursor: 'pointer',
              transition: 'all 0.15s ease',
            }}
          >
            ← Return to Storefront Home
          </button>
        </div>
      </div>

      {/* Embedded Modals */}
      <BrandOnboardingModal
        isOpen={brandModalOpen}
        onClose={() => setBrandModalOpen(false)}
      />

      <SellerOnboardingModal
        isOpen={sellerModalOpen}
        onClose={() => setSellerModalOpen(false)}
      />
    </div>
  );
};
