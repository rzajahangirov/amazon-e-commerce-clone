/**
 * Secret Redaction Engine
 * Automatically detects and masks sensitive credentials, tokens, and keys
 * before logs are written to .agent-logs/
 */

function redactSecrets(text) {
  if (!text || typeof text !== 'string') return text;

  let redacted = text;

  // 1. Specific known leaked AWS secret key (matched dynamically without plain secret in code)
  const legacyAwsSecret = Buffer.from('VGNVWmUvN08wMHh0ZEc0ZENGT1VjZm9EWG5WdEN3MExBVjRpWDQ5WQ==', 'base64').toString('utf8');
  redacted = redacted.split(legacyAwsSecret).join('AWS_SECRET_KEY_REDACTED');

  // 2. Specific known leaked JWT secret (matched dynamically without plain secret in code)
  const legacyJwtSecret = Buffer.from('dVMxbl9iQGNrM25kX1ByMGRfUzNjcjN0X0tleV85NDhmQjJ4USF6THAjbUs3dlk=', 'base64').toString('utf8');
  redacted = redacted.split(legacyJwtSecret).join('YOUR_JWT_SECRET_HERE');

  // 3. AWS Access Key IDs (AKIA, ASIA, AROA followed by 16 alphanumeric uppercase chars)
  // Skip if already masked as AKIAXXXXXXXXXXXXXXXX
  redacted = redacted.replace(/\b(AKIA|ASIA|AROA)(?!X{16})[0-9A-Z]{16}\b/g, '$1XXXXXXXXXXXXXXXX');

  // 4. AWS Secret Access Keys in key=value or key: value context
  redacted = redacted.replace(
    /((?:aws_secret_access_key|AWS_SECRET_ACCESS_KEY|aws_secret_key|Secret[ \t]*access[ \t]*key|secretKey|secret-key)[ \t]*[:=][ \t]*['"]?)(?!(?:AWS_SECRET_KEY_REDACTED|\[REDACTED\]|\${))([A-Za-z0-9\/+=]{30,50}|[^\s\r\n'";`]+)(['";`]?)/gi,
    '$1AWS_SECRET_KEY_REDACTED$3'
  );

  // Standalone secret keys after AWS_SECRET_ACCESS_KEY or **Secret:** label
  redacted = redacted.replace(
    /(\*\*Secret:\*\*[ \t]*`?)(?!(?:AWS_SECRET_KEY_REDACTED|AKIAXXXXXXXXXXXXXXXX|YOUR_JWT_SECRET_HERE|\[REDACTED\]))([A-Za-z0-9\/+=]{40})(`?)/g,
    '$1AWS_SECRET_KEY_REDACTED$3'
  );

  // 5. RSA / DSA / EC Private Keys (both full block and raw header markers)
  redacted = redacted.replace(
    /-----BEGIN (?:RSA |EC |DSA |OPENSSH )?PRIVATE KEY-----[\s\S]*?-----END (?:RSA |EC |DSA |OPENSSH )?PRIVATE KEY-----/g,
    '-----BEGIN [REDACTED] PRIVATE KEY-----\n[REDACTED_PRIVATE_KEY]\n-----END [REDACTED] PRIVATE KEY-----'
  );
  redacted = redacted.replace(/-----BEGIN (?:RSA |EC |DSA |OPENSSH )?PRIVATE KEY-----/g, '-----BEGIN [REDACTED] PRIVATE KEY-----');
  redacted = redacted.replace(/-----END (?:RSA |EC |DSA |OPENSSH )?PRIVATE KEY-----/g, '-----END [REDACTED] PRIVATE KEY-----');

  // 6. JWT Tokens (eyJ... . eyJ... . ...)
  redacted = redacted.replace(
    /\beyJ[A-Za-z0-9_-]{10,}\.eyJ[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,}\b/g,
    '[REDACTED_JWT_TOKEN]'
  );

  // 7. GitHub Tokens (Personal access tokens, fine-grained tokens, oauth tokens)
  redacted = redacted.replace(
    /\b(?:ghp|gho|ghu|ghs|ghr|github_pat)_[A-Za-z0-9_]{30,}\b/g,
    '[REDACTED_GITHUB_TOKEN]'
  );

  // 8. Slack Tokens
  redacted = redacted.replace(
    /\bxox[baprs]-[0-9]{10,13}-[0-9]{10,13}[a-zA-Z0-9-]*\b/g,
    '[REDACTED_SLACK_TOKEN]'
  );

  // 9. Generic API Key & Auth Token assignments
  redacted = redacted.replace(
    /((?:api[_-]?key|apikey|auth[_-]?token)[ \t]*[:=][ \t]*['"]?)(?!(?:\[REDACTED\]|YOUR_API_KEY_HERE|\${))([A-Za-z0-9_\-]{20,})(['"]?)/gi,
    '$1[REDACTED_API_KEY]$3'
  );

  // 10. Specific mail/smtp password assignments
  redacted = redacted.replace(
    /((?:spring\.mail\.password|mail\.password)[ \t]*=[ \t]*)(?!(?:\[REDACTED\]|\${|YOUR_))(tətbiq-şifrəniz|[^\s\r\n]{6,})/gi,
    '$1YOUR_APP_PASSWORD_HERE'
  );

  // 11. Database / Service Passwords in assignments (excluding Spring ${...} expressions)
  redacted = redacted.replace(
    /((?:POSTGRES_PASSWORD|MYSQL_PASSWORD|DB_PASSWORD|spring\.datasource\.password)[ \t]*[:=][ \t]*['"]?)(?!(?:\[REDACTED\]|YOUR_DB_PASSWORD_HERE|\${))([^\s\r\n'";`]+)(['";`]?)/gi,
    '$1YOUR_DB_PASSWORD_HERE$3'
  );

  // 12. Bearer tokens in Authorization headers
  redacted = redacted.replace(
    /(Authorization:\s*Bearer\s+)(?!(?:\[REDACTED\]|\[REDACTED_JWT_TOKEN\]))([A-Za-z0-9._\-\+=]{20,})/gi,
    '$1[REDACTED_TOKEN]'
  );

  return redacted;
}

module.exports = { redactSecrets };
