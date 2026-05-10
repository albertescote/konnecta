"use client";

import { useEffect, useRef, useState, useTransition } from "react";
import { useRouter, useSearchParams } from "next/navigation";

interface Props {
  children: [React.ReactNode, React.ReactNode]; // [WeekendView, PlansHubView]
  activeView: "weekend" | "all";
}

export default function SwipeContainer({ children, activeView }: Props) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [isPending, startTransition] = useTransition();
  
  // Local state to provide instant feedback
  const [internalView, setInternalView] = useState(activeView);
  const [offset, setOffset] = useState(0);
  const [isDragging, setIsDragging] = useState(false);
  const [isTransitioning, setIsTransitioning] = useState(false);
  
  const startX = useRef(0);
  const startY = useRef(0);
  const currentX = useRef(0);
  const directionLocked = useRef<"horizontal" | "vertical" | null>(null);

  // Scroll to top when view changes
  useEffect(() => {
    window.scrollTo({ top: 0, behavior: "instant" });
  }, [internalView]);

  // Sync internal state if props change (e.g. from the toggle button)
  if (activeView !== internalView && !isDragging) {
    setInternalView(activeView);
    setIsTransitioning(true);
  }

  const handleTouchStart = (e: React.TouchEvent) => {
    const target = e.target as HTMLElement;
    if (target.closest('[data-no-swipe]')) {
      directionLocked.current = "vertical";
      return;
    }

    startX.current = e.touches[0].clientX;
    startY.current = e.touches[0].clientY;
    currentX.current = e.touches[0].clientX;
    directionLocked.current = null;
    setIsDragging(false);
    setOffset(0);
  };

  const handleTouchMove = (e: React.TouchEvent) => {
    const touchX = e.touches[0].clientX;
    const touchY = e.touches[0].clientY;
    const diffX = touchX - startX.current;
    const diffY = touchY - startY.current;

    if (!directionLocked.current) {
      if (Math.abs(diffX) > 10 || Math.abs(diffY) > 10) {
        if (Math.abs(diffX) > Math.abs(diffY)) {
          directionLocked.current = "horizontal";
          setIsDragging(true);
          setIsTransitioning(true);
        } else {
          directionLocked.current = "vertical";
        }
      }
    }

    if (directionLocked.current === "horizontal") {
      if (e.cancelable) e.preventDefault();
      let newOffset = diffX;
      if (internalView === "weekend" && newOffset > 0) newOffset /= 3;
      if (internalView === "all" && newOffset < 0) newOffset /= 3;
      setOffset(newOffset);
      currentX.current = touchX;
    }
  };

  const handleTouchEnd = () => {
    if (directionLocked.current === "horizontal") {
      const threshold = window.innerWidth * 0.25;
      const diffX = currentX.current - startX.current;

      if (Math.abs(diffX) > threshold) {
        const nextView = diffX < 0 ? "all" : "weekend";
        if (nextView !== internalView) {
          setInternalView(nextView);
          startTransition(() => {
            updateView(nextView);
          });
        }
      }
    }

    setIsDragging(false);
    setOffset(0);
    directionLocked.current = null;
  };

  const updateView = (view: "weekend" | "all") => {
    const params = new URLSearchParams(searchParams.toString());
    params.set("view", view);
    router.push(`?${params.toString()}`, { scroll: false });
  };

  const baseTranslate = internalView === "weekend" ? 0 : -50;
  const dragTranslate = (offset / (window.innerWidth * 2)) * 100;
  const totalTranslate = baseTranslate + dragTranslate;

  return (
    <div 
      className="w-full relative overflow-hidden touch-pan-y flex flex-col"
      onTouchStart={handleTouchStart}
      onTouchMove={handleTouchMove}
      onTouchEnd={handleTouchEnd}
    >
      {/* Loading bar for server updates */}
      <div className={`absolute top-0 left-0 h-0.5 bg-blue-500 z-50 transition-all duration-500 ${isPending ? 'w-full opacity-100' : 'w-0 opacity-0'}`} />

      <div 
        className={`flex w-[200%] items-start ${!isDragging ? 'transition-transform duration-300 cubic-bezier(0.25, 0.1, 0.25, 1)' : ''}`}
        style={{ 
          transform: `translateX(${totalTranslate}%)`,
          willChange: 'transform'
        }}
        onTransitionEnd={() => setIsTransitioning(false)}
      >
        {/* Weekend View */}
        <div 
          className={`w-1/2 flex-shrink-0 transition-opacity duration-300 ${internalView === "weekend" ? 'opacity-100' : 'opacity-0'}`}
          style={{ 
            height: internalView === "weekend" || isTransitioning ? 'auto' : 0,
            overflow: internalView === "weekend" || isTransitioning ? 'visible' : 'hidden'
          }}
        >
          {children[0]}
        </div>

        {/* Plans Hub View */}
        <div 
          className={`w-1/2 flex-shrink-0 transition-opacity duration-300 ${internalView === "all" ? 'opacity-100' : 'opacity-0'}`}
          style={{ 
            height: internalView === "all" || isTransitioning ? 'auto' : 0,
            overflow: internalView === "all" || isTransitioning ? 'visible' : 'hidden'
          }}
        >
          {children[1]}
        </div>
      </div>
    </div>
  );
}
