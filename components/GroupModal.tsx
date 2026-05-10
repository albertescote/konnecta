"use client";

import { useState, useEffect, useTransition } from "react";
import { createGroup, getGroupMembers, updateMemberRole, removeMember, deleteGroup, leaveGroup } from "@/app/actions/groups";
import { X, Plus, Copy, Check, Users, Shield, UserMinus, Trash2, Loader2, LogOut } from "lucide-react";
import Portal from "./Portal";
import { Group, GroupMembershipWithProfile } from "@/types";

interface Props {
  onClose: () => void;
  activeGroup?: Group;
  initialMode?: "invite" | "create" | "manage";
  userId?: string;
}

export default function GroupModal({ onClose, activeGroup, initialMode = "invite", userId }: Props) {
  const [mode, setMode] = useState(initialMode);
  const [loading, setLoading] = useState(false);
  const [isPending, startTransition] = useTransition();
  const [error, setError] = useState<string | null>(null);
  const [copied, setCopied] = useState(false);
  const [members, setMembers] = useState<GroupMembershipWithProfile[]>([]);
  const [fetchingMembers, setFetchingMembers] = useState(false);

  useEffect(() => {
    if (mode === "manage" && activeGroup) {
      loadMembers();
    }
  }, [mode, activeGroup]);

  const loadMembers = async () => {
    if (!activeGroup) return;
    setFetchingMembers(true);
    const res = await getGroupMembers(activeGroup.id);
    if (res.success && res.data) {
      setMembers(res.data);
    }
    setFetchingMembers(false);
  };

  const inviteUrl = activeGroup?.invite_token
    ? `${window.location.origin}/join/${activeGroup.invite_token}` 
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

  const handleUpdateRole = async (memberId: string, role: "admin" | "member") => {
    if (!activeGroup) return;
    startTransition(async () => {
      const res = await updateMemberRole(activeGroup.id, memberId, role);
      if (res.success) {
        await loadMembers();
      } else {
        alert(res.error);
      }
    });
  };

  const handleRemoveMember = async (memberId: string) => {
    if (!activeGroup) return;
    if (!confirm("Segur que vols eliminar aquest membre del grup?")) return;
    
    startTransition(async () => {
      const res = await removeMember(activeGroup.id, memberId);
      if (res.success) {
        await loadMembers();
      } else {
        alert(res.error);
      }
    });
  };

  const handleDeleteGroup = async () => {
    if (!activeGroup) return;
    if (!confirm(`SEGUR que vols eliminar el grup "${activeGroup.name}"? Aquesta acció no es pot desfer i s'esborraran tots els plans i activitats.`)) return;

    startTransition(async () => {
      const res = await deleteGroup(activeGroup.id);
      if (res.success) {
        onClose();
      } else {
        alert(res.error);
      }
    });
  };

  const handleLeaveGroup = async () => {
    if (!activeGroup) return;
    if (!confirm(`Segur que vols sortir del grup "${activeGroup.name}"?`)) return;

    startTransition(async () => {
      const res = await leaveGroup(activeGroup.id);
      if (res.success) {
        onClose();
      } else {
        alert(res.error);
      }
    });
  };

  const isAdmin = activeGroup?.role === "admin";

  return (
    <Portal>
      <div className="fixed inset-0 z-[9999] flex items-center justify-center p-4 bg-black/70 backdrop-blur-md">
        <div className="bg-white dark:bg-zinc-900 w-full max-w-sm rounded-[2.5rem] shadow-2xl overflow-hidden border border-zinc-100 dark:border-zinc-800 animate-in zoom-in-95 duration-200 flex flex-col max-h-[90vh]">
          <div className="p-6 flex items-center justify-between border-b border-zinc-50 dark:border-zinc-800 flex-shrink-0">
            <h3 className="font-black text-xl tracking-tight text-zinc-950 dark:text-white uppercase">
              {mode === "create" ? "Nou Grup" : mode === "manage" ? "Gestionar Grup" : "Convida amics"}
            </h3>
            <button onClick={onClose} className="p-2 text-zinc-400 hover:text-zinc-600">
              <X size={24} />
            </button>
          </div>

          <div className="overflow-y-auto flex-1 no-scrollbar p-8">
            {mode === "create" ? (
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
                      className="w-full px-4 py-3 rounded-xl bg-zinc-50 dark:bg-zinc-800 border-none outline-none focus:ring-2 focus:ring-zinc-900 dark:focus:ring-zinc-100 font-bold text-zinc-950 dark:text-white"
                    />
                  </div>
                  <div className="space-y-1">
                    <label className="text-[10px] font-black text-zinc-400 uppercase tracking-[0.2em] px-1">
                      Clau de creació (Secret)
                    </label>
                    <input
                      name="secret"
                      type="password"
                      placeholder="Introduir clau..."
                      required
                      className="w-full px-4 py-3 rounded-xl bg-zinc-50 dark:bg-zinc-800 border-none outline-none focus:ring-2 focus:ring-zinc-900 dark:focus:ring-zinc-100 font-bold text-zinc-950 dark:text-white"
                    />
                    <p className="text-[9px] text-zinc-400 mt-1.5 px-1 uppercase tracking-widest">Només usuaris autoritzats</p>
                  </div>
                </div>

                <button
                  type="submit"
                  disabled={loading}
                  className="w-full py-4 bg-zinc-900 dark:bg-zinc-100 text-white dark:text-zinc-900 rounded-2xl font-black shadow-xl active:scale-95 transition-all disabled:opacity-50"
                >
                  {loading ? "CREANT..." : "CREAR GRUP"}
                </button>
              </form>
            ) : mode === "manage" ? (
              <div className="space-y-8">
                <div className="space-y-4">
                  <h4 className="text-[10px] font-black text-zinc-400 uppercase tracking-[0.2em] px-1">Membres del grup</h4>
                  
                  {error && mode === "manage" && (
                    <p className="text-xs font-bold text-red-500 bg-red-50 dark:bg-red-900/20 p-3 rounded-xl mx-1">
                      {error}
                    </p>
                  )}

                  {fetchingMembers ? (
                    <div className="flex justify-center py-4">
                      <Loader2 className="animate-spin text-zinc-300" />
                    </div>
                  ) : members.length === 0 ? (
                    <div className="text-center py-8 bg-zinc-50 dark:bg-zinc-800/50 rounded-2xl border border-dashed border-zinc-200 dark:border-zinc-700">
                      <p className="text-xs font-bold text-zinc-400 uppercase tracking-widest px-4">
                        No s&apos;han trobat membres o no tens permisos per veure&apos;ls.
                      </p>
                    </div>
                  ) : (
                    <div className="space-y-3">
                      {members.map((member) => (
                        <div key={member.user_id} className="flex items-center justify-between p-3 bg-zinc-50 dark:bg-zinc-800/50 rounded-2xl border border-zinc-100 dark:border-zinc-800">
                          <div className="flex items-center gap-3">
                            <div className="w-8 h-8 rounded-full bg-zinc-200 dark:bg-zinc-700 flex items-center justify-center overflow-hidden">
                              {member.profiles.avatar_url ? (
                                <img src={member.profiles.avatar_url} alt="" className="w-full h-full object-cover" />
                              ) : (
                                <span className="text-[10px] font-bold">{(member.profiles.full_name || member.profiles.email)[0]}</span>
                              )}
                            </div>
                            <div className="flex flex-col">
                              <span className="text-xs font-bold text-zinc-950 dark:text-white">
                                {member.profiles.full_name || member.profiles.email.split("@")[0]}
                                {member.user_id === userId && " (Tu)"}
                              </span>
                              <span className="text-[9px] font-black uppercase text-zinc-400 tracking-wider flex items-center gap-1">
                                {member.role === "admin" && <Shield size={8} className="text-amber-500" />}
                                {member.role}
                              </span>
                            </div>
                          </div>
                          
                          {userId !== member.user_id && (
                            <div className="flex items-center gap-1">
                              {member.role === "member" && (
                                <button
                                  onClick={() => handleUpdateRole(member.user_id, "admin")}
                                  disabled={isPending}
                                  className="p-2 text-amber-500 hover:bg-amber-50 dark:hover:bg-amber-900/20 rounded-lg transition-colors"
                                  title="Fer Admin"
                                >
                                  <Shield size={16} />
                                </button>
                              )}
                              <button
                                onClick={() => handleRemoveMember(member.user_id)}
                                disabled={isPending}
                                className="p-2 text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-lg transition-colors"
                                title="Eliminar"
                              >
                                <UserMinus size={16} />
                              </button>
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                <div className="pt-6 border-t border-zinc-100 dark:border-zinc-800 flex flex-col gap-3">
                  {!isAdmin ? (
                    <button
                      onClick={handleLeaveGroup}
                      disabled={isPending}
                      className="flex items-center justify-center gap-2 w-full py-4 text-zinc-500 hover:bg-zinc-50 dark:hover:bg-zinc-800 rounded-2xl font-black transition-colors border border-zinc-100 dark:border-zinc-800"
                    >
                      <LogOut size={20} />
                      SORTIR DEL GRUP
                    </button>
                  ) : (
                    <button
                      onClick={handleDeleteGroup}
                      disabled={isPending}
                      className="flex items-center justify-center gap-2 w-full py-4 text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-2xl font-black transition-colors"
                    >
                      <Trash2 size={20} />
                      ELIMINAR GRUP
                    </button>
                  )}
                </div>
              </div>
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
              </div>
            )}
          </div>
        </div>
      </div>
    </Portal>
  );
}
