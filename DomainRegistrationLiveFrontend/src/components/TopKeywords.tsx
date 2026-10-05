import type { KeywordEntry } from "@/types/api";

type HourSlot = "this" | "last" | "2h";

interface Props {
  keywords: KeywordEntry[];
  slot: HourSlot;
}

export default function TopKeywords({ keywords, slot }: Props) {
  const max = keywords[0]?.count ?? 1;

  return (
    <div className="bg-white border border-gray-200 rounded-lg overflow-hidden">
      <div className="px-4 pt-3.5 pb-2.5 border-b border-gray-100">
        <div className="flex items-center gap-1.5 text-[13px] font-semibold text-gray-800">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
            <line x1="18" y1="20" x2="18" y2="10" />
            <line x1="12" y1="20" x2="12" y2="4" />
            <line x1="6" y1="20" x2="6" y2="14" />
          </svg>
          Top keywords
        </div>
        <div className="text-[11px] text-gray-400 mt-0.5">
          {slot === "this" ? "this hour" : slot === "last" ? "last hour (60-120 min ago)" : "2 hours ago"} — bar shows prefix vs suffix use; click to see the domains
        </div>
      </div>

      <div className="px-4 pb-3.5 pt-3">
        {keywords.map((kw) => (
          <div key={kw.word} className="flex items-center gap-2.5 py-1.5">
            <span className="text-[13px] text-gray-900 min-w-[70px] xl:min-w-[90px] whitespace-nowrap overflow-hidden text-ellipsis">{kw.word}</span>
            <div className="flex-1 h-2.5 bg-gray-100 rounded-[3px] overflow-hidden relative">

              <div
                className="h-full rounded-[3px] bg-gray-700 opacity-25 transition-all duration-400 ease-out"
                style={{ width: `${(kw.count / max) * 100}%` }}
              />

              <div
                className="absolute top-0 left-0 h-full rounded-[3px] bg-gray-700 transition-all duration-400 ease-out"
                style={{ width: `${(kw.pfx / max) * 100}%` }}
              />
            </div>
            <span className="text-[12px] text-gray-500 min-w-[28px] text-right">{kw.count}</span>
          </div>
        ))}
      </div>

      <div className="px-4 py-2 bg-gray-50 border-t border-gray-100 text-[10px] text-gray-400 flex items-center gap-3">
        <span className="flex items-center gap-1.5"><span className="w-2 h-2 rounded-full bg-gray-700"></span>prefix</span>
        <span className="flex items-center gap-1.5"><span className="w-2 h-2 rounded-full bg-indigo-500"></span>suffix</span>
        <span className="flex items-center gap-1.5"><span className="w-2 h-2 rounded-full bg-gray-300"></span>standalone</span>
      </div>
    </div>
  );
}
