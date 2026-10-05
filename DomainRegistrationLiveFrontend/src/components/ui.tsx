

import React from "react";


type HourSlot = "this" | "last" | "2h";

const HOUR_SLOTS: { key: HourSlot; label: string }[] = [
  { key: "this", label: "This hour" },
  { key: "last", label: "Last hour" },
  { key: "2h", label: "2 hours ago" },
];

interface HourSelectorProps {
  value: HourSlot;
  onChange: (v: HourSlot) => void;
}

export function HourSelector({ value, onChange }: HourSelectorProps) {
  return (
    <div>
      <div className="flex gap-1.5 mb-2">
        {HOUR_SLOTS.map(({ key, label }) => (
          <button
            key={key}
            className={[
              "px-3.5 py-1 rounded-full border text-[12px] transition-all duration-100 font-sans cursor-pointer",
              value === key
                ? "bg-[#1e2a3a] text-white border-[#1e2a3a]"
                : "border-gray-300 text-gray-500 hover:border-gray-400 hover:text-gray-700",
            ].join(" ")}
            onClick={() => onChange(key)}
            id={`hour-slot-${key}`}
          >
            {label}
          </button>
        ))}
      </div>
      <p className="text-[12px] text-gray-400 mb-4.5">
        Tip: type an extension in the search box (for example{" "}
        <code>.si</code>) to see up to 200 of its new domains for the hour
        selected above.
      </p>
    </div>
  );
}
