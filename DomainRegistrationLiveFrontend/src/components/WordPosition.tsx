import type { WordEntry } from "@/types/api";

interface Props {
  prefixes: WordEntry[];
  suffixes: WordEntry[];
}

export default function WordPosition({ prefixes, suffixes }: Props) {
  const maxPfx = prefixes[0]?.count ?? 1;
  const maxSfx = suffixes[0]?.count ?? 1;

  return (
    <div className="bg-white border border-gray-200 rounded-lg overflow-hidden mb-4">
      <div className="px-4 pt-3.5 pb-2.5 border-b border-gray-100">
        <div className="flex items-center gap-1.5 text-[13px] font-semibold text-gray-800">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
            <line x1="5" y1="12" x2="19" y2="12" />
            <polyline points="12 5 19 12 12 19" />
          </svg>
          How words sit in a name
        </div>
        <div className="text-[11px] text-gray-400 mt-0.5">
          lead word (prefix) vs trailing word (suffix), past hour
        </div>
      </div>
      <div className="p-4">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <div className="text-[10px] font-bold tracking-wider uppercase text-gray-400 mb-2.5">Top Prefixes · Lead Word</div>
            {prefixes.map((w) => (
              <div key={w.word} className="flex items-center gap-2 py-1">
                <span className="text-[13px] text-gray-900 min-w-[80px]">{w.word}</span>
                <div className="flex-1 h-2 bg-gray-100 rounded-sm overflow-hidden">
                  <div
                    className="h-full rounded-sm bg-gray-700"
                    style={{ width: `${(w.count / maxPfx) * 100}%` }}
                  />
                </div>
                <span className="text-[12px] text-gray-500 min-w-[24px] text-right">{w.count}</span>
              </div>
            ))}
          </div>
          <div>
            <div className="text-[10px] font-bold tracking-wider uppercase text-gray-400 mb-2.5">Top Suffixes · Trailing Word</div>
            {suffixes.map((w) => (
              <div key={w.word} className="flex items-center gap-2 py-1">
                <span className="text-[13px] text-gray-900 min-w-[80px]">{w.word}</span>
                <div className="flex-1 h-2 bg-gray-100 rounded-sm overflow-hidden">
                  <div
                    className="h-full rounded-sm bg-indigo-500"
                    style={{ width: `${(w.count / maxSfx) * 100}%` }}
                  />
                </div>
                <span className="text-[12px] text-gray-500 min-w-[24px] text-right">{w.count}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
