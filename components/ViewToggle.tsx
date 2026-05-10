"use client";

import { useRouter, useSearchParams } from "next/navigation";

export default function ViewToggle() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const currentView = searchParams.get("view") || "weekend";

  const handleSwitch = (view: "weekend" | "all") => {
    if (view === currentView) return;
    const params = new URLSearchParams(searchParams.toString());
    params.set("view", view);
    router.push(`?${params.toString()}`, { scroll: false });
  };

  return (
    <div className="flex items-center w-full max-w-md mx-auto h-12 relative">
      <button
        onClick={() => handleSwitch("weekend")}
        className={`flex-1 h-full flex items-center justify-center text-[11px] font-black uppercase tracking-[0.2em] transition-all duration-300 ${
          currentView === "weekend"
            ? "text-zinc-950 dark:text-white"
            : "text-zinc-400"
        }`}
      >
        Cap de Setmana
      </button>
      
      <div className="h-4 w-[1px] bg-zinc-200 dark:bg-zinc-800" />

      <button
        onClick={() => handleSwitch("all")}
        className={`flex-1 h-full flex items-center justify-center text-[11px] font-black uppercase tracking-[0.2em] transition-all duration-300 ${
          currentView === "all"
            ? "text-zinc-950 dark:text-white"
            : "text-zinc-400"
        }`}
      >
        Tots els Plans
      </button>

      {/* Animated Underline */}
      <div 
        className="absolute bottom-0 h-0.5 bg-zinc-950 dark:bg-white transition-all duration-300 ease-out"
        style={{ 
          width: '40%', 
          left: currentView === "weekend" ? '5%' : '55%' 
        }}
      />
    </div>
  );
}
