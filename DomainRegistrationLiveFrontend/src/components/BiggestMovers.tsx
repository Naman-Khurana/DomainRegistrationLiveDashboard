import type { MoverEntry } from "@/types/api";

interface Props {
  movers: MoverEntry[];
}

export default function BiggestMovers({ movers }: Props) {
  // Split into two columns
  const mid = Math.ceil(movers.length / 2);
  const left = movers.slice(0, mid);
  const right = movers.slice(mid);

  return (
    <div className="bg-white border border-gray-200 rounded-lg overflow-hidden mb-4">
      <div className="px-4 pt-3.5 pb-2.5 border-b border-gray-100">
        <div className="flex items-center gap-1.5 text-[13px] font-semibold text-gray-800">
          <span className="text-[14px]">🔥</span>
          Biggest movers today
        </div>
        <div className="text-[11px] text-gray-400 mt-0.5">
          today&apos;s full-day count vs its 7-day norm — click any word to research it
        </div>
      </div>
      <div className="p-4">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-x-6 gap-y-0">
          <div>
            {left.map((m) => (
              <div key={m.word} className="flex items-center justify-between py-1.5 border-b border-gray-100 last:border-0">
                <span className="text-[13px] font-medium text-gray-900">{m.word}</span>
                <div className="flex items-center gap-2.5">
                  <span className="text-[13px] text-gray-500">{m.today}</span>
                  <span className="text-[12px] font-semibold text-red-600">×{Math.round(m.lift)}</span>
                </div>
              </div>
            ))}
          </div>
          <div>
            {right.map((m) => (
              <div key={m.word} className="flex items-center justify-between py-1.5 border-b border-gray-100 last:border-0">
                <span className="text-[13px] font-medium text-gray-900">{m.word}</span>
                <div className="flex items-center gap-2.5">
                  <span className="text-[13px] text-gray-500">{m.today}</span>
                  <span className="text-[12px] font-semibold text-red-600">×{Math.round(m.lift)}</span>
                </div>
              </div>
            ))}
          </div>
        </div>
        <p className="text-[11px] text-gray-400 mt-3 leading-relaxed">
          Baseline from DotWeekly&apos;s daily new-registration history. A high multiple means the word is being registered far more today than it usually is.
        </p>
      </div>
    </div>
  );
}
