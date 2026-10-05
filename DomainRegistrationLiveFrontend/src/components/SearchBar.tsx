"use client";

import { useEffect, useImperativeHandle, useRef, useState, forwardRef } from "react";
import type { SearchEntry, SearchResponse } from "@/types/api";
import { SEARCH_URL } from "@/app/constants/url_constants";

// ─── Time range configuration ─────────────────────────────────────────────────

type HValue = 0 | 60 | 120;

interface TimeRange {
  h: HValue;
  label: string;
  resultLabel: string;
}

const TIME_RANGES: TimeRange[] = [
  { h: 0, label: "This hour", resultLabel: "in the past hour" },
  { h: 60, label: "Last hour", resultLabel: "in the last hour" },
  { h: 120, label: "2 hours ago", resultLabel: "2 hours ago" },
];

// ─── TLD badge colours ────────────────────────────────────────────────────────

function tldClass(tld: string): string {
  const t = tld.replace(/^\./, "");
  const map: Record<string, string> = {
    com: "bg-green-100 text-green-700",
    net: "bg-emerald-100 text-emerald-800",
    org: "bg-indigo-100 text-indigo-700",
    io: "bg-sky-100 text-sky-700",
    ai: "bg-amber-100 text-amber-800",
    xyz: "bg-purple-100 text-purple-700",
    app: "bg-green-100 text-green-700",
    tech: "bg-blue-100 text-blue-700",
    online: "bg-pink-100 text-pink-700",
  };
  return map[t] ?? "bg-gray-100 text-gray-500";
}

// ─── Result summary line ──────────────────────────────────────────────────────

function resultSummary(count: number, keyword: string, h: HValue): string {
  const range = TIME_RANGES.find((r) => r.h === h)!;
  return `${count} new registration${count !== 1 ? "s" : ""} containing "${keyword}" ${range.resultLabel}`;
}


export interface SearchBarHandle {
  /** Programmatically submit a keyword exactly as if the user typed and pressed Search. */
  triggerSearch: (keyword: string) => void;
}



