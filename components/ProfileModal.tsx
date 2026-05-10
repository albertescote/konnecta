"use client";

import { useState, useRef, useEffect } from "react";
import { createClient } from "@/lib/supabase/client";
import { updateProfile } from "@/app/actions/profile";
import { signOut } from "@/app/actions/auth";
import { X, Camera, Loader2, User, LogOut, Settings, UserPlus, Plus } from "lucide-react";
import Portal from "./Portal";
import { Profile, Group } from "@/types";
import { User as SupabaseUser } from "@supabase/supabase-js";
import GroupModal from "./GroupModal";

interface Props {
  user: SupabaseUser;
  profile: Profile | null;
  groups: Group[];
  activeGroupId: string;
  onClose: () => void;
}

export default function ProfileModal({ user, profile, groups, activeGroupId, onClose }: Props) {
  const [loading, setLoading] = useState(false);
  const [fullName, setFullName] = useState(profile?.full_name || "");
  const [avatarUrl, setAvatarUrl] = useState(profile?.avatar_url || "");
  const [groupModal, setGroupModal] = useState<{ open: boolean; mode: "invite" | "create" | "manage" }>({
    open: false,
    mode: "invite",
  });
  
  const fileInputRef = useRef<HTMLInputElement>(null);
  const supabase = createClient();

  const activeGroup = groups.find(g => g.id === activeGroupId);
  const isAdmin = activeGroup?.role === "admin";

  // Lock body scroll when modal is open
  useEffect(() => {
    const originalOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = originalOverflow;
    };
  }, []);

  const handleUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    try {
      setLoading(true);
      const file = e.target.files?.[0];
      if (!file) return;

      const fileExt = file.name.split(".").pop();
      const filePath = `${user.id}/${Math.random()}.${fileExt}`;

      const { error: uploadError } = await supabase.storage
        .from("avatars")
        .upload(filePath, file);

      if (uploadError) throw uploadError;

      const { data } = supabase.storage.from("avatars").getPublicUrl(filePath);
      setAvatarUrl(data.publicUrl);
    } catch (error) {
      console.error("Error pujant la imatge:", error);
      alert("Error al pujar la imatge");
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    const formData = new FormData();
    formData.append("full_name", fullName);
    formData.append("avatar_url", avatarUrl);

    try {
      await updateProfile(formData);
      onClose();
    } catch {
      alert("Error al guardar el perfil");
    } finally {
      setLoading(false);
    }
  };

  const isDirty =
    fullName !== (profile?.full_name || "") ||
    avatarUrl !== (profile?.avatar_url || "");

  return (
    <Portal>
      <div 
        className="fixed inset-0 z-[9999] flex items-center justify-center p-4 bg-black/70 backdrop-blur-md overflow-hidden"
        onClick={onClose}
        style={{ touchAction: 'none' }}
      >
        <div 
          className="bg-white dark:bg-zinc-900 w-full max-w-sm rounded-[2.5rem] shadow-2xl overflow-hidden border border-zinc-100 dark:border-zinc-800 animate-in zoom-in-95 duration-200 my-auto max-h-[90vh] flex flex-col"
          onClick={(e) => e.stopPropagation()}
          style={{ touchAction: 'auto' }}
        >
          {/* Fixed Header */}
          <div className="p-6 flex items-center justify-between border-b border-zinc-50 dark:border-zinc-800 flex-shrink-0">
            <h3 className="font-black text-xl tracking-tight text-zinc-950 dark:text-white uppercase">
              El teu perfil
            </h3>
            <button
              onClick={onClose}
              className="p-2 text-zinc-400 hover:text-zinc-600"
            >
              <X size={24} />
            </button>
          </div>

          {/* Scrollable Content */}
          <div className="overflow-y-auto flex-1 no-scrollbar overscroll-contain">
            <div className="p-8 space-y-8">
              {/* Avatar Upload */}
              <div className="flex flex-col items-center gap-4">
                <div className="relative group">
                  <div className="w-24 h-24 rounded-full bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center overflow-hidden border-4 border-white dark:border-zinc-900 shadow-xl">
                    {avatarUrl ? (
                      <img
                        src={avatarUrl}
                        alt="Avatar"
                        className="w-full h-full object-cover"
                      />
                    ) : (
                      <User size={40} className="text-zinc-300" />
                    )}
                  </div>
                  <button
                    type="button"
                    onClick={() => fileInputRef.current?.click()}
                    className="absolute bottom-0 right-0 p-2 bg-zinc-900 dark:bg-zinc-100 text-white dark:text-zinc-900 rounded-full shadow-lg hover:scale-110 transition-transform"
                  >
                    <Camera size={16} />
                  </button>
                  <input
                    type="file"
                    ref={fileInputRef}
                    onChange={handleUpload}
                    accept="image/*"
                    className="hidden"
                  />
                </div>
                <p className="text-[10px] font-black text-zinc-400 uppercase tracking-widest text-center">
                  Clica per canviar la foto
                </p>
              </div>

              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="space-y-1">
                  <label className="text-[10px] font-black text-zinc-400 uppercase tracking-[0.2em] px-1">
                    Correu electrònic
                  </label>
                  <input
                    value={user.email}
                    readOnly
                    className="w-full px-4 py-3 rounded-xl bg-zinc-50 dark:bg-zinc-800/50 text-zinc-400 font-medium cursor-not-allowed border-none outline-none"
                  />
                </div>

                <div className="space-y-1">
                  <label className="text-[10px] font-black text-zinc-400 uppercase tracking-[0.2em] px-1">
                    Nom d&apos;usuari
                  </label>
                  <input
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    placeholder="Com et diuen els amics?"
                    required
                    className="w-full px-4 py-3 rounded-xl bg-zinc-50 dark:bg-zinc-800 border-none outline-none focus:ring-2 focus:ring-zinc-900 dark:focus:ring-zinc-100 font-bold text-zinc-950 dark:text-white"
                  />
                </div>

                <button
                  type="submit"
                  disabled={loading || !isDirty}
                  className="w-full py-4 bg-zinc-900 dark:bg-zinc-100 text-white dark:text-zinc-900 rounded-2xl font-black shadow-xl active:scale-95 transition-all disabled:opacity-50 flex items-center justify-center gap-2"
                >
                  {loading && <Loader2 size={20} className="animate-spin" />}
                  GUARDAR CANVIS
                </button>
              </form>

              {/* Group Management Section */}
              {activeGroup && (
                <div className="pt-6 border-t border-zinc-100 dark:border-zinc-800 space-y-4">
                  <div className="flex items-center gap-2 px-1">
                    <Settings size={14} className="text-zinc-400" />
                    <h4 className="text-[10px] font-black text-zinc-400 uppercase tracking-[0.2em]">
                      Gestió de grup: <span className="text-zinc-950 dark:text-white">{activeGroup.name}</span>
                    </h4>
                  </div>
                  
                  {isAdmin ? (
                    <div className="flex gap-2">
                      <button
                        onClick={() => setGroupModal({ open: true, mode: "invite" })}
                        className="flex-1 flex items-center justify-center gap-2 py-4 bg-white dark:bg-zinc-900 border border-zinc-100 dark:border-zinc-800 rounded-2xl text-[10px] font-black uppercase tracking-widest text-zinc-600 dark:text-zinc-400 hover:bg-zinc-50 dark:hover:bg-zinc-800 transition-colors shadow-sm"
                      >
                        <UserPlus size={16} />
                        Convidar
                      </button>
                      <button
                        onClick={() => setGroupModal({ open: true, mode: "manage" })}
                        className="flex-1 flex items-center justify-center gap-2 py-4 bg-white dark:bg-zinc-900 border border-zinc-100 dark:border-zinc-800 rounded-2xl text-[10px] font-black uppercase tracking-widest text-zinc-600 dark:text-zinc-400 hover:bg-zinc-50 dark:hover:bg-zinc-800 transition-colors shadow-sm"
                      >
                        <Settings size={16} />
                        Gestionar
                      </button>
                    </div>
                  ) : (
                    <div className="px-1">
                      <p className="text-[10px] font-bold text-zinc-400 uppercase tracking-widest leading-relaxed">
                        Ets membre d&apos;aquest grup. Només els administradors poden convidar o gestionar membres.
                      </p>
                    </div>
                  )}
                </div>
              )}

              {/* Create Group Section */}
              <div className="pt-6 border-t border-zinc-100 dark:border-zinc-800 space-y-4">
                <div className="flex items-center gap-2 px-1">
                  <Plus size={14} className="text-zinc-400" />
                  <h4 className="text-[10px] font-black text-zinc-400 uppercase tracking-[0.2em]">
                    Vols crear un grup?
                  </h4>
                </div>
                <button
                  onClick={() => setGroupModal({ open: true, mode: "create" })}
                  className="w-full flex items-center justify-center gap-2 py-4 bg-zinc-50 dark:bg-zinc-800 rounded-2xl text-[10px] font-black uppercase tracking-widest text-zinc-600 dark:text-zinc-300 hover:bg-zinc-100 dark:hover:bg-zinc-700 transition-colors"
                >
                  <Plus size={16} />
                  CREAR NOU GRUP
                </button>
              </div>

              <div className="pt-4 border-t border-zinc-100 dark:border-zinc-800 flex flex-col gap-4">
                <button
                  type="button"
                  onClick={async () => {
                    if (confirm("Segur que vols tancar la sessió?")) {
                      await signOut();
                    }
                  }}
                  className="w-full py-4 bg-zinc-100 dark:bg-zinc-800 text-zinc-600 dark:text-zinc-400 rounded-2xl font-bold active:scale-95 transition-all flex items-center justify-center gap-2"
                >
                  <LogOut size={20} />
                  TANCAR SESSIÓ
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      {groupModal.open && (
        <GroupModal
          onClose={() => setGroupModal({ ...groupModal, open: false })}
          activeGroup={activeGroup}
          initialMode={groupModal.mode}
          userId={user.id}
        />
      )}
    </Portal>
  );
}
