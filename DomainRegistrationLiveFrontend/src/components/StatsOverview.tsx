
import CountUp from "react-countup";
import type { NowStats } from "@/types/api";

interface Props {
    now: NowStats;
}

interface AnimatedNumberProps {
    value: number;
    duration?: number;
}

function AnimatedNumber({
    value,
    duration = 0.7,
}: AnimatedNumberProps) {
    return (
        <CountUp
            end={value}
            duration={duration}
            separator=","
            preserveValue
            useEasing
        />
    );
}

export default function StatsOverview({ now }: Props) {
    const pct = (n: number) =>
        now.format.total > 0
            ? Math.round((n / now.format.total) * 100)
            : 0;

    return (
        <>
            {/* ── Stats cards ── */}
            <div
                className="grid grid-cols-[1fr_1fr_2fr] gap-3.5 mb-3.5"
                role="region"
                aria-label="Registration statistics"
            >
                {/* Card 1 – dark */}
                <div className="bg-[#1e2a3a] text-white px-5 py-5 rounded-lg border border-gray-200">
                    <div className="text-[10px] font-semibold uppercase tracking-widest text-white/50 mb-2">
                        Seen in the last hour · live sample
                    </div>

                    <div className="text-[36px] font-bold leading-none tracking-tight mb-1">
                        <AnimatedNumber value={now.h60} />
                    </div>

                    <div className="text-xs text-white/40">
                        ≈ <AnimatedNumber value={Math.round(now.perMin)} /> / min
                    </div>
                </div>

                {/* Card 2 */}
                <div className="bg-white px-5 py-5 rounded-lg border border-gray-200">
                    <div className="text-[10px] font-semibold uppercase tracking-widest text-gray-400 mb-2">
                        Last 10 minutes
                    </div>

                    <div className="text-[36px] font-bold leading-none tracking-tight mb-1 text-gray-900">
                        <AnimatedNumber value={now.m10} />
                    </div>

                    <div className="text-xs text-gray-400">
                        <AnimatedNumber value={now.m1} /> in the last minute
                    </div>
                </div>

                {/* Card 3 */}
                <div className="bg-white px-5 py-5 rounded-lg border border-gray-200">
                    <div className="text-[10px] font-semibold uppercase tracking-widest text-gray-400 mb-2">
                        🔥 Hottest keyword right now
                    </div>

                    <div>
                        <span className="text-[28px] font-bold text-gray-900">
                            {now.topKeyword?.word ?? "—"}
                        </span>

                        <span className="text-[13px] text-gray-500 ml-2">
                            in{" "}
                            <AnimatedNumber
                                value={now.topKeyword?.count ?? 0}
                            />{" "}
                            names / hr
                        </span>
                    </div>
                </div>
            </div>


            <div
                className="flex flex-wrap items-center gap-5 py-2.5 pb-4 text-[12px] text-gray-500"
                aria-label="Domain format breakdown"
            >
                <span className="font-semibold text-gray-700">
                    Past hour
                </span>

                <span>
                    <strong className="text-gray-800 font-semibold">
                        <AnimatedNumber value={now.format.multiword} />
                    </strong>{" "}
                    multi-word ({pct(now.format.multiword)}%)
                </span>

                <span>
                    <strong className="text-gray-800 font-semibold">
                        <AnimatedNumber value={now.format.oneword} />
                    </strong>{" "}
                    one-word ({pct(now.format.oneword)}%)
                </span>

                <span>
                    <strong className="text-gray-800 font-semibold">
                        <AnimatedNumber value={now.format.short5} />
                    </strong>{" "}
                    ≤5 chars ({pct(now.format.short5)}%)
                </span>

                <span>
                    <strong className="text-gray-800 font-semibold">
                        <AnimatedNumber value={now.format.hyphen} />
                    </strong>{" "}
                    hyphenated ({pct(now.format.hyphen)}%)
                </span>

                <span>
                    <strong className="text-gray-800 font-semibold">
                        <AnimatedNumber value={now.format.numeric} />
                    </strong>{" "}
                    numeric ({pct(now.format.numeric)}%)
                </span>
            </div>
        </>
    );
}
