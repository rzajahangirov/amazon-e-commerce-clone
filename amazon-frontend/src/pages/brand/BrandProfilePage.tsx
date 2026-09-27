import React, { useEffect, useState } from 'react';
import { brandApi } from '../../api/brandApi';
import type {
  BrandProfileResponseDto,
  BrandUpdateRequestResponseDto,
  CreateBrandUpdateRequestDto,
} from '../../api/brandTypes';
import { useBrandRBAC } from '../../hooks/useBrandRBAC';
import '../../components/brand/BrandCommon.css';

export const BrandProfilePage: React.FC = () => {
  const { permissions, activeRole } = useBrandRBAC();
  const [profile, setProfile] = useState<BrandProfileResponseDto | null>(null);
  const [history, setHistory] = useState<BrandUpdateRequestResponseDto[]>([]);
  const [loading, setLoading] = useState(true);

  // Proposal form state
  const [proposedName, setProposedName] = useState('Nexus Innovations Global Technologies Inc.');
  const [proposedTrademarkNo, setProposedTrademarkNo] = useState('USPTO Reg #97412854 (Amended Serial #)');
  const [officialLegalJustification, setOfficialLegalJustification] = useState(
    'Corporate Reincorporation / Legal Name Change',
  );
  const [proposedLogoUrl, setProposedLogoUrl] = useState(
    'https://cdn.nexusbrand.com/assets/nexus-v2-horizontal-dark.svg',
  );
  const [proposedAbout, setProposedAbout] = useState(
    'Nexus Innovations Global Technologies Inc. is a premier designer and manufacturer of consumer high-density power systems, smart wearable optics, and zero-latency wireless transmission hardware. Established in 2018 with principal engineering laboratories in Seattle, WA. All authorized hardware is backed by our comprehensive 24-Month Unified Global Replacement Warranty and cryptographically serialized authentic anti-counterfeit labels.',
  );
  const [submitting, setSubmitting] = useState(false);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  const loadData = async () => {
    try {
      setLoading(true);
      const [profData, histData] = await Promise.all([
        brandApi.getBrandProfile(),
        brandApi.getUpdateRequests(),
      ]);
      setProfile(profData);
      setHistory(histData);
    } catch (err) {
      console.error('Failed to load profile & update history:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    let ignore = false;
    const run = async () => {
      try {
        const [profData, histData] = await Promise.all([
          brandApi.getBrandProfile(),
          brandApi.getUpdateRequests(),
        ]);
        if (!ignore) {
          setProfile(profData);
          setHistory(histData);
        }
      } catch (err) {
        console.error('Failed to load profile & update history:', err);
      } finally {
        if (!ignore) {
          setLoading(false);
        }
      }
    };
    void run();
    return () => {
      ignore = true;
    };
  }, []);

  const handleSubmitProposal = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!proposedName || !officialLegalJustification) return;

    setSubmitting(true);
    try {
      const dto: CreateBrandUpdateRequestDto = {
        proposedBrandName: proposedName,
        proposedTrademarkNo,
        proposedLogoUrl,
        proposedAboutText: proposedAbout,
        officialLegalJustification,
      };

      await brandApi.submitUpdateRequest(dto);
      setSuccessMsg('Profile change request successfully submitted for Platform Brand Governance Admin review!');
      void loadData();
    } catch (err) {
      console.error('Error submitting proposal:', err);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading && !profile) {
    return (
      <div className="brand-card" style={{ padding: '3rem', textAlign: 'center', color: '#64748b' }}>
        <p>Loading official brand registry records &amp; trademark governance ledger...</p>
      </div>
    );
  }

  return (
    <div className="brand-page-container">
      {/* Page Header */}
      <div className="brand-page-header">
        <div className="brand-page-title-wrap">
          <div className="brand-page-kicker">
            <span style={{ width: 8, height: 8, borderRadius: '50%', backgroundColor: '#0284c7' }} />
            Entity ID: NX-90218-US &bull; Principal Registry
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <h1 className="brand-page-title" style={{ margin: 0 }}>
              Brand Profile &amp; Trademark Governance
            </h1>
            <span className="brand-badge brand-badge-locked">
              &bull; ACTIVE &amp; VERIFIED
            </span>
          </div>
          <p className="brand-page-desc">
            Official brand entity records, trademark documentation, and authorized administrative modification proposals.
          </p>
        </div>

        <div className="brand-page-actions">
          <a
            href="#propose-section"
            className="brand-btn brand-btn-primary"
            style={{ textDecoration: 'none' }}
          >
            <span>📝 Propose Changes</span>
          </a>
        </div>
      </div>

      {/* Pending Admin Review Banner (from Screenshot 6) */}
      <div
        style={{
          background: 'linear-gradient(90deg, #f0fdf4 0%, #e0f2fe 100%)',
          border: '1px solid #bae6fd',
          borderRadius: 8,
          padding: '1rem 1.25rem',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.85rem' }}>
          <span style={{ fontSize: '1.5rem' }}>📑</span>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <strong style={{ color: '#0369a1', fontSize: '0.875rem' }}>
                Pending Platform Admin Review: Request #CR-2024-8819
              </strong>
              <span className="brand-badge brand-badge-review" style={{ fontSize: '0.675rem' }}>
                Class 09 Scope Expansion
              </span>
            </div>
            <div style={{ fontSize: '0.75rem', color: '#475569', marginTop: '0.15rem' }}>
              Submitted on Oct 28, 2024 by Elena Vance. Estimated review turnaround: 3–5 business days.
            </div>
          </div>
        </div>

        <div style={{ textAlign: 'right' }}>
          <div style={{ fontSize: '0.675rem', color: '#64748b', textTransform: 'uppercase', fontWeight: 600 }}>Queue Position</div>
          <div style={{ fontSize: '0.85rem', fontWeight: 700, color: '#0f172a' }}>#14 of 92 pending</div>
        </div>
      </div>

      {/* Current Live Brand Registry Records (3 Panels from Screenshot 6) */}
      <div className="brand-card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <span style={{ fontSize: '1.2rem' }}>🛡️</span>
            <div>
              <h3 className="brand-card-title" style={{ margin: 0 }}>Current Live Brand Registry Records</h3>
              <p style={{ margin: 0, fontSize: '0.75rem', color: '#64748b' }}>
                Cryptographically signed registry ledger synchronized with USPTO TSDR network
              </p>
            </div>
          </div>
          <span style={{ fontSize: '0.7rem', color: '#047857', fontWeight: 600 }}>
            &bull; LAST LEDGER SYNC: 14 MIN AGO
          </span>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 1fr) minmax(0, 1.2fr) minmax(0, 1fr)', gap: '1.5rem' }}>
          {/* Panel 1: Primary Registered Mark */}
          <div style={{ background: '#f8fafc', padding: '1rem', borderRadius: 8, border: '1px solid #e2e8f0' }}>
            <div style={{ fontSize: '0.675rem', fontWeight: 700, color: '#64748b', textTransform: 'uppercase', marginBottom: '0.75rem' }}>
              Primary Registered Mark
            </div>

            <div style={{ textAlign: 'center', padding: '1rem', background: '#ffffff', borderRadius: 6, border: '1px solid #e2e8f0', marginBottom: '1rem' }}>
              <div
                style={{
                  width: 48,
                  height: 48,
                  margin: '0 auto 0.5rem',
                  backgroundColor: '#0f172a',
                  borderRadius: 8,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: '#38bdf8',
                  fontSize: '1.5rem',
                }}
              >
                🛡️
              </div>
              <strong style={{ display: 'block', fontSize: '0.9rem', color: '#0f172a' }}>
                {profile?.brand.name || 'Nexus Innovations Global'}
              </strong>
              <span style={{ fontSize: '0.7rem', color: '#64748b' }}>Master Brand Nomenclature (SVG Vector)</span>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.45rem', fontSize: '0.775rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: '#64748b' }}>Account Status:</span>
                <span style={{ color: '#047857', fontWeight: 700 }}>&bull; ACTIVE</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: '#64748b' }}>Primary Brand Owner:</span>
                <span style={{ fontWeight: 600, color: '#0f172a' }}>{profile?.brand.ownerName || 'Elena Vance'}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: '#64748b' }}>Contact Email:</span>
                <span style={{ color: '#0284c7' }}>elena.vance@nexusbrand.com</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '0.4rem', paddingTop: '0.4rem', borderTop: '1px solid #e2e8f0' }}>
                <span style={{ fontWeight: 700, color: '#334155' }}>GLOBAL ASIN REACH:</span>
                <span style={{ fontWeight: 800, color: '#0f172a' }}>1,482 Listings</span>
              </div>
            </div>
          </div>

          {/* Panel 2: Federal Registration Meta */}
          <div style={{ background: '#f8fafc', padding: '1rem', borderRadius: 8, border: '1px solid #e2e8f0' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
              <span style={{ fontSize: '0.675rem', fontWeight: 700, color: '#64748b', textTransform: 'uppercase' }}>
                Federal Registration Meta
              </span>
              <a href="#uspto" style={{ fontSize: '0.7rem', color: '#0284c7', fontWeight: 600, textDecoration: 'none' }} onClick={(e) => e.preventDefault()}>
                USPTO TSDR Portal ↗
              </a>
            </div>

            <div style={{ background: '#ffffff', padding: '0.75rem', borderRadius: 6, border: '1px solid #e2e8f0', marginBottom: '0.75rem' }}>
              <div style={{ fontSize: '0.7rem', color: '#64748b' }}>Registration Number</div>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: '0.15rem' }}>
                <code style={{ fontSize: '0.85rem', fontWeight: 800, color: '#0f172a' }}>
                  USPTO Reg #{profile?.brand.trademarkRegistrationNumber || '97412854'}
                </code>
                <span style={{ cursor: 'pointer', fontSize: '0.8rem' }} title="Copy Reg Number">📋</span>
              </div>
            </div>

            <div style={{ fontSize: '0.75rem', color: '#334155', lineHeight: 1.45, marginBottom: '0.75rem' }}>
              <strong>Nice Classification:</strong> Class 09 (Consumer Electronics), Class 14 (Smart Horology), Class 28 (Interactive Hardware)
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem', fontSize: '0.75rem', marginBottom: '0.75rem' }}>
              <div>
                <span style={{ color: '#64748b', display: 'block', fontSize: '0.7rem' }}>Jurisdiction</span>
                <strong style={{ color: '#0f172a' }}>United States (US)</strong>
              </div>
              <div>
                <span style={{ color: '#64748b', display: 'block', fontSize: '0.7rem' }}>Mark Structure</span>
                <strong style={{ color: '#0f172a' }}>Word &amp; Design Mark</strong>
              </div>
              <div>
                <span style={{ color: '#64748b', display: 'block', fontSize: '0.7rem' }}>Grant Date</span>
                <strong style={{ color: '#0f172a' }}>June 14, 2021</strong>
              </div>
              <div>
                <span style={{ color: '#64748b', display: 'block', fontSize: '0.7rem' }}>Next Renewal</span>
                <strong style={{ color: '#047857' }}>June 14, 2031</strong>
              </div>
            </div>

            <div style={{ fontSize: '0.7rem', color: '#64748b', borderTop: '1px solid #e2e8f0', paddingTop: '0.4rem' }}>
              ✓ Standard Character Claim: Section 8 &amp; 15 Affidavits Synchronized
            </div>
          </div>

          {/* Panel 3: Attached Brand Proofs */}
          <div style={{ background: '#f8fafc', padding: '1rem', borderRadius: 8, border: '1px solid #e2e8f0', display: 'flex', flexDirection: 'column' }}>
            <div style={{ fontSize: '0.675rem', fontWeight: 700, color: '#64748b', textTransform: 'uppercase', marginBottom: '0.75rem' }}>
              Attached Brand Proofs
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', flex: 1 }}>
              {[
                { name: 'USPTO_Certificate_97412854.pdf', size: '2.4 MB', type: 'Verified Grant' },
                { name: 'Trademark_Specimen_2024.pdf', size: '5.1 MB', type: 'In-Commerce Proof' },
                { name: 'Power_of_Attorney_Nexus.pdf', size: '1.2 MB', type: 'Legal Counsel' },
              ].map((doc) => (
                <div
                  key={doc.name}
                  style={{
                    background: '#ffffff',
                    border: '1px solid #e2e8f0',
                    padding: '0.5rem 0.75rem',
                    borderRadius: 6,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <span style={{ color: '#dc2626', fontSize: '1rem' }}>📄</span>
                    <div>
                      <div style={{ fontSize: '0.75rem', fontWeight: 600, color: '#0f172a' }}>{doc.name}</div>
                      <div style={{ fontSize: '0.675rem', color: '#64748b' }}>{doc.size} &bull; {doc.type}</div>
                    </div>
                  </div>
                  <span style={{ fontSize: '0.75rem', color: '#047857', fontWeight: 600 }}>✓ Verified</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Propose Profile Changes for Admin Review (Form from Screenshot 6) */}
      <div id="propose-section" className="brand-card">
        <div className="brand-card-header">
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <span style={{ fontSize: '1.2rem' }}>📝</span>
              <h3 className="brand-card-title" style={{ margin: 0 }}>
                Propose Profile Changes for Admin Review
              </h3>
            </div>
            <p style={{ margin: '0.2rem 0 0', fontSize: '0.75rem', color: '#64748b' }}>
              Submit legally binding amendments to your verified brand registry identity
            </p>
          </div>
          <span style={{ fontSize: '0.7rem', color: '#94a3b8', fontFamily: 'monospace' }}>
            SEC-AMEND-MOD-04
          </span>
        </div>

        {/* Success Alert */}
        {successMsg && (
          <div style={{ background: '#f0fdf4', border: '1px solid #86efac', color: '#166534', padding: '0.75rem 1rem', borderRadius: 6, fontSize: '0.825rem', marginBottom: '1rem' }}>
            ✓ {successMsg}
          </div>
        )}

        {/* Important Governance Notice (from Screenshot 6) */}
        <div
          style={{
            background: '#fffbeb',
            border: '1px solid #fde68a',
            borderRadius: 6,
            padding: '0.85rem 1rem',
            display: 'flex',
            gap: '0.65rem',
            alignItems: 'flex-start',
            marginBottom: '1.25rem',
          }}
        >
          <span style={{ color: '#d97706', fontSize: '1.1rem' }}>⚠️</span>
          <div style={{ fontSize: '0.775rem', color: '#92400e', lineHeight: 1.45 }}>
            <strong>Important Governance Notice:</strong> Changes to brand legal entity, registered name, or trademark certificates require formal review and approval by Amazon Platform Brand Governance Admins before taking effect on live marketplace listings. Any fraudulent or non-conforming submission may trigger automated listing suppression under Registry Protection Rules &sect;14.
          </div>
        </div>

        {/* DYNAMIC RBAC GUARD: Check permissions.canProposeProfileUpdate */}
        {permissions.canProposeProfileUpdate ? (
          <form onSubmit={handleSubmitProposal}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
              <div className="brand-form-row">
                <div className="brand-form-group">
                  <label className="brand-label">Proposed Brand Entity Name *</label>
                  <input
                    type="text"
                    className="brand-input"
                    value={proposedName}
                    onChange={(e) => setProposedName(e.target.value)}
                    required
                  />
                  <span className="brand-help-text">Must match legal Articles of Amendment precisely.</span>
                </div>

                <div className="brand-form-group">
                  <label className="brand-label">Proposed Trademark Registration Number *</label>
                  <input
                    type="text"
                    className="brand-input"
                    value={proposedTrademarkNo}
                    onChange={(e) => setProposedTrademarkNo(e.target.value)}
                    required
                  />
                  <span className="brand-help-text">Include class designations if altering international classes.</span>
                </div>
              </div>

              {/* UI REQUIREMENT: officialLegalJustification field */}
              <div className="brand-form-group">
                <label className="brand-label">Official Legal Justification / Reason *</label>
                <select
                  className="brand-input"
                  value={officialLegalJustification}
                  onChange={(e) => setOfficialLegalJustification(e.target.value)}
                  required
                >
                  <option value="Corporate Reincorporation / Legal Name Change">
                    Corporate Reincorporation / Legal Name Change
                  </option>
                  <option value="Trademark Specimen Expansion">
                    Trademark Specimen Expansion
                  </option>
                  <option value="International Class Addition">
                    International Class Addition
                  </option>
                  <option value="Merger / Acquisition & Brand Unification">
                    Merger / Acquisition &amp; Brand Unification
                  </option>
                  <option value="Official Correction of Typographical Record">
                    Official Correction of Typographical Record
                  </option>
                </select>
                <span className="brand-help-text">
                  Determines verification protocol &amp; SLA priority for Amazon Platform review team.
                </span>
              </div>

              <div className="brand-form-group">
                <label className="brand-label">Proposed Mark Logo Vector / Asset URL *</label>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <input
                    type="url"
                    className="brand-input"
                    value={proposedLogoUrl}
                    onChange={(e) => setProposedLogoUrl(e.target.value)}
                    required
                  />
                  <span
                    style={{
                      flexShrink: 0,
                      fontSize: '0.75rem',
                      color: '#047857',
                      fontWeight: 600,
                      display: 'flex',
                      alignItems: 'center',
                    }}
                  >
                    ✓ SVG Resolved
                  </span>
                </div>
              </div>

              <div className="brand-form-group">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <label className="brand-label">
                    Proposed About Brand Narrative &amp; Certified Warranty Policy *
                  </label>
                  <span style={{ fontSize: '0.7rem', color: '#64748b' }}>
                    Character count: {proposedAbout.length} / 2000
                  </span>
                </div>
                <textarea
                  rows={4}
                  className="brand-textarea"
                  value={proposedAbout}
                  onChange={(e) => setProposedAbout(e.target.value)}
                  required
                />
                <span className="brand-help-text">
                  This narrative displays on your public Amazon Brand Storefront and verified product detail pages.
                </span>
              </div>

              {/* Upload Supporting Evidentiary Documentation */}
              <div className="brand-form-group">
                <label className="brand-label">Upload Supporting Evidentiary Documentation *</label>
                <div
                  style={{
                    border: '2px dashed #cbd5e1',
                    borderRadius: 8,
                    padding: '1.5rem',
                    textAlign: 'center',
                    backgroundColor: '#f8fafc',
                  }}
                >
                  <span style={{ fontSize: '1.8rem', display: 'block', marginBottom: '0.4rem' }}>☁️</span>
                  <strong style={{ display: 'block', fontSize: '0.85rem', color: '#0f172a' }}>
                    USPTO Certificate Amendments, Certificates of Merger, or POA
                  </strong>
                  <span style={{ display: 'block', fontSize: '0.725rem', color: '#64748b', marginTop: '0.25rem' }}>
                    Accepted formats: PDF, TIFF, PNG up to 25MB each. Registrar stamps and seals required.
                  </span>
                </div>
              </div>

              {/* Form Actions */}
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  paddingTop: '1rem',
                  borderTop: '1px solid #e2e8f0',
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.75rem', color: '#64748b' }}>
                  <span>🔒</span>
                  <span>Digitally signed with Brand Owner Key</span>
                </div>

                <div>
                  <button
                    type="submit"
                    className="brand-btn brand-btn-primary"
                    disabled={submitting}
                  >
                    <span>{submitting ? 'Submitting...' : '✓ Submit Profile Change Request for Review'}</span>
                  </button>
                </div>
              </div>
            </div>
          </form>
        ) : (
          <div className="brand-guard-banner">
            <p>
              <strong>Access Guarded:</strong> Logged in as <code>{activeRole}</code>. You do not have permission to submit legal trademark or entity amendments. Only <code>BRAND_OWNER</code>, <code>BRAND_SUPER_ADMIN</code>, and <code>BRAND_ADMIN</code> can propose modifications.
            </p>
          </div>
        )}
      </div>

      {/* Administrative Modification History (from Screenshot 6) */}
      <div className="brand-card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
          <div>
            <h3 className="brand-card-title" style={{ margin: 0 }}>
              <span>📜</span> Administrative Modification History
            </h3>
            <p style={{ margin: '0.2rem 0 0', fontSize: '0.75rem', color: '#64748b' }}>
              Archived record of all previous trademark revisions, name amendments, and ownership validations
            </p>
          </div>
        </div>

        <div className="brand-table-wrap">
          <table className="brand-table">
            <thead>
              <tr>
                <th>Request ID</th>
                <th>Modification Scope</th>
                <th>Initiated By</th>
                <th>Adjudicated Date</th>
                <th>Reviewing Admin</th>
                <th>Outcome</th>
                <th>Certificate</th>
              </tr>
            </thead>
            <tbody>
              {history.map((req) => (
                <tr key={req.id}>
                  <td>
                    <code style={{ fontWeight: 700, color: '#0f172a' }}>{req.id}</code>
                  </td>
                  <td>
                    <div style={{ fontWeight: 600, color: '#0f172a' }}>
                      {req.proposedBrandName || 'Trademark Record Modification'}
                    </div>
                    <div style={{ fontSize: '0.725rem', color: '#64748b' }}>
                      {req.officialLegalJustification || 'Official Reincorporation Amendment'}
                    </div>
                  </td>
                  <td>
                    <div style={{ fontWeight: 600, color: '#334155' }}>Elena Vance</div>
                    <div style={{ fontSize: '0.675rem', color: '#64748b' }}>(Owner)</div>
                  </td>
                  <td>
                    <span style={{ fontSize: '0.775rem' }}>
                      {req.createdAt ? new Date(req.createdAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }) : 'Nov 12, 2023'}
                    </span>
                  </td>
                  <td>
                    <span style={{ fontSize: '0.775rem', color: '#334155' }}>
                      Admin J. Sterling (Amazon BR)
                    </span>
                  </td>
                  <td>
                    {req.status === 'APPROVED' && (
                      <span className="brand-badge brand-badge-locked">&bull; APPROVED</span>
                    )}
                    {req.status === 'PENDING' && (
                      <span className="brand-badge brand-badge-review">&bull; PENDING REVIEW</span>
                    )}
                    {req.status === 'REJECTED' && (
                      <span className="brand-badge brand-badge-danger">&bull; REJECTED</span>
                    )}
                  </td>
                  <td>
                    <span className="brand-badge brand-badge-locked">Verified</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
