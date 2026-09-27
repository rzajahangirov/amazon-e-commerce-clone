import React, { useState } from 'react';
import { apiRequest } from '../../api/client';
import { useCustomerAuth } from '../../context/CustomerAuthContext';

interface BrandOnboardingModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess?: () => void;
}

export const BrandOnboardingModal: React.FC<BrandOnboardingModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
}) => {
  const { user } = useCustomerAuth();

  const [applicantName, setApplicantName] = useState(user?.fullName || '');
  const [applicantEmail, setApplicantEmail] = useState(user?.email || '');
  const [password, setPassword] = useState('');
  const [applicantPhone, setApplicantPhone] = useState('');
  const [brandName, setBrandName] = useState('');
  const [trademarkRegistrationNumber, setTrademarkRegistrationNumber] = useState('');
  const [brandCountry, setBrandCountry] = useState('US');
  const [aboutText, setAboutText] = useState('');

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [submitted, setSubmitted] = useState(false);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    const brandSlug = brandName
      .toLowerCase()
      .trim()
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-+|-+$/g, '');

    try {
      await apiRequest('/brand-applications', {
        method: 'POST',
        body: JSON.stringify({
          applicantName,
          applicantEmail,
          password: password || 'BrandSecurePassword123!',
          applicantPhone: applicantPhone || undefined,
          brandName,
          brandSlug: brandSlug || 'my-brand',
          trademarkRegistrationNumber,
          brandCountry,
          aboutText: aboutText || undefined,
        }),
      });

      setSubmitted(true);
      if (onSuccess) onSuccess();
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('Failed to submit brand application. Please check your data.');
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
            <span style={{ fontSize: '1.4rem' }}>🛡️</span>
            <div>
              <h3 style={{ margin: 0, fontSize: '1.15rem', color: '#0f172a', fontWeight: 700 }}>
                Enterprise Brand Registry Application
              </h3>
              <p style={{ margin: 0, fontSize: '0.8rem', color: '#64748b' }}>
                Protect your intellectual property &amp; gain Brand Registry privileges
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
            <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>🎉</div>
            <h4 style={{ fontSize: '1.3rem', color: '#0f172a', marginBottom: '0.5rem' }}>
              Application Submitted Successfully!
            </h4>
            <p style={{ color: '#475569', fontSize: '0.9rem', lineHeight: 1.5, marginBottom: '1.5rem' }}>
              Your application for <strong>{brandName}</strong> has been logged into the Brand
              Governance queue. Once approved by our compliance team, you will receive full
              Brand Owner permissions across the portal.
            </p>
            <button
              type="button"
              onClick={onClose}
              style={{
                backgroundColor: '#2563eb',
                color: '#ffffff',
                border: 'none',
                borderRadius: '6px',
                padding: '0.65rem 1.5rem',
                fontWeight: 600,
                fontSize: '0.9rem',
                cursor: 'pointer',
              }}
            >
              Done
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
                  Brand Name *
                </label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Apex Audio"
                  value={brandName}
                  onChange={(e) => setBrandName(e.target.value)}
                  style={{ width: '100%', padding: '0.5rem 0.75rem', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '0.875rem' }}
                />
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                  Trademark Registration # *
                </label>
                <input
                  type="text"
                  required
                  placeholder="e.g. US-TM-894123"
                  value={trademarkRegistrationNumber}
                  onChange={(e) => setTrademarkRegistrationNumber(e.target.value)}
                  style={{ width: '100%', padding: '0.5rem 0.75rem', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '0.875rem' }}
                />
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
              <div>
                <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                  Applicant Full Name *
                </label>
                <input
                  type="text"
                  required
                  placeholder="Your Full Legal Name"
                  value={applicantName}
                  onChange={(e) => setApplicantName(e.target.value)}
                  style={{ width: '100%', padding: '0.5rem 0.75rem', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '0.875rem' }}
                />
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                  Applicant Email *
                </label>
                <input
                  type="email"
                  required
                  placeholder="name@company.com"
                  value={applicantEmail}
                  onChange={(e) => setApplicantEmail(e.target.value)}
                  style={{ width: '100%', padding: '0.5rem 0.75rem', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '0.875rem' }}
                />
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
              <div>
                <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                  Jurisdiction / Country *
                </label>
                <select
                  value={brandCountry}
                  onChange={(e) => setBrandCountry(e.target.value)}
                  style={{ width: '100%', padding: '0.5rem 0.75rem', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '0.875rem', backgroundColor: '#ffffff' }}
                >
                  <option value="US">United States (USPTO)</option>
                  <option value="EU">European Union (EUIPO)</option>
                  <option value="GB">United Kingdom (UKIPO)</option>
                  <option value="CA">Canada (CIPO)</option>
                  <option value="DE">Germany (DPMA)</option>
                  <option value="JP">Japan (JPO)</option>
                </select>
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                  Contact Phone
                </label>
                <input
                  type="tel"
                  placeholder="+1 (555) 000-0000"
                  value={applicantPhone}
                  onChange={(e) => setApplicantPhone(e.target.value)}
                  style={{ width: '100%', padding: '0.5rem 0.75rem', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '0.875rem' }}
                />
              </div>
            </div>

            {!user && (
              <div>
                <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                  Account Password *
                </label>
                <input
                  type="password"
                  required
                  placeholder="Minimum 8 characters"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  style={{ width: '100%', padding: '0.5rem 0.75rem', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '0.875rem' }}
                />
              </div>
            )}

            <div>
              <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Brand Description / Category Focus
              </label>
              <textarea
                rows={2}
                placeholder="Brief summary of your product line, catalog scope, and authorized distribution channels..."
                value={aboutText}
                onChange={(e) => setAboutText(e.target.value)}
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
                  backgroundColor: '#2563eb',
                  color: '#ffffff',
                  fontWeight: 600,
                  fontSize: '0.875rem',
                  cursor: loading ? 'not-allowed' : 'pointer',
                  opacity: loading ? 0.7 : 1,
                }}
              >
                {loading ? 'Submitting Application...' : 'Submit Application'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};
