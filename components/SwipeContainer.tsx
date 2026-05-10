"use client";

import { useEffect, useRef, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";

interface Props {
  children: [React.ReactNode, React.ReactNode]; // [WeekendView, PlansHubView]
  activeView: "weekend" | "all";
}

export default function SwipeContainer({ children, activeView }: Props) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const containerRef = useRef<HTMLDivElement>(null);
  const [touchStart, setTouchStart] = useState<number | null>(null);
  const [touchEnd, setTouchEnd] = useState<number | null>(null);
  const [isSwiping, setIsSwiping] = useState(false);
  const [offset, setOffset] = useState(0);

  // Minimum distance to trigger swipe (pixels)
  const minSwipeDistance = 50;

  const handleTouchStart = (e: React.TouchEvent) => {
    setTouchEnd(null);
    setTouchStart(e.targetTouches[0].clientX);
    setIsSwiping(true);
  };

  const handleTouchMove = (e: React.TouchEvent) => {
    if (!touchStart) return;
    const currentTouch = e.targetTouches[0].clientX;
    const diff = currentTouch - touchStart;
    
    // Only allow swiping in valid directions based on current view
    if (activeView === "weekend" && diff > 0) return;
    if (activeView === "all" && diff < 0) return;

    setOffset(diff);
    setTouchEnd(currentTouch);
  };

  const handleTouchEnd = () => {
    setIsSwiping(false);
    setOffset(0);
    if (!touchStart || !touchEnd) return;
    
    const distance = touchStart - touchEnd;
    const isLeftSwipe = distance > minSwipeDistance;
    const isRightSwipe = distance < -minSwipeDistance;

    if (isLeftSwipe && activeView === "weekend") {
      updateView("all");
    } else if (isRightSwipe && activeView === "all") {
      updateView("weekend");
    }
  };

  const updateView = (view: "weekend" | "all") => {
    const params = new URLSearchParams(searchParams.toString());
    params.set("view", view);
    router.push(`?${params.toString()}`, { scroll: false });
  };

  return (
    <div 
      ref={containerRef}
      className="w-full relative overflow-hidden touch-pan-y"
      onTouchStart={handleTouchStart}
      onTouchMove={handleTouchMove}
      onTouchEnd={handleTouchEnd}
    >
      <div 
        className={`flex w-[200%] transition-transform duration-300 ease-out ${isSwiping ? 'transition-none' : ''}`}
        style={{ 
          transform: `translateX(${activeView === "weekend" ? (offset > 0 ? 0 : offset) : -50 + (offset / window.innerWidth * 100)}%)` 
        }}
      >
        <div className="w-1/2 flex-shrink-0">
          {children[0]}
        </div>
        <div className="w-1/2 flex-shrink-0">
          {activeView === "all" || isSwiping ? children[1] : null}
        </div>
      </div>
    </div>
  );
}
