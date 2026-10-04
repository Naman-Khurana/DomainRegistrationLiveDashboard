"use client";

import { useState } from "react";
import type { RisingEntry } from "@/types/api";

type Window = "15m" | "1h" | "3h";

interface Props {
  data: Record<Window, RisingEntry[]>;
  /** Called with the keyword string when a pill is clicked. */
  onKeywordClick?: (keyword: string) => void;
}

const WINDOWS: { key: Window; label: string }[] = [
  { key: "15m", label: "15 min" },
  { key: "1h", label: "1 hour" },
  { key: "3h", label: "3 hours" },
];

export default function RisingKeywords({ data, onKeywordClick }: Props) {
  const [activeWindow, setActiveWindow] = useState<Window>("3h");
  const [selectedWord, setSelectedWord] = useState<string | null>(null);

  const keywords = data[activeWindow] ?? [];

  return (
    <div className="border border-gray-200 rounded-lg overflow-hidden" id="rising-keywords">
      {/* Header */}
      <div className="px-4 pt-3.5 pb-2.5 border-b border-gray-100">
        <div className="flex items-center gap-1.5 text-[13px] font-semibold text-gray-800">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" aria-hidden="true">
            <polyline points="23 6 13.5 15.5 8.5 10.5 1 18" />
            <polyline points="17 6 23 6 23 12" />
          </svg>
          Rising keywords
        </div>
        <div className="text-[11px] text-gray-400 mt-0.5">surging now vs the window just before</div>
      </div>

      {/* Body */}
      <div className="p-4">
        {/* Time tabs */}
        <div className="flex gap-1.5 mb-3.5" role="tablist" aria-label="Time window">
          {WINDOWS.map(({ key, label }) => (
            <button
              key={key}
              role="tab"
              aria-selected={activeWindow === key}
              id={`rising-tab-${key}`}
              onClick={() => { setActiveWindow(key); setSelectedWord(null); }}
              className={[
                "px-3.5 py-1 rounded-full border text-[12px] transition-all duration-100 font-sans",
                activeWindow === key
                  ? "bg-[#1e2a3a] text-white border-[#1e2a3a]"
                  : "border-gray-300 text-gray-500 hover:border-gray-400 hover:text-gray-700",
              ].join(" ")}
            >
              {label}
            </button>
          ))}
        </div>

        {/* Keyword pills */}
        <div className="flex flex-wrap gap-1.5 mb-3" role="list">
          {keywords.map((entry) => {
            const isNew = entry.prior === 0;
            const isSelected = selectedWord === entry.word;
            return (
              <button
                key={entry.word}
                role="listitem"
                aria-pressed={isSelected}
                id={`kw-${entry.word.toLowerCase()}`}
                onClick={() => {
                  setSelectedWord(isSelected ? null : entry.word);
                  onKeywordClick?.(entry.word);
                }}
                className={[
                  "inline-flex items-center gap-1 px-2.5 py-1 border rounded-full text-[12px] transition-all duration-100 font-sans cursor-pointer",
                  isSelected
                    ? "bg-[#1e2a3a] text-white border-[#1e2a3a]"
                    : "bg-white border-gray-200 text-gray-800 hover:border-gray-400 hover:bg-gray-50",
                ].join(" ")}
              >
                <span className="font-medium">{entry.word}</span>
                {isNew ? (
                  <span className={`text-[10px] font-bold px-1.5 py-0.5 rounded ${isSelected ? "bg-white/15 text-green-300" : "bg-green-100 text-green-700"}`}>
                    NEW
                  </span>
                ) : (
                  <span className={`text-[11px] font-semibold ${isSelected ? "text-green-300" : "text-green-600"}`}>
                    ×{entry.lift.toFixed(1)}
                  </span>
                )}
                <span className={`text-[11px] ${isSelected ? "text-white/50" : "text-gray-400"}`}>
                  {entry.recent}
                </span>
              </button>
            );
          })}
        </div>

        {/* Legend */}
        <div className="text-[11px] text-gray-400 leading-relaxed border-t border-gray-100 pt-2.5">
          <strong>×N</strong> = rising N× faster than the prior {activeWindow} ·{" "}
          <strong>N</strong> = registered in the last {activeWindow} ·{" "}
          <strong>NEW</strong> = none before this window
        </div>
      </div>
    </div>
  );
}
