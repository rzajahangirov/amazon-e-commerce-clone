import React, { useEffect, useState } from 'react';
import { brandApi } from '../../api/brandApi';
import type { BrandPostResponseDto, CreateBrandPostRequestDto } from '../../api/brandTypes';
import { useBrandRBAC } from '../../hooks/useBrandRBAC';
import { userInitials } from '../../utils/format';
import '../../components/brand/BrandCommon.css';

export const BrandMarketingPage: React.FC = () => {
  const { permissions, activeRole } = useBrandRBAC();
  const [posts, setPosts] = useState<BrandPostResponseDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedAsin, setSelectedAsin] = useState('ALL');
  const [selectedPlacement, setSelectedPlacement] = useState('ALL');
  const [showCreateModal, setShowCreateModal] = useState(false);

  // Form states
  const [imageUrl, setImageUrl] = useState('');
  const [caption, setCaption] = useState('');
  const [linkedAsin, setLinkedAsin] = useState('B09V3HM1LR');
  const [reachCount, setReachCount] = useState<number>(0);
  const [likesCount, setLikesCount] = useState<number>(0);

  const fetchPosts = async () => {
    try {
      setLoading(true);
      const res = await brandApi.getBrandPosts(0, 50);
      setPosts(res.content);
    } catch (err) {
      console.error('Failed to load brand posts:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    let ignore = false;
    const run = async () => {
      try {
        const res = await brandApi.getBrandPosts(0, 50);
        if (!ignore) setPosts(res.content);
      } catch (err) {
        console.error('Failed to load brand posts:', err);
      } finally {
        if (!ignore) setLoading(false);
      }
    };
    void run();
    return () => { ignore = true; };
  }, []);

  const handleCreatePost = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!imageUrl || !caption) return;

    const dto: CreateBrandPostRequestDto = {
      imageUrl,
      caption,
      linkedAsin,
      reachCount,
      likesCount,
    };

    await brandApi.createBrandPost(dto);
    setShowCreateModal(false);
    setImageUrl('');
    setCaption('');
    void fetchPosts();
  };

  const filteredPosts = posts.filter((p) => {
    const matchesAsin = selectedAsin === 'ALL' || p.linkedAsin === selectedAsin;
    const matchesPlacement =
      selectedPlacement === 'ALL' || (p.placement && p.placement.includes(selectedPlacement));
    return matchesAsin && matchesPlacement;
  });

  if (loading && posts.length === 0) {
    return (
      <div className="brand-card" style={{ padding: '3rem', textAlign: 'center', color: '#64748b' }}>
        <p>Loading brand lifestyle campaigns &amp; social feed metrics...</p>
      </div>
    );
  }

  return (
    <div className="brand-page-container">
      {/* Page Header */}
      <div className="brand-page-header">
        <div className="brand-page-title-wrap">
          <div className="brand-page-kicker">
            <span style={{ width: 8, height: 8, borderRadius: '50%', backgroundColor: '#0284c7' }} />
            Brand Registry Marketing Hub / Social &amp; Lifestyle Syndication
          </div>
          <h1 className="brand-page-title" style={{ margin: 0 }}>
            Brand Marketing Posts &amp; Social Feeds
          </h1>
          <p className="brand-page-desc">
            Publish Amazon Brand Posts, manage lifestyle imagery, schedule product spotlight campaigns, and engage verified brand followers across detail pages and storefront feeds.
          </p>
        </div>

        <div className="brand-page-actions">
          {/* DYNAMIC RBAC GUARD: Hide "+ Create Marketing Post" for BRAND_SELLER */}
          {permissions.canCreateMarketingPost ? (
            <button
              type="button"
              className="brand-btn brand-btn-primary"
              onClick={() => setShowCreateModal(true)}
            >
              <span>+ Create Marketing Post</span>
            </button>
          ) : (
            <button
              type="button"
              className="brand-btn brand-btn-outline"
              disabled
              title={`Read-only access: ${activeRole} cannot create marketing posts`}
            >
              <span>🔒 Marketing Creation Disabled</span>
            </button>
          )}
        </div>
      </div>

      {/* RBAC Notice if Seller */}
      {permissions.isSeller && (
        <div className="brand-guard-banner">
          <p>
            <strong>Role Notice:</strong> Logged in as <code>BRAND_SELLER</code>. Marketing creation controls are hidden. You have read-only visibility into live campaign engagement and reach telemetry.
          </p>
        </div>
      )}

      {/* KPI Cards Row (4 Cards) */}
      <div className="brand-kpi-grid" style={{ gridTemplateColumns: 'repeat(4, 1fr)' }}>
        <div className="brand-kpi-card">
          <div className="brand-kpi-header">
            <span>Active Posts</span>
            <div className="brand-kpi-icon">📝</div>
          </div>
          <div className="brand-kpi-value-wrap">
            <span className="brand-kpi-value">{posts.length || 86}</span>
          </div>
          <div className="brand-kpi-subtext">
            <span>+4 this wk</span>
          </div>
        </div>

        <div className="brand-kpi-card">
          <div className="brand-kpi-header">
            <span>Total Reach</span>
            <div className="brand-kpi-icon">👁️</div>
          </div>
          <div className="brand-kpi-value-wrap">
            <span className="brand-kpi-value">482.4K</span>
          </div>
          <div className="brand-kpi-subtext">
            <span>↗ 18.2%</span>
          </div>
        </div>

        <div className="brand-kpi-card">
          <div className="brand-kpi-header">
            <span>Follower CTR</span>
            <div className="brand-kpi-icon">🖱️</div>
          </div>
          <div className="brand-kpi-value-wrap">
            <span className="brand-kpi-value">5.2%</span>
          </div>
          <div className="brand-kpi-subtext muted">
            <span>vs 3.1% cat. avg</span>
          </div>
        </div>

        <div className="brand-kpi-card">
          <div className="brand-kpi-header">
            <span>Store Clicks</span>
            <div className="brand-kpi-icon">🏪</div>
          </div>
          <div className="brand-kpi-value-wrap">
            <span className="brand-kpi-value">1,420</span>
          </div>
          <div className="brand-kpi-subtext">
            <span>+142 direct</span>
          </div>
        </div>
      </div>

      {/* Toolbar Filters */}
      <div className="brand-toolbar">
        <div className="brand-toolbar-group">
          <span style={{ fontSize: '0.8rem', color: '#64748b' }}>🎯</span>
          <select
            className="brand-select"
            value={selectedAsin}
            onChange={(e) => setSelectedAsin(e.target.value)}
          >
            <option value="ALL">Filter by Linked ASIN: ALL (6 ASINs)</option>
            <option value="B09V3HM1LR">B09V3HM1LR (ApexPro Headphones)</option>
            <option value="B08F52C53K">B08F52C53K (VoltCore Battery)</option>
            <option value="B0BFDJM75G">B0BFDJM75G (OmniFlex Stand)</option>
            <option value="B09L7QPR2V">B09L7QPR2V (PowerMatrix Station)</option>
            <option value="B07V2Q7K3W">B07V2Q7K3W (SoundWave Speaker)</option>
            <option value="B09T8P1M4N">B09T8P1M4N (PureShield Screen)</option>
          </select>

          <select
            className="brand-select"
            value={selectedPlacement}
            onChange={(e) => setSelectedPlacement(e.target.value)}
          >
            <option value="ALL">Placement: Storefront + PDP</option>
            <option value="PDP Live Feed">PDP Live Feed</option>
            <option value="Brand Store">Brand Store</option>
            <option value="Follow Feed">Follow Feed</option>
          </select>

          <select className="brand-select" defaultValue="recent">
            <option value="recent">Sort: Most Recent</option>
            <option value="reach">Sort: Highest Reach</option>
            <option value="likes">Sort: Most Likes</option>
          </select>
        </div>

        <div className="brand-toolbar-group">
          <span style={{ fontSize: '0.8rem', color: '#64748b' }}>
            Displaying {filteredPosts.length} of {posts.length} campaigns
          </span>
        </div>
      </div>

      {/* Social Feed Cards Grid (Matching Screenshot 1) */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '1.5rem' }}>
        {filteredPosts.map((post) => (
          <div
            key={post.id}
            className="brand-card"
            style={{
              padding: 0,
              overflow: 'hidden',
              display: 'flex',
              flexDirection: 'column',
              transition: 'transform 0.15s ease, box-shadow 0.15s ease',
            }}
          >
            {/* Image Container with Placement Badge */}
            <div style={{ position: 'relative', width: '100%', height: 210, backgroundColor: '#0f172a' }}>
              <img
                src={post.imageUrl}
                alt={post.caption}
                style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                loading="lazy"
              />
              <span
                style={{
                  position: 'absolute',
                  top: 10,
                  left: 10,
                  background: 'rgba(15, 23, 42, 0.85)',
                  backdropFilter: 'blur(4px)',
                  color: '#ffffff',
                  fontSize: '0.675rem',
                  fontWeight: 600,
                  padding: '0.2rem 0.5rem',
                  borderRadius: 4,
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.3rem',
                }}
              >
                <span style={{ width: 6, height: 6, borderRadius: '50%', backgroundColor: '#38bdf8' }} />
                {post.placement || 'PDP Live Feed'}
              </span>
            </div>

            {/* Post Content */}
            <div style={{ padding: '1rem', display: 'flex', flexDirection: 'column', gap: '0.75rem', flex: 1 }}>
              {/* Linked ASIN Badge & Date */}
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontSize: '0.725rem' }}>
                <span
                  style={{
                    backgroundColor: '#f0fdf4',
                    color: '#0f766e',
                    border: '1px solid #99f6e4',
                    fontWeight: 700,
                    padding: '0.15rem 0.45rem',
                    borderRadius: 4,
                    letterSpacing: '0.02em',
                  }}
                >
                  # ASIN: {post.linkedAsin || 'B09V3HM1LR'}
                </span>
                <span style={{ color: '#64748b' }}>
                  {post.createdAt ? new Date(post.createdAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }) : 'Oct 24, 2024'}
                </span>
              </div>

              {/* Caption */}
              <p
                style={{
                  margin: 0,
                  fontSize: '0.85rem',
                  fontWeight: 500,
                  color: '#1e293b',
                  lineHeight: 1.45,
                  flex: 1,
                }}
              >
                {post.caption}
              </p>

              {/* Footer: Author & Social Metrics (reachCount, likesCount) */}
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  paddingTop: '0.75rem',
                  borderTop: '1px solid #f1f5f9',
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.45rem' }}>
                  <div
                    style={{
                      width: 24,
                      height: 24,
                      borderRadius: '50%',
                      backgroundColor: '#6366f1',
                      color: '#ffffff',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      fontSize: '0.65rem',
                      fontWeight: 700,
                    }}
                  >
                    {userInitials(post.authorName || 'SJ')}
                  </div>
                  <span style={{ fontSize: '0.75rem', fontWeight: 600, color: '#334155' }}>
                    {post.authorName || 'Sarah Jenkins'}
                  </span>
                </div>

                {/* UI REQUIREMENT: Display reachCount and likesCount */}
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', fontSize: '0.75rem', color: '#64748b' }}>
                  <span style={{ display: 'flex', alignItems: 'center', gap: '0.2rem' }}>
                    <span>👁️</span>
                    <strong>{post.reachCount ? `${(post.reachCount / 1000).toFixed(1)}k` : '14.2k'}</strong>
                  </span>
                  <span style={{ display: 'flex', alignItems: 'center', gap: '0.2rem', color: '#047857' }}>
                    <span>❤️</span>
                    <strong>{post.likesCount || 890}</strong>
                  </span>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Modal: Create Marketing Post */}
      {showCreateModal && (
        <div className="brand-modal-backdrop">
          <div className="brand-modal" style={{ maxWidth: 540 }}>
            <div className="brand-modal-header">
              <span className="brand-modal-title">+ Create Marketing Brand Post</span>
              <button
                type="button"
                className="brand-icon-btn"
                onClick={() => setShowCreateModal(false)}
              >
                ✕
              </button>
            </div>
            <form onSubmit={handleCreatePost}>
              <div className="brand-modal-body">
                <div className="brand-form-group">
                  <label className="brand-label">Lifestyle Image Asset URL *</label>
                  <input
                    type="url"
                    className="brand-input"
                    placeholder="https://images.unsplash.com/photo-..."
                    value={imageUrl}
                    onChange={(e) => setImageUrl(e.target.value)}
                    required
                  />
                  <span className="brand-help-text">High-res 16:9 or 1:1 lifestyle visual approved for Storefront syndication.</span>
                </div>

                <div className="brand-form-group">
                  <label className="brand-label">Post Caption &amp; Storytelling *</label>
                  <textarea
                    rows={3}
                    className="brand-textarea"
                    placeholder="e.g. Immersive silence. The ApexPro Gen-4 in Deep Graphite."
                    value={caption}
                    onChange={(e) => setCaption(e.target.value)}
                    required
                  />
                </div>

                <div className="brand-form-row">
                  {/* UI REQUIREMENT: Linked ASIN */}
                  <div className="brand-form-group">
                    <label className="brand-label">Linked Product ASIN *</label>
                    <input
                      type="text"
                      className="brand-input"
                      placeholder="e.g. B09V3HM1LR"
                      value={linkedAsin}
                      onChange={(e) => setLinkedAsin(e.target.value.toUpperCase())}
                      required
                    />
                  </div>

                  {/* Initial Reach / Likes */}
                  <div className="brand-form-group">
                    <label className="brand-label">Reach Count</label>
                    <input
                      type="number"
                      className="brand-input"
                      placeholder="0"
                      value={reachCount}
                      onChange={(e) => setReachCount(Number(e.target.value))}
                    />
                  </div>

                  <div className="brand-form-group">
                    <label className="brand-label">Likes Count</label>
                    <input
                      type="number"
                      className="brand-input"
                      placeholder="0"
                      value={likesCount}
                      onChange={(e) => setLikesCount(Number(e.target.value))}
                    />
                  </div>
                </div>
              </div>

              <div className="brand-modal-footer">
                <button
                  type="button"
                  className="brand-btn brand-btn-outline"
                  onClick={() => setShowCreateModal(false)}
                >
                  Cancel
                </button>
                <button type="submit" className="brand-btn brand-btn-primary">
                  Publish to Storefront Feed
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
