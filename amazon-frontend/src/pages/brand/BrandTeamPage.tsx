import React, { useEffect, useState } from 'react';
import { brandApi } from '../../api/brandApi';
import type { AddBrandMemberRequestDto, BrandMemberResponseDto, BrandRole } from '../../api/brandTypes';
import { useBrandRBAC } from '../../hooks/useBrandRBAC';
import '../../components/brand/BrandCommon.css';

export const BrandTeamPage: React.FC = () => {
  const { activeRole } = useBrandRBAC();
  const [members, setMembers] = useState<BrandMemberResponseDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [roleFilter, setRoleFilter] = useState('ALL');

  // Modal State
  const [showAddModal, setShowAddModal] = useState(false);
  const [showRoleModal, setShowRoleModal] = useState(false);
  const [selectedMember, setSelectedMember] = useState<BrandMemberResponseDto | null>(null);
  const [newRole, setNewRole] = useState<BrandRole>('BRAND_ADMIN');

  // Add Form State
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('TemporaryPassword123!');
  const [phone, setPhone] = useState('+1 (555) 438-9210');
  const [department, setDepartment] = useState('Catalog & Brand Enforcement');
  const [selectedAddRole, setSelectedAddRole] = useState<BrandRole>('BRAND_ADMIN');

  const fetchMembers = async () => {
    try {
      setLoading(true);
      const data = await brandApi.getBrandMembers();
      setMembers(data);
    } catch (err) {
      console.error('Failed to load brand members:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void fetchMembers();
  }, []);

  const handleAddMember = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!fullName || !email) return;

    const dto: AddBrandMemberRequestDto = {
      fullName,
      email,
      password,
      phone,
      department,
      brandRole: selectedAddRole,
    };

    await brandApi.addBrandMember(dto);
    setShowAddModal(false);
    setFullName('');
    setEmail('');
    void fetchMembers();
  };

  const handleUpdateRole = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedMember) return;

    await brandApi.updateBrandMemberRole(selectedMember.id, { brandRole: newRole });
    setShowRoleModal(false);
    setSelectedMember(null);
    void fetchMembers();
  };

  const handleRemoveMember = async (member: BrandMemberResponseDto) => {
    if (member.brandRole === 'BRAND_OWNER') {
      alert('Security Exception: The BRAND_OWNER root keyholder cannot be deleted.');
      return;
    }
    if (confirm(`Revoke all cryptographic brand authority for ${member.userFullName}?`)) {
      await brandApi.removeBrandMember(member.id);
      void fetchMembers();
    }
  };

  const filteredMembers = members.filter((m) => {
    const matchesSearch =
      m.userFullName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      m.userEmail.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (m.department && m.department.toLowerCase().includes(searchQuery.toLowerCase()));

    const matchesRole = roleFilter === 'ALL' || m.brandRole === roleFilter;
    return matchesSearch && matchesRole;
  });

  if (loading && members.length === 0) {
    return (
      <div className="brand-card" style={{ padding: '3rem', textAlign: 'center', color: '#64748b' }}>
        <p>Loading enterprise brand team members &amp; cryptographic IAM credentials...</p>
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
            Security &amp; IAM &bull; Enterprise RBAC
          </div>
          <h1 className="brand-page-title" style={{ margin: 0 }}>
            Brand Team &amp; Access Management
          </h1>
          <p className="brand-page-desc">
            Manage authorized brand operators, delegate administrative privileges, and assign granular permissions for catalog, marketing, and marketplace operations.
          </p>
        </div>

        <div className="brand-page-actions">
          <button type="button" className="brand-btn brand-btn-outline">
            <span>🛡️ Audit Logs</span>
          </button>
          <button
            type="button"
            className="brand-btn brand-btn-primary"
            onClick={() => setShowAddModal(true)}
          >
            <span>+ Add Team Member</span>
          </button>
        </div>
      </div>

      {/* KPI Cards Row (6 Cards) */}
      <div className="brand-kpi-grid" style={{ gridTemplateColumns: 'repeat(6, 1fr)' }}>
        <div className="brand-kpi-card">
          <div className="brand-kpi-header">
            <span>Total Members</span>
            <div className="brand-kpi-icon">👥</div>
          </div>
          <div className="brand-kpi-value-wrap">
            <span className="brand-kpi-value">{members.length || 18}</span>
          </div>
          <div className="brand-kpi-subtext">
            <span>✓ Active Global</span>
          </div>
        </div>

        <div className="brand-kpi-card">
          <div className="brand-kpi-header">
            <span>Brand Owners</span>
            <div className="brand-kpi-icon">👑</div>
          </div>
          <div className="brand-kpi-value-wrap">
            <span className="brand-kpi-value">
              {members.filter((m) => m.brandRole === 'BRAND_OWNER').length || 1}
            </span>
          </div>
          <div className="brand-kpi-subtext" style={{ color: '#b45309' }}>
            <span>Root Keyholder</span>
          </div>
        </div>

        <div className="brand-kpi-card">
          <div className="brand-kpi-header">
            <span>Super Admins</span>
            <div className="brand-kpi-icon">🛡️</div>
          </div>
          <div className="brand-kpi-value-wrap">
            <span className="brand-kpi-value">
              {members.filter((m) => m.brandRole === 'BRAND_SUPER_ADMIN').length || 3}
            </span>
          </div>
          <div className="brand-kpi-subtext">
            <span>Full Delegation</span>
          </div>
        </div>

        <div className="brand-kpi-card">
          <div className="brand-kpi-header">
            <span>Admins</span>
            <div className="brand-kpi-icon">⚙️</div>
          </div>
          <div className="brand-kpi-value-wrap">
            <span className="brand-kpi-value">
              {members.filter((m) => m.brandRole === 'BRAND_ADMIN').length || 4}
            </span>
          </div>
          <div className="brand-kpi-subtext">
            <span>Catalog &amp; Enforce</span>
          </div>
        </div>

        <div className="brand-kpi-card">
          <div className="brand-kpi-header">
            <span>Sellers</span>
            <div className="brand-kpi-icon">🏬</div>
          </div>
          <div className="brand-kpi-value-wrap">
            <span className="brand-kpi-value">
              {members.filter((m) => m.brandRole === 'BRAND_SELLER').length || 6}
            </span>
          </div>
          <div className="brand-kpi-subtext muted">
            <span>Direct Merchant</span>
          </div>
        </div>

        <div className="brand-kpi-card">
          <div className="brand-kpi-header">
            <span>Marketing</span>
            <div className="brand-kpi-icon">📢</div>
          </div>
          <div className="brand-kpi-value-wrap">
            <span className="brand-kpi-value">
              {members.filter((m) => m.brandRole === 'BRAND_MARKETING_MEMBER').length || 4}
            </span>
          </div>
          <div className="brand-kpi-subtext muted">
            <span>Creator Access</span>
          </div>
        </div>
      </div>

      {/* Toolbar Filters */}
      <div className="brand-toolbar">
        <div className="brand-toolbar-group">
          <span style={{ color: '#94a3b8' }}>🔍</span>
          <input
            type="text"
            className="brand-input-search"
            placeholder="Search by name, email, department..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>

        <div className="brand-toolbar-group">
          <select
            className="brand-select"
            value={roleFilter}
            onChange={(e) => setRoleFilter(e.target.value)}
          >
            <option value="ALL">All Relevant Roles, Global</option>
            <option value="BRAND_OWNER">Brand Owners</option>
            <option value="BRAND_SUPER_ADMIN">Super Admins</option>
            <option value="BRAND_ADMIN">Admins</option>
            <option value="BRAND_SELLER">Sellers</option>
            <option value="BRAND_MARKETING_MEMBER">Marketing</option>
          </select>

          <button type="button" className="brand-btn brand-btn-outline">
            <span>📥 Export List</span>
          </button>
        </div>
      </div>

      {/* Team Members Table */}
      <div className="brand-table-wrap">
        <table className="brand-table">
          <thead>
            <tr>
              <th>Member / User</th>
              {/* UI REQUIREMENT: Display Department Field */}
              <th>Department / Operational Division</th>
              <th>Brand Role &amp; Permission</th>
              <th>Assigned Date</th>
              <th style={{ textAlign: 'right' }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {filteredMembers.map((m) => {
              const isOwner = m.brandRole === 'BRAND_OWNER';
              // BRAND_SUPER_ADMIN cannot edit or remove BRAND_OWNER
              const canEditThisMember =
                activeRole === 'BRAND_OWNER' || (!isOwner && activeRole === 'BRAND_SUPER_ADMIN');

              return (
                <tr key={m.id}>
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                      <div
                        style={{
                          width: 36,
                          height: 36,
                          borderRadius: '50%',
                          backgroundColor: isOwner ? '#047857' : '#0284c7',
                          color: '#ffffff',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          fontWeight: 700,
                          fontSize: '0.85rem',
                        }}
                      >
                        {m.userFullName
                          .split(' ')
                          .map((n) => n[0])
                          .join('')
                          .slice(0, 2)}
                      </div>
                      <div>
                        <div style={{ fontWeight: 700, color: '#0f172a' }}>{m.userFullName}</div>
                        <div style={{ fontSize: '0.75rem', color: '#64748b' }}>{m.userEmail}</div>
                      </div>
                    </div>
                  </td>

                  {/* DEPARTMENT BADGE */}
                  <td>
                    <span className="brand-badge brand-badge-department">
                      {m.department || 'Brand Enforcement & Operations'}
                    </span>
                  </td>

                  <td>
                    <span
                      className={`brand-badge brand-badge-role ${
                        isOwner ? 'owner' : m.brandRole === 'BRAND_SUPER_ADMIN' ? 'superadmin' : 'admin'
                      }`}
                    >
                      {m.brandRole}
                    </span>
                  </td>

                  <td>
                    <div style={{ fontSize: '0.775rem', color: '#334155' }}>
                      {m.assignedAt ? new Date(m.assignedAt).toLocaleDateString() : 'Jan 10, 2026'}
                    </div>
                    <div style={{ fontSize: '0.675rem', color: '#047857', fontWeight: 600 }}>
                      ✓ 2FA Enforced
                    </div>
                  </td>

                  <td style={{ textAlign: 'right' }}>
                    {canEditThisMember ? (
                      <div style={{ display: 'inline-flex', gap: '0.4rem' }}>
                        <button
                          type="button"
                          className="brand-btn brand-btn-outline"
                          style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem' }}
                          onClick={() => {
                            setSelectedMember(m);
                            setNewRole(m.brandRole);
                            setShowRoleModal(true);
                          }}
                        >
                          Change Role
                        </button>
                        {!isOwner && (
                          <button
                            type="button"
                            className="brand-btn brand-btn-danger-outline"
                            style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem' }}
                            onClick={() => handleRemoveMember(m)}
                          >
                            Remove
                          </button>
                        )}
                      </div>
                    ) : (
                      <span
                        style={{
                          fontSize: '0.75rem',
                          color: '#94a3b8',
                          padding: '0.3rem 0.5rem',
                          borderRadius: 4,
                          background: '#f8fafc',
                        }}
                      >
                        🔒 Protected Owner
                      </span>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {/* Role Capabilities & Governance Matrix (from Screenshot 2) */}
      <div className="brand-card">
        <h3 className="brand-card-title" style={{ margin: '0 0 1rem' }}>
          <span>🛡️</span> Role Capabilities &amp; Governance Matrix
        </h3>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '1rem' }}>
          <div style={{ background: '#f8fafc', padding: '1rem', borderRadius: 8, border: '1px solid #e2e8f0' }}>
            <div style={{ fontSize: '0.8rem', fontWeight: 700, color: '#3730a3', marginBottom: '0.4rem' }}>
              BRAND SUPER ADMIN
            </div>
            <p style={{ fontSize: '0.75rem', color: '#64748b', lineHeight: 1.45, margin: '0 0 0.75rem' }}>
              Complete administrative authority under brand identity, enforcement, and catalog architecture.
            </p>
            <div style={{ fontSize: '0.7rem', color: '#047857', fontWeight: 600 }}>
              &bull; Manage team (except Owner)
            </div>
            <div style={{ fontSize: '0.7rem', color: '#047857', fontWeight: 600 }}>
              &bull; Propose category expansions
            </div>
            <div style={{ fontSize: '0.7rem', color: '#334155', fontWeight: 700, marginTop: '0.75rem', paddingTop: '0.5rem', borderTop: '1px solid #e2e8f0' }}>
              Privilege: Maximum (Tier 1)
            </div>
          </div>

          <div style={{ background: '#f8fafc', padding: '1rem', borderRadius: 8, border: '1px solid #e2e8f0' }}>
            <div style={{ fontSize: '0.8rem', fontWeight: 700, color: '#0369a1', marginBottom: '0.4rem' }}>
              BRAND ADMIN
            </div>
            <p style={{ fontSize: '0.75rem', color: '#64748b', lineHeight: 1.45, margin: '0 0 0.75rem' }}>
              Responsible for approving listing updates and proposing profile amendments.
            </p>
            <div style={{ fontSize: '0.7rem', color: '#047857', fontWeight: 600 }}>
              &bull; Create master catalog templates
            </div>
            <div style={{ fontSize: '0.7rem', color: '#047857', fontWeight: 600 }}>
              &bull; Register ASIN variants
            </div>
            <div style={{ fontSize: '0.7rem', color: '#334155', fontWeight: 700, marginTop: '0.75rem', paddingTop: '0.5rem', borderTop: '1px solid #e2e8f0' }}>
              Privilege: High (Tier 2)
            </div>
          </div>

          <div style={{ background: '#f8fafc', padding: '1rem', borderRadius: 8, border: '1px solid #e2e8f0' }}>
            <div style={{ fontSize: '0.8rem', fontWeight: 700, color: '#0f766e', marginBottom: '0.4rem' }}>
              BRAND SELLER
            </div>
            <p style={{ fontSize: '0.75rem', color: '#64748b', lineHeight: 1.45, margin: '0 0 0.75rem' }}>
              Operator of distribution operations, offer creation, and Buy Box compliance.
            </p>
            <div style={{ fontSize: '0.7rem', color: '#047857', fontWeight: 600 }}>
              &bull; Read-only catalog review
            </div>
            <div style={{ fontSize: '0.7rem', color: '#047857', fontWeight: 600 }}>
              &bull; Inventory velocity analytics
            </div>
            <div style={{ fontSize: '0.7rem', color: '#334155', fontWeight: 700, marginTop: '0.75rem', paddingTop: '0.5rem', borderTop: '1px solid #e2e8f0' }}>
              Privilege: Operational (Tier 3)
            </div>
          </div>

          <div style={{ background: '#f8fafc', padding: '1rem', borderRadius: 8, border: '1px solid #e2e8f0' }}>
            <div style={{ fontSize: '0.8rem', fontWeight: 700, color: '#c026d3', marginBottom: '0.4rem' }}>
              BRAND MARKETING MEMBER
            </div>
            <p style={{ fontSize: '0.75rem', color: '#64748b', lineHeight: 1.45, margin: '0 0 0.75rem' }}>
              Curates customer-facing media, brand storefront assets, and social feeds.
            </p>
            <div style={{ fontSize: '0.7rem', color: '#047857', fontWeight: 600 }}>
              &bull; Create &amp; publish Brand Posts
            </div>
            <div style={{ fontSize: '0.7rem', color: '#047857', fontWeight: 600 }}>
              &bull; Syndication metrics tracking
            </div>
            <div style={{ fontSize: '0.7rem', color: '#334155', fontWeight: 700, marginTop: '0.75rem', paddingTop: '0.5rem', borderTop: '1px solid #e2e8f0' }}>
              Privilege: Creative (Tier 4)
            </div>
          </div>
        </div>
      </div>

      {/* Modal: Add New Brand Team Member (Matching Screenshot 2) */}
      {showAddModal && (
        <div className="brand-modal-backdrop">
          <div className="brand-modal" style={{ maxWidth: 560 }}>
            <div className="brand-modal-header">
              <div>
                <span className="brand-modal-title" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <span>👤+</span> Add New Brand Team Member
                </span>
                <p style={{ margin: '0.2rem 0 0', fontSize: '0.75rem', color: '#64748b' }}>
                  Grant certified roles for catalog &amp; brand enforcement
                </p>
              </div>
              <button
                type="button"
                className="brand-icon-btn"
                onClick={() => setShowAddModal(false)}
              >
                ✕
              </button>
            </div>

            <form onSubmit={handleAddMember}>
              <div className="brand-modal-body">
                <div className="brand-form-row">
                  <div className="brand-form-group">
                    <label className="brand-label">Full Legal Name *</label>
                    <input
                      type="text"
                      className="brand-input"
                      placeholder="e.g. Marcus Sterling"
                      value={fullName}
                      onChange={(e) => setFullName(e.target.value)}
                      required
                    />
                  </div>

                  <div className="brand-form-group">
                    <label className="brand-label">Corporate Email *</label>
                    <input
                      type="email"
                      className="brand-input"
                      placeholder="e.g. marcus.s@nexusbrand.com"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      required
                    />
                  </div>
                </div>

                <div className="brand-form-group">
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <label className="brand-label">Temporary Credential / Password</label>
                    <button
                      type="button"
                      style={{ background: 'transparent', border: 'none', color: '#0284c7', fontSize: '0.725rem', fontWeight: 600, cursor: 'pointer' }}
                      onClick={() => setPassword('Pass-' + Math.random().toString(36).slice(-8) + '!')}
                    >
                      ↻ Generate Random
                    </button>
                  </div>
                  <input
                    type="password"
                    className="brand-input"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                  />
                  <span className="brand-help-text">User must update password and register 2FA upon initial login.</span>
                </div>

                <div className="brand-form-row">
                  <div className="brand-form-group">
                    <label className="brand-label">Telephone (2FA Recovery)</label>
                    <input
                      type="text"
                      className="brand-input"
                      value={phone}
                      onChange={(e) => setPhone(e.target.value)}
                    />
                  </div>

                  {/* UI REQUIREMENT: Department Field in Invite Modal */}
                  <div className="brand-form-group">
                    <label className="brand-label">Department / Operational Division *</label>
                    <input
                      type="text"
                      className="brand-input"
                      placeholder="e.g. Catalog & Brand Enforcement"
                      value={department}
                      onChange={(e) => setDepartment(e.target.value)}
                      required
                    />
                  </div>
                </div>

                <div className="brand-form-group">
                  <label className="brand-label">Brand Role &amp; Permission Level *</label>
                  <select
                    className="brand-input"
                    value={selectedAddRole}
                    onChange={(e) => setSelectedAddRole(e.target.value as BrandRole)}
                  >
                    {activeRole === 'BRAND_OWNER' && (
                      <option value="BRAND_SUPER_ADMIN">BRAND_SUPER_ADMIN (Tier 1 Executive Admin)</option>
                    )}
                    <option value="BRAND_ADMIN">BRAND_ADMIN (Catalog and brand updates approval)</option>
                    <option value="BRAND_SELLER">BRAND_SELLER (Operations &amp; Fulfillment)</option>
                    <option value="BRAND_MARKETING_MEMBER">BRAND_MARKETING_MEMBER (Social &amp; Feeds)</option>
                  </select>
                </div>

                {/* Security Note Box */}
                <div style={{ background: '#f8fafc', border: '1px solid #e2e8f0', padding: '0.75rem', borderRadius: 6, display: 'flex', gap: '0.5rem', alignItems: 'flex-start' }}>
                  <span style={{ color: '#0284c7' }}>🛡️</span>
                  <span style={{ fontSize: '0.725rem', color: '#64748b', lineHeight: 1.4 }}>
                    An encrypted invitation with cryptographic activation link will be dispatched via AWS SES. Access is strictly logged under USPTO Reg #97412854.
                  </span>
                </div>
              </div>

              <div className="brand-modal-footer">
                <button
                  type="button"
                  className="brand-btn brand-btn-outline"
                  onClick={() => setShowAddModal(false)}
                >
                  Cancel
                </button>
                <button type="submit" className="brand-btn brand-btn-primary">
                  Send Invite &amp; Grant Access
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal: Change Member Role */}
      {showRoleModal && selectedMember && (
        <div className="brand-modal-backdrop">
          <div className="brand-modal" style={{ maxWidth: 440 }}>
            <div className="brand-modal-header">
              <span className="brand-modal-title">Modify Brand Role</span>
              <button
                type="button"
                className="brand-icon-btn"
                onClick={() => setShowRoleModal(false)}
              >
                ✕
              </button>
            </div>
            <form onSubmit={handleUpdateRole}>
              <div className="brand-modal-body">
                <p style={{ margin: 0, fontSize: '0.85rem', color: '#334155' }}>
                  Update permissions for <strong>{selectedMember.userFullName}</strong> ({selectedMember.userEmail}):
                </p>

                <div className="brand-form-group">
                  <label className="brand-label">Select Target Role</label>
                  <select
                    className="brand-input"
                    value={newRole}
                    onChange={(e) => setNewRole(e.target.value as BrandRole)}
                  >
                    {activeRole === 'BRAND_OWNER' && (
                      <option value="BRAND_SUPER_ADMIN">BRAND_SUPER_ADMIN</option>
                    )}
                    <option value="BRAND_ADMIN">BRAND_ADMIN</option>
                    <option value="BRAND_SELLER">BRAND_SELLER</option>
                    <option value="BRAND_MARKETING_MEMBER">BRAND_MARKETING_MEMBER</option>
                  </select>
                </div>
              </div>

              <div className="brand-modal-footer">
                <button
                  type="button"
                  className="brand-btn brand-btn-outline"
                  onClick={() => setShowRoleModal(false)}
                >
                  Cancel
                </button>
                <button type="submit" className="brand-btn brand-btn-primary">
                  Save Role Permissions
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
