"use client";

import { useState } from "react";
import { Users, Plus } from "lucide-react";
import GroupModal from "./GroupModal";

interface Props {
  userId: string;
}

export default function NoGroupView({ userId }: Props) {
  const [showModal, setShowModal] = useState(false);

  return (
    <div className="flex flex-col items-center gap-4 text-center py-24 px-6">
      <div className="w-20 h-20 bg-zinc-100 dark:bg-zinc-800 rounded-full flex items-center justify-center text-zinc-400 mb-2">
        <Users size={40} />
      </div>
      <p className="text-lg font-black tracking-tight">
        Encara no formes part de cap grup.
      </p>
      <p className="text-sm text-zinc-500 mb-4">
        Crea un grup nou per començar a planejar caps de setmana amb els teus amics.
      </p>

      <button
        onClick={() => setShowModal(true)}
        className="flex items-center justify-center gap-2 px-8 py-4 bg-zinc-900 dark:bg-zinc-100 text-white dark:text-zinc-900 rounded-2xl font-black shadow-xl active:scale-95 transition-all"
      >
        <Plus size={20} />
        CREAR UN GRUP
      </button>

      <p className="text-[10px] font-bold text-zinc-400 uppercase tracking-widest mt-4">
        O demana que t&apos;hi convidin des del teu perfil.
      </p>

      {showModal && (
        <GroupModal
          onClose={() => setShowModal(false)}
          initialMode="create"
          userId={userId}
        />
      )}
    </div>
  );
}
