import { useMemo, useState } from 'react';
import { useAdminUsers } from '../hooks/useAdminUsers';
import { useDebouncedValue } from '../hooks/useDebouncedValue';
import { formatRoleLabel, userInitials } from '../utils/format';
import { formatTimeAgo } from '../utils/timeAgo';

export function AdminUsersSellersPage() {
  const [emailInput, setEmailInput] = useState('');
  const [role, setRole] = useState('');
  const [activeFilter, setActiveFilter] = useState<'all' | 'active' | 'suspended'>('all');
  const debouncedEmail = useDebouncedValue(emailInput, 300);

  const filters = useMemo(
    () => ({
      email: debouncedEmail || undefined,
      role: role || undefined,
      active:
        activeFilter === 'all' ? undefined : activeFilter === 'active' ? true : false,
      size: 20,
    }),
    [activeFilter, debouncedEmail, role],
  );

  const { users, pagination, loading, error, page, setPage } = useAdminUsers(filters);

  return (
    <>
      <header className="page-header">
        <div>
          <h1>User &amp; Seller Governance</h1>
          <p>Manage customer accounts, seller access, and role assignments.</p>
        </div>
      </header>

      <section className="panel">
        <div className="filters-row">
          <label>
            Email / name filter
            <input
              type="search"
              placeholder="Filter by email..."
              value={emailInput}
              onChange={(event) => setEmailInput(event.target.value)}
            />
          </label>
          <label>
            Role
            <select value={role} onChange={(event) => setRole(event.target.value)}>
              <option value="">All roles</option>
              <option value="ROLE_CUSTOMER">Customer</option>
              <option value="ROLE_SELLER">Seller</option>
              <option value="ROLE_ADMIN">Admin</option>
            </select>
          </label>
          <label>
            Status
            <select
              value={activeFilter}
              onChange={(event) =>
                setActiveFilter(event.target.value as 'all' | 'active' | 'suspended')
              }
            >
              <option value="all">All statuses</option>
              <option value="active">Active</option>
              <option value="suspended">Suspended</option>
            </select>
          </label>
        </div>

        {loading && <div className="state-message loading">Loading users…</div>}
        {error && <div className="state-message error">{error}</div>}

        {!loading && !error && (
          <>
            <table className="data-table">
              <thead>
                <tr>
                  <th>User ID</th>
                  <th>Full name &amp; profile</th>
                  <th>Email</th>
                  <th>Assigned roles</th>
                  <th>Last active</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => {
                  const suspended = user.status === 'SUSPENDED';
                  return (
                    <tr key={user.id} className={suspended ? 'row-suspended' : undefined}>
                      <td>{user.id.slice(0, 8).toUpperCase()}</td>
                      <td>
                        <div className="user-cell">
                          {user.avatarUrl ? (
                            <img
                              className="avatar"
                              src={user.avatarUrl}
                              alt=""
                              referrerPolicy="no-referrer"
                            />
                          ) : (
                            <span className="avatar" aria-hidden>
                              {userInitials(user.fullName)}
                            </span>
                          )}
                          <div>
                            <strong>{user.fullName}</strong>
                          </div>
                        </div>
                      </td>
                      <td>{user.email}</td>
                      <td>
                        {user.roles.map((assignedRole) => (
                          <span key={assignedRole} className="role-pill">
                            {formatRoleLabel(assignedRole)}
                          </span>
                        ))}
                      </td>
                      <td>{formatTimeAgo(user.lastActiveAt)}</td>
                      <td>
                        <span
                          className={`status-pill ${suspended ? 'suspended' : 'active'}`}
                        >
                          <span className="dot" />
                          {user.status === 'ACTIVE' ? 'Active' : 'Suspended'}
                        </span>
                      </td>
                    </tr>
                  );
                })}
                {users.length === 0 && (
                  <tr>
                    <td colSpan={6}>No users matched the current filters.</td>
                  </tr>
                )}
              </tbody>
            </table>

            {pagination && (
              <div className="pagination-bar">
                <span>
                  Showing {users.length} of {pagination.totalElements} registered accounts
                </span>
                <div className="pagination-controls">
                  <button
                    type="button"
                    disabled={page <= 0}
                    onClick={() => setPage((current) => Math.max(0, current - 1))}
                  >
                    Previous
                  </button>
                  <button
                    type="button"
                    disabled={pagination.last}
                    onClick={() => setPage((current) => current + 1)}
                  >
                    Next
                  </button>
                </div>
              </div>
            )}
          </>
        )}
      </section>
    </>
  );
}
