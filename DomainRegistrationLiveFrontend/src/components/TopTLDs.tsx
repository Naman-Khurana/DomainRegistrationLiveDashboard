import type { TldEntry } from "@/types/api";

type HourSlot = "this" | "last" | "2h";

interface Props {
  tlds: TldEntry[];
  slot: HourSlot;
}

const TLD_COLORS: Record<string, string> = {
  ".com": "#3b82f6",
  ".xyz": "#8b5cf6",
  ".org": "#6366f1",
  ".online": "#ec4899",
  ".ai": "#f59e0b",
  ".net": "#10b981",
  ".app": "#84cc16",
  ".store": "#f97316",
  ".site": "#06b6d4",
};

function tldClass(tld: string) {
  const t = tld.replace(/^\./, "");
  const known = ["com", "xyz", "org", "online", "ai", "net", "app"];
  if (!known.includes(t)) return "bg-gray-100 text-gray-500";

  const colors: Record<string, string> = {
    com: "bg-blue-100 text-blue-700",
    xyz: "bg-purple-100 text-purple-700",
    org: "bg-indigo-100 text-indigo-700",
    online: "bg-pink-100 text-pink-700",
    ai: "bg-amber-100 text-amber-800",
    net: "bg-emerald-100 text-emerald-800",
    app: "bg-green-100 text-green-700",
  };
  return colors[t] || "bg-gray-100 text-gray-500";
}

export default function TopTLDs({ tlds, slot }: Props) {
  const max = tlds[0]?.count ?? 1;

  return (
    <div className="bg-white border border-gray-200 rounded-lg overflow-hidden">
      <div className="px-4 pt-3.5 pb-2.5 border-b border-gray-100">
        <div className="flex items-center gap-1.5 text-[13px] font-semibold text-gray-800">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
            <circle cx="12" cy="12" r="10" />
            <line x1="2" y1="12" x2="22" y2="12" />
            <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z" />
          </svg>
          Top TLDs
        </div>
        <div className="text-[11px] text-gray-400 mt-0.5">
          {slot === "this" ? "this hour" : slot === "last" ? "last hour" : "2 hours ago"}
        </div>
      </div>
      <div className="px-4 py-3">
        {tlds.map((tld) => {
          const color = TLD_COLORS[tld.tld] ?? "#94a3b8";
          return (
            <div key={tld.tld} className="flex items-center gap-2.5 py-[5px]">
              <span className={`text-[12px] font-semibold min-w-[56px] text-center px-1 py-0.5 rounded-[3px] ${tldClass(tld.tld)}`}>
                {tld.tld}
              </span>
              <div className="flex-1 h-2.5 bg-gray-100 rounded-[3px] overflow-hidden">
                <div
                  className="h-full rounded-[3px] transition-all duration-400 ease-out"
                  style={{
                    width: `${(tld.count / max) * 100}%`,
                    background: color,
                  }}
                />
              </div>
              <span className="text-[12px] text-gray-500 min-w-[80px] text-right whitespace-nowrap">
                <strong className="text-gray-700 font-medium">{tld.count.toLocaleString()}</strong> · {tld.share.toFixed(1)}%
              </span>
            </div>
          );
        })}
      </div>
      <div className="px-4 py-3 bg-gray-50 border-t border-gray-100 text-[11px]">
        <a href="#tlds" className="text-blue-600 hover:underline flex items-center gap-1 font-medium">
          Research any TLD over 24h-30 days, or compare up to four →
        </a>
      </div>
    </div>
  );
}
