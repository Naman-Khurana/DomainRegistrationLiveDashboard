import type { RepeatEntry } from "@/types/api";

interface Props {
  repeats: RepeatEntry[];
}

function tldClass(tld: string): string {
  const t = tld.replace(/^\./, "");
  const map: Record<string, string> = {
    com: "bg-green-100 text-green-700",
    net: "bg-emerald-100 text-emerald-800",
    org: "bg-indigo-100 text-indigo-700",
    io: "bg-sky-100 text-sky-700",
    ai: "bg-amber-100 text-amber-800",
    xyz: "bg-purple-100 text-purple-700",
    app: "bg-indigo-100 text-indigo-700",
    tech: "bg-blue-100 text-blue-700",
    online: "bg-cyan-100 text-cyan-800",
    dev: "bg-blue-100 text-blue-600",
    site: "bg-lime-100 text-lime-800",
    store: "bg-rose-100 text-rose-700",
  };
  return map[t] ?? "bg-gray-100 text-gray-500";
}

export default function RepeatsSection({ repeats }: Props) {
  if (!repeats || repeats.length === 0) return null;

  return (
    <div className="bg-white border border-gray-200 rounded-lg overflow-hidden mb-6">
      <div className="px-5 pt-4 pb-3 border-b border-gray-100">
        <div className="flex items-center gap-2 text-[15px] font-semibold text-[#1e2a3a]">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="text-teal-700">
            <rect x="9" y="9" width="13" height="13" rx="2" ry="2" />
            <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
          </svg>
          Same name, multiple extensions
        </div>
        <div className="text-[12px] text-gray-400 mt-1 pl-6">
          one name registered on 2+ TLDs in the last 6h — brand launches &amp; defensive registrations
        </div>
      </div>

      <div className="p-5 pt-2">
        {repeats.map((r, i) => (
          <div key={`${r.sld}-${i}`} className="flex flex-col sm:flex-row sm:items-center justify-between py-3 border-b border-gray-100 last:border-0 gap-3">
            <div className="text-[14px] font-medium text-teal-800 shrink-0">
              {r.sld}
            </div>
            <div className="flex items-center gap-3 shrink-0">
              <span className="text-[13px] font-semibold text-teal-900">{r.tlds}×</span>
              <div className="flex flex-wrap items-center gap-1.5">
                {r.tldList.map((tld) => {
                  const displayTld = tld.startsWith('.') ? tld : `.${tld}`;
                  return (
                    <span key={tld} className={`text-[11px] font-medium px-1.5 py-0.5 rounded ${tldClass(displayTld)}`}>
                      {displayTld}
                    </span>
                  );
                })}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
