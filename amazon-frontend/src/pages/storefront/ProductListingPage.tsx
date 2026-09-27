import React, { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { storefrontApi } from '../../api/storefrontApi';
import type { Category, Product, ProductSortBy } from '../../api/storefrontTypes';
import { ProductCard } from '../../components/storefront/ProductCard';
import './ProductListingPage.css';

export const ProductListingPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(true);

  // Read URL query params
  const query = searchParams.get('query') || '';
  const categoryId = searchParams.get('categoryId') || '';
  const minPrice = searchParams.get('minPrice') ? Number(searchParams.get('minPrice')) : undefined;
  const maxPrice = searchParams.get('maxPrice') ? Number(searchParams.get('maxPrice')) : undefined;
  const minRating = searchParams.get('minRating') ? Number(searchParams.get('minRating')) : undefined;
  const sortBy = (searchParams.get('sortBy') as ProductSortBy) || 'FEATURED';
  const page = Number(searchParams.get('page')) || 0;
  const badgeFilter = searchParams.get('badge') || '';

  // Local state for custom price inputs
  const [customMin, setCustomMin] = useState(minPrice?.toString() || '');
  const [customMax, setCustomMax] = useState(maxPrice?.toString() || '');

  // Load Categories once
  useEffect(() => {
    storefrontApi
      .getRootCategories()
      .then((data) => setCategories(data))
      .catch(() => setCategories([]));
  }, []);

  useEffect(() => {
    let ignore = false;
    const run = async () => {
      try {
        setLoading(true);
        const res = await storefrontApi.searchProducts({
          query: query || undefined,
          categoryId: categoryId || undefined,
          minPrice,
          maxPrice,
          minRating,
          sortBy,
          page,
          size: 16,
        });

        let items = res.content || [];
        if (badgeFilter) {
          items = items.filter((p) => p.badgeTag === badgeFilter);
        }

        if (!ignore) {
          setProducts(items);
          setTotalElements(res.totalElements ?? items.length);
          setTotalPages(res.totalPages ?? 1);
        }
      } catch {
        if (!ignore) {
          setProducts([]);
          setTotalElements(0);
          setTotalPages(1);
        }
      } finally {
        if (!ignore) setLoading(false);
      }
    };
    void run();
    return () => { ignore = true; };
  }, [query, categoryId, minPrice, maxPrice, minRating, sortBy, page, badgeFilter]);

  const updateParam = (key: string, value: string | undefined) => {
    const next = new URLSearchParams(searchParams);
    if (value !== undefined && value !== '') {
      next.set(key, value);
    } else {
      next.delete(key);
    }
    next.set('page', '0'); // reset to first page on filter change
    setSearchParams(next);
  };

  const handlePriceApply = (e: React.FormEvent) => {
    e.preventDefault();
    const next = new URLSearchParams(searchParams);
    if (customMin) next.set('minPrice', customMin);
    else next.delete('minPrice');

    if (customMax) next.set('maxPrice', customMax);
    else next.delete('maxPrice');

    next.set('page', '0');
    setSearchParams(next);
  };

  const clearAllFilters = () => {
    const next = new URLSearchParams();
    if (query) next.set('query', query);
    setSearchParams(next);
    setCustomMin('');
    setCustomMax('');
  };

  return (
    <div className="plp-container">
      {/* Top Banner & Active Filters Summary */}
      <div className="plp-top-bar">
        <div className="plp-result-count">
          {loading ? (
            <span>Searching catalog...</span>
          ) : (
            <span>
              1-{products.length} of {totalElements} results
              {query && (
                <span>
                  {' '}
                  for <span className="plp-query-highlight">"{query}"</span>
                </span>
              )}
            </span>
          )}
        </div>

        {/* Sort Selector */}
        <div className="plp-sort-wrapper">
          <label htmlFor="plp-sort-select">Sort by:</label>
          <select
            id="plp-sort-select"
            value={sortBy}
            onChange={(e) => updateParam('sortBy', e.target.value)}
            className="plp-sort-select"
          >
            <option value="FEATURED">Featured</option>
            <option value="PRICE_ASC">Price: Low to High</option>
            <option value="PRICE_DESC">Price: High to Low</option>
            <option value="AVG_CUSTOMER_REVIEW">Avg. Customer Review</option>
            <option value="BEST_SELLERS">Best Sellers</option>
            <option value="NEWEST">Newest Arrivals</option>
          </select>
        </div>
      </div>

      <div className="plp-layout">
        {/* Left Filter Sidebar */}
        <aside className="plp-sidebar">
          {/* Clear Filters Button if any filter active */}
          {(categoryId || minPrice || maxPrice || minRating || badgeFilter) && (
            <div className="plp-filter-group">
              <button type="button" onClick={clearAllFilters} className="plp-clear-btn">
                ✕ Clear all filters
              </button>
            </div>
          )}

          {/* Department / Category Filter */}
          <div className="plp-filter-group">
            <h4 className="plp-filter-title">Department</h4>
            <ul className="plp-filter-list">
              <li
                className={!categoryId ? 'active' : ''}
                onClick={() => updateParam('categoryId', undefined)}
              >
                All Departments
              </li>
              {categories.map((cat) => (
                <li
                  key={cat.id}
                  className={categoryId === cat.id ? 'active' : ''}
                  onClick={() => updateParam('categoryId', cat.id)}
                >
                  {cat.name}
                </li>
              ))}
            </ul>
          </div>

          {/* Customer Reviews Rating Filter */}
          <div className="plp-filter-group">
            <h4 className="plp-filter-title">Customer Reviews</h4>
            <div className="plp-rating-options">
              {[4, 3, 2].map((stars) => (
                <div
                  key={stars}
                  className={`plp-rating-row ${minRating === stars ? 'active' : ''}`}
                  onClick={() => updateParam('minRating', minRating === stars ? undefined : String(stars))}
                >
                  <div className="plp-stars-preview">
                    {[1, 2, 3, 4, 5].map((s) => (
                      <span key={s} style={{ color: s <= stars ? '#de7921' : '#e7e7e7', fontSize: '1.1rem' }}>
                        ★
                      </span>
                    ))}
                  </div>
                  <span>& Up</span>
                </div>
              ))}
            </div>
          </div>

          {/* Price Range Filter */}
          <div className="plp-filter-group">
            <h4 className="plp-filter-title">Price</h4>
            <ul className="plp-filter-list">
              <li onClick={() => { updateParam('minPrice', undefined); updateParam('maxPrice', '100'); }}>
                Under $100
              </li>
              <li onClick={() => { updateParam('minPrice', '100'); updateParam('maxPrice', '300'); }}>
                $100 to $300
              </li>
              <li onClick={() => { updateParam('minPrice', '300'); updateParam('maxPrice', '700'); }}>
                $300 to $700
              </li>
              <li onClick={() => { updateParam('minPrice', '700'); updateParam('maxPrice', undefined); }}>
                $700 & Above
              </li>
            </ul>

            <form onSubmit={handlePriceApply} className="plp-price-form">
              <input
                type="number"
                placeholder="$ Min"
                value={customMin}
                onChange={(e) => setCustomMin(e.target.value)}
                className="plp-price-input"
              />
              <span style={{ color: '#888' }}>-</span>
              <input
                type="number"
                placeholder="$ Max"
                value={customMax}
                onChange={(e) => setCustomMax(e.target.value)}
                className="plp-price-input"
              />
              <button type="submit" className="plp-price-submit">
                Go
              </button>
            </form>
          </div>

          {/* Badges / Deal Filter */}
          <div className="plp-filter-group">
            <h4 className="plp-filter-title">Enterprise Deals & Badges</h4>
            <ul className="plp-filter-list">
              <li
                className={badgeFilter === 'BEST_SELLER' ? 'active' : ''}
                onClick={() => updateParam('badge', badgeFilter === 'BEST_SELLER' ? undefined : 'BEST_SELLER')}
              >
                Best Sellers Only
              </li>
              <li
                className={badgeFilter === 'DEAL_OF_THE_DAY' ? 'active' : ''}
                onClick={() => updateParam('badge', badgeFilter === 'DEAL_OF_THE_DAY' ? undefined : 'DEAL_OF_THE_DAY')}
              >
                Today's Deals
              </li>
              <li
                className={badgeFilter === 'ENTERPRISE_TIER' ? 'active' : ''}
                onClick={() => updateParam('badge', badgeFilter === 'ENTERPRISE_TIER' ? undefined : 'ENTERPRISE_TIER')}
              >
                Enterprise Tier Certified
              </li>
            </ul>
          </div>
        </aside>

        {/* Product Grid & Results */}
        <section className="plp-results">
          {loading ? (
            <div className="plp-loading">
              <div className="plp-spinner"></div>
              <p>Fetching matching enterprise products...</p>
            </div>
          ) : products.length > 0 ? (
            <>
              <div className="plp-grid">
                {products.map((product) => (
                  <ProductCard key={product.id} product={product} />
                ))}
              </div>

              {/* Pagination */}
              {totalPages > 1 && (
                <div className="plp-pagination">
                  <button
                    type="button"
                    disabled={page <= 0}
                    onClick={() => updateParam('page', String(page - 1))}
                    className="plp-page-btn"
                  >
                    ← Previous
                  </button>
                  <span className="plp-page-info">
                    Page {page + 1} of {totalPages}
                  </span>
                  <button
                    type="button"
                    disabled={page >= totalPages - 1}
                    onClick={() => updateParam('page', String(page + 1))}
                    className="plp-page-btn"
                  >
                    Next →
                  </button>
                </div>
              )}
            </>
          ) : (
            <div className="plp-no-results">
              <h3>No products found</h3>
              <p>Try adjusting your search query, clearing filters, or browsing other categories.</p>
              <button type="button" onClick={clearAllFilters} className="plp-reset-btn">
                Reset all filters
              </button>
            </div>
          )}
        </section>
      </div>
    </div>
  );
};
