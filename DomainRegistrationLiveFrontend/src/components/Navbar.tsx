"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const TABS: Array<{ label: string; href: string; icon?: string }> = [
  { label: "Live", href: "/", icon: "live" },
];

export default function Navbar() {
  const pathname = usePathname();

  return (
    <nav className="border-b border-gray-200 bg-white sticky top-0 z-50" aria-label="Main navigation">
      <div className="max-w-5xl mx-auto px-5 flex items-center gap-1 h-[52px]">
        {TABS.map((tab) => {
          const isActive =
            tab.href === "/" ? pathname === "/" : pathname.startsWith(tab.href);
          return (
            <Link
              key={tab.href}
              href={tab.href}
              aria-current={isActive ? "page" : undefined}
              className={[
                "flex items-center gap-1.5 px-3.5 py-1.5 rounded-full border text-[13px] font-medium transition-all duration-150 whitespace-nowrap",
                isActive
                  ? "bg-[#1e2a3a] text-white border-transparent"
                  : "border-transparent text-gray-500 hover:text-gray-800 hover:bg-gray-100",
              ].join(" ")}
            >
              {tab.icon === "live" && (
                <span
                  className="w-1.5 h-1.5 rounded-full bg-green-400 animate-pulse-dot"
                  aria-hidden="true"
                />
              )}
              {tab.label}
            </Link>
          );
        })}
      </div>
    </nav>
  );
}
