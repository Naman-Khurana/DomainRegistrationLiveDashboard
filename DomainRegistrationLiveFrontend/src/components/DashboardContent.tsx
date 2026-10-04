import { memo, useRef } from "react";
import type { StatsPayload, FeedEntry } from "@/types/api";

import StatsOverview from "./StatsOverview";
import SearchBar, { type SearchBarHandle } from "@/components/SearchBar";
import RisingKeywords from "@/components/RisingKeywords";
import LiveFeed from "@/components/LiveFeed";
import TopStatsContainer from "@/components/TopStatsContainer";
import WhereRegistered from "@/components/WhereRegistered";
import WordPosition from "@/components/WordPosition";
import BiggestMovers from "@/components/BiggestMovers";
import DailyRegistrarCount from "@/components/DailyRegistrarCount";

interface Props {
    snapshot: StatsPayload;
    incomingEntries: FeedEntry[];
    feedLoading: boolean;
}


//Everything below the header.  It is memoized: the parent re-renders every second to update the
// updated Xs ago" badge, but these props only change when a new snapshot or feed entries arrive.

function DashboardContent({ snapshot, incomingEntries, feedLoading }: Props) {
    const { now, today } = snapshot;
    const totalConfirmed = now.registrars.reduce((s, r) => s + r.count, 0);
    const searchBarRef = useRef<SearchBarHandle>(null);

    const handleKeywordClick = (keyword: string) => {
        searchBarRef.current?.triggerSearch(keyword);
    };

    return (
        <>
            <StatsOverview now={now} />
            <SearchBar ref={searchBarRef} />

            <div className="flex flex-col lg:flex-row gap-6 items-start">
                <div className="flex-1 min-w-0">
                    <div className="mb-6 mt-6">
                        <RisingKeywords data={now.risingKeywords} onKeywordClick={handleKeywordClick} />
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
                    <LiveFeed incomingEntries={incomingEntries} isLoading={feedLoading} />
                </div>
            </div>
        </>
    );
}

export default memo(DashboardContent);