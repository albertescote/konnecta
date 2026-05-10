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
  const [offset, setOffset] = useState(0);
  const [isDragging, setIsDragging] = useState(false);
  
  const startX = useRef(0);
  const startY = useRef(0);
  const currentX = useRef(0);
  const directionLocked = useRef<"horizontal" | "vertical" | null>(null);

  const handleTouchStart = (e: React.TouchEvent) => {
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

    // Determine direction on first significant movement
    if (!directionLocked.current) {
      if (Math.abs(diffX) > 10 || Math.abs(diffY) > 10) {
        if (Math.abs(diffX) > Math.abs(diffY)) {
          directionLocked.current = "horizontal";
          setIsDragging(true);
        } else {
          directionLocked.current = "vertical";
        }
      }
    }

    if (directionLocked.current === "horizontal") {
      // Prevent vertical scrolling while swiping
      if (e.cancelable) e.preventDefault();

      let newOffset = diffX;

      // Friction/Resistance when swiping past limits
      if (activeView === "weekend" && newOffset > 0) newOffset /= 3;
      if (activeView === "all" && newOffset < 0) newOffset /= 3;

      setOffset(newOffset);
      currentX.current = touchX;
    }
  };

  const handleTouchEnd = () => {
    if (directionLocked.current === "horizontal") {
      const threshold = window.innerWidth * 0.25; // 25% of screen width
      const diffX = currentX.current - startX.current;

      if (Math.abs(diffX) > threshold) {
        if (diffX < 0 && activeView === "weekend") {
          updateView("all");
        } else if (diffX > 0 && activeView === "all") {
          updateView("weekend");
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

  // Calculate the final translation percentage
  const baseTranslate = activeView === "weekend" ? 0 : -50;
  // Convert pixel offset to percentage of the 200% width container
  const dragTranslate = (offset / (window.innerWidth * 2)) * 100;
  const totalTranslate = baseTranslate + dragTranslate;

  return (
    <div 
      className="w-full relative overflow-hidden touch-pan-y"
      onTouchStart={handleTouchStart}
      onTouchMove={handleTouchMove}
      onTouchEnd={handleTouchEnd}
    >
      <div 
        className={`flex w-[200%] ${!isDragging ? 'transition-transform duration-300 cubic-bezier(0.25, 0.1, 0.25, 1)' : ''}`}
        style={{ 
          transform: `translateX(${totalTranslate}%)`,
          willChange: 'transform'
        }}
      >
        <div className="w-1/2 flex-shrink-0">
          {children[0]}
        </div>
        <div className="w-1/2 flex-shrink-0">
          {activeView === "all" || isDragging ? children[1] : null}
        </div>
      </div>
    </div>
  );
}
