import React, { useState } from 'react';
import { useCustomerAuth } from '../../context/CustomerAuthContext';

export const AuthModal: React.FC = () => {
  const {
    authModalOpen,
    authModalMode,
    authModalMessage,
    closeAuthModal,
    openAuthModal,
    login,
    register,
  } = useCustomerAuth();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [phone, setPhone] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!authModalOpen) return null;

  const isSignIn = authModalMode === 'signin';

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      if (isSignIn) {
        await login(email, password);
      } else {
        await register({ fullName, email, password, phone: phone || undefined });
      }
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('Authentication failed. Please verify your credentials.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleDemoFill = (role: 'customer' | 'brand' | 'admin' = 'customer') => {
    setPassword('Password123!');
    if (role === 'brand') {
      setEmail('nike.owner@test.com');
    } else if (role === 'admin') {
      setEmail('admin@amazon-platform.com');
    } else {
      setEmail('customer1@test.com');
    }
  };

  return (
    <div
      style={{
        position: 'fixed',
        inset: 0,
        zIndex: 9999,
        backgroundColor: 'rgba(0, 0, 0, 0.65)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '16px',
        backdropFilter: 'blur(3px)',
      }}
      onClick={closeAuthModal}
    >
      <div
        style={{
          backgroundColor: '#ffffff',
          borderRadius: '8px',
          width: '100%',
          maxWidth: '420px',
          boxShadow: '0 10px 25px rgba(0,0,0,0.3)',
          overflow: 'hidden',
          animation: 'fadeInModal 0.2s ease-out',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div
          style={{
            backgroundColor: '#131921',
            padding: '16px 20px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            borderBottom: '1px solid #232f3e',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span style={{ color: '#ffffff', fontSize: '1.25rem', fontWeight: 800, letterSpacing: '-0.5px' }}>
              amazon<span style={{ color: '#ff9900' }}>.enterprise</span>
            </span>
          </div>
          <button
            type="button"
            onClick={closeAuthModal}
            style={{
              background: 'none',
              border: 'none',
              color: '#ffffff',
              fontSize: '1.2rem',
              cursor: 'pointer',
              lineHeight: 1,
            }}
          >
            ✕
          </button>
        </div>

        {/* Form Body */}
        <div style={{ padding: '24px' }}>
          {authModalMessage && (
            <div
              style={{
                backgroundColor: '#f0f8ff',
                borderLeft: '4px solid #007185',
                padding: '10px 12px',
                borderRadius: '4px',
                fontSize: '0.84rem',
                color: '#0f1111',
                marginBottom: '16px',
              }}
            >
              {authModalMessage}
            </div>
          )}

          <h2 style={{ fontSize: '1.4rem', fontWeight: 600, color: '#0f1111', margin: '0 0 16px 0' }}>
            {isSignIn ? 'Sign in' : 'Create an account'}
          </h2>

          {error && (
            <div
              style={{
                backgroundColor: '#fff0f0',
                border: '1px solid #ffd8d8',
                borderRadius: '4px',
                padding: '10px 12px',
                color: '#cc0c39',
                fontSize: '0.84rem',
                marginBottom: '16px',
              }}
            >
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            {!isSignIn && (
              <div style={{ marginBottom: '14px' }}>
                <label
                  style={{ display: 'block', fontSize: '0.84rem', fontWeight: 700, marginBottom: '4px', color: '#0f1111' }}
                >
                  Your name
                </label>
                <input
                  type="text"
                  required
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  placeholder="First and last name"
                  style={{
                    width: '100%',
                    padding: '8px 12px',
                    borderRadius: '4px',
                    border: '1px solid #888c8c',
                    fontSize: '0.9rem',
                    boxSizing: 'border-box',
                  }}
                />
              </div>
            )}

            <div style={{ marginBottom: '14px' }}>
              <label
                style={{ display: 'block', fontSize: '0.84rem', fontWeight: 700, marginBottom: '4px', color: '#0f1111' }}
              >
                Email
              </label>
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="name@company.com"
                style={{
                  width: '100%',
                  padding: '8px 12px',
                  borderRadius: '4px',
                  border: '1px solid #888c8c',
                  fontSize: '0.9rem',
                  boxSizing: 'border-box',
                }}
              />
            </div>

            <div style={{ marginBottom: '14px' }}>
              <label
                style={{ display: 'block', fontSize: '0.84rem', fontWeight: 700, marginBottom: '4px', color: '#0f1111' }}
              >
                Password
              </label>
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="At least 6 characters"
                style={{
                  width: '100%',
                  padding: '8px 12px',
                  borderRadius: '4px',
                  border: '1px solid #888c8c',
                  fontSize: '0.9rem',
                  boxSizing: 'border-box',
                }}
              />
            </div>

            {!isSignIn && (
              <div style={{ marginBottom: '14px' }}>
                <label
                  style={{ display: 'block', fontSize: '0.84rem', fontWeight: 700, marginBottom: '4px', color: '#0f1111' }}
                >
                  Phone number (optional)
                </label>
                <input
                  type="tel"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  placeholder="+1 (555) 000-0000"
                  style={{
                    width: '100%',
                    padding: '8px 12px',
                    borderRadius: '4px',
                    border: '1px solid #888c8c',
                    fontSize: '0.9rem',
                    boxSizing: 'border-box',
                  }}
                />
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              style={{
                width: '100%',
                padding: '10px',
                borderRadius: '8px',
                border: '1px solid #ffd814',
                backgroundColor: '#ffd814',
                color: '#0f1111',
                fontSize: '0.9rem',
                fontWeight: 600,
                cursor: loading ? 'not-allowed' : 'pointer',
                boxShadow: '0 2px 5px rgba(213, 217, 217, 0.5)',
                marginBottom: '16px',
              }}
            >
              {loading ? 'Processing...' : isSignIn ? 'Sign in' : 'Create your account'}
            </button>
          </form>

          {/* Quick Demo Fill */}
          {isSignIn && (
            <div style={{ marginBottom: '16px', display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <div style={{ fontSize: '0.72rem', color: '#565959', fontWeight: 600, textAlign: 'center' }}>
                Quick Test Credentials:
              </div>
              <div style={{ display: 'flex', gap: '6px', justifyContent: 'center', flexWrap: 'wrap' }}>
                <button
                  type="button"
                  onClick={() => handleDemoFill('customer')}
                  style={{
                    background: 'none',
                    border: '1px dashed #007185',
                    color: '#007185',
                    padding: '4px 8px',
                    borderRadius: '4px',
                    fontSize: '0.74rem',
                    cursor: 'pointer',
                    fontWeight: 600,
                  }}
                  title="customer1@test.com"
                >
                  Customer
                </button>
                <button
                  type="button"
                  onClick={() => handleDemoFill('brand')}
                  style={{
                    background: 'none',
                    border: '1px dashed #2563eb',
                    color: '#2563eb',
                    padding: '4px 8px',
                    borderRadius: '4px',
                    fontSize: '0.74rem',
                    cursor: 'pointer',
                    fontWeight: 600,
                  }}
                  title="nike.owner@test.com"
                >
                  Nike Brand Owner
                </button>
                <button
                  type="button"
                  onClick={() => handleDemoFill('admin')}
                  style={{
                    background: 'none',
                    border: '1px dashed #7c3aed',
                    color: '#7c3aed',
                    padding: '4px 8px',
                    borderRadius: '4px',
                    fontSize: '0.74rem',
                    cursor: 'pointer',
                    fontWeight: 600,
                  }}
                  title="admin@amazon-platform.com"
                >
                  Admin
                </button>
              </div>
            </div>
          )}

          {/* Switch Mode */}
          <div
            style={{
              borderTop: '1px solid #e7e7e7',
              paddingTop: '16px',
              textAlign: 'center',
              fontSize: '0.84rem',
            }}
          >
            {isSignIn ? (
              <span>
                New to Amazon?{' '}
                <button
                  type="button"
                  onClick={() => openAuthModal('register')}
                  style={{
                    background: 'none',
                    border: 'none',
                    color: '#007185',
                    cursor: 'pointer',
                    fontWeight: 600,
                    textDecoration: 'underline',
                    padding: 0,
                  }}
                >
                  Create your Amazon account
                </button>
              </span>
            ) : (
              <span>
                Already have an account?{' '}
                <button
                  type="button"
                  onClick={() => openAuthModal('signin')}
                  style={{
                    background: 'none',
                    border: 'none',
                    color: '#007185',
                    cursor: 'pointer',
                    fontWeight: 600,
                    textDecoration: 'underline',
                    padding: 0,
                  }}
                >
                  Sign in
                </button>
              </span>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
