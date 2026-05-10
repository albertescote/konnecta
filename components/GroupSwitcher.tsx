"use client";

import { useTransition } from "react";
import { Users, ChevronDown } from "lucide-react";
import { Group } from "@/types";
import { setActiveGroup } from "@/app/actions/groups";

interface Props {
  groups: Group[];
  activeGroupId: string;
}

export default function GroupSwitcher({ groups, activeGroupId }: Props) {
  const [isPending, startTransition] = useTransition();

  const activeGroup = groups.find((g) => g.id === activeGroupId) || groups[0];

  const handleSwitch = (groupId: string) => {
    if (groupId === activeGroupId) return;
    startTransition(async () => {
      await setActiveGroup(groupId);
    });
  };

  if (!groups.length) return null;

  return (
    <div className="relative group">
      <select
        value={activeGroupId}
        onChange={(e) => handleSwitch(e.target.value)}
        disabled={isPending}
        className="appearance-none pl-10 pr-10 py-3 rounded-2xl bg-white dark:bg-zinc-900 border border-zinc-100 dark:border-zinc-800 shadow-sm text-xs font-black uppercase tracking-widest text-zinc-950 dark:text-white outline-none focus:ring-2 focus:ring-blue-500/20 transition-all cursor-pointer disabled:opacity-50 min-w-[160px]"
      >
        {groups.map((group) => (
          <option key={group.id} value={group.id}>
            {group.name}
          </option>
        ))}
      </select>
      <div className="absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none text-zinc-400">
        <Users size={18} />
      </div>
      <div className="absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none text-zinc-400">
        <ChevronDown size={16} />
      </div>
    </div>
  );
}
