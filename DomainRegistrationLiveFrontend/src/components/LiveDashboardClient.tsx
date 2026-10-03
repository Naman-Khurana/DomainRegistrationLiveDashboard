"use client";

import { useEffect, useState, useRef } from "react";
import type { StatsPayload, FeedEntry } from "@/types/api";

import SectionHeader from "@/components/SectionHeader";
import SearchBar from "@/components/SearchBar";
import RisingKeywords from "@/components/RisingKeywords";
import LiveFeed from "@/components/LiveFeed";
import TopStatsContainer from "@/components/TopStatsContainer";
import WhereRegistered from "@/components/WhereRegistered";
import WordPosition from "@/components/WordPosition";
import BiggestMovers from "@/components/BiggestMovers";
import DailyRegistrarCount from "@/components/DailyRegistrarCount";

import { SNAPSHOT_URL, FEED_URL } from "@/app/constants/url_constants";
import { toMillis } from "@/lib/time";


interface Props {
  initialSnapshot: StatsPayload;
  initialFeed: FeedEntry[];
}

/** When the snapshot was built, in ms.  Works whether the API sends a number or an ISO string. */
function snapshotMillis(s: StatsPayload): number {
  return toMillis(s.updatedAt) ?? toMillis(s.builtAt) ?? Date.now();
}

export default function LiveDashboardClient({ initialSnapshot, initialFeed }: Props) {
  const [snapshot, setSnapshot] = useState<StatsPayload>(initialSnapshot);

  // allIncoming = every entry ever received (initial feed + each snapshot's feed).
  // LiveFeed owns deduplication; we just accumulate here.
  const [allIncoming, setAllIncoming] = useState<FeedEntry[]>([]);
  // feedLoading drives the skeleton loader in LiveFeed
  const [feedLoading, setFeedLoading] = useState(true);

  // Fetch the initial feed once from the backend on mount.
  useEffect(() => {
    let cancelled = false;
    const load = async () => {
      try {
        const res = await fetch(FEED_URL, { cache: "no-store" });
        if (!res.ok) throw new Error(`Feed ${res.status}`);
        const data = await res.json();
        // Backend may return: array directly, { items: [] }, or { feed: [] }
        const items: FeedEntry[] = Array.isArray(data)
          ? data
          : (data.items ?? data.feed ?? []);
        if (!cancelled && items.length > 0) {
          setAllIncoming(items);
        }
      } catch {
        // backend not reachable – seed from initialFeed so the panel is not empty
        if (!cancelled && initialFeed.length > 0) {
          setAllIncoming(initialFeed);
        }
      } finally {
        if (!cancelled) setFeedLoading(false);
      }
    };
    load();
    return () => { cancelled = true; };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const [ago, setAgo] = useState(0);
  const fetchingRef = useRef(false);

  useEffect(() => {
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
          const res = await fetch(SNAPSHOT_URL, { cache: "no-store" });
          if (res.ok) {
            const data: StatsPayload = await res.json();
            setSnapshot(data);
            // Append snapshot feed entries to accumulator; LiveFeed deduplicates
            if (data.feed && data.feed.length > 0) {
              setAllIncoming((prev) => [...prev, ...data.feed]);
            }
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

  const { now, today } = snapshot;
  const totalConfirmed = now.registrars.reduce((s, r) => s + r.count, 0);

  return (
    <main className="max-w-[1150px] mx-auto px-5 pb-[60px]">
      <SectionHeader now={now} ago={ago} />
      <SearchBar />

      <div className="flex flex-col lg:flex-row gap-6 items-start">
        <div className="flex-1 min-w-0">
          <div className="mb-6 mt-6">
            <RisingKeywords data={now.risingKeywords} />
          </div>

          <TopStatsContainer keywords={now.topKeywords} tlds={now.tlds} />

          <div className="mb-6">
            <WhereRegistered registrars={now.registrars} />
          </div>

          <hr className="border-t border-gray-200 my-6" />

          <WordPosition prefixes={now.prefixes} suffixes={now.suffixes} />
          <BiggestMovers movers={today.movers} />

          <hr className="border-t border-gray-200 my-6" />

          <DailyRegistrarCount
            registrars={now.registrars}
            totalConfirmed={totalConfirmed}
            totalChecked={Math.round(totalConfirmed / 0.872)}
            unchecked={9408}
          />
        </div>

        <div className="w-full lg:w-[360px] flex-shrink-0 mt-6 lg:mt-0">
          <LiveFeed incomingEntries={allIncoming} isLoading={feedLoading} />
        </div>
      </div>
    </main>
  );
}