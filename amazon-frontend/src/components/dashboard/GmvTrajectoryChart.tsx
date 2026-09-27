import {
  Area,
  AreaChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import type { DailyGmvPoint } from '../../api/types';
import { formatChartDate, formatCompactCurrency, formatCurrency } from '../../utils/format';

interface GmvTrajectoryChartProps {
  data: DailyGmvPoint[];
}

export function GmvTrajectoryChart({ data }: GmvTrajectoryChartProps) {
  const chartData = data.map((point) => ({
    ...point,
    gmv: Number(point.gmv),
    label: formatChartDate(point.date),
  }));

  return (
    <div style={{ width: '100%', height: 320 }}>
      <ResponsiveContainer>
        <AreaChart data={chartData} margin={{ top: 8, right: 12, left: 0, bottom: 0 }}>
          <defs>
            <linearGradient id="gmvFill" x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor="#2563eb" stopOpacity={0.35} />
              <stop offset="95%" stopColor="#2563eb" stopOpacity={0.02} />
            </linearGradient>
          </defs>
          <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
          <XAxis
            dataKey="label"
            tick={{ fontSize: 11, fill: '#64748b' }}
            axisLine={false}
            tickLine={false}
            minTickGap={24}
          />
          <YAxis
            tick={{ fontSize: 11, fill: '#64748b' }}
            axisLine={false}
            tickLine={false}
            tickFormatter={(value) => formatCompactCurrency(Number(value))}
            width={72}
          />
          <Tooltip
            formatter={(value) => formatCurrency(Number(value))}
            labelFormatter={(_, payload) => {
              const row = payload?.[0]?.payload as { date?: string; label?: string };
              return row?.date ?? row?.label ?? '';
            }}
          />
          <Area
            type="monotone"
            dataKey="gmv"
            stroke="#2563eb"
            strokeWidth={2}
            fill="url(#gmvFill)"
            dot={false}
            activeDot={{ r: 4 }}
          />
        </AreaChart>
      </ResponsiveContainer>
    </div>
  );
}
