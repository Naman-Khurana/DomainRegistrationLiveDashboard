import type { StatsPayload, FeedEntry } from "@/types/api";
import { SNAPSHOT_URL, FEED_URL } from "@/app/constants/url_constants";

export async function fetchSnapshot(): Promise<StatsPayload> {
  const res = await fetch(SNAPSHOT_URL, {
    cache: "no-store",
  });
  if (!res.ok) throw new Error(`Snapshot fetch failed: ${res.status}`);
  return res.json();
}

export async function fetchFeed(afterSeq?: number): Promise<FeedEntry[]> {
  const res = await fetch(FEED_URL, { cache: "no-store" });
  if (!res.ok) throw new Error(`Feed fetch failed: ${res.status}`);
  return res.json();
}
