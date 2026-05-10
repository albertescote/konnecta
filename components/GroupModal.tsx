"use client";

import { useState } from "react";
import { createGroup } from "@/app/actions/groups";
import { X, Plus, Copy, Check, Users } from "lucide-react";
import Portal from "./Portal";
import { Group } from "@/types";

interface Props {
  onClose: () => void;
  activeGroup?: Group;
}

export default function GroupModal({ onClose, activeGroup }: Props) {
  const [isCreating, setIsCreating] = useState(!activeGroup);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [copied, setCopied] = useState(false);

  const inviteUrl = activeGroup 
    ? `${window.location.origin}/join/${activeGroup.slug}` 
    : "";

  const handleCopy = () => {
    navigator.clipboard.writeText(inviteUrl);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleCreate = async (formData: FormData) => {
    setLoading(true);
    setError(null);
    const res = await createGroup(formData);
    setLoading(false);
    if (res.success) {
      onClose();
    } else {
      setError(res.error || "Error al crear el grup");
    }
  };

  return (
    <Portal>
      <div className="fixed inset-0 z-[9999] flex items-center justify-center p-4 bg-black/70 backdrop-blur-md">
        <div className="bg-white dark:bg-zinc-900 w-full max-w-sm rounded-[2.5rem] shadow-2xl overflow-hidden border border-zinc-100 dark:border-zinc-800 animate-in zoom-in-95 duration-200">
          <div className="p-6 flex items-center justify-between border-b border-zinc-50 dark:border-zinc-800">
            <h3 className="font-black text-xl tracking-tight text-zinc-950 dark:text-white uppercase">
              {isCreating ? "Nou Grup" : "Convida amics"}
            </h3>
            <button onClick={onClose} className="p-2 text-zinc-400 hover:text-zinc-600">
              <X size={24} />
            </button>
          </div>

          <div className="p-8">
            {isCreating ? (
              <form action={handleCreate} className="space-y-6">
                {error && (
                  <p className="text-xs font-bold text-red-500 bg-red-50 dark:bg-red-900/20 p-3 rounded-xl">
                    {error}
                  </p>
                )}
                <div className="space-y-4">
                  <div className="space-y-1">
                    <label className="text-[10px] font-black text-zinc-400 uppercase tracking-[0.2em] px-1">
                      Nom del grup
                    </label>
                    <input
                      name="name"
                      placeholder="Ex: Els del poble, Padel Team..."
                      required
                      className="w-full px-4 py-3 rounded-xl bg-zinc-50 dark:bg-zinc-800 border-none outline-none focus:ring-2 focus:ring-zinc-900 dark:focus:ring-zinc-100 font-bold"
                    />
                  </div>
                </div>

                <button
                  type="submit"
                  disabled={loading}
                  className="w-full py-4 bg-zinc-900 dark:bg-zinc-100 text-white dark:text-zinc-900 rounded-2xl font-black shadow-xl active:scale-95 transition-all disabled:opacity-50"
                >
                  {loading ? "CREANT..." : "CREAR GRUP"}
                </button>
                
                {activeGroup && (
                  <button
                    type="button"
                    onClick={() => setIsCreating(false)}
                    className="w-full py-2 text-[10px] font-black text-zinc-400 uppercase tracking-widest hover:text-zinc-600 transition-colors"
                  >
                    Cancel·lar
                  </button>
                )}
              </form>
            ) : (
              <div className="space-y-8 text-center">
                <div className="w-20 h-20 bg-blue-50 dark:bg-blue-900/30 rounded-full flex items-center justify-center text-blue-500 mx-auto">
                  <Users size={40} />
                </div>
                <div className="space-y-2">
                  <h4 className="text-xl font-black text-zinc-950 dark:text-white tracking-tight">{activeGroup?.name}</h4>
                  <p className="text-sm text-zinc-500">Comparteix aquest enllaç amb els amics perquè s&apos;uneixin al grup.</p>
                </div>

                <div className="flex gap-2 items-center p-2 bg-zinc-50 dark:bg-zinc-800 rounded-2xl border border-zinc-100 dark:border-zinc-700">
                  <input
                    readOnly
                    value={inviteUrl}
                    className="flex-1 bg-transparent border-none outline-none text-xs font-medium text-zinc-400 px-2 truncate"
                  />
                  <button
                    onClick={handleCopy}
                    className={`p-3 rounded-xl transition-all ${copied ? 'bg-green-500 text-white' : 'bg-zinc-900 dark:bg-zinc-100 text-white dark:text-zinc-900'}`}
                  >
                    {copied ? <Check size={18} /> : <Copy size={18} />}
                  </button>
                </div>

                <button
                  onClick={() => setIsCreating(true)}
                  className="flex items-center justify-center gap-2 w-full py-4 bg-zinc-100 dark:bg-zinc-800 text-zinc-950 dark:text-white rounded-2xl font-black active:scale-95 transition-all"
                >
                  <Plus size={20} />
                  CREAR UN ALTRE GRUP
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </Portal>
  );
}
