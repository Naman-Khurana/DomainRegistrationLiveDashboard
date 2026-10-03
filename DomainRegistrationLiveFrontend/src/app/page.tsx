import { getMockSnapshot, getMockFeed } from "@/lib/api";
import type { StatsPayload } from "@/types/api";

// Client components
import SectionHeader from "@/components/SectionHeader";
import SearchBar from "@/components/SearchBar";
import RisingKeywords from "@/components/RisingKeywords";
import LiveFeed from "@/components/LiveFeed";
import TopStatsContainer from "@/components/TopStatsContainer";
import WhereRegistered from "@/components/WhereRegistered";
import WordPosition from "@/components/WordPosition";
import BiggestMovers from "@/components/BiggestMovers";
import DailyRegistrarCount from "@/components/DailyRegistrarCount";
import Navbar from "@/components/Navbar";

async function getSnapshot(): Promise<StatsPayload> {
  const BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";
  try {
    const res = await fetch(`${BASE_URL}/v1/snapshot`, {
      next: { revalidate: 10 },
    });
    if (!res.ok) throw new Error(`${res.status}`);
    return res.json();
  } catch {
    return getMockSnapshot();
  }
}

export default async function LivePage() {
  const snapshot = await getSnapshot();
  const { now, today, builtAt } = snapshot;

  const initialFeed = getMockFeed();

  // For daily registrar count, compute totals
  const totalConfirmed = now.registrars.reduce((s, r) => s + r.count, 0);

  return (
    <>
      <Navbar />
      <main className="max-w-[1150px] mx-auto px-5 pb-[60px]">
        {/* Header + stats (client for live-updating "Xs ago") */}
        <SectionHeader now={now} builtAt={builtAt} />

        {/* Search */}
        <SearchBar />

        {/* Layout: Left Column (Insights) + Right Column (Feed) */}
        <div className="flex flex-col lg:flex-row gap-6 items-start">
          
          {/* LEFT COLUMN */}
          <div className="flex-1 min-w-0">
            <div className="mb-6">
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

            {/* Daily registrar count with stacked bars */}
            <DailyRegistrarCount
              registrars={now.registrars}
              totalConfirmed={totalConfirmed}
              totalChecked={Math.round(totalConfirmed / 0.872)}
              unchecked={9408}
            />
          </div>

          {/* RIGHT COLUMN */}
          <div className="w-full lg:w-[360px] flex-shrink-0">
            <LiveFeed initialFeed={initialFeed} />
          </div>

        </div>
      </main>
    </>
  );
}
