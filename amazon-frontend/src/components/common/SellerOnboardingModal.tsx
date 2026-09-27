import React, { useState } from 'react';
import { apiRequest } from '../../api/client';
import { useCustomerAuth } from '../../context/CustomerAuthContext';

interface SellerOnboardingModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess?: () => void;
}

export const SellerOnboardingModal: React.FC<SellerOnboardingModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
}) => {
  const { user } = useCustomerAuth();

  const [fullName, setFullName] = useState(user?.fullName || '');
  const [email, setEmail] = useState(user?.email || '');
  const [password, setPassword] = useState('');
  const [phone, setPhone] = useState('+1 (555) 019-2834');
  const [storeName, setStoreName] = useState('');
  const [taxNumber, setTaxNumber] = useState('');
  const [businessAddress, setBusinessAddress] = useState('');
  const [bankAccountDetails, setBankAccountDetails] = useState('');

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [submitted, setSubmitted] = useState(false);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      await apiRequest('/auth/seller/register', {
        method: 'POST',
        body: JSON.stringify({
          fullName,
          email,
          password: password || 'SellerSecurePassword123!',
          phone,
          storeName,
          taxNumber,
          businessAddress,
          bankAccountDetails: bankAccountDetails || undefined,
        }),
      });

      setSubmitted(true);
      if (onSuccess) onSuccess();
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('Failed to complete merchant registration. Please verify your details.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      style={{
        position: 'fixed',
        inset: 0,
        zIndex: 9999,
        backgroundColor: 'rgba(15, 23, 42, 0.75)',
        backdropFilter: 'blur(4px)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '1rem',
      }}
      onClick={onClose}
    >
      <div
        style={{
          backgroundColor: '#ffffff',
          borderRadius: '12px',
          width: '100%',
          maxWidth: '560px',
          maxHeight: '90vh',
          overflowY: 'auto',
          boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
          border: '1px solid #e2e8f0',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <div
          style={{
            padding: '1.25rem 1.5rem',
            borderBottom: '1px solid #f1f5f9',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
            <span style={{ fontSize: '1.4rem' }}>🏪</span>
            <div>
              <h3 style={{ margin: 0, fontSize: '1.15rem', color: '#0f172a', fontWeight: 700 }}>
                ApexMerchant Seller Onboarding
              </h3>
              <p style={{ margin: 0, fontSize: '0.8rem', color: '#64748b' }}>
                Register your 3P merchant store and unlock selling privileges
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            style={{
              background: 'none',
              border: 'none',
              fontSize: '1.2rem',
              cursor: 'pointer',
              color: '#94a3b8',
            }}
          >
            ✕
          </button>
        </div>

        {submitted ? (
          <div style={{ padding: '2.5rem', textAlign: 'center' }}>
            <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>📦</div>
            <h4 style={{ fontSize: '1.3rem', color: '#0f172a', marginBottom: '0.5rem' }}>
              Merchant Store Registered!
            </h4>
            <p style={{ color: '#475569', fontSize: '0.9rem', lineHeight: 1.5, marginBottom: '1.5rem' }}>
              Congratulations! Your seller account for <strong>{storeName}</strong> has been created.
              You now have full access to listing management, order fulfillment, and merchant analytics.
            </p>
            <button
              type="button"
              onClick={() => {
                onClose();
                window.location.reload();
              }}
              style={{
                backgroundColor: '#0284c7',
                color: '#ffffff',
                border: 'none',
                borderRadius: '6px',
                padding: '0.65rem 1.5rem',
                fontWeight: 600,
                fontSize: '0.9rem',
                cursor: 'pointer',
              }}
            >
              Enter Seller Central
            </button>
          </div>
        ) : (
          <form onSubmit={handleSubmit} style={{ padding: '1.5rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {error && (
              <div
                style={{
                  backgroundColor: '#fef2f2',
                  border: '1px solid #fecaca',
                  color: '#991b1b',
                  padding: '0.75rem',
                  borderRadius: '6px',
                  fontSize: '0.85rem',
                }}
              >
                {error}
              </div>
            )}

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
              <div>
                <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                  Store / Trade Name *
                </label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Apex Direct US"
                  value={storeName}
                  onChange={(e) => setStoreName(e.target.value)}
                  style={{ width: '100%', padding: '0.5rem 0.75rem', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '0.875rem' }}
                />
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                  State Tax Permit / EIN *
                </label>
                <input
                  type="text"
                  required
                  placeholder="e.g. US-TAX-982410"
                  value={taxNumber}
                  onChange={(e) => setTaxNumber(e.target.value)}
                  style={{ width: '100%', padding: '0.5rem 0.75rem', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '0.875rem' }}
                />
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
              <div>
                <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                  Owner Full Name *
                </label>
                <input
                  type="text"
                  required
                  placeholder="Legal representative name"
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  style={{ width: '100%', padding: '0.5rem 0.75rem', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '0.875rem' }}
                />
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                  Support Email *
                </label>
                <input
                  type="email"
                  required
                  placeholder="seller@store.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  style={{ width: '100%', padding: '0.5rem 0.75rem', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '0.875rem' }}
                />
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
              <div>
                <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                  Merchant Phone *
                </label>
                <input
                  type="tel"
                  required
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  style={{ width: '100%', padding: '0.5rem 0.75rem', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '0.875rem' }}
                />
              </div>

              {!user && (
                <div>
                  <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                    Password *
                  </label>
                  <input
                    type="password"
                    required
                    placeholder="Min 6 characters"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    style={{ width: '100%', padding: '0.5rem 0.75rem', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '0.875rem' }}
                  />
                </div>
              )}
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Registered Business Address *
              </label>
              <input
                type="text"
                required
                placeholder="Suite, Street, City, State, ZIP"
                value={businessAddress}
                onChange={(e) => setBusinessAddress(e.target.value)}
                style={{ width: '100%', padding: '0.5rem 0.75rem', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '0.875rem' }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Bank Settlement Account
              </label>
              <input
                type="text"
                placeholder="Routing &amp; Account Details (e.g. Chase Bank **** 4812)"
                value={bankAccountDetails}
                onChange={(e) => setBankAccountDetails(e.target.value)}
                style={{ width: '100%', padding: '0.5rem 0.75rem', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '0.875rem' }}
              />
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '0.5rem' }}>
              <button
                type="button"
                onClick={onClose}
                style={{
                  padding: '0.6rem 1.2rem',
                  border: '1px solid #cbd5e1',
                  borderRadius: '6px',
                  backgroundColor: '#ffffff',
                  color: '#475569',
                  fontWeight: 600,
                  fontSize: '0.875rem',
                  cursor: 'pointer',
                }}
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={loading}
                style={{
                  padding: '0.6rem 1.4rem',
                  border: 'none',
                  borderRadius: '6px',
                  backgroundColor: '#0284c7',
                  color: '#ffffff',
                  fontWeight: 600,
                  fontSize: '0.875rem',
                  cursor: loading ? 'not-allowed' : 'pointer',
                  opacity: loading ? 0.7 : 1,
                }}
              >
                {loading ? 'Creating Store...' : 'Complete Registration'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};
