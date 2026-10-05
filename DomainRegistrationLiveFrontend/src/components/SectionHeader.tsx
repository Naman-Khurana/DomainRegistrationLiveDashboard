interface Props {
  ago?: number;
}

const EXCLUDED_TLDS = Array.from(
  new Set(
    (
      ".ph .de .ir .es .eu .at .vn .gr .ae .li .im .lv .ge .np " +
      ".pe .st .sa .bz .lk .bd .hr .zw .md .bg .by .az .uy .lu .tn .py .li " +
      ".am .at .tm .ao .cy .so .vc .ci .ee .ba .mn .mk .gt .qa .la .et .mt " +
      ".ve .mz .tj .ag .om .ug .bo .iq .mv .sy .edu .gl .do .cd .nc .lc"
    ).split(/\s+/)
  )
);

function timeAgo(diffInSeconds: number): string {
  if (diffInSeconds < 60) return `${diffInSeconds}s ago`;
  if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)}m ago`;
  return `${Math.floor(diffInSeconds / 3600)}h ago`;
}

export default function SectionHeader({ ago }: Props) {
  return (
    <>
      {/* ── Page header ── */}
      <div className="pt-8 pb-5">
        <div className="flex items-center justify-between mb-1.5">
          <h1 className="flex items-center gap-2.5 text-[22px] font-bold text-gray-900">
            Near Real Time Registrations
          </h1>

          {ago !== undefined && (
            <div className="flex items-center gap-2 text-xs font-semibold text-green-600" aria-live="polite">
              <span className="w-2 h-2 rounded-full bg-green-500 animate-pulse-dot" aria-hidden="true" />
              <span>LIVE</span>
              <span className="text-gray-400 font-normal" suppressHydrationWarning>
                · updated {timeAgo(ago)}
              </span>
            </div>
          )}
        </div>
        <p className="text-[13px] text-gray-500 max-w-xl leading-relaxed">
          A live sample of brand-new domains as they appear, updated every few seconds.{" "}
          <a href="#rising-keywords" className="text-blue-600 hover:underline">
            Click any keyword
          </a>{" "}
          to see the names behind it.
        </p>
      </div>

      <div className="bg-amber-50 border border-amber-300 rounded-full text-[12px] text-amber-800 px-3.5 py-2 mb-2.5" role="note">
        Live sample of registry-confirmed new registrations — not every registration. The complete daily count is
        further down the page.
      </div>

      <p className="text-[11px] text-gray-400 leading-relaxed mb-5">
        Not shown: {EXCLUDED_TLDS.join(" ")} — these registries don&apos;t publish a registration date, so new names
        there can&apos;t be confirmed.
      </p>
    </>
  );
}