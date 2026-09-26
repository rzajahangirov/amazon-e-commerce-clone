#!/usr/bin/env node
/**
 * Agent Capture Hook for Antigravity IDE
 * Captures verbatim prompts and final responses per turn to .agent-logs/
 */

const fs = require('fs');
const path = require('path');
const { redactSecrets } = require(path.join(__dirname, 'redactor.js'));

// Repo root is parent of .agents
const repoRoot = path.resolve(__dirname, '..');
const logsDir = path.join(repoRoot, '.agent-logs');

// App data directory for Antigravity IDE
const defaultBrainDir = path.join(
  process.env.USERPROFILE || process.env.HOME || '',
  '.gemini',
  'antigravity-ide',
  'brain'
);

function formatTimestampForFilename(isoStr) {
  // Converts 2026-08-28T09:14:02.118Z -> 2026-08-28_09-14-02
  const d = new Date(isoStr);
  const pad = (n) => String(n).padStart(2, '0');
  const YYYY = d.getUTCFullYear();
  const MM = pad(d.getUTCMonth() + 1);
  const DD = pad(d.getUTCDate());
  const HH = pad(d.getUTCHours());
  const mm = pad(d.getUTCMinutes());
  const ss = pad(d.getUTCSeconds());
  return `${YYYY}-${MM}-${DD}_${HH}-${mm}-${ss}`;
}

function formatDate(isoStr) {
  const d = new Date(isoStr);
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getUTCFullYear()}-${pad(d.getUTCMonth() + 1)}-${pad(d.getUTCDate())}`;
}

function extractPrompt(rawContent) {
  if (!rawContent) return '';
  const match = rawContent.match(/<USER_REQUEST>([\s\S]*?)<\/USER_REQUEST>/);
  if (match) {
    return match[1].trim();
  }
  return rawContent.trim();
}

function parseTranscript(transcriptFullPath) {
  if (!fs.existsSync(transcriptFullPath)) return null;
  let content = '';
  try {
    content = fs.readFileSync(transcriptFullPath, 'utf8');
  } catch {
    return null;
  }
  if (!content.trim()) return null;

  const lines = content.trim().split('\n').filter(Boolean);
  const exchanges = [];
  let currentExchange = null;
  let detectedModel = 'gemini-3.8-flash';

  for (const line of lines) {
    let step;
    try {
      step = JSON.parse(line);
    } catch {
      continue;
    }

    // Check for model change in user settings or metadata
    if (step.content && typeof step.content === 'string') {
      const modelMatch = step.content.match(/Model Selection` from .*? to (.*?)(?:\.\s*No need|\.$|\n)/);
      if (modelMatch) {
        detectedModel = modelMatch[1].trim().toLowerCase().replace(/\s+/g, '-').replace(/[()]/g, '');
      }
    }

    if (step.type === 'USER_INPUT') {
      currentExchange = {
        num: exchanges.length + 1,
        promptTime: step.created_at || new Date().toISOString(),
        prompt: extractPrompt(step.content),
        model: detectedModel,
        responseTime: null,
        response: null
      };
      exchanges.push(currentExchange);
    } else if (
      step.source === 'MODEL' &&
      step.type === 'PLANNER_RESPONSE' &&
      step.content &&
      typeof step.content === 'string' &&
      step.content.trim()
    ) {
      if (currentExchange) {
        currentExchange.responseTime = step.created_at || new Date().toISOString();
        currentExchange.response = step.content.trim();
        if (!currentExchange.model) {
          currentExchange.model = detectedModel;
        }
      }
    }
  }

  return { exchanges, detectedModel };
}

function generateMarkdownLog(sessionId, exchanges, modelName, author, project) {
  if (!exchanges || exchanges.length === 0) return null;

  const shortId = sessionId.slice(0, 8);
  const firstPromptTime = exchanges[0].promptTime;
  const lastPromptTime = exchanges[exchanges.length - 1].promptTime;
  const logDate = formatDate(firstPromptTime);
  const totalExchanges = exchanges.length;
  const model = exchanges[0].model || modelName || 'gemini-3.8-flash';

  let md = `---
session_id: ${sessionId}
date: ${logDate}
author: ${author}
model: ${model}
tool: antigravity
project: ${project}
total_exchanges: ${totalExchanges}
first_prompt_time: ${firstPromptTime}
last_prompt_time: ${lastPromptTime}
---

# Session Log - ${logDate}

Session: \`${shortId}\` | Project: \`${project}\` | Author: \`${author}\`

---
`;

  for (const ex of exchanges) {
    const sanitizedPrompt = redactSecrets(ex.prompt);
    md += `\n[LOG_ENTRY type=PROMPT num=${ex.num} session=${shortId}]\n`;
    md += `timestamp: ${ex.promptTime}\n`;
    md += `model: ${ex.model || model}\n\n`;
    md += `${sanitizedPrompt}\n\n`;

    if (ex.response) {
      const sanitizedResponse = redactSecrets(ex.response);
      md += `\n[LOG_ENTRY type=RESPONSE num=${ex.num} session=${shortId}]\n`;
      md += `timestamp: ${ex.responseTime || ex.promptTime}\n`;
      md += `model: ${ex.model || model}\n\n`;
      md += `${sanitizedResponse}\n\n`;
    }
  }

  // Safety net: redact the entire generated markdown before returning
  md = redactSecrets(md);

  return { md, firstPromptTime };
}

