"use client";

import { useEffect, useState, useRef } from "react";
import type { StatsPayload, FeedEntry } from "@/types/api";

import SectionHeader from "@/components/SectionHeader";
import DashboardContent from "./DashboardContent";
import DashboardLoader from "./DashboardLoader";

import { SNAPSHOT_URL, FEED_URL } from "@/app/constants/url_constants";
import { toMillis } from "@/lib/time";


const INITIAL_RETRY_MS = 3000;


function snapshotMillis(s: StatsPayload): number {
  return toMillis(s.updatedAt) ?? toMillis(s.builtAt) ?? Date.now();
}

/** The one place that calls the snapshot API: used for the first load and for every poll. */
async function fetchSnapshot(signal?: AbortSignal): Promise<StatsPayload> {
  const res = await fetch(SNAPSHOT_URL, { cache: "no-store", signal });
  if (!res.ok) throw new Error(`Snapshot ${res.status}`);
  return res.json();
}

export default function LiveDashboardClient() {
  const [snapshot, setSnapshot] = useState<StatsPayload | null>(null);
  const [loadFailed, setLoadFailed] = useState(false);

  const [allIncoming, setAllIncoming] = useState<FeedEntry[]>([]);
  const [feedLoading, setFeedLoading] = useState(true);

  const [ago, setAgo] = useState(0);
  const fetchingRef = useRef(false);

  // 1) First snapshot.  Keeps retrying until it succeeds, so a slow or restarting backend just shows the loader.
  useEffect(() => {
    let cancelled = false;
    let timer: ReturnType<typeof setTimeout> | undefined;
    const controller = new AbortController();

    const load = async () => {
      try {
        const data = await fetchSnapshot(controller.signal);
        if (cancelled) return;
        setSnapshot(data);
        if (data.feed && data.feed.length > 0) {
          setAllIncoming((prev) => [...prev, ...data.feed]);
        }
      } catch {
        if (cancelled) return;
        setLoadFailed(true);
        timer = setTimeout(load, INITIAL_RETRY_MS);
      }
    };

    load();
    return () => {
      cancelled = true;
      controller.abort();
      clearTimeout(timer);
    };
  }, []);

  // 2) First-load feed, once, in parallel with the snapshot.

  useEffect(() => {
    let cancelled = false;
    const load = async () => {
      try {
        const res = await fetch(FEED_URL, { cache: "no-store" });
        if (!res.ok) throw new Error(`Feed ${res.status}`);
        const data = await res.json();
        // Backend may return: array directly, { items: [] }, or { feed: [] }
        const items: FeedEntry[] = Array.isArray(data) ? data : (data.items ?? data.feed ?? []);
        if (!cancelled && items.length > 0) {
          setAllIncoming((prev) => [...prev, ...items]);
        }
      } catch {
        // backend not reachable: the panel starts empty and fills from the snapshots
      } finally {
        if (!cancelled) setFeedLoading(false);
      }
    };
    load();
    return () => {
      cancelled = true;
    };
  }, []);

  // 3) Polling.  Starts only once the first snapshot is in.
  useEffect(() => {
    if (!snapshot) return;

    const updateAgo = () => {
      const diff = Math.floor((Date.now() - snapshotMillis(snapshot)) / 1000);
      setAgo(diff >= 0 ? diff : 0);
      return diff;
    };

    updateAgo();

    const interval = setInterval(async () => {
      const diff = updateAgo();

      if (diff >= 8 && !fetchingRef.current) {
        fetchingRef.current = true;
        try {
          const data = await fetchSnapshot();
          setSnapshot(data);

          if (data.feed && data.feed.length > 0) {
            setAllIncoming((prev) => [...prev, ...data.feed]);
          }
        } catch (e) {
          console.error("Failed to fetch snapshot:", e);
        } finally {
          fetchingRef.current = false;
        }
      }
    }, 1000);

    return () => clearInterval(interval);
  }, [snapshot]);

  return (
    <main className="max-w-[1150px] mx-auto px-5 pb-[60px]">
      {/* the LIVE badge appears once there is data; the rest of the header is always visible */}
      <SectionHeader ago={snapshot ? ago : undefined} />

      {snapshot ? (
        <DashboardContent snapshot={snapshot} incomingEntries={allIncoming} feedLoading={feedLoading} />
      ) : (
        <DashboardLoader failed={loadFailed} />
      )}
    </main>
  );
}