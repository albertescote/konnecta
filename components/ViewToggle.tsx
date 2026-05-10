"use client";

import { useRouter, useSearchParams } from "next/navigation";
import { CalendarDays, LayoutGrid } from "lucide-react";

export default function ViewToggle() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const currentView = searchParams.get("view") || "weekend";

  const handleSwitch = (view: "weekend" | "all") => {
    if (view === currentView) return;
    const params = new URLSearchParams(searchParams.toString());
    params.set("view", view);
    router.push(`?${params.toString()}`);
  };

  return (
    <div className="flex p-1 bg-zinc-100 dark:bg-zinc-800 rounded-2xl w-full max-w-md mx-auto shadow-inner">
      <button
        onClick={() => handleSwitch("weekend")}
        className={`flex-1 flex items-center justify-center gap-2 py-3 rounded-xl text-xs font-black uppercase tracking-widest transition-all ${
          currentView === "weekend"
            ? "bg-white dark:bg-zinc-700 text-zinc-950 dark:text-white shadow-sm scale-[1.02]"
            : "text-zinc-400 hover:text-zinc-600"
        }`}
      >
        <CalendarDays size={16} />
        Cap de Setmana
      </button>
      <button
        onClick={() => handleSwitch("all")}
        className={`flex-1 flex items-center justify-center gap-2 py-3 rounded-xl text-xs font-black uppercase tracking-widest transition-all ${
          currentView === "all"
            ? "bg-white dark:bg-zinc-700 text-zinc-950 dark:text-white shadow-sm scale-[1.02]"
            : "text-zinc-400 hover:text-zinc-600"
        }`}
      >
        <LayoutGrid size={16} />
        Tots els Plans
      </button>
    </div>
  );
}
