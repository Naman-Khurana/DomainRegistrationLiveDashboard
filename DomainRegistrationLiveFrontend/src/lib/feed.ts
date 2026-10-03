import type { FeedEntry } from "@/types/api";

export const MAX_FEED = 60;


export function mergeFeed(current: FeedEntry[], incoming: FeedEntry[], max = MAX_FEED): FeedEntry[] {
  if (incoming.length === 0) return current;

  const seen = new Set(current.map((e) => e.seq));
  const fresh = incoming.filter((e) => !seen.has(e.seq));
  if (fresh.length === 0) return current;

  return [...fresh, ...current].sort((a, b) => b.seq - a.seq).slice(0, max);
}