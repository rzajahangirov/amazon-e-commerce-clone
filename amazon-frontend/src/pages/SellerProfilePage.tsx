import { useEffect, useRef, useState } from 'react';
import { useSellerProfile } from '../hooks/useSellerProfile';
import type { UpdateSellerProfileRequest } from '../api/sellerTypes';
import '../components/seller/SellerCommon.css';

export function SellerProfilePage() {
  const { data, loading, saving, error, save } = useSellerProfile();
  const initializedRef = useRef(false);

  const [formData, setFormData] = useState<UpdateSellerProfileRequest>({
    storeName: '',
    supportEmail: '',
    merchantPhone: '',
    returnPolicyUrl: '',
    legalName: '',
    stateTaxPermitNumber: '',
    businessAddress: '',
    bankAccountDetails: '',
  });

  const [isDirty, setIsDirty] = useState(false);
  const [saveSuccess, setSaveSuccess] = useState(false);

  useEffect(() => {
    if (data && !initializedRef.current) {
      initializedRef.current = true;
      const next: UpdateSellerProfileRequest = {
        storeName: data.storeName || '',
        supportEmail: data.supportEmail || '',
        merchantPhone: data.merchantPhone || '',
        returnPolicyUrl: data.returnPolicyUrl || '',
        legalName: data.legalName || '',
        stateTaxPermitNumber: data.stateTaxPermitNumber || '',
        businessAddress: data.businessAddress || '',
        bankAccountDetails: data.bankAccountDetails || '',
      };
      setFormData(next);
      setIsDirty(false);
    }
  }, [data]);

  const handleChange = (field: keyof UpdateSellerProfileRequest, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    setIsDirty(true);
    setSaveSuccess(false);
  };

  const handleDiscard = () => {
    if (data) {
      setFormData({
        storeName: data.storeName || '',
        supportEmail: data.supportEmail || '',
        merchantPhone: data.merchantPhone || '',
        returnPolicyUrl: data.returnPolicyUrl || '',
        legalName: data.legalName || '',
        stateTaxPermitNumber: data.stateTaxPermitNumber || '',
        businessAddress: data.businessAddress || '',
        bankAccountDetails: data.bankAccountDetails || '',
      });
      setIsDirty(false);
    }
  };

  const handleSubmit = async () => {
    const success = await save(formData);
    if (success) {
      setIsDirty(false);
      setSaveSuccess(true);
      setTimeout(() => setSaveSuccess(false), 4000);
    }
  };

  if (loading && !data) {
    return (
      <div className="seller-card" style={{ textAlign: 'center', padding: '3rem' }}>
        <p style={{ color: '#64748b' }}>Loading verified seller credentials and store profile...</p>
      </div>
    );
  }

  return (
    <div>
      {/* Breadcrumb Header */}
      <div className="seller-page-header">
        <div>
          <div style={{ fontSize: '0.72rem', fontWeight: 700, letterSpacing: '0.06em', color: '#64748b', textTransform: 'uppercase', marginBottom: '0.2rem' }}>
            SETTINGS &gt; STORE PROFILE &amp; SETTLEMENT • <span style={{ color: '#2563eb' }}>SECURE CONFIG v4.2</span>
          </div>
          <h1>Store Profile, Tax &amp; Settlement Banking</h1>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', fontSize: '0.75rem', color: '#059669', fontWeight: 600 }}>
          <span className="seller-status-dot" /> SSL 256-BIT ENCRYPTED / SYNC ID: 8820-F7B
        </div>
      </div>

      {saveSuccess && (
        <div style={{ background: '#ecfdf5', border: '1px solid #a7f3d0', color: '#065f46', padding: '0.75rem 1rem', borderRadius: '8px', marginBottom: '1.25rem', fontSize: '0.85rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <span>✓</span>
          <strong>Profile updated successfully.</strong> Changes are synchronized with the Amazon Partner Network.
        </div>
      )}

      {error && (
        <div style={{ background: '#fef2f2', border: '1px solid #fecaca', color: '#b91c1c', padding: '0.75rem 1rem', borderRadius: '8px', marginBottom: '1.25rem', fontSize: '0.85rem' }}>
          <strong>Error saving profile:</strong> {error}
        </div>
      )}

      {/* Store Identity Banner Card */}
      <div className="seller-card" style={{ marginBottom: '1.5rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
          <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
            <div style={{ width: '56px', height: '56px', background: '#eff6ff', borderRadius: '12px', display: 'grid', placeItems: 'center', fontSize: '1.75rem', color: '#2563eb', border: '1px solid #bfdbfe' }}>
              🏪
            </div>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <h2 style={{ margin: 0, fontSize: '1.35rem' }}>{formData.storeName || data?.storeName || 'Apex Retail US LLC'}</h2>
                <span className="seller-badge-green">✓ Verified Merchant</span>
              </div>
              <div style={{ display: 'flex', gap: '0.5rem', marginTop: '0.35rem', alignItems: 'center' }}>
                <span className="seller-badge-blue">Marketplace Level: Professional Enterprise</span>
                <span style={{ fontSize: '0.75rem', color: '#64748b' }}>
                  Brand: <strong>{data?.brandName || 'Apple'}</strong>
                </span>
              </div>
            </div>
          </div>

          <div style={{ background: '#f0fdf4', border: '1px solid #bbf7d0', padding: '0.75rem 1.25rem', borderRadius: '10px', textAlign: 'center' }}>
            <div style={{ fontSize: '1.25rem', fontWeight: 800, color: '#15803d' }}>99.8%</div>
            <div style={{ fontSize: '0.72rem', fontWeight: 700, color: '#166534', textTransform: 'uppercase' }}>Healthy Standing</div>
            <div style={{ fontSize: '0.68rem', color: '#15803d' }}>0 Policy Violations (365d)</div>
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '1rem', marginTop: '1.25rem', paddingTop: '1rem', borderTop: '1px solid #f1f5f9' }}>
          <div>
            <span style={{ fontSize: '0.68rem', fontWeight: 700, color: '#64748b', textTransform: 'uppercase' }}>Seller Account ID</span>
            <div style={{ fontSize: '0.85rem', fontFamily: 'monospace', fontWeight: 600 }}>
              {data?.id?.slice(0, 16) || 'A28948102948'}
            </div>
          </div>
          <div>
            <span style={{ fontSize: '0.68rem', fontWeight: 700, color: '#64748b', textTransform: 'uppercase' }}>Registered Federal EIN</span>
            <div style={{ fontSize: '0.85rem', fontFamily: 'monospace', fontWeight: 600 }}>
              {data?.taxNumber || 'XX-XXX9412'} <span className="seller-badge-green" style={{ fontSize: '0.65rem' }}>IRS Matched</span>
            </div>
          </div>
          <div>
            <span style={{ fontSize: '0.68rem', fontWeight: 700, color: '#64748b', textTransform: 'uppercase' }}>Primary Marketplace</span>
            <div style={{ fontSize: '0.85rem', fontWeight: 600 }}>
              🇺🇸 Amazon.com (United States)
            </div>
          </div>
        </div>
      </div>

      {/* Two Column Layout */}
      <div className="seller-dash-grid">
        <div>
          {/* SEC-01 Public Store Information */}
          <div className="seller-card">
            <div className="seller-card-header">
              <div>
                <h2>🏪 Public Store Information</h2>
                <p className="seller-card-subtitle">
                  Customer-facing storefront identity, buyer support coordinates, and return policies.
                </p>
              </div>
              <span className="seller-badge-blue">SEC - 01</span>
            </div>

            <div className="seller-form-group">
              <label>Store Display Name</label>
              <input
                type="text"
                className="seller-input"
                style={{ width: '100%' }}
                value={formData.storeName || ''}
                onChange={(e) => handleChange('storeName', e.target.value)}
              />
              <div className="seller-form-help">Appears on Buy Box and invoice headers.</div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <div className="seller-form-group">
                <label>Support &amp; Customer Email *</label>
                <input
                  type="email"
                  className="seller-input"
                  style={{ width: '100%' }}
                  value={formData.supportEmail || ''}
                  onChange={(e) => handleChange('supportEmail', e.target.value)}
                  placeholder="support@yourmerchant.com"
                />
                <div className="seller-form-help">SLA guarantee: Responses monitored within 24 hours.</div>
              </div>

              <div className="seller-form-group">
                <label>Merchant Phone Number *</label>
                <input
                  type="tel"
                  className="seller-input"
                  style={{ width: '100%' }}
                  value={formData.merchantPhone || ''}
                  onChange={(e) => handleChange('merchantPhone', e.target.value)}
                  placeholder="+1 (555) 000-0000"
                />
                <div className="seller-form-help">Used exclusively for buyer escalation callbacks.</div>
              </div>
            </div>

            <div className="seller-form-group">
              <label>Customer Return Policy URL *</label>
              <div style={{ display: 'flex', gap: '0.5rem' }}>
                <input
                  type="url"
                  className="seller-input"
                  style={{ flex: 1 }}
                  value={formData.returnPolicyUrl || ''}
                  onChange={(e) => handleChange('returnPolicyUrl', e.target.value)}
                  placeholder="https://yourstore.com/returns-policy"
                />
                <button
                  type="button"
                  className="seller-btn seller-btn-outline"
                  onClick={() => {
                    if (formData.returnPolicyUrl) window.open(formData.returnPolicyUrl, '_blank');
                  }}
                >
                  ↗ Test Link
                </button>
              </div>
              <div className="seller-form-help">Public SLA return terms accessible to marketplace buyers.</div>
            </div>
          </div>

          {/* SEC-02 Business & Tax Details */}
          <div className="seller-card">
            <div className="seller-card-header">
              <div>
                <h2>🏛️ Business &amp; Tax Details</h2>
                <p className="seller-card-subtitle">
                  IRS corporate identity, certified legal entity filings, and state resale exemptions.
                </p>
              </div>
              <span className="seller-badge-blue">SEC - 02</span>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <div className="seller-form-group">
                <label>Registered Business Legal Name *</label>
                <input
                  type="text"
                  className="seller-input"
                  style={{ width: '100%' }}
                  value={formData.legalName || ''}
                  onChange={(e) => handleChange('legalName', e.target.value)}
                  placeholder="e.g. Apex Retail US Operations LLC"
                />
                <div className="seller-form-help">Must strictly match IRS Form SS-4 letter.</div>
              </div>

              <div className="seller-form-group">
                <label>State Tax Reseller Permit Number *</label>
                <input
                  type="text"
                  className="seller-input"
                  style={{ width: '100%' }}
                  value={formData.stateTaxPermitNumber || ''}
                  onChange={(e) => handleChange('stateTaxPermitNumber', e.target.value)}
                  placeholder="e.g. NY-TX-984420-EXP2026"
                />
                <div className="seller-form-help">Exemption certificate on file for multi-state nexus.</div>
              </div>
            </div>

            <div className="seller-form-group">
              <label>Registered Physical Headquarters Address *</label>
              <textarea
                className="seller-input"
                style={{ width: '100%', minHeight: '60px', fontFamily: 'inherit' }}
                value={formData.businessAddress || ''}
                onChange={(e) => handleChange('businessAddress', e.target.value)}
                placeholder="750 3rd Avenue, Suite 1400, New York, NY 10017"
              />
              <div className="seller-form-help">Official physical nexus location (P.O. Boxes not permitted).</div>
            </div>

            <div style={{ background: '#f8fafc', border: '1px solid #e2e8f0', padding: '0.85rem 1rem', borderRadius: '8px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <strong style={{ fontSize: '0.82rem' }}>Federal Tax Identifier (EIN)</strong>
                <div style={{ fontSize: '0.72rem', color: '#64748b' }}>W-9 Status: Verified &amp; On-File (IRS TIN Match Passed)</div>
              </div>
              <span className="seller-badge-green" style={{ fontSize: '0.75rem' }}>✓ Verified</span>
            </div>
          </div>
        </div>

        {/* Right Column: Payout & Settlement */}
        <div>
          <div className="seller-card">
            <div className="seller-card-header">
              <div>
                <h3>💳 Payout &amp; Settlement</h3>
                <p className="seller-card-subtitle">Automated funds distribution &amp; ACH merchant bank vault.</p>
              </div>
              <span className="seller-badge-blue">Vault Encrypted</span>
            </div>

            <div style={{ background: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: '8px', padding: '1rem', marginBottom: '1rem' }}>
              <div style={{ fontSize: '0.72rem', fontWeight: 700, color: '#64748b', textTransform: 'uppercase', display: 'flex', justifyContent: 'space-between' }}>
                <span>Next Disbursement Window</span>
                <span className="seller-badge-green">Bi-weekly ACH</span>
              </div>
              <div style={{ fontSize: '1.6rem', fontWeight: 800, color: '#0f172a', margin: '0.35rem 0' }}>
                $48,210.00
              </div>
              <div style={{ fontSize: '0.75rem', color: '#059669', fontWeight: 600 }}>
                +14.2% vs previous payout
              </div>
              <div style={{ fontSize: '0.7rem', color: '#64748b', marginTop: '0.2rem' }}>
                Scheduled Release: Nov 01, 2024 at 23:59 UTC
              </div>
            </div>

            <div className="seller-form-group">
              <label>Depository Bank Account Details</label>
              <textarea
                className="seller-input"
                style={{ width: '100%', minHeight: '60px', fontFamily: 'monospace', fontSize: '0.8rem' }}
                value={formData.bankAccountDetails || ''}
                onChange={(e) => handleChange('bankAccountDetails', e.target.value)}
                placeholder="Bank: JPMorgan Chase, Routing: 021000021, Acct: ****8492"
              />
              <div className="seller-form-help">Secured through NACHA encrypted network.</div>
            </div>

            <div style={{ background: '#eff6ff', border: '1px solid #bfdbfe', borderRadius: '8px', padding: '0.85rem', marginBottom: '1rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem' }}>
                <span style={{ fontWeight: 600, color: '#1e3a8a' }}>Account Level Reserve (FBA Hold)</span>
                <strong style={{ color: '#1e3a8a' }}>$5,400.00</strong>
              </div>
              <div style={{ fontSize: '0.7rem', color: '#3b82f6', marginTop: '0.25rem' }}>
                10.0% standard reserve held against potential buyer returns and chargeback cycles.
              </div>
            </div>

            <div style={{ fontSize: '0.72rem', color: '#64748b', lineHeight: 1.4, borderTop: '1px solid #f1f5f9', paddingTop: '0.75rem' }}>
              🔒 <strong>Financial Security Protocol:</strong> Any alteration to bank routing initiates an automatic 24-hour verification hold during which pending disbursements are paused.
            </div>
          </div>
        </div>
      </div>

      {/* Floating Save Bar when isDirty */}
      {isDirty && (
        <div
          style={{
            position: 'sticky',
            bottom: '1rem',
            background: '#0f172a',
            color: '#ffffff',
            borderRadius: '10px',
            padding: '1rem 1.5rem',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            boxShadow: '0 10px 25px rgba(0, 0, 0, 0.25)',
            zIndex: 30,
            marginTop: '1.5rem',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <span style={{ color: '#f59e0b', fontSize: '1.2rem' }}>●</span>
            <div>
              <strong style={{ display: 'block', fontSize: '0.88rem' }}>You have unsaved changes in Business &amp; Tax Details</strong>
              <small style={{ color: '#94a3b8', fontSize: '0.75rem' }}>Changes will take effect across the Amazon US Marketplace once committed.</small>
            </div>
          </div>
          <div style={{ display: 'flex', gap: '0.75rem' }}>
            <button
              type="button"
              className="seller-btn seller-btn-outline"
              style={{ background: 'transparent', color: '#cbd5e1', borderColor: '#475569' }}
              onClick={handleDiscard}
            >
              Discard Changes
            </button>
            <button
              type="button"
              className="seller-btn seller-btn-primary"
              disabled={saving}
              onClick={() => void handleSubmit()}
            >
              {saving ? 'Saving...' : '💾 Save Profile Changes'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
