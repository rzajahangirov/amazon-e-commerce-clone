import { GmvTrajectoryChart } from '../components/dashboard/GmvTrajectoryChart';
import { useAdminAnalytics } from '../hooks/useAdminAnalytics';
import { formatCompactCurrency, formatCount, formatCurrency } from '../utils/format';

export function AdminDashboardPage() {
  const { data, loading, error, refetch } = useAdminAnalytics();

  if (loading && !data) {
    return <div className="state-message loading">Loading executive analytics…</div>;
  }

  if (error && !data) {
    return (
      <div className="state-message error">
        {error}
        <button type="button" onClick={() => void refetch()} style={{ marginLeft: '0.75rem' }}>
          Retry
        </button>
      </div>
    );
  }

  if (!data) {
    return null;
  }

  return (
    <>
      <header className="page-header">
        <div>
          <h1>Executive Dashboard</h1>
          <p>Real-time platform GMV, order volume, and brand registry workload.</p>
        </div>
        <button type="button" onClick={() => void refetch()}>
          Refresh
        </button>
      </header>

      <section className="kpi-grid">
        <article className="kpi-card">
          <small>Total GMV</small>
          <strong>{formatCompactCurrency(Number(data.totalGMV))}</strong>
          <div className="kpi-meta">{formatCurrency(Number(data.totalGMV))} lifetime</div>
        </article>
        <article className="kpi-card">
          <small>Total Orders</small>
          <strong>{formatCount(data.totalOrdersCount)}</strong>
        </article>
        <article className="kpi-card">
          <small>Active Buyers</small>
          <strong>{formatCount(data.totalActiveUsers)}</strong>
        </article>
        <article className="kpi-card">
          <small>Verified Sellers</small>
          <strong>{formatCount(data.totalActiveSellers)}</strong>
        </article>
        <article className="kpi-card">
          <small>Brand Registry</small>
          <strong>{formatCount(data.totalActiveBrands)}</strong>
          <span className="kpi-badge">
            {formatCount(data.totalPendingBrandApplications)} Pending
          </span>
        </article>
      </section>

      <section className="dashboard-grid">
        <article className="panel">
          <div className="panel-header">
            <h2>GMV Trajectory &amp; Fulfillment Volume</h2>
            <span className="kpi-meta">Last 30 days (daily GMV)</span>
          </div>
          <GmvTrajectoryChart data={data.dailyGmvTrajectory ?? []} />
        </article>

        <article className="panel">
          <h2>Top Performing Brands</h2>
          <table className="data-table">
            <thead>
              <tr>
                <th>Brand</th>
                <th>Revenue</th>
              </tr>
            </thead>
            <tbody>
              {(data.topPerformingBrands ?? []).slice(0, 6).map((brand) => (
                <tr key={brand.id}>
                  <td>{brand.name}</td>
                  <td>{formatCurrency(Number(brand.revenue))}</td>
                </tr>
              ))}
              {(data.topPerformingBrands ?? []).length === 0 && (
                <tr>
                  <td colSpan={2}>No brand revenue data yet.</td>
                </tr>
              )}
            </tbody>
          </table>
        </article>
      </section>
    </>
  );
}
