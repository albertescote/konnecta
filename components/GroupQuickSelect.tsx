"use client";

import { useState, useTransition } from "react";
import { Group } from "@/types";
import { setActiveGroup } from "@/app/actions/groups";
import { ChevronDown, Check, Users, X } from "lucide-react";
import Portal from "./Portal";

interface Props {
  groups: Group[];
  activeGroupId: string;
}

export default function GroupQuickSelect({ groups, activeGroupId }: Props) {
  const [isOpen, setIsOpen] = useState(false);
  const [isPending, startTransition] = useTransition();

  const activeGroup = groups.find((g) => g.id === activeGroupId);

  const handleSwitch = (groupId: string) => {
    if (groupId === activeGroupId) {
      setIsOpen(false);
      return;
    }
    startTransition(async () => {
      await setActiveGroup(groupId);
      setIsOpen(false);
    });
  };

  return (
    <>
      <button
        onClick={() => groups.length > 1 && setIsOpen(true)}
        className="flex items-center gap-1.5 group text-left max-w-full"
        title={groups.length > 1 ? "Canviar de grup" : undefined}
      >
        <p className="text-zinc-500 text-[10px] font-black uppercase tracking-[0.2em] mt-3 truncate group-active:text-zinc-800 dark:group-active:text-zinc-300 transition-colors">
          {activeGroup?.name || "Benvingut"}
        </p>
        {groups.length > 1 && (
          <ChevronDown size={10} className="mt-3 text-zinc-400 group-hover:text-zinc-600 transition-colors shrink-0" />
        )}
      </button>

      {isOpen && (
        <Portal>
          <div 
            className="fixed inset-0 z-[10000] flex flex-col justify-end sm:justify-center items-center p-4 bg-black/60 backdrop-blur-sm animate-in fade-in duration-300"
            onClick={() => setIsOpen(false)}
          >
            <div 
              className="w-full max-w-sm bg-white dark:bg-zinc-900 rounded-t-[2.5rem] sm:rounded-[2.5rem] shadow-2xl overflow-hidden border border-zinc-100 dark:border-zinc-800 animate-in slide-in-from-bottom-8 sm:zoom-in-95 duration-300 flex flex-col max-h-[70vh]"
              onClick={(e) => e.stopPropagation()}
            >
              <div className="p-6 flex items-center justify-between border-b border-zinc-50 dark:border-zinc-800 flex-shrink-0">
                <div className="flex items-center gap-2">
                  <Users size={18} className="text-blue-500" />
                  <h3 className="font-black text-sm tracking-widest text-zinc-950 dark:text-white uppercase">
                    Els teus grups
                  </h3>
                </div>
                <button 
                  onClick={() => setIsOpen(false)} 
                  className="p-2 text-zinc-400 hover:text-zinc-600 dark:hover:text-zinc-200 transition-colors"
                >
                  <X size={20} />
                </button>
              </div>

              <div className="overflow-y-auto no-scrollbar p-4 space-y-2">
                {groups.map((group) => {
                  const isSelected = group.id === activeGroupId;
                  return (
                    <button
                      key={group.id}
                      onClick={() => handleSwitch(group.id)}
                      disabled={isPending}
                      className={`w-full flex items-center justify-between p-5 rounded-3xl transition-all active:scale-[0.98] ${
                        isSelected 
                          ? "bg-zinc-900 dark:bg-zinc-100 text-white dark:text-zinc-900 shadow-lg" 
                          : "bg-zinc-50 dark:bg-zinc-800/50 text-zinc-600 dark:text-zinc-400 hover:bg-zinc-100 dark:hover:bg-zinc-800"
                      }`}
                    >
                      <span className="font-black text-xs uppercase tracking-widest truncate pr-4">
                        {group.name}
                      </span>
                      {isSelected && <Check size={18} />}
                    </button>
                  );
                })}
              </div>

              <div className="p-6 bg-zinc-50/50 dark:bg-zinc-800/20 text-center flex-shrink-0">
                <p className="text-[9px] font-bold text-zinc-400 uppercase tracking-widest">
                  {groups.length} {groups.length === 1 ? 'grup disponible' : 'grups disponibles'}
                </p>
              </div>
            </div>
          </div>
        </Portal>
      )}
    </>
  );
}
