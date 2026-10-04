import type { StatsPayload, FeedEntry } from "@/types/api";

const BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";
const LIVE_URL_PREFIX = "/v1/live";


export async function fetchSnapshot(): Promise<StatsPayload> {
  const res = await fetch(`${BASE_URL}${LIVE_URL_PREFIX}/snapshot`, {
    next: { revalidate: 10 },
  });
  if (!res.ok) throw new Error(`Snapshot fetch failed: ${res.status}`);
  return res.json();
}



export async function fetchFeed(afterSeq?: number): Promise<FeedEntry[]> {
  const url = `${BASE_URL}${LIVE_URL_PREFIX}/feed`;
  const res = await fetch(url, { cache: "no-store" });
  if (!res.ok) throw new Error(`Feed fetch failed: ${res.status}`);
  return res.json();
}

