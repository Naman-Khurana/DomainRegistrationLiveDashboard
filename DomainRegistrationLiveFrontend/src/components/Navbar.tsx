"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const TABS: Array<{ label: string; href: string; icon?: string }> = [
  { label: "Live", href: "/", icon: "live" },
];

export default function Navbar() {
  const pathname = usePathname();

  return (
    <nav
      className="sticky top-0 z-50 border-b border-gray-200 bg-white"
      aria-label="Main navigation"
    >
      <div className="max-w-[1150px] mx-auto px-5 h-[56px] flex items-center justify-between">
        {/* Site name */}
        <Link
          href="/"
          className="text-[17px] font-semibold tracking-[-0.02em] text-[#1e2a3a]"
        >
          Domain Registration Dashboard
        </Link>

        {/* Navigation */}
        <div className="flex items-center">
          {TABS.map((tab) => {
            const isActive =
              tab.href === "/"
                ? pathname === "/"
                : pathname.startsWith(tab.href);

            return (
              <Link
                key={tab.href}
                href={tab.href}
                aria-current={isActive ? "page" : undefined}
                className={[
                  "flex items-center gap-2 rounded-lg px-4 py-2 text-[13px] font-medium",
                  "transition-colors duration-150 whitespace-nowrap",
                  isActive
                    ? "bg-[#1e2a3a] text-white"
                    : "text-gray-500 hover:bg-gray-100 hover:text-gray-800",
                ].join(" ")}
              >
                {tab.icon === "live" && (
                  <span
                    className="h-1.5 w-1.5 rounded-full bg-green-400"
                    aria-hidden="true"
                  />
                )}
                {tab.label}
              </Link>
            );
          })}
        </div>
      </div>
    </nav>
  );
}