const SearchBar = forwardRef<SearchBarHandle>(function SearchBar(_, ref) {

  const [inputValue, setInputValue] = useState("");

  const [submittedQuery, setSubmittedQuery] = useState("");

  const [selectedH, setSelectedH] = useState<HValue>(0);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [results, setResults] = useState<SearchResponse | null>(null);
  const isOpen = submittedQuery.trim().length > 0;

  const abortRef = useRef<AbortController | null>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  // Cancel any in-flight request when the component unmounts
  useEffect(() => () => abortRef.current?.abort(), []);

  const submitQuery = (q: string) => {
    const trimmed = q.trim();
    if (!trimmed) return;
    setInputValue(trimmed);
    setSelectedH(0);
    setSubmittedQuery(trimmed);
  };

  useEffect(() => {
    if (!submittedQuery.trim()) return;

    abortRef.current?.abort();
    const controller = new AbortController();
    abortRef.current = controller;

    setLoading(true);
    setError(null);

    const params = new URLSearchParams({
      q: submittedQuery.trim(),
      h: String(selectedH),
    });

    fetch(`${SEARCH_URL}?${params}`, {
      cache: "no-store",
      signal: controller.signal,
    })
      .then((res) => {
        if (!res.ok) throw new Error(`Search failed (${res.status})`);
        return res.json() as Promise<SearchResponse>;
      })
      .then((data) => {
        if (!controller.signal.aborted) setResults(data);
      })
      .catch(() => {
        if (!controller.signal.aborted)
          setError("Search is unavailable right now. Please try again.");
      })
      .finally(() => {
        if (abortRef.current === controller) setLoading(false);
      });
  }, [submittedQuery, selectedH]);

  useImperativeHandle(ref, () => ({
    triggerSearch: (keyword: string) => {
      submitQuery(keyword);
      // Scroll the search bar into view so the user can see it open
      setTimeout(() => inputRef.current?.scrollIntoView({ behavior: "smooth", block: "nearest" }), 50);
    },
  }));

  // ── Close / reset ──
  const handleClose = () => {
    abortRef.current?.abort();
    setSubmittedQuery("");
    setInputValue("");
    setSelectedH(0);
    setResults(null);
    setLoading(false);
    setError(null);
  };


  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    submitQuery(inputValue);
    if (!inputValue.trim()) handleClose();
  };


  const handleRangeChange = (h: HValue) => {
    if (h === selectedH) return;
    setSelectedH(h);
  };

  return (
    <div className="mb-6">
      {/* ── Search input row ── */}
      <form
        onSubmit={handleSubmit}
        role="search"
        aria-label="Search live registrations"
        className={`flex items-center gap-2 px-3 py-2 border rounded-lg bg-white transition-all ${isOpen
          ? "border-gray-300 shadow-sm"
          : "border-gray-200"
          }`}
      >
        {/* Left icon */}
        <span className="shrink-0 text-gray-400 pointer-events-none" aria-hidden="true">
          <svg width="16" height="16" viewBox="0 0 15 15" fill="none">
            <circle cx="6.5" cy="6.5" r="5" stroke="currentColor" strokeWidth="1.5" />
            <line x1="10.5" y1="10.5" x2="14" y2="14" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
          </svg>
        </span>

        {/* Input */}
        <input
          ref={inputRef}
          id="live-search"
          type="search"
          className="flex-1 min-w-0 py-1 text-[13.5px] bg-transparent text-gray-900 placeholder-gray-400 outline-none font-sans"
          placeholder="Search the live registrations: a keyword (ai, crypto, roofing...) or an extension (.si, .co.uk)"
          value={inputValue}
          onChange={(e) => {
            const v = e.target.value;
            setInputValue(v);
            if (v === "") handleClose();
          }}
          autoComplete="off"
        />

        {/* Search button */}
        <button
          type="submit"
          id="live-search-btn"
          disabled={loading}
          className="shrink-0 flex items-center gap-1.5 px-4 py-1.5 bg-gray-700 hover:bg-gray-900 text-white text-[12.5px] font-medium rounded-md transition-colors font-sans disabled:opacity-60 disabled:cursor-not-allowed"
        >
          <svg width="11" height="11" viewBox="0 0 15 15" fill="none" aria-hidden="true">
            <circle cx="6.5" cy="6.5" r="5" stroke="currentColor" strokeWidth="1.5" />
            <line x1="10.5" y1="10.5" x2="14" y2="14" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
          </svg>
          {loading ? "Searching…" : "Search"}
        </button>

        {/* Close button — only shown when results are open */}
        {isOpen && (
          <button
            type="button"
            onClick={handleClose}
            aria-label="Close search results"
            className="shrink-0 w-7 h-7 flex items-center justify-center rounded-full text-gray-400 hover:text-gray-700 hover:bg-gray-100 transition-colors"
          >
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        )}
      </form>

      {/* Expanded results panel */}
      {isOpen && (
        <div className="mt-3 border border-gray-200 rounded-lg bg-white p-4">

          <div className="flex items-center gap-2 mb-3">
            {TIME_RANGES.map(({ h, label }) => (
              <button
                key={h}
                type="button"
                onClick={() => handleRangeChange(h)}
                className={`px-3 py-1 rounded-full text-[12px] font-medium transition-colors ${selectedH === h
                  ? "bg-[#1e2a3a] text-white"
                  : "bg-white border border-gray-300 text-gray-600 hover:bg-gray-50"
                  }`}
              >
                {label}
              </button>
            ))}
          </div>

          {/* Result summary */}
          {!loading && !error && results && (
            <p className="text-[12.5px] text-gray-600 mb-3">
              {resultSummary(results.entries.length, results.keyword, selectedH)}
            </p>
          )}

          {/* Loading state */}
          {loading && (
            <div className="py-6 text-center text-[12px] text-gray-400 animate-pulse">
              Searching…
            </div>
          )}

          {/* Error state */}
          {!loading && error && (
            <p role="alert" className="py-4 text-center text-[12px] text-red-500">
              {error}
            </p>
          )}

          {/* Results grid */}
          {!loading && !error && results && results.entries.length === 0 && (
            <p className="py-4 text-center text-[12px] text-gray-400">
              No registrations found for &ldquo;{results.keyword}&rdquo; in this time range.
            </p>
          )}

          {!loading && !error && results && results.entries.length > 0 && (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-x-6 gap-y-0">
              {results.entries.map((entry: SearchEntry) => (
                <div
                  key={`${entry.seq}-${entry.domain}`}
                  className="flex items-center justify-between py-2 border-b border-gray-100 last:border-0 gap-2 min-w-0"
                >
                  {/* Domain */}
                  <span
                    className="text-[12.5px] font-medium text-gray-900 truncate min-w-0"
                    title={entry.domain}
                  >
                    {entry.domain}
                  </span>

                  {/* Registrar + TLD badge */}
                  <div className="flex items-center gap-1.5 shrink-0">
                    <span className="text-[11px] text-gray-400 whitespace-nowrap max-w-[90px] truncate" title={entry.registrar}>
                      {entry.registrar}
                    </span>
                    <span className={`text-[10.5px] font-semibold px-1.5 py-0.5 rounded whitespace-nowrap ${tldClass(entry.tld)}`}>
                      {entry.tld}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
});

export default SearchBar;