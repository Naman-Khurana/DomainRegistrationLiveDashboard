import type { RegistrarEntry } from "@/types/api";

interface Props {
  registrars: RegistrarEntry[];
}

export default function WhereRegistered({ registrars }: Props) {
  const max = registrars[0]?.count ?? 1;

  return (
    <div className="bg-white border border-gray-200 rounded-lg overflow-hidden mb-4">
      <div className="px-4 pt-3.5 pb-2.5 border-b border-gray-100">
        <div className="flex items-center gap-1.5 text-[13px] font-semibold text-gray-800">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
            <rect x="3" y="3" width="18" height="18" rx="2" />
            <path d="M3 9h18M9 21V9" />
          </svg>
          Where they&apos;re registered
        </div>
        <div className="text-[11px] text-gray-400 mt-0.5">
          registrar confirmed by each registry, past hour — top 15
        </div>
      </div>
      <div className="p-4">
        {registrars.map((reg) => (
          <div key={reg.registrarId ?? reg.registrar} className="flex items-center gap-2.5 py-1.5">
            <span className="text-[13px] text-gray-900 min-w-[120px] whitespace-nowrap overflow-hidden text-ellipsis">{reg.registrar}</span>
            <div className="flex-1 h-2.5 bg-gray-100 rounded-[3px] overflow-hidden">
              <div
                className="h-full rounded-[3px] bg-gray-400 transition-all duration-400 ease-out"
                style={{ width: `${(reg.count / max) * 100}%` }}
              />
            </div>
            <span className="text-[12px] text-gray-500 min-w-[36px] text-right">{reg.count.toLocaleString()}</span>
          </div>
        ))}
        <p className="text-[11px] text-gray-400 mt-2.5 leading-relaxed">
          New names seen on the live feed in the past hour, with the registrar each registry records.
          Only names registered in the last 72 hours count, and a few extensions (e.g. .ru) aren&apos;t covered.
          For complete totals, see the daily count below.
        </p>
      </div>
    </div>
  );
}
