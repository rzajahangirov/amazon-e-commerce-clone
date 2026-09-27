import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { storefrontApi } from '../../api/storefrontApi';
import type { Product, Review } from '../../api/storefrontTypes';
import { BadgePill } from '../../components/storefront/BadgePill';
import { RatingStars } from '../../components/storefront/RatingStars';
import { useCustomerAuth } from '../../context/CustomerAuthContext';
import './ProductDetailPage.css';

export const ProductDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { addToCart, isFavorited, toggleFavorite, isAuthenticated, openAuthModal } = useCustomerAuth();

  const [product, setProduct] = useState<Product | null>(null);
  const [fbtProducts, setFbtProducts] = useState<Product[]>([]);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [selectedFbtIds, setSelectedFbtIds] = useState<Set<string>>(new Set());

  const [loading, setLoading] = useState(true);
  const [quantity, setQuantity] = useState(1);
  const [addingToCart, setAddingToCart] = useState(false);
  const [addedSuccess, setAddedSuccess] = useState(false);
  const [fbtAdding, setFbtAdding] = useState(false);
  const [fbtAdded, setFbtAdded] = useState(false);

  // Review Form Modal
  const [reviewModalOpen, setReviewModalOpen] = useState(false);
  const [reviewRating, setReviewRating] = useState(5);
  const [reviewComment, setReviewComment] = useState('');
  const [submittingReview, setSubmittingReview] = useState(false);
  const [reviewError, setReviewError] = useState<string | null>(null);


  useEffect(() => {
    if (!id) return;
    let ignore = false;
    const run = async () => {
      try {
        setLoading(true);
        const [prod, fbt, revs] = await Promise.all([
          storefrontApi.getProductById(id),
          storefrontApi.getFrequentlyBoughtTogether(id).catch(() => []),
          storefrontApi.getProductReviews(id, 0, 10).catch(() => ({ content: [] })),
        ]);
        if (!ignore) {
          setProduct(prod);
          setFbtProducts(fbt || []);
          setSelectedFbtIds(new Set((fbt || []).map((p) => p.id)));
          setReviews(revs.content || []);
        }
      } catch {
        if (!ignore) setProduct(null);
      } finally {
        if (!ignore) setLoading(false);
      }
    };
    void run();
    window.scrollTo({ top: 0, behavior: 'smooth' });
    return () => { ignore = true; };
  }, [id]);

  if (loading) {
    return (
      <div className="pdp-loading-container">
        <div className="pdp-spinner"></div>
        <p>Loading enterprise product specifications...</p>
      </div>
    );
  }

  if (!product) {
    return (
      <div className="pdp-error-container">
        <h2>Product Not Found</h2>
        <p>The product you requested could not be located or may have been unlisted.</p>
        <Link to="/products" className="pdp-back-btn">
          Back to Catalog
        </Link>
      </div>
    );
  }

  const price = product.buyBoxPrice ?? product.basePrice ?? 0;
  const listPrice = product.listPrice;
  const discount = product.discountPercentage;
  const favorited = isFavorited(product.id) || product.isFavorited;

  // Best listing offer
  const listingId =
    product.variants?.[0]?.listings?.[0]?.id ||
    product.variants?.[0]?.id ||
    product.id;

  const handleAddToCart = async () => {
    try {
      setAddingToCart(true);
      await addToCart(listingId, quantity, product);
      setAddedSuccess(true);
      setTimeout(() => setAddedSuccess(false), 2500);
    } finally {
      setAddingToCart(false);
    }
  };

  const handleBuyNow = async () => {
    await addToCart(listingId, quantity, product);
    navigate('/checkout');
  };

  // Frequently bought together calculations
  const selectedFbtList = fbtProducts.filter((p) => selectedFbtIds.has(p.id));
  const fbtTotalPrice =
    price +
    selectedFbtList.reduce((sum, p) => sum + (p.buyBoxPrice ?? p.basePrice ?? 0), 0);

  const toggleFbtItem = (fbtId: string) => {
    setSelectedFbtIds((prev) => {
      const next = new Set(prev);
      if (next.has(fbtId)) next.delete(fbtId);
      else next.add(fbtId);
      return next;
    });
  };

  const handleAddFbtAll = async () => {
    try {
      setFbtAdding(true);
      // Add source product
      await addToCart(listingId, 1, product);
      // Add each selected complementary product
      for (const item of selectedFbtList) {
        const itemListingId =
          item.variants?.[0]?.listings?.[0]?.id || item.variants?.[0]?.id || item.id;
        await addToCart(itemListingId, 1, item);
      }
      setFbtAdded(true);
      setTimeout(() => setFbtAdded(false), 3000);
    } finally {
      setFbtAdding(false);
    }
  };

  // Review submission
  const handleSubmitReview = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!isAuthenticated) {
      openAuthModal('signin', 'Please sign in to submit a review');
      return;
    }
    try {
      setSubmittingReview(true);
      setReviewError(null);
      await storefrontApi.createReview(product.id, {
        rating: reviewRating,
        comment: reviewComment || undefined,
      });
      setReviewModalOpen(false);
      setReviewComment('');
      // Refresh reviews
      const updated = await storefrontApi.getProductReviews(product.id, 0, 10);
      setReviews(updated.content || []);
    } catch (err: unknown) {
      if (err instanceof Error) {
        setReviewError(err.message);
      } else {
        setReviewError('Failed to submit review');
      }
    } finally {
      setSubmittingReview(false);
    }
  };

  const displayImage =
    product.mainImageUrl ||
    'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=800&auto=format&fit=crop&q=80';

  return (
    <div className="pdp-container">
      {/* Breadcrumbs */}
      <nav className="pdp-breadcrumbs">
        <Link to="/">Home</Link>
        <span>›</span>
        <Link to={`/products?categoryId=${product.categoryId || ''}`}>
          {product.categoryName || 'Products'}
        </Link>
        <span>›</span>
        <span className="pdp-crumb-title">{product.title}</span>
      </nav>

      {/* Main 3-Column Product Showcase */}
      <div className="pdp-showcase-grid">
        {/* Left: Gallery */}
        <div className="pdp-gallery-column">
          <div className="pdp-main-image-wrap">
            <img src={displayImage} alt={product.title} className="pdp-main-image" />
          </div>
        </div>

        {/* Center: Details */}
        <div className="pdp-info-column">
          {product.badgeTag && (
            <div style={{ marginBottom: '8px' }}>
              <BadgePill tag={product.badgeTag} size="md" />
            </div>
          )}

          <h1 className="pdp-title">{product.title}</h1>

          {product.modelNumber && (
            <div className="pdp-model-number">
              Model ID: <span>{product.modelNumber}</span>
              {product.masterSku && <span> • SKU: {product.masterSku}</span>}
            </div>
          )}

          {product.brandName && (
            <div className="pdp-brand-link">
              Brand: <Link to={`/products?query=${product.brandName}`}>{product.brandName}</Link>
            </div>
          )}

          {/* Rating & Sales Velocity */}
          <div className="pdp-rating-row">
            <RatingStars rating={product.averageRating} totalReviews={product.totalReviews} size="md" />
            {product.salesVolumeText && (
              <span className="pdp-sales-volume-badge">{product.salesVolumeText}</span>
            )}
          </div>

          <hr className="pdp-divider" />

          {/* Pricing Block */}
          <div className="pdp-price-block">
            <div className="pdp-price-main">
              {discount && discount > 0 && (
                <span className="pdp-discount-badge">-{discount}%</span>
              )}
              <span className="pdp-price-currency">$</span>
              <span className="pdp-price-amount">{price.toFixed(2)}</span>
            </div>

            {listPrice && listPrice > price && (
              <div className="pdp-list-price">
                Typical price: <span className="pdp-strikethrough">${listPrice.toFixed(2)}</span>
              </div>
            )}

            <div className="pdp-tax-note">Available with corporate Net-30 invoicing at checkout.</div>
          </div>

          {/* Description Highlights */}
          {product.description && (
            <div className="pdp-description-section">
              <h4>About this item</h4>
              <p>{product.description}</p>
            </div>
          )}
        </div>

        {/* Right: Amazon Buy Box Card */}
        <div className="pdp-buybox-column">
          <div className="pdp-buybox-card">
            <div className="pdp-buybox-price">
              <span className="pdp-bb-currency">$</span>
              <span className="pdp-bb-amount">{price.toFixed(2)}</span>
            </div>

            {/* Delivery Estimate */}
            {product.deliveryEstimate && (
              <div className="pdp-bb-delivery">
                <span className="pdp-bb-prime">Prime</span>
                <span>{product.deliveryEstimate}</span>
              </div>
            )}

            <div className="pdp-bb-stock in-stock">
              In Stock & Ready for Enterprise Dispatch
            </div>

            {/* Quantity Selector */}
            <div className="pdp-bb-qty-row">
              <label htmlFor="pdp-qty-select">Quantity:</label>
              <select
                id="pdp-qty-select"
                value={quantity}
                onChange={(e) => setQuantity(Number(e.target.value))}
                className="pdp-bb-qty-select"
              >
                {[1, 2, 3, 4, 5, 10, 20].map((num) => (
                  <option key={num} value={num}>
                    {num}
                  </option>
                ))}
              </select>
            </div>

            {/* Add to Cart CTA */}
            <button
              type="button"
              onClick={handleAddToCart}
              disabled={addingToCart}
              className={`pdp-bb-btn add-to-cart ${addedSuccess ? 'success' : ''}`}
            >
              {addedSuccess ? '✓ Added to Cart' : addingToCart ? 'Adding...' : 'Add to Cart'}
            </button>

            {/* Buy Now CTA */}
            <button type="button" onClick={handleBuyNow} className="pdp-bb-btn buy-now">
              Buy Now
            </button>

            {/* Wishlist Toggle Button */}
            <button
              type="button"
              onClick={() => toggleFavorite(product)}
              className="pdp-bb-btn wishlist"
            >
              <span style={{ color: favorited ? '#cc0c39' : 'currentColor' }}>
                {favorited ? '♥ Favorited in Wish List' : '♡ Add to Wish List'}
              </span>
            </button>

            {/* Seller & Dispatch Info */}
            <div className="pdp-bb-attribution">
              <div className="pdp-bb-attr-row">
                <span>Ships from</span>
                <span>Amazon Enterprise Logistics</span>
              </div>
              <div className="pdp-bb-attr-row">
                <span>Sold by</span>
                <span>{product.sellerName || 'ApexMart Direct'}</span>
              </div>
              <div className="pdp-bb-attr-row">
                <span>Returns</span>
                <span>30-day Enterprise Replacement</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Frequently Bought Together Bundle */}
      {fbtProducts.length > 0 && (
        <section className="pdp-fbt-section">
          <h2 className="pdp-section-title">Frequently Bought Together</h2>
          <div className="pdp-fbt-card">
            <div className="pdp-fbt-visuals">
              {/* Source Product */}
              <div className="pdp-fbt-item-thumb">
                <img src={displayImage} alt={product.title} />
              </div>

              {/* Complementary Products */}
              {fbtProducts.map((fbt) => (
                <React.Fragment key={fbt.id}>
                  <div className="pdp-fbt-plus">+</div>
                  <div
                    className={`pdp-fbt-item-thumb ${!selectedFbtIds.has(fbt.id) ? 'inactive' : ''}`}
                  >
                    <img
                      src={
                        fbt.mainImageUrl ||
                        'https://images.unsplash.com/photo-1546868871-7041f2a55e12?w=400&auto=format&fit=crop&q=60'
                      }
                      alt={fbt.title}
                    />
                  </div>
                </React.Fragment>
              ))}
            </div>

            {/* CTA & Total */}
            <div className="pdp-fbt-action-wrap">
              <div className="pdp-fbt-price-label">
                Total price:{' '}
                <span className="pdp-fbt-total">${fbtTotalPrice.toFixed(2)}</span>
              </div>
              <button
                type="button"
                onClick={handleAddFbtAll}
                disabled={fbtAdding}
                className="pdp-fbt-btn"
              >
                {fbtAdded ? '✓ Bundle Added to Cart' : fbtAdding ? 'Adding...' : `Add All ${1 + selectedFbtList.length} to Cart`}
              </button>
            </div>
          </div>

          {/* FBT Checkboxes List */}
          <div className="pdp-fbt-checkbox-list">
            <div className="pdp-fbt-checkbox-item">
              <input type="checkbox" checked disabled id="fbt-source-chk" />
              <label htmlFor="fbt-source-chk">
                <strong>This item:</strong> {product.title} —{' '}
                <span className="pdp-fbt-price">${price.toFixed(2)}</span>
              </label>
            </div>

            {fbtProducts.map((fbt) => {
              const fbtPrice = fbt.buyBoxPrice ?? fbt.basePrice ?? 0;
              return (
                <div key={fbt.id} className="pdp-fbt-checkbox-item">
                  <input
                    type="checkbox"
                    id={`fbt-chk-${fbt.id}`}
                    checked={selectedFbtIds.has(fbt.id)}
                    onChange={() => toggleFbtItem(fbt.id)}
                  />
                  <label htmlFor={`fbt-chk-${fbt.id}`}>
                    <Link to={`/products/${fbt.id}`}>{fbt.title}</Link> —{' '}
                    <span className="pdp-fbt-price">${fbtPrice.toFixed(2)}</span>
                  </label>
                </div>
              );
            })}
          </div>
        </section>
      )}

      {/* Technical Specifications Table */}
      {product.specifications && Object.keys(product.specifications).length > 0 && (
        <section className="pdp-specs-section">
          <h2 className="pdp-section-title">Technical Specifications</h2>
          <div className="pdp-specs-table-wrap">
            <table className="pdp-specs-table">
              <tbody>
                {Object.entries(product.specifications).map(([key, val]) => (
                  <tr key={key}>
                    <td className="pdp-spec-key">{key}</td>
                    <td className="pdp-spec-val">{val}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      )}

      {/* Customer Reviews Section with Enterprise Badges */}
      <section className="pdp-reviews-section">
        <div className="pdp-reviews-header">
          <div>
            <h2 className="pdp-section-title">Customer Reviews & Enterprise Verified Feedback</h2>
            <div className="pdp-reviews-sub">
              <RatingStars rating={product.averageRating} totalReviews={product.totalReviews} size="md" />
              <span>Based on {product.totalReviews} verified purchaser reviews</span>
            </div>
          </div>
          <button
            type="button"
            onClick={() => {
              if (!isAuthenticated) {
                openAuthModal('signin', 'Sign in to write a customer review');
              } else {
                setReviewModalOpen(true);
              }
            }}
            className="pdp-write-review-btn"
          >
            Write a Customer Review
          </button>
        </div>

        {reviews.length > 0 ? (
          <div className="pdp-reviews-list">
            {reviews.map((rev) => (
              <div key={rev.id} className="pdp-review-card">
                <div className="pdp-review-author-row">
                  <div className="pdp-review-avatar">
                    {rev.userFullName?.charAt(0) || 'U'}
                  </div>
                  <div>
                    <div className="pdp-review-author-name">{rev.userFullName}</div>
                    {/* Enterprise Role and Badge */}
                    <div className="pdp-review-enterprise-info">
                      {rev.userTitleRole && (
                        <span className="pdp-review-role-title">{rev.userTitleRole}</span>
                      )}
                      {rev.isVerifiedEnterprise && (
                        <span className="pdp-review-enterprise-pill">
                          ★ Verified Enterprise Reviewer
                        </span>
                      )}
                    </div>
                  </div>
                </div>

                <div className="pdp-review-rating-row">
                  <RatingStars rating={rev.rating} size="sm" showCount={false} />
                  <span className="pdp-review-verified">Verified Purchase</span>
                  <span className="pdp-review-date">
                    Reviewed on {new Date(rev.createdAt).toLocaleDateString()}
                  </span>
                </div>

                {rev.comment && <p className="pdp-review-comment">{rev.comment}</p>}
              </div>
            ))}
          </div>
        ) : (
          <div className="pdp-no-reviews">
            <p>No customer reviews yet. Be the first enterprise client to review this product!</p>
          </div>
        )}
      </section>

      {/* Review Submission Modal */}
      {reviewModalOpen && (
        <div className="pdp-modal-overlay" onClick={() => setReviewModalOpen(false)}>
          <div className="pdp-modal-card" onClick={(e) => e.stopPropagation()}>
            <div className="pdp-modal-header">
              <h3>Write a Customer Review</h3>
              <button
                type="button"
                onClick={() => setReviewModalOpen(false)}
                className="pdp-modal-close"
              >
                ✕
              </button>
            </div>

            <form onSubmit={handleSubmitReview} className="pdp-modal-body">
              {reviewError && <div className="pdp-review-error">{reviewError}</div>}

              <div className="pdp-form-group">
                <label>Overall rating:</label>
                <div className="pdp-rating-picker">
                  {[1, 2, 3, 4, 5].map((star) => (
                    <button
                      key={star}
                      type="button"
                      onClick={() => setReviewRating(star)}
                      className={`pdp-star-btn ${star <= reviewRating ? 'active' : ''}`}
                    >
                      ★
                    </button>
                  ))}
                  <span className="pdp-rating-label">{reviewRating} of 5 stars</span>
                </div>
              </div>

              <div className="pdp-form-group">
                <label htmlFor="review-comment">Add a written review:</label>
                <textarea
                  id="review-comment"
                  required
                  rows={4}
                  value={reviewComment}
                  onChange={(e) => setReviewComment(e.target.value)}
                  placeholder="What did you like or dislike? How does it perform in your enterprise environment?"
                  className="pdp-textarea"
                />
              </div>

              <div className="pdp-modal-actions">
                <button
                  type="button"
                  onClick={() => setReviewModalOpen(false)}
                  className="pdp-btn-cancel"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submittingReview}
                  className="pdp-btn-submit"
                >
                  {submittingReview ? 'Submitting...' : 'Submit Review'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
