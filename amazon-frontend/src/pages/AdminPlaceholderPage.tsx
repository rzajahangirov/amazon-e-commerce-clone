interface AdminPlaceholderPageProps {
  title: string;
  description: string;
}

export function AdminPlaceholderPage({ title, description }: AdminPlaceholderPageProps) {
  return (
    <>
      <header className="page-header">
        <div>
          <h1>{title}</h1>
          <p>{description}</p>
        </div>
      </header>
      <section className="panel">
        <p style={{ margin: 0, color: '#64748b' }}>
          This screen is wired in the admin shell. Connect additional brand-management endpoints
          from the admin API documentation when ready.
        </p>
      </section>
    </>
  );
}
