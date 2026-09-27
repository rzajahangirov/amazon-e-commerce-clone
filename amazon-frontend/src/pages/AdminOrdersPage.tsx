import { useMemo, useState } from 'react';
import { useAdminOrders } from '../hooks/useAdminOrders';
import type { OrderStatus } from '../api/types';
import { defaultOrderDateRange } from '../utils/dateRange';
import { formatCurrency } from '../utils/format';

const STATUS_OPTIONS: Array<OrderStatus | ''> = [
  '',
  'PENDING',
  'CONFIRMED',
  'SHIPPED',
  'DELIVERED',
  'CANCELLED',
  'REFUNDED',
];

export function AdminOrdersPage() {
  const initialRange = useMemo(() => defaultOrderDateRange(30), []);
  const [startDate, setStartDate] = useState(initialRange.startDate);
  const [endDate, setEndDate] = useState(initialRange.endDate);
  const [status, setStatus] = useState<OrderStatus | ''>('');

  const { orders, pagination, loading, error, page, setPage } = useAdminOrders({
    startDate,
    endDate,
    status: status || undefined,
    size: 20,
  });

  return (
    <>
      <header className="page-header">
        <div>
          <h1>Global Orders</h1>
          <p>Search platform orders with administrative date-range filters.</p>
        </div>
      </header>

      <section className="panel">
        <div className="filters-row">
          <label>
            Start date
            <input
              type="date"
              value={startDate}
              max={endDate}
              onChange={(event) => setStartDate(event.target.value)}
            />
          </label>
          <label>
            End date
            <input
              type="date"
              value={endDate}
              min={startDate}
              onChange={(event) => setEndDate(event.target.value)}
            />
          </label>
          <label>
            Status
            <select
              value={status}
              onChange={(event) => setStatus(event.target.value as OrderStatus | '')}
            >
              {STATUS_OPTIONS.map((option) => (
                <option key={option || 'ALL'} value={option}>
                  {option || 'All statuses'}
                </option>
              ))}
            </select>
          </label>
        </div>

        {loading && <div className="state-message loading">Loading orders…</div>}
        {error && <div className="state-message error">{error}</div>}

        {!loading && !error && (
          <>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Order #</th>
                  <th>Buyer</th>
                  <th>Total</th>
                  <th>Status</th>
                  <th>Placed</th>
                </tr>
              </thead>
              <tbody>
                {orders.map((order) => (
                  <tr key={order.id}>
                    <td>{order.orderNumber}</td>
                    <td>{order.buyerEmail}</td>
                    <td>{formatCurrency(Number(order.totalAmount))}</td>
                    <td>{order.status}</td>
                    <td>{new Date(order.placedAt).toLocaleString()}</td>
                  </tr>
                ))}
                {orders.length === 0 && (
                  <tr>
                    <td colSpan={5}>No orders found for the selected date range.</td>
                  </tr>
                )}
              </tbody>
            </table>

            {pagination && (
              <div className="pagination-bar">
                <span>
                  Showing page {pagination.pageNumber + 1} of {Math.max(pagination.totalPages, 1)}{' '}
                  ({pagination.totalElements} orders)
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
