import React from 'react';

interface RatingStarsProps {
  rating: number;
  totalReviews?: number;
  size?: 'sm' | 'md' | 'lg';
  showCount?: boolean;
}

export const RatingStars: React.FC<RatingStarsProps> = ({
  rating = 0,
  totalReviews,
  size = 'md',
  showCount = true,
}) => {
  const rounded = Math.round(rating * 10) / 10;
  const fullStars = Math.floor(rounded);
  const hasHalf = rounded - fullStars >= 0.3 && rounded - fullStars <= 0.7;

  const starSize = size === 'sm' ? 13 : size === 'lg' ? 18 : 15;

  return (
    <div
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: '4px',
        fontSize: size === 'sm' ? '0.78rem' : '0.85rem',
      }}
    >
      <div style={{ display: 'inline-flex', alignItems: 'center', color: '#de7921' }} title={`${rounded} out of 5 stars`}>
        {[1, 2, 3, 4, 5].map((starIndex) => {
          if (starIndex <= fullStars) {
            return (
              <svg
                key={starIndex}
                width={starSize}
                height={starSize}
                viewBox="0 0 24 24"
                fill="#de7921"
                stroke="#de7921"
                strokeWidth="1"
              >
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
              </svg>
            );
          } else if (starIndex === fullStars + 1 && hasHalf) {
            return (
              <svg key={starIndex} width={starSize} height={starSize} viewBox="0 0 24 24">
                <defs>
                  <linearGradient id={`half-${starIndex}`}>
                    <stop offset="50%" stopColor="#de7921" />
                    <stop offset="50%" stopColor="#d5d9d9" />
                  </linearGradient>
                </defs>
                <polygon
                  points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"
                  fill={`url(#half-${starIndex})`}
                  stroke="#de7921"
                  strokeWidth="0.5"
                />
              </svg>
            );
          } else {
            return (
              <svg
                key={starIndex}
                width={starSize}
                height={starSize}
                viewBox="0 0 24 24"
                fill="#e7e7e7"
                stroke="#d5d9d9"
                strokeWidth="1"
              >
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
              </svg>
            );
          }
        })}
      </div>

      {showCount && (
        <span style={{ color: '#007185', fontSize: '0.8rem', marginLeft: '2px', fontWeight: 500 }}>
          {totalReviews !== undefined ? totalReviews.toLocaleString() : rounded.toFixed(1)}
        </span>
      )}
    </div>
  );
};
