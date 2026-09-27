import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { storefrontApi } from '../../api/storefrontApi';
import type { Product, Category } from '../../api/storefrontTypes';
import { ProductCard } from '../../components/storefront/ProductCard';
import './HomePage.css';

export const HomePage: React.FC = () => {
  const [featuredProducts, setFeaturedProducts] = useState<Product[]>([]);
  const [dealsProducts, setDealsProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadHomeData() {
      try {
        setLoading(true);
        const [cats, searchRes, dealsRes] = await Promise.all([
          storefrontApi.getRootCategories().catch(() => []),
          storefrontApi.searchProducts({ size: 8, sortBy: 'BEST_SELLERS' }).catch(() => ({ content: [] })),
          storefrontApi.searchProducts({ size: 8, sortBy: 'FEATURED' }).catch(() => ({ content: [] })),
        ]);

        setCategories(cats);
        setFeaturedProducts(searchRes.content || []);

        // Filter deals or fall back
        const deals = (dealsRes.content || []).filter(
          (p) => (p.discountPercentage && p.discountPercentage > 0) || p.badgeTag === 'DEAL_OF_THE_DAY'
        );
        setDealsProducts(deals.length > 0 ? deals : (dealsRes.content || []).slice(0, 4));
      } finally {
        setLoading(false);
      }
    }
    loadHomeData();
  }, []);

  return (
    <div className="home-container">
      {/* Hero Banner with Gradient Overlay */}
      <div className="home-hero">
        <div className="home-hero-content">
          <span className="home-hero-badge">Enterprise Spring Showcase</span>
          <h1 className="home-hero-title">Next-Gen QD-OLED Displays & Engineering Workstations</h1>
          <p className="home-hero-desc">
            Equip your enterprise workforce with certified color-accurate monitors, high-speed Thunderbolt 4
            docks, and ergonomic mounting ecosystems.
          </p>
          <div className="home-hero-actions">
            <Link to="/products?query=monitor" className="home-hero-btn primary">
              Shop Enterprise Monitors
            </Link>
            <Link to="/products" className="home-hero-btn secondary">
              Browse All Hardware
            </Link>
          </div>
        </div>
      </div>

      {/* Floating Category Quick Cards */}
      <div className="home-categories-grid">
        {categories.slice(0, 4).map((cat) => (
          <div key={cat.id} className="home-cat-card">
            <h3 className="home-cat-card-title">{cat.name}</h3>
            <div className="home-cat-card-img-wrap">
              <img
                src="https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=500&auto=format&fit=crop&q=60"
                alt={cat.name}
              />
            </div>
            <Link to={`/products?categoryId=${cat.id}`} className="home-cat-card-link">
              Explore collection →
            </Link>
          </div>
        ))}

        {categories.length === 0 && (
          <>
            <div className="home-cat-card">
              <h3 className="home-cat-card-title">Monitors & Displays</h3>
              <div className="home-cat-card-img-wrap">
                <img
                  src="https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=500&auto=format&fit=crop&q=60"
                  alt="Monitors"
                />
              </div>
              <Link to="/products?query=monitor" className="home-cat-card-link">
                Shop Displays →
              </Link>
            </div>
            <div className="home-cat-card">
              <h3 className="home-cat-card-title">Computer Accessories</h3>
              <div className="home-cat-card-img-wrap">
                <img
                  src="https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=500&auto=format&fit=crop&q=60"
                  alt="Accessories"
                />
              </div>
              <Link to="/products" className="home-cat-card-link">
                Shop Cables & Docks →
              </Link>
            </div>
            <div className="home-cat-card">
              <h3 className="home-cat-card-title">Enterprise Audio</h3>
              <div className="home-cat-card-img-wrap">
                <img
                  src="https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&auto=format&fit=crop&q=60"
                  alt="Audio"
                />
              </div>
              <Link to="/products" className="home-cat-card-link">
                Shop Headsets →
              </Link>
            </div>
            <div className="home-cat-card">
              <h3 className="home-cat-card-title">Office Ergonomics</h3>
              <div className="home-cat-card-img-wrap">
                <img
                  src="https://images.unsplash.com/photo-1580481077111-a83d7350fbe7?w=500&auto=format&fit=crop&q=60"
                  alt="Ergonomics"
                />
              </div>
              <Link to="/products" className="home-cat-card-link">
                Shop Monitor Arms →
              </Link>
            </div>
          </>
        )}
      </div>

      {/* Today's Deals Section */}
      {dealsProducts.length > 0 && (
        <section className="home-shelf-section">
          <div className="home-shelf-header">
            <div>
              <h2 className="home-shelf-title">Today's Deals for Enterprise</h2>
              <p className="home-shelf-subtitle">Limited-time promotional pricing on top corporate hardware</p>
            </div>
            <Link to="/products?badge=DEAL_OF_THE_DAY" className="home-shelf-link">
              See all deals →
            </Link>
          </div>

          <div className="home-products-grid">
            {dealsProducts.map((p) => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>
        </section>
      )}

      {/* Best Sellers / Featured Section */}
      <section className="home-shelf-section">
        <div className="home-shelf-header">
          <div>
            <h2 className="home-shelf-title">Best Sellers & Recommended Catalog</h2>
            <p className="home-shelf-subtitle">Highest volume workstation picks trusted by IT leaders</p>
          </div>
          <Link to="/products?sortBy=BEST_SELLERS" className="home-shelf-link">
            View best sellers →
          </Link>
        </div>

        {loading ? (
          <div className="home-loading">Loading live product catalog...</div>
        ) : featuredProducts.length > 0 ? (
          <div className="home-products-grid">
            {featuredProducts.map((p) => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>
        ) : (
          <div className="home-empty">
            <p>No products available yet. Check back soon!</p>
          </div>
        )}
      </section>

      {/* Corporate Value Prop Banner */}
      <div className="home-value-props">
        <div className="home-vp-item">
          <div className="home-vp-icon">🚀</div>
          <div>
            <h4>Fast Enterprise Delivery</h4>
            <p>Next-day regional hub fulfillment with SLA tracking</p>
          </div>
        </div>
        <div className="home-vp-item">
          <div className="home-vp-icon">🛡️</div>
          <div>
            <h4>Brand Registry Protection</h4>
            <p>100% verified authentic hardware with anti-counterfeit ASIN locks</p>
          </div>
        </div>
        <div className="home-vp-item">
          <div className="home-vp-icon">💳</div>
          <div>
            <h4>Corporate Net-30 Invoicing</h4>
            <p>Consolidated procurement invoicing for IT departments</p>
          </div>
        </div>
      </div>
    </div>
  );
};
