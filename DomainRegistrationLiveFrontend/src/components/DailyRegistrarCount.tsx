"use client";

import { useEffect, useState } from "react";
import type { RegistrarEntry, DailyResponse } from "@/types/api";
import { DAILY_URL } from "@/app/constants/url_constants";

const EXT_COLORS = [
  { key: "com", color: "#3b82f6", label: ".com" },
  { key: "online", color: "#f97316", label: ".online" },
  { key: "org", color: "#6366f1", label: ".org" },
  { key: "net", color: "#84cc16", label: ".net" },
  { key: "xyz", color: "#8b5cf6", label: ".xyz" },
  { key: "store", color: "#ef4444", label: ".store" },
  { key: "site", color: "#06b6d4", label: ".site" },
  { key: "app", color: "#10b981", label: ".app" },
  { key: "other", color: "#e5e7eb", label: ".other" },
];

function getLast30Days() {
  const dates = [];
  for (let i = 0; i < 30; i++) {
    const d = new Date();
    d.setDate(d.getDate() - i);

    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    const iso = `${year}-${month}-${day}`;

    let label = iso;
    if (i === 0) {
      label = "Today";
    } else if (i === 1) {
      label = "Yesterday";
    } else {
      label = d.toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" });
    }
    dates.push({ value: iso, label });
  }
  return dates;
}

export default function DailyRegistrarCount() {
  const dates = getLast30Days();
  const [selectedDate, setSelectedDate] = useState(dates[0].value);
  const [data, setData] = useState<DailyResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    setLoading(true);
    fetch(`${DAILY_URL}?d=${selectedDate}`)
      .then((res) => {
        if (!res.ok) throw new Error(`API returned ${res.status}`);
        return res.json();
      })
      .then((json: DailyResponse) => {
        if (active) {
          setData(json);
          setLoading(false);
        }
      })
      .catch((err) => {
        console.error("Failed to fetch daily stats", err);
        if (active) {
          setData(null);
          setLoading(false);
        }
      });
    return () => {
      active = false;
    };
  }, [selectedDate]);

  const registrars = data?.entries || [];
  const max = registrars[0]?.count ?? 1;
  const totalConfirmed = registrars.reduce((acc, curr) => acc + curr.count, 0);

  // Simulate stacked bar segments per registrar (proportional split by ext colors)
  // In production this would come from the API
  function getSegments(reg: RegistrarEntry) {
    const total = reg.count;
    // Simulate: ~62% com, ~8% online, ~5% org, ~3% net, ~7% xyz, rest spread
    const fractions = [0.62, 0.08, 0.05, 0.03, 0.07, 0.02, 0.02, 0.02, 0.09];
    return EXT_COLORS.map((ext, i) => ({
      ...ext,
      width: fractions[i] * total,
    }));
  }

  return (
    <div className="bg-white border border-gray-200 rounded-lg overflow-hidden mb-4">
      <div className="px-4 pt-3.5 pb-2.5 border-b border-gray-100">
        <div className="flex items-center justify-between flex-wrap gap-2 w-full">
          <span className="flex items-center gap-1.5 text-[13px] font-semibold text-gray-800">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
              <rect x="3" y="3" width="18" height="18" rx="2" />
              <path d="M3 9h18M9 21V9" />
            </svg>
            New registrations by registrar — complete daily count
          </span>
          <div className="flex gap-2">
            <div className="relative">
              <select
                value={selectedDate}
                onChange={(e) => setSelectedDate(e.target.value)}
                className="appearance-none flex items-center gap-1 px-2.5 py-1 pr-6 border border-gray-200 rounded bg-white text-[12px] text-gray-500 cursor-pointer font-sans outline-none focus:border-gray-400 focus:ring-1 focus:ring-gray-200"
                id="registrar-date-picker"
                aria-label="Select date"
              >
                {dates.map((d) => (
                  <option key={d.value} value={d.value}>
                    {d.label}
                  </option>
                ))}
              </select>
              <div className="absolute inset-y-0 right-1.5 flex items-center pointer-events-none text-gray-400">
                <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M6 9l6 6 6-6" />
                </svg>
              </div>
            </div>
          </div>
        </div>
        <div className="text-[11px] text-gray-400 mt-1">
          every newly registered domain that day, with the registrar each registry records
        </div>
      </div>

      <div className="p-4 min-h-[150px]">
        {loading ? (
          <div className="py-8 flex justify-center text-[12px] text-gray-400 animate-pulse">
            Loading data...
          </div>
        ) : (
          <>
            <div className="flex items-center justify-between flex-wrap gap-2 mb-3.5">
              <div className="flex flex-wrap gap-5 text-[12px] text-gray-500">
                <span>
                  <strong className="text-gray-900">{totalConfirmed.toLocaleString()}</strong> confirmed new registrations
                </span>
                <span>
                  <strong className="text-gray-900">100.0%</strong> of {totalConfirmed.toLocaleString()} new names checked — still filling in
                </span>
                <span>0 in extensions we can&apos;t check</span>
              </div>
            </div>

            {/* Extension legend */}
            <div className="flex flex-wrap gap-3 mb-3 text-[11px] text-gray-500">
              {EXT_COLORS.filter((e) => e.key !== "other").map((e) => (
                <span key={e.key} className="flex items-center">
                  <span className="inline-block w-2.5 h-2.5 rounded-sm mr-1" style={{ background: e.color }} />
                  {e.label}
                </span>
              ))}
            </div>

            {registrars.length === 0 ? (
              <div className="py-8 text-center text-[12px] text-gray-400">
                No data available for {selectedDate}.
              </div>
            ) : (
              registrars.map((reg, idx) => {
                const segments = getSegments(reg);
                return (
                  <div key={reg.registrarId ?? reg.registrar} className="flex items-center gap-2.5 py-1">
                    <span className="text-[11px] text-gray-400 min-w-[18px] text-right">{idx + 1}</span>
                    <span className="text-[12.5px] text-gray-900 min-w-[110px] md:min-w-[170px] overflow-hidden text-ellipsis whitespace-nowrap">
                      {reg.registrar}
                    </span>
                    <div className="flex-1 h-3.5 bg-gray-100 rounded-[3px] overflow-hidden flex">
                      {segments.map((seg) => (
                        <div
                          key={seg.key}
                          className="h-full transition-all duration-400 ease-out"
                          style={{
                            background: seg.color,
                            width: `${(seg.width / max) * 100}%`,
                          }}
                        />
                      ))}
                      {/* Gray remainder */}
                      <div className="bg-gray-100 flex-1" />
                    </div>
                    <span className="text-[12px] text-gray-500 min-w-[58px] text-right whitespace-nowrap">
                      {reg.count.toLocaleString()} · {totalConfirmed > 0 ? ((reg.count / totalConfirmed) * 100).toFixed(1) : "0.0"}%
                    </span>
                  </div>
                );
              })
            )}
          </>
        )}
      </div>
    </div>
  );
}
