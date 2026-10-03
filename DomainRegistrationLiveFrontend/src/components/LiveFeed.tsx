"use client";

import { useEffect, useRef, useState } from "react";
import type { FeedEntry } from "@/types/api";

interface Props {
  /** All accumulated incoming entries (initial + from each snapshot). Newest-first preferred but not required. */
  incomingEntries: FeedEntry[];
  /** True while the initial /v1/live/feed request is in-flight. */
  isLoading?: boolean;
}

const MAX_FEED = 60;

function randInt(min: number, max: number) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function tldClass(tld: string): string {
  const t = tld.replace(/^\./, "");
  const known = ["com", "xyz", "org", "online", "ai", "lat", "baby", "app", "net", "si"];
  if (!known.includes(t)) return "bg-gray-100 text-gray-500";

  const colors: Record<string, string> = {
    com: "bg-blue-100 text-blue-700",
    xyz: "bg-purple-100 text-purple-700",
    org: "bg-indigo-100 text-indigo-700",
    online: "bg-pink-100 text-pink-700",
    ai: "bg-amber-100 text-amber-800",
    lat: "bg-emerald-100 text-emerald-800",
    baby: "bg-fuchsia-100 text-fuchsia-800",
    app: "bg-green-100 text-green-700",
    net: "bg-emerald-100 text-emerald-800",
    si: "bg-yellow-100 text-yellow-900",
  };
  return colors[t] || "bg-gray-100 text-gray-500";
}

function timeAgo(ms: number): string {
  const diff = Math.max(0, Math.floor((Date.now() - ms) / 1000));
  if (diff < 60) return `${diff}s ago`;
  return `${Math.floor(diff / 60)}m ago`;
}

