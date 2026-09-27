import {
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import type { HourlyOrderInflux } from '../../api/sellerTypes';

interface Props {
  data: HourlyOrderInflux[];
}

export function OrderInfluxChart({ data }: Props) {
  const chartData = data.map((d) => ({
    ...d,
    orderCount: Number(d.orderCount),
  }));

  return (
    <div style={{ width: '100%', height: 260 }}>
      <ResponsiveContainer>
        <BarChart data={chartData} margin={{ top: 8, right: 12, left: 0, bottom: 0 }}>
          <defs>
            <linearGradient id="influxBarFill" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#2563eb" stopOpacity={0.85} />
              <stop offset="100%" stopColor="#2563eb" stopOpacity={0.35} />
            </linearGradient>
          </defs>
          <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
          <XAxis
            dataKey="hour"
            tick={{ fontSize: 11, fill: '#64748b' }}
            axisLine={false}
            tickLine={false}
            minTickGap={16}
          />
          <YAxis
            tick={{ fontSize: 11, fill: '#64748b' }}
            axisLine={false}
            tickLine={false}
            allowDecimals={false}
            width={40}
          />
          <Tooltip
            cursor={{ fill: 'rgba(37, 99, 235, 0.06)' }}
            contentStyle={{
              borderRadius: '10px',
              border: '1px solid #e2e8f0',
              fontSize: '0.85rem',
              boxShadow: '0 4px 12px rgba(0,0,0,0.08)',
            }}
            formatter={(value) => [`${value ?? 0} orders`, 'Volume']}
            labelFormatter={(label) => `${String(label ?? '')} UTC`}
          />
          <Bar
            dataKey="orderCount"
            fill="url(#influxBarFill)"
            radius={[4, 4, 0, 0]}
            maxBarSize={28}
          />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
