"use client";

import { useState, useEffect } from "react";
import TopKeywords from "./TopKeywords";
import TopTLDs from "./TopTLDs";
import { HourSelector } from "./ui";
import type { KeywordEntry, TldEntry } from "@/types/api";
import { LIVE_URL_PREFIX } from "@/app/constants/url_constants";

type HourSlot = "this" | "last" | "2h";

interface Props {
  keywords: KeywordEntry[];
  tlds: TldEntry[];
}

interface BlockData {
  topKeywords: KeywordEntry[];
  topTlds: TldEntry[];
}

export default function TopStatsContainer({ keywords, tlds }: Props) {
  const [slot, setSlot] = useState<HourSlot>("this");
  const [blockData, setBlockData] = useState<Partial<Record<"last" | "2h", BlockData | null>>>({});
  const [blockLoading, setBlockLoading] = useState(false);

  useEffect(() => {
    if (slot === "this") return;

    // Already fetched — don't re-fetch
    if (blockData[slot] !== undefined) return;

    const t = slot === "last" ? 60 : 120;
    setBlockLoading(true);

    fetch(`${LIVE_URL_PREFIX}/block?t=${t}`)
      .then((res) => {
        if (!res.ok) throw new Error(`Block API failed (${res.status})`);
        return res.json();
      })
      .then((data: BlockData) => {
        setBlockData((prev) => ({ ...prev, [slot]: data }));
      })
      .catch(() => {
        setBlockData((prev) => ({ ...prev, [slot]: null }));
      })
      .finally(() => setBlockLoading(false));
  }, [slot]);

  const displayKeywords =
    slot === "this" || blockLoading || !blockData[slot as "last" | "2h"]
      ? keywords
      : blockData[slot as "last" | "2h"]!.topKeywords;

  const displayTlds =
    slot === "this" || blockLoading || !blockData[slot as "last" | "2h"]
      ? tlds
      : blockData[slot as "last" | "2h"]!.topTlds;

  return (
    <div className="mb-6">
      <HourSelector value={slot} onChange={setSlot} />

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-5">
        <TopKeywords keywords={displayKeywords} slot={slot} />
        <TopTLDs tlds={displayTlds} slot={slot} />
      </div>
    </div>
  );
}
