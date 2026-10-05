// ─── Snapshot Models ─────────────────────────────────────────────────────────
// Mirrors com.project.DomainRegistrationLive.dto.SnapshotModels

export interface KeywordEntry {
  word: string;
  count: number;
  pfx: number;
  sfx: number;
  stop: boolean;
}

export interface TldEntry {
  tld: string;
  count: number;
  share: number;
}

export interface RegistrarEntry {
  registrarId: number | null;
  registrar: string;
  count: number;
}

export interface RisingEntry {
  word: string;
  recent: number;
  prior: number;
  lift: number;
}

export interface WordEntry {
  word: string;
  count: number;
}

export interface FormatEntry {
  total: number;
  multiword: number;
  oneword: number;
  numeric: number;
  hyphen: number;
  short5: number;
}

export interface RepeatEntry {
  sld: string;
  words: string[];
  tlds: number;
  tldList: string[];
}

export interface HourEntry {
  hr: string;
  count: number;
}

export interface MoverEntry {
  word: string;
  today: number;
  baseline: number;
  lift: number;
}

export interface FeedEntry {
  seq: number;
  domain: string;
  tld: string;
  registrarId: number | null;
  registrar: string;
  t: number; // epoch ms
}

export interface NowStats {
  h60: number;
  m10: number;
  m1: number;
  perMin: number;
  topKeyword: KeywordEntry;
  topKeywords: KeywordEntry[];
  tlds: TldEntry[];
  registrars: RegistrarEntry[];
  risingKeywords: Record<"15m" | "1h" | "3h", RisingEntry[]>;
  prefixes: WordEntry[];
  suffixes: WordEntry[];
  format: FormatEntry;
  repeats: RepeatEntry[];
  hourly: HourEntry[];
}

export interface TodayStats {
  movers: MoverEntry[];
}

export interface StatsPayload {
  snapshotId?: number;
  updatedAt?: number;
  serverNow?: number;
  builtAt?: number;
  lastCycleAt: number | null;
  now: NowStats;
  today: TodayStats;
  feed: FeedEntry[];
}

// ─── Feed API ─────────────────────────────────────────────────────────────────

export interface FeedResponse {
  items: FeedEntry[];
  latestRegisteredAt: string;
}

// ─── Search API ───────────────────────────────────────────────────────────────

export interface SearchEntry {
  seq: number;
  domain: string;
  tld: string;
  registrarId: number;
  registrar: string;
  t: number; // epoch ms
}

export interface SearchResponse {
  keyword: string;
  entries: SearchEntry[];
}

// ─── Daily API ────────────────────────────────────────────────────────────────

export interface DailyResponse {
  date: string;
  entries: RegistrarEntry[];
}