export default function LiveFeed({ incomingEntries, isLoading = false }: Props) {
  // The feed that is currently rendered
  const [visibleFeed, setVisibleFeed] = useState<FeedEntry[]>([]);

  // Frozen snapshot shown while paused. New entries accumulate in the queue
  // but aren't rendered until resumed.
  const [frozen, setFrozen] = useState<FeedEntry[] | null>(null);
  const paused = frozen !== null;
  const visible = frozen ?? visibleFeed;

  // Pending queue — entries waiting to be progressively revealed (newest first)
  const queueRef = useRef<FeedEntry[]>([]);

  // All seq keys ever processed (prevents duplicates across snapshots)
  const knownSeqsRef = useRef<Set<number>>(new Set());

  // Whether the initial feed has been loaded (first non-empty incomingEntries)
  const initializedRef = useRef(false);

  // Set of seq numbers for entries that were JUST revealed (drives the highlight)
  // We use a counter-per-seq so parallel reveal batches don't clobber each other.
  const [newSeqs, setNewSeqs] = useState<Set<number>>(new Set());

  // Re-render every 10 s so "Xs ago" labels stay fresh
  const [, setTick] = useState(0);
  useEffect(() => {
    const id = setInterval(() => setTick((t) => t + 1), 10_000);
    return () => clearInterval(id);
  }, []);

  // ─── React to new incoming entries ──────────────────────────────────────────
  useEffect(() => {
    if (incomingEntries.length === 0) return;

    const fresh = incomingEntries.filter((e) => !knownSeqsRef.current.has(e.seq));
    if (fresh.length === 0) return;

    fresh.forEach((e) => knownSeqsRef.current.add(e.seq));

    // Sort newest-first within the fresh batch
    const sorted = [...fresh].sort((a, b) => b.seq - a.seq);

    if (!initializedRef.current) {
      // ── First load: render all at once, no animation ──
      initializedRef.current = true;
      setVisibleFeed(sorted.slice(0, MAX_FEED));
      return;
    }

    // ── Subsequent arrivals: add to queue for progressive reveal ──
    // Newest entries go to the front of the queue so they appear first
    queueRef.current = [...sorted, ...queueRef.current];
  }, [incomingEntries]);

  // ─── Progressive drain ──────────────────────────────────────────────────────
  // Runs every 500 ms. Takes a random batch from the queue and prepends it
  // to visibleFeed (unless paused). Each batch gets a temporary blue highlight.
  useEffect(() => {
    const id = setInterval(() => {
      if (paused) return;
      if (queueRef.current.length === 0) return;

      // Random batch size 1–3; keeps the "live stream" feel
      const batchSize = randInt(1, 3);
      const batch = queueRef.current.splice(0, batchSize);
      if (batch.length === 0) return;

      const batchSeqs = new Set(batch.map((e) => e.seq));

      setVisibleFeed((prev) => [...batch, ...prev].slice(0, MAX_FEED));

      // Highlight the newly revealed entries
      setNewSeqs((prev) => {
        const next = new Set(prev);
        batchSeqs.forEach((s) => next.add(s));
        return next;
      });

      // Remove highlight after the CSS animation (900 ms) completes
      setTimeout(() => {
        setNewSeqs((prev) => {
          const next = new Set(prev);
          batchSeqs.forEach((s) => next.delete(s));
          return next;
        });
      }, 950);
    }, 500);

    return () => clearInterval(id);
  }, [paused]);

  return (
    <div className="bg-white border border-gray-200 rounded-lg overflow-hidden">
      {/* ── Header ── */}
      <div className="px-4 pt-3.5 pb-2.5 border-b border-gray-100">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-1.5 text-[13px] font-semibold text-gray-800">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
              <circle cx="12" cy="12" r="10" />
              <polyline points="12 6 12 12 16 14" />
            </svg>
            Live registrations
          </div>
          <button
            className="flex items-center gap-1 px-2.5 py-1 border border-gray-200 rounded text-[11px] font-medium bg-white text-gray-500 hover:bg-gray-50 transition-colors cursor-pointer"
            onClick={() => setFrozen(paused ? null : visibleFeed)}
            aria-label={paused ? "Resume live feed" : "Pause live feed"}
            id="feed-pause-btn"
          >
            {paused ? (
              <>
                <svg width="10" height="10" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
                  <polygon points="5 3 19 12 5 21 5 3" />
                </svg>
                Resume
              </>
            ) : (
              <>
                <svg width="10" height="10" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
                  <rect x="6" y="4" width="4" height="16" />
                  <rect x="14" y="4" width="4" height="16" />
                </svg>
                Pause
              </>
            )}
          </button>
        </div>
      </div>

      {/* ── Feed list ── */}
      <div className="max-h-[480px] overflow-y-auto px-4" aria-live="polite" aria-label="Live domain registrations">
        {isLoading ? (
          <div className="py-2" aria-busy="true" aria-label="Loading feed">
            {Array.from({ length: 10 }).map((_, i) => (
              <div
                key={i}
                className="flex items-center justify-between py-2.5 border-b border-gray-100 last:border-0 animate-pulse"
              >
                <div className="flex-1 space-y-1.5">
                  <div className="h-3 bg-gray-200 rounded w-3/5" />
                  <div className="h-2.5 bg-gray-100 rounded w-2/5" />
                </div>
                <div className="h-4 w-12 bg-gray-200 rounded ml-3" />
              </div>
            ))}
          </div>
        ) : visible.length === 0 ? (
          <p className="text-[12px] text-gray-400 text-center py-8">No registrations yet.</p>
        ) : (
          visible.map((item) => (
            <div
              key={`${item.seq}-${item.domain}`}
              className={`flex items-center justify-between py-2 border-b border-gray-100 last:border-0 ${
                newSeqs.has(item.seq) ? "feed-item-new" : ""
              }`}
            >
              <div>
                <div className="text-[13px] font-medium text-gray-900">{item.domain}</div>
                <div className="text-[11px] text-gray-500" suppressHydrationWarning>
                  {item.registrar} · {timeAgo(item.t)}
                </div>
              </div>
              <span className={`text-[11px] font-semibold px-2 py-0.5 rounded whitespace-nowrap ${tldClass(item.tld)}`}>
                {item.tld}
              </span>
            </div>
          ))
        )}
      </div>
    </div>
  );
}