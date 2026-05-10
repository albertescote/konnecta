"use client";

import { useState, useTransition } from "react";
import { Users, ChevronDown, Plus, UserPlus } from "lucide-react";
import { Group } from "@/types";
import { setActiveGroup } from "@/app/actions/groups";
import GroupModal from "./GroupModal";

interface Props {
  groups: Group[];
  activeGroupId: string;
}

export default function GroupSwitcher({ groups, activeGroupId }: Props) {
  const [isPending, startTransition] = useTransition();
  const [isModalOpen, setIsModalOpen] = useState(false);

  const activeGroup = groups.find((g) => g.id === activeGroupId) || groups[0];

  const handleSwitch = (groupId: string) => {
    if (groupId === activeGroupId) return;
    startTransition(async () => {
      await setActiveGroup(groupId);
    });
  };

  return (
    <>
      <div className="flex items-center gap-2">
        <div className="relative flex-1">
          <select
            value={activeGroupId}
            onChange={(e) => handleSwitch(e.target.value)}
            disabled={isPending}
            className="w-full appearance-none pl-10 pr-10 py-3 rounded-2xl bg-white dark:bg-zinc-900 border border-zinc-100 dark:border-zinc-800 shadow-sm text-xs font-black uppercase tracking-widest text-zinc-950 dark:text-white outline-none focus:ring-2 focus:ring-blue-500/20 transition-all cursor-pointer disabled:opacity-50"
          >
            {groups.length > 0 ? (
              groups.map((group) => (
                <option key={group.id} value={group.id}>
                  {group.name}
                </option>
              ))
            ) : (
              <option value="">Cap grup seleccionat</option>
            )}
          </select>
          <div className="absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none text-zinc-400">
            <Users size={18} />
          </div>
          <div className="absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none text-zinc-400">
            <ChevronDown size={16} />
          </div>
        </div>

        <button
          onClick={() => setIsModalOpen(true)}
          className="p-3 rounded-2xl bg-white dark:bg-zinc-900 border border-zinc-100 dark:border-zinc-800 shadow-sm text-zinc-500 dark:text-zinc-400 hover:scale-105 active:scale-95 transition-all"
          title={activeGroup ? "Convida amics" : "Nou grup"}
        >
          {activeGroup ? <UserPlus size={20} /> : <Plus size={20} />}
        </button>
      </div>

      {isModalOpen && (
        <GroupModal
          onClose={() => setIsModalOpen(false)}
          activeGroup={activeGroup}
        />
      )}
    </>
  );
}
