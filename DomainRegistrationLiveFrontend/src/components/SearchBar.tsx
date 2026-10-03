"use client";

import { useState } from "react";

interface Props {
  onSearch?: (query: string) => void;
}

export default function SearchBar({ onSearch }: Props) {
  const [value, setValue] = useState("");

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSearch?.(value.trim());
  };

  return (
    <form
      className="relative mb-6"
      onSubmit={handleSubmit}
      role="search"
      aria-label="Search live registrations"
    >
      {/* Search icon */}
      <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none" aria-hidden="true">
        <svg width="15" height="15" viewBox="0 0 15 15" fill="none">
          <circle cx="6.5" cy="6.5" r="5" stroke="currentColor" strokeWidth="1.5" />
          <line x1="10.5" y1="10.5" x2="14" y2="14" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
        </svg>
      </span>

      <input
        id="live-search"
        type="search"
        className="w-full py-3 pl-10 pr-28 text-[13.5px] border border-gray-200 rounded-lg bg-white text-gray-900 placeholder-gray-400 outline-none transition focus:border-gray-400 focus:ring-2 focus:ring-black/5 font-sans"
        placeholder="Search the live registrations: a keyword (ai, crypto, roofing...) or an extension (.si, .co.uk)"
        value={value}
        onChange={(e) => setValue(e.target.value)}
        autoComplete="off"
      />

      <button
        type="submit"
        id="live-search-btn"
        className="absolute right-1.5 top-1/2 -translate-y-1/2 flex items-center gap-1.5 px-4 py-1.5 bg-gray-600 hover:bg-gray-800 text-white text-[13px] font-medium rounded transition-colors font-sans"
      >
        <svg width="12" height="12" viewBox="0 0 15 15" fill="none" aria-hidden="true">
          <circle cx="6.5" cy="6.5" r="5" stroke="currentColor" strokeWidth="1.5" />
          <line x1="10.5" y1="10.5" x2="14" y2="14" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
        </svg>
        Search
      </button>
    </form>
  );
}
