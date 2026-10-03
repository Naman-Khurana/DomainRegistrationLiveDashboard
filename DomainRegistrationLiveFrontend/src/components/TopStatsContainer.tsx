"use client";

import { useState } from "react";
import TopKeywords from "./TopKeywords";
import TopTLDs from "./TopTLDs";
import { HourSelector } from "./ui";
import type { KeywordEntry, TldEntry } from "@/types/api";

type HourSlot = "this" | "last" | "2h";

interface Props {
  keywords: KeywordEntry[];
  tlds: TldEntry[];
}

export default function TopStatsContainer({ keywords, tlds }: Props) {
  const [slot, setSlot] = useState<HourSlot>("last");

  return (
    <div className="mb-6">
      <HourSelector value={slot} onChange={setSlot} />
      
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-5">
        <TopKeywords keywords={keywords} slot={slot} />
        <TopTLDs tlds={tlds} slot={slot} />
      </div>
    </div>
  );
}
