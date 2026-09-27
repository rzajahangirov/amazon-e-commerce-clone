import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import type { Product } from '../../api/storefrontTypes';
import { BadgePill } from './BadgePill';
import { RatingStars } from './RatingStars';
import { useCustomerAuth } from '../../context/CustomerAuthContext';

interface ProductCardProps {
  product: Product;
}

export const ProductCard: React.FC<ProductCardProps> = ({ product }) => {
  const { addToCart, isFavorited, toggleFavorite } = useCustomerAuth();
  const [adding, setAdding] = useState(false);
  const [added, setAdded] = useState(false);

  const favorited = isFavorited(product.id) || product.isFavorited;

  const price = product.buyBoxPrice ?? product.basePrice ?? 0;
  const listPrice = product.listPrice;
  const discount = product.discountPercentage;

  // Resolve best listing ID
  const listingId =
    product.variants?.[0]?.listings?.[0]?.id ||
    product.variants?.[0]?.id ||
    product.id;

  const handleQuickAdd = async (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    try {
      setAdding(true);
      await addToCart(listingId, 1, product);
      setAdded(true);
      setTimeout(() => setAdded(false), 2000);
    } catch {
      // ignore
    } finally {
      setAdding(false);
    }
  };

  const handleFavoriteClick = async (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    await toggleFavorite(product);
  };

  const displayImage =
    product.mainImageUrl ||
    'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=500&auto=format&fit=crop&q=60';

  return (
    <div
      style={{
        backgroundColor: '#ffffff',
        border: '1px solid #e7e7e7',
        borderRadius: '8px',
        padding: '16px',
        display: 'flex',
        flexDirection: 'column',
        position: 'relative',
        transition: 'box-shadow 0.2s ease, transform 0.2s ease',
        height: '100%',
      }}
      onMouseEnter={(e) => {
        e.currentTarget.style.boxShadow = '0 6px 16px rgba(0,0,0,0.1)';
        e.currentTarget.style.transform = 'translateY(-2px)';
      }}
      onMouseLeave={(e) => {
        e.currentTarget.style.boxShadow = 'none';
        e.currentTarget.style.transform = 'none';
      }}
    >
      {/* Top Bar: Badge & Heart */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'flex-start',
          marginBottom: '8px',
          minHeight: '24px',
        }}
      >
        <div>{product.badgeTag && <BadgePill tag={product.badgeTag} size="sm" />}</div>
        <button
          type="button"
          onClick={handleFavoriteClick}
          aria-label={favorited ? 'Remove from Wishlist' : 'Add to Wishlist'}
          style={{
            background: 'none',
            border: 'none',
            cursor: 'pointer',
            padding: '4px',
            color: favorited ? '#cc0c39' : '#888888',
            transition: 'transform 0.15s ease',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
          onMouseDown={(e) => (e.currentTarget.style.transform = 'scale(1.2)')}
          onMouseUp={(e) => (e.currentTarget.style.transform = 'scale(1.0)')}
        >
          <svg
            width="20"
            height="20"
            viewBox="0 0 24 24"
            fill={favorited ? '#cc0c39' : 'none'}
            stroke={favorited ? '#cc0c39' : 'currentColor'}
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z" />
          </svg>
        </button>
      </div>

      {/* Product Image Link */}
      <Link
        to={`/products/${product.id}`}
        style={{
          textDecoration: 'none',
          color: 'inherit',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          flexGrow: 1,
        }}
      >
        <div
          style={{
            width: '100%',
            height: '190px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            overflow: 'hidden',
            marginBottom: '12px',
          }}
        >
          <img
            src={displayImage}
            alt={product.title}
            loading="lazy"
            style={{
              maxHeight: '100%',
              maxWidth: '100%',
              objectFit: 'contain',
              transition: 'transform 0.25s ease',
            }}
          />
        </div>

        {/* Title & Model */}
        <div style={{ width: '100%', marginBottom: '6px' }}>
          <h3
            style={{
              fontSize: '0.92rem',
              fontWeight: 500,
              color: '#0f1111',
              margin: '0 0 4px 0',
              lineHeight: 1.35,
              display: '-webkit-box',
              WebkitLineClamp: 2,
              WebkitBoxOrient: 'vertical',
              overflow: 'hidden',
              minHeight: '2.5em',
            }}
            title={product.title}
          >
            {product.title}
          </h3>
          {product.modelNumber && (
            <div style={{ fontSize: '0.72rem', color: '#565959', fontFamily: 'monospace' }}>
              Model: {product.modelNumber}
            </div>
          )}
        </div>

        {/* Ratings & Sales Volume */}
        <div style={{ width: '100%', marginBottom: '8px' }}>
          <RatingStars rating={product.averageRating} totalReviews={product.totalReviews} size="sm" />
          {product.salesVolumeText && (
            <div style={{ fontSize: '0.74rem', color: '#565959', marginTop: '3px' }}>
              {product.salesVolumeText}
            </div>
          )}
        </div>

        {/* Price & Discounts */}
        <div style={{ width: '100%', marginTop: 'auto', marginBottom: '8px' }}>
          <div style={{ display: 'flex', alignItems: 'baseline', gap: '6px', flexWrap: 'wrap' }}>
            <span style={{ fontSize: '1.25rem', fontWeight: 700, color: '#0f1111' }}>
              <span style={{ fontSize: '0.75rem', verticalAlign: 'top', marginRight: '1px' }}>$</span>
              {price.toFixed(2)}
            </span>

            {listPrice && listPrice > price && (
              <span style={{ fontSize: '0.8rem', color: '#565959', textDecoration: 'line-through' }}>
                ${listPrice.toFixed(2)}
              </span>
            )}

            {discount && discount > 0 && (
              <span
                style={{
                  backgroundColor: '#cc0c39',
                  color: '#ffffff',
                  fontSize: '0.7rem',
                  fontWeight: 700,
                  padding: '2px 5px',
                  borderRadius: '3px',
                }}
              >
                -{discount}%
              </span>
            )}
          </div>

          {/* Delivery Estimate */}
          {product.deliveryEstimate && (
            <div
              style={{
                fontSize: '0.76rem',
                color: '#0f1111',
                marginTop: '4px',
                display: 'flex',
                alignItems: 'center',
                gap: '4px',
              }}
            >
              <span style={{ color: '#007185', fontWeight: 600 }}>Prime</span>
              <span>{product.deliveryEstimate}</span>
            </div>
          )}
        </div>
      </Link>

      {/* Quick Add CTA */}
      <button
        type="button"
        onClick={handleQuickAdd}
        disabled={adding}
        style={{
          width: '100%',
          padding: '8px 12px',
          borderRadius: '20px',
          border: '1px solid #ffd814',
          backgroundColor: added ? '#067d62' : '#ffd814',
          color: added ? '#ffffff' : '#0f1111',
          fontSize: '0.82rem',
          fontWeight: 600,
          cursor: adding ? 'not-allowed' : 'pointer',
          boxShadow: '0 2px 5px rgba(213, 217, 217, 0.5)',
          transition: 'background-color 0.2s ease, transform 0.1s ease',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          gap: '6px',
        }}
        onMouseEnter={(e) => {
          if (!added) e.currentTarget.style.backgroundColor = '#f7ca00';
        }}
        onMouseLeave={(e) => {
          if (!added) e.currentTarget.style.backgroundColor = '#ffd814';
        }}
      >
        {added ? (
          <>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3">
              <polyline points="20 6 9 17 4 12" />
            </svg>
            Added to Cart
          </>
        ) : adding ? (
          'Adding...'
        ) : (
          'Add to Cart'
        )}
      </button>
    </div>
  );
};
