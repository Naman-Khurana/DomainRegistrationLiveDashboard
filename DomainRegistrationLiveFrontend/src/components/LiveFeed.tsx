"use client";

import { useEffect, useRef, useState } from "react";
import type { FeedEntry } from "@/types/api";
import { getMockFeed } from "@/lib/api";

interface Props {
  initialFeed: FeedEntry[];
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
    si: "bg-yellow-100 text-yellow-900"
  };
  return colors[t] || "bg-gray-100 text-gray-500";
}

function timeAgo(ms: number): string {
  const diff = Math.floor((Date.now() - ms) / 1000);
  if (diff < 60) return `${diff}s ago`;
  return `${Math.floor(diff / 60)}m ago`;
}

const POLL_INTERVAL = 8000;
const MAX_FEED = 60;

export default function LiveFeed({ initialFeed }: Props) {
  const [feed, setFeed] = useState<FeedEntry[]>(initialFeed);
  const [paused, setPaused] = useState(false);
  const [tick, setTick] = useState(0);
  const pausedRef = useRef(paused);
  pausedRef.current = paused;

  // Tick timestamps for "Xs ago"
  useEffect(() => {
    const id = setInterval(() => setTick((t) => t + 1), 10000);
    return () => clearInterval(id);
  }, []);

  // Poll for new feed items
  useEffect(() => {
    const poll = async () => {
      if (pausedRef.current) return;
      try {
        const res = await fetch(
          `/api/feed?afterSeq=${feed[0]?.seq ?? 0}`,
          { cache: "no-store" }
        );
        if (!res.ok) return;
        const newItems: FeedEntry[] = await res.json();
        if (newItems.length > 0) {
          setFeed((prev) => [...newItems, ...prev].slice(0, MAX_FEED));
        }
      } catch {
        // backend may not be running; use mock increments
        const mockNew = getMockFeed()
          .slice(0, 2)
          .map((e) => ({ ...e, seq: e.seq + Date.now(), t: Date.now() }));
        setFeed((prev) => [...mockNew, ...prev].slice(0, MAX_FEED));
      }
    };

    const id = setInterval(poll, POLL_INTERVAL);
    return () => clearInterval(id);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className="bg-white border border-gray-200 rounded-lg overflow-hidden">
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
            onClick={() => setPaused((p) => !p)}
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

      <div className="max-h-[480px] overflow-y-auto px-4" aria-live="polite" aria-label="Live domain registrations">
        {feed.map((item) => (
          <div key={`${item.seq}-${item.domain}`} className="flex items-center justify-between py-2 border-b border-gray-100 last:border-0">
            <div>
              <div className="text-[13px] font-medium text-gray-900">{item.domain}</div>
              <div className="text-[11px] text-gray-500">
                {item.registrar} · {timeAgo(item.t)}
              </div>
            </div>
            <span className={`text-[11px] font-semibold px-2 py-0.5 rounded whitespace-nowrap ${tldClass(item.tld)}`}>
              {item.tld}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}
