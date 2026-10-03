import type { StatsPayload, FeedEntry } from "@/types/api";

const BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

// ─── Snapshot (server-side safe) ──────────────────────────────────────────────

export async function fetchSnapshot(): Promise<StatsPayload> {
  const res = await fetch(`${BASE_URL}/v1/snapshot`, {
    next: { revalidate: 10 },
  });
  if (!res.ok) throw new Error(`Snapshot fetch failed: ${res.status}`);
  return res.json();
}

// ─── Feed (client-side polling) ───────────────────────────────────────────────

export async function fetchFeed(afterSeq?: number): Promise<FeedEntry[]> {
  const url = afterSeq != null
    ? `${BASE_URL}/v1/feed?afterSeq=${afterSeq}`
    : `${BASE_URL}/v1/feed`;
  const res = await fetch(url, { cache: "no-store" });
  if (!res.ok) throw new Error(`Feed fetch failed: ${res.status}`);
  return res.json();
}

// ─── Mock data for development (when backend is not running) ──────────────────

export function getMockSnapshot(): StatsPayload {
  return {
    builtAt: Date.now(),
    lastCycleAt: Date.now() - 8000,
    now: {
      h60: 8517,
      m10: 1363,
      m1: 116,
      perMin: 142,
      topKeyword: { word: "Para", count: 52, pfx: 30, sfx: 22, stop: false },
      topKeywords: [
        { word: "Group", count: 51, pfx: 20, sfx: 31, stop: false },
        { word: "Life", count: 50, pfx: 18, sfx: 32, stop: false },
        { word: "Studio", count: 45, pfx: 12, sfx: 33, stop: false },
        { word: "Line", count: 41, pfx: 15, sfx: 26, stop: false },
        { word: "App", count: 39, pfx: 14, sfx: 25, stop: false },
        { word: "Services", count: 38, pfx: 10, sfx: 28, stop: false },
        { word: "Hub", count: 30, pfx: 8, sfx: 22, stop: false },
        { word: "Labs", count: 27, pfx: 9, sfx: 18, stop: false },
        { word: "Solutions", count: 26, pfx: 7, sfx: 19, stop: false },
        { word: "Store", count: 26, pfx: 6, sfx: 20, stop: false },
      ],
      tlds: [
        { tld: ".com", count: 5036, share: 65.6 },
        { tld: ".xyz", count: 551, share: 7.2 },
        { tld: ".org", count: 386, share: 5.0 },
        { tld: ".online", count: 317, share: 4.1 },
        { tld: ".ai", count: 266, share: 3.5 },
        { tld: ".net", count: 241, share: 3.1 },
        { tld: ".app", count: 177, share: 2.3 },
      ],
      registrars: [
        { registrarId: 1, registrar: "GoDaddy.com", count: 1291 },
        { registrarId: 2, registrar: "Cloudflare", count: 892 },
        { registrarId: 3, registrar: "Namecheap", count: 704 },
        { registrarId: 4, registrar: "GMO Internet Group, In...", count: 637 },
        { registrarId: 5, registrar: "HOSTINGER operations", count: 570 },
        { registrarId: 6, registrar: "Squarespace Domains", count: 519 },
        { registrarId: 7, registrar: "OpusDNS", count: 406 },
        { registrarId: 8, registrar: "Spaceship", count: 355 },
        { registrarId: 9, registrar: "Dynadot", count: 253 },
        { registrarId: 10, registrar: "Porkbun", count: 252 },
        { registrarId: 11, registrar: "Name.com", count: 239 },
        { registrarId: 12, registrar: "Wix.com", count: 209 },
        { registrarId: 13, registrar: "Tucows Domains", count: 184 },
        { registrarId: 14, registrar: "NameSilo", count: 113 },
        { registrarId: 15, registrar: "Dominet (HK)", count: 109 },
      ],
      risingKeywords: {
        "15m": [
          { word: "Heyfrankie", recent: 71, prior: 0, lift: 0 },
          { word: "Para", recent: 53, prior: 2, lift: 26.5 },
          { word: "Mba", recent: 51, prior: 2, lift: 25.5 },
          { word: "Vancouver", recent: 15, prior: 0, lift: 0 },
        ],
        "1h": [
          { word: "Para", recent: 53, prior: 2, lift: 26.5 },
          { word: "Neighborhood", recent: 25, prior: 2, lift: 12.5 },
          { word: "Second", recent: 51, prior: 5, lift: 10.2 },
          { word: "Roofing", recent: 32, prior: 5, lift: 6.4 },
          { word: "Arch", recent: 21, prior: 4, lift: 5.3 },
          { word: "Iron", recent: 42, prior: 10, lift: 4.7 },
          { word: "Fix", recent: 28, prior: 6, lift: 4.7 },
          { word: "Betzazino", recent: 22, prior: 5, lift: 4.4 },
          { word: "Solar", recent: 62, prior: 15, lift: 4.1 },
          { word: "Vista", recent: 23, prior: 6, lift: 3.8 },
          { word: "Partner", recent: 15, prior: 4, lift: 3.8 },
        ],
        "3h": [
          { word: "Heyfrankie", recent: 71, prior: 0, lift: 0 },
          { word: "Para", recent: 53, prior: 2, lift: 26.5 },
          { word: "Mba", recent: 51, prior: 2, lift: 25.5 },
          { word: "Vancouver", recent: 15, prior: 0, lift: 0 },
          { word: "Neighborhood", recent: 25, prior: 2, lift: 12.5 },
          { word: "Second", recent: 51, prior: 5, lift: 10.2 },
          { word: "Roofing", recent: 32, prior: 5, lift: 6.4 },
          { word: "Arch", recent: 21, prior: 4, lift: 5.3 },
          { word: "Iron", recent: 42, prior: 10, lift: 4.7 },
          { word: "Fix", recent: 28, prior: 6, lift: 4.7 },
        ],
      },
      prefixes: [
        { word: "Para", count: 51 },
        { word: "Iron", count: 32 },
        { word: "New", count: 16 },
        { word: "Try", count: 16 },
        { word: "Life", count: 14 },
        { word: "Tank", count: 14 },
        { word: "Bekapwa", count: 13 },
        { word: "Dna", count: 12 },
        { word: "Bitcoin", count: 12 },
        { word: "Car", count: 11 },
      ],
      suffixes: [
        { word: "Studio", count: 43 },
        { word: "Group", count: 41 },
        { word: "App", count: 34 },
        { word: "Services", count: 30 },
        { word: "Hub", count: 30 },
        { word: "Labs", count: 27 },
        { word: "Solutions", count: 26 },
        { word: "Store", count: 26 },
        { word: "Lab", count: 25 },
        { word: "Shop", count: 21 },
      ],
      format: {
        total: 8517,
        multiword: 5343,
        oneword: 3175,
        numeric: 37,
        hyphen: 696,
        short5: 375,
      },
      hourly: [
        { hr: "2026-10-02T14:00:00Z", count: 8200 },
        { hr: "2026-10-02T15:00:00Z", count: 8517 },
      ],
    },
    today: {
      movers: [
        { word: "Avalabs", today: 67, baseline: 1, lift: 67 },
        { word: "Fundr", today: 56, baseline: 1, lift: 56 },
        { word: "Nexolo", today: 50, baseline: 1, lift: 50 },
        { word: "Nbalive", today: 50, baseline: 1, lift: 50 },
        { word: "Kristall", today: 96, baseline: 2, lift: 48 },
        { word: "Fundly", today: 48, baseline: 1, lift: 48 },
        { word: "Riverhead", today: 47, baseline: 1, lift: 47 },
        { word: "Solomeo", today: 46, baseline: 1, lift: 46 },
        { word: "Dimerian", today: 42, baseline: 1, lift: 42 },
        { word: "Dominate", today: 106, baseline: 2.7, lift: 39.8 },
        { word: "Listing", today: 833, baseline: 22.5, lift: 37.1 },
        { word: "Firework", today: 35, baseline: 1, lift: 35 },
        { word: "Ttchoj", today: 30, baseline: 1, lift: 30 },
        { word: "Dpdpaket", today: 30, baseline: 1, lift: 30 },
        { word: "Cntv", today: 29, baseline: 1, lift: 29 },
        { word: "Letv", today: 28, baseline: 1, lift: 28 },
        { word: "Dominating", today: 27, baseline: 1, lift: 27 },
        { word: "Lunna", today: 27, baseline: 1, lift: 27 },
      ],
    },
  };
}

export function getMockFeed(): FeedEntry[] {
  const now = Date.now();
  const registrars = ["GoDaddy.com", "Porkbun", "Namecheap", "Cloudflare", "Spaceship", "Dynadot", "Name.com", "IONOS SE", "NameCheap", "DomainRegistry.com"];
  const tlds = [".com", ".xyz", ".org", ".online", ".ai", ".lat", ".baby", ".app", ".com.mx", ".si"];
  const domains = [
    "BnBiscuits", "Tron2get", "AbcSightreading", "ThinkCost", "Lfrengenhariapg",
    "FixMyFeesInc", "ProtracEngineering", "Sakirsen", "Lekkertip", "TheSoulMateShift",
    "CleverCardsAppreciation", "Kivrenregen", "KeraMetric", "ExasperaSystems",
    "Carmarga", "ScalesToTails", "MgAtRadingCo", "HeartFieldPress", "ArtOfCommunityCare",
    "Gdmjr", "Eee2026", "MarbleCameo", "NewBedfordRealty", "DnaDear",
    "BTCFeminine", "TankDesignServices", "TankDesignChat"
  ];

  return domains.map((name, i) => {
    const tld = tlds[i % tlds.length];
    return {
      seq: 10000 - i,
      domain: name + tld,
      tld: tld,
      registrarId: i + 1,
      registrar: registrars[i % registrars.length],
      t: now - i * 13000,
    };
  });
}