function processSession(sessionId, transcriptPath = null) {
  if (!sessionId) return false;
  if (!fs.existsSync(logsDir)) {
    fs.mkdirSync(logsDir, { recursive: true });
  }

  let fullTranscriptPath = transcriptPath;
  if (!fullTranscriptPath) {
    fullTranscriptPath = path.join(defaultBrainDir, sessionId, '.system_generated', 'logs', 'transcript_full.jsonl');
  }

  // If transcriptPath points to transcript.jsonl, check transcript_full.jsonl
  if (fullTranscriptPath.endsWith('transcript.jsonl')) {
    const fullAlternative = path.join(path.dirname(fullTranscriptPath), 'transcript_full.jsonl');
    if (fs.existsSync(fullAlternative)) {
      fullTranscriptPath = fullAlternative;
    }
  }

  if (!fs.existsSync(fullTranscriptPath)) {
    return false;
  }

  try {
    const transcriptStat = fs.statSync(fullTranscriptPath);
    const lastMtime = sessionMtimeCache.get(sessionId) || 0;
    if (transcriptStat.mtimeMs <= lastMtime) {
      return false; // No changes since last check
    }

    const result = parseTranscript(fullTranscriptPath);
    if (!result || result.exchanges.length === 0) return false;

    const author = 'rzajahangirov';
    const project = 'amazon-e-commerce-clone';
    const generated = generateMarkdownLog(sessionId, result.exchanges, result.detectedModel, author, project);
    if (!generated) return false;

    const timePrefix = formatTimestampForFilename(generated.firstPromptTime);
    const targetFileName = `${timePrefix}_${sessionId}.md`;
    const targetFilePath = path.join(logsDir, targetFileName);

    // Remove any previous file for this session if timestamp changed
    const existingFiles = fs.readdirSync(logsDir).filter(f => f.endsWith(`_${sessionId}.md`));
    for (const f of existingFiles) {
      if (f !== targetFileName) {
        try { fs.unlinkSync(path.join(logsDir, f)); } catch {}
      }
    }

    // Check if content actually changed before writing
    if (fs.existsSync(targetFilePath)) {
      try {
        const existingContent = fs.readFileSync(targetFilePath, 'utf8');
        if (existingContent === generated.md) {
          sessionMtimeCache.set(sessionId, transcriptStat.mtimeMs);
          return targetFilePath;
        }
      } catch {}
    }

    fs.writeFileSync(targetFilePath, generated.md, 'utf8');
    sessionMtimeCache.set(sessionId, transcriptStat.mtimeMs);
    return targetFilePath;
  } catch (err) {
    // Ignore transient file lock errors
    return false;
  }
}

const sessionMtimeCache = new Map();

function processAllSessions() {
  if (!fs.existsSync(defaultBrainDir)) return;
  const sessions = fs.readdirSync(defaultBrainDir).filter(f => {
    try {
      const stats = fs.statSync(path.join(defaultBrainDir, f));
      return stats.isDirectory();
    } catch {
      return false;
    }
  });
  for (const s of sessions) {
    processSession(s);
  }
}

function watchMode() {
  processAllSessions();

  if (!fs.existsSync(defaultBrainDir)) return;

  try {
    fs.watch(defaultBrainDir, { recursive: true }, (eventType, filename) => {
      if (filename && filename.includes('transcript')) {
        const parts = filename.split(/[/\\]/);
        const sessionId = parts[0];
        if (sessionId) {
          processSession(sessionId);
        }
      }
    });
  } catch {}

  // Periodic poll every 2 seconds
  setInterval(() => {
    processAllSessions();
  }, 2000);
}

function readStdin() {
  return new Promise((resolve) => {
    if (process.stdin.isTTY) {
      return resolve(null);
    }
    let data = '';
    let resolved = false;

    const onData = (chunk) => {
      data += chunk;
    };

    process.stdin.on('data', onData);
    process.stdin.on('end', () => {
      if (!resolved) {
        resolved = true;
        resolve(data || null);
      }
    });
    process.stdin.on('error', () => {
      if (!resolved) {
        resolved = true;
        resolve(null);
      }
    });

    // Timeout if no data received
    setTimeout(() => {
      if (!resolved) {
        resolved = true;
        process.stdin.removeListener('data', onData);
        process.stdin.pause();
        resolve(data || null);
      }
    }, 150);
  });
}

async function main() {
  const args = process.argv.slice(2);

  if (args.includes('--watch')) {
    watchMode();
    return;
  }

  let hookPayload = null;
  const stdinRaw = await readStdin();
  if (stdinRaw) {
    try {
      hookPayload = JSON.parse(stdinRaw);
    } catch {}
  }

  let targetSession = hookPayload?.conversationId;
  let transcriptPath = hookPayload?.transcriptPath;

  for (let i = 0; i < args.length; i++) {
    if (args[i] === '--session' && args[i + 1]) {
      targetSession = args[i + 1];
      i++;
    } else if (args[i] === '--all') {
      processAllSessions();
      console.log(JSON.stringify({}));
      return;
    }
  }

  if (targetSession) {
    processSession(targetSession, transcriptPath);
  } else {
    processAllSessions();
  }

  // Hook response contract
  console.log(JSON.stringify({}));
}

main().catch((err) => {
  console.log(JSON.stringify({}));
});
