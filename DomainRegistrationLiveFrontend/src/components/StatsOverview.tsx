import type { NowStats } from "@/types/api";

interface Props {
    now: NowStats;
}

export default function StatsOverview({ now }: Props) {
    const pct = (n: number) => (now.format.total > 0 ? Math.round((n / now.format.total) * 100) : 0);

    return (
        <>
            {/* ── Stats cards ── */}
            <div
                className="grid grid-cols-[260px_1fr_1fr] border border-gray-200 rounded-lg overflow-hidden mb-3.5"
                style={{ gap: "1px", background: "#e5e7eb" }}
                role="region"
                aria-label="Registration statistics"
            >
                {/* Card 1 – dark */}
                <div className="bg-[#1e2a3a] text-white px-5 py-5">
                    <div className="text-[10px] font-semibold uppercase tracking-widest text-white/50 mb-2">
                        Seen in the last hour · live sample
                    </div>
                    <div className="text-[36px] font-bold leading-none tracking-tight mb-1">{now.h60.toLocaleString()}</div>
                    <div className="text-xs text-white/40">≈ {Math.round(now.perMin)} / min</div>
                </div>

                {/* Card 2 */}
                <div className="bg-white px-5 py-5">
                    <div className="text-[10px] font-semibold uppercase tracking-widest text-gray-400 mb-2">
                        Last 10 minutes
                    </div>
                    <div className="text-[36px] font-bold leading-none tracking-tight mb-1 text-gray-900">
                        {now.m10.toLocaleString()}
                    </div>
                    <div className="text-xs text-gray-400">{now.m1} in the last minute</div>
                </div>

                {/* Card 3 */}
                <div className="bg-white px-5 py-5">
                    <div className="text-[10px] font-semibold uppercase tracking-widest text-gray-400 mb-2">
                        🔥 Hottest keyword right now
                    </div>
                    <div>
                        <span className="text-[28px] font-bold text-gray-900">{now.topKeyword?.word ?? "—"}</span>
                        <span className="text-[13px] text-gray-500 ml-2">in {now.topKeyword?.count ?? 0} names / hr</span>
                    </div>
                </div>
            </div>

            {/* ── Format bar ── */}
            <div
                className="flex flex-wrap items-center gap-5 py-2.5 pb-4 text-[12px] text-gray-500"
                aria-label="Domain format breakdown"
            >
                <span className="font-semibold text-gray-700">Past hour</span>
                <span>
                    <strong className="text-gray-800 font-semibold">{now.format.multiword.toLocaleString()}</strong>{" "}
                    multi-word ({pct(now.format.multiword)}%)
                </span>
                <span>
                    <strong className="text-gray-800 font-semibold">{now.format.oneword.toLocaleString()}</strong>{" "}
                    one-word ({pct(now.format.oneword)}%)
                </span>
                <span>
                    <strong className="text-gray-800 font-semibold">{now.format.short5.toLocaleString()}</strong>{" "}
                    ≤5 chars ({pct(now.format.short5)}%)
                </span>
                <span>
                    <strong className="text-gray-800 font-semibold">{now.format.hyphen.toLocaleString()}</strong>{" "}
                    hyphenated ({pct(now.format.hyphen)}%)
                </span>
                <span>
                    <strong className="text-gray-800 font-semibold">{now.format.numeric.toLocaleString()}</strong>{" "}
                    numeric ({pct(now.format.numeric)}%)
                </span>
            </div>
        </>
    );
}