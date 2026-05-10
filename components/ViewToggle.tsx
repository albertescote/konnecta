"use client";

import { useRouter, useSearchParams } from "next/navigation";
import { useState, useTransition, useEffect } from "react";

export default function ViewToggle() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const activeView = (searchParams.get("view") || "weekend") as "weekend" | "all";
  
  const [internalView, setInternalView] = useState(activeView);
  const [isPending, startTransition] = useTransition();

  // Sync with URL if changed externally (like from a swipe)
  useEffect(() => {
    setInternalView(activeView);
  }, [activeView]);

  const handleSwitch = (view: "weekend" | "all") => {
    if (view === internalView) return;
    
    // Optimistic update for the toggle UI
    setInternalView(view);
    
    startTransition(() => {
      const params = new URLSearchParams(searchParams.toString());
      params.set("view", view);
      router.push(`?${params.toString()}`, { scroll: false });
    });
  };

  return (
    <div className="flex items-center w-full max-w-md mx-auto h-12 relative">
      <button
        onClick={() => handleSwitch("weekend")}
        className={`flex-1 h-full flex items-center justify-center text-[11px] font-black uppercase tracking-[0.2em] transition-all duration-300 ${
          internalView === "weekend"
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
          internalView === "all"
            ? "text-zinc-950 dark:text-white"
            : "text-zinc-400"
        }`}
      >
        Tots els Plans
      </button>

      {/* Animated Underline */}
      <div 
        className={`absolute bottom-0 h-0.5 bg-zinc-950 dark:bg-white transition-all duration-300 ease-out ${isPending ? 'opacity-50' : 'opacity-100'}`}
        style={{ 
          width: '40%', 
          left: internalView === "weekend" ? '5%' : '55%' 
        }}
      />
    </div>
  );
}
