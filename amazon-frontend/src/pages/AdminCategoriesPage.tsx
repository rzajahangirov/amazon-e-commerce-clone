import { usePendingCategories } from '../hooks/usePendingCategories';

export function AdminCategoriesPage() {
  const { categories, loading, error, refetch } = usePendingCategories();

  return (
    <>
      <header className="page-header">
        <div>
          <h1>Category Governance</h1>
          <p>Review brand-proposed taxonomy changes awaiting platform approval.</p>
        </div>
        <button type="button" onClick={() => void refetch()}>
          Refresh
        </button>
      </header>

      <section className="panel">
        {loading && <div className="state-message loading">Loading pending categories…</div>}
        {error && <div className="state-message error">{error}</div>}

        {!loading && !error && categories.length === 0 && (
          <div className="state-message loading">No pending category proposals.</div>
        )}

        {!loading &&
          !error &&
          categories.map((category) => (
            <article key={category.id} className="category-card">
              <h3>{category.name}</h3>
              <p>
                Slug: <code>{category.slug}</code> • Level {category.level}
              </p>
              {category.commercialJustification ? (
                <div className="justification">
                  <strong>Commercial justification</strong>
                  <p style={{ margin: 0 }}>{category.commercialJustification}</p>
                </div>
              ) : (
                <div className="justification">
                  <strong>Commercial justification</strong>
                  <p style={{ margin: 0, color: '#64748b' }}>
                    No justification provided by the submitter.
                  </p>
                </div>
              )}
            </article>
          ))}
      </section>
    </>
  );
}
