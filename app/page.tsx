import { createClient } from "@/lib/supabase/server";
import { getUpcomingFriday, formatDbDate, ca } from "@/lib/utils";
import VotingSection from "@/components/VotingSection";
import AttendanceList from "@/components/AttendanceList";
import WeekendSelector from "@/components/WeekendSelector";
import WeatherCard from "@/components/WeatherCard";
import HallOfFame from "@/components/HallOfFame";
import ThemeToggle from "@/components/ThemeToggle";
import ActivityBoard from "@/components/ActivityBoard";
import ProfileButton from "@/components/ProfileButton";
import PullToRefresh from "@/components/PullToRefresh";
import ViewToggle from "@/components/ViewToggle";
import PlansHub from "@/components/PlansHub";
import SwipeContainer from "@/components/SwipeContainer";
import GroupQuickSelect from "@/components/GroupQuickSelect";
import { format, parseISO, addDays } from "date-fns";
import { Suspense } from "react";
import { cookies } from "next/headers";
import { Group } from "@/types";
import { Users } from "lucide-react";

export default async function Home({
  searchParams,
}: {
  searchParams: Promise<{ [key: string]: string | string[] | undefined }>;
}) {
  const supabase = await createClient();
  const {
    data: { user },
  } = await supabase.auth.getUser();

  const cookieStore = await cookies();
  let groupId = cookieStore.get("konnecta_group_id")?.value;

  const params = await searchParams;
  const selectedDateStr =
    (params.date as string) || formatDbDate(getUpcomingFriday());

  const anchorDate = parseISO(selectedDateStr);
  const sat = addDays(anchorDate, 1);
  const sun = addDays(anchorDate, 2);
  const displayDate = `${format(sat, "d 'de' MMM", { locale: ca })} - ${format(sun, "d 'de' MMM", { locale: ca })}`;

  // Fetch groups if user is logged in
  let userGroups: Group[] = [];
  if (user) {
    const { data: memberships } = await supabase
      .from("group_memberships")
      .select("role, groups (*)")
      .eq("user_id", user.id);

    userGroups = (memberships?.map((m: any) => ({
      ...m.groups,
      role: m.role,
    })) || []) as Group[];

    // Validate if the cookie groupId is still valid for this user
    if (groupId && !userGroups.some(g => g.id === groupId)) {
      groupId = undefined;
    }

    // Auto-select first group if none active or cookie was invalid
    if (!groupId && userGroups.length > 0) {
      groupId = userGroups[0].id;
    }
  }

  const [profileResponse, userPlanResponse] = user
    ? await Promise.all([
        supabase.from("profiles").select("*").eq("id", user.id).single(),
        supabase
          .from("weekend_plans")
          .select("status, comment")
          .eq("user_id", user.id)
          .eq("weekend_date", selectedDateStr)
          .eq("group_id", groupId || "")
          .single(),
      ])
    : [{ data: null }, { data: null }];

  const profile = profileResponse.data;
  const userPlan = userPlanResponse.data;
  const userStatus =
    (userPlan?.status as "going" | "not_going" | "pending" | null) || null;
  const userComment = userPlan?.comment || null;

  const currentView = (params.view as "weekend" | "all") || "weekend";
  const activeGroup = userGroups.find((g) => g.id === groupId);

  return (
    <main className="min-h-screen bg-background text-foreground flex flex-col items-center transition-colors duration-300 overflow-x-hidden">
      <PullToRefresh />

      {/* 1. TOP BAR (Sticky) */}
      <div className="w-full bg-background sticky top-0 z-40 px-6 pt-8 pb-4 border-b border-zinc-100 dark:border-zinc-800 text-zinc-950 dark:text-white">
        <header className="flex items-start justify-between w-full max-w-md mx-auto text-zinc-950 dark:text-white">
          <div className="flex flex-col min-w-0">
            <h1 className="text-3xl font-black tracking-tighter leading-[0.85] text-zinc-950 dark:text-white flex flex-col">
              <span>KONNECTA</span>
            </h1>
            {user && groupId ? (
              <GroupQuickSelect groups={userGroups} activeGroupId={groupId} />
            ) : (
              <p className="text-zinc-500 text-[10px] font-black uppercase tracking-[0.2em] mt-3 truncate">
                {activeGroup?.name || "Benvingut"}
              </p>
            )}
          </div>

          <div className="flex items-center gap-2 mt-1 text-zinc-950 dark:text-white">
            <ThemeToggle />
            {user && (
              <ProfileButton 
                user={user} 
                profile={profile} 
                groups={userGroups} 
                activeGroupId={groupId || ""} 
              />
            )}
          </div>
        </header>
        {user && groupId && (
          <div className="w-full max-w-md mx-auto mt-4">
            <ViewToggle />
          </div>
        )}
      </div>

      <div className="w-full max-w-md px-4 flex flex-col gap-6 pb-12 mt-6">
        {!user ? (
          <div className="flex flex-col items-center gap-4 text-center py-24">
            <p className="text-lg font-medium opacity-60">
              Connecta amb els amics.
            </p>
            <a
              href="/login"
              className="px-10 py-4 bg-zinc-900 dark:bg-zinc-100 text-white dark:text-zinc-900 rounded-2xl font-black shadow-xl"
            >
              INICIA SESSIÓ
            </a>
          </div>
        ) : !groupId ? (
          <div className="flex flex-col items-center gap-4 text-center py-24 px-6">
            <div className="w-20 h-20 bg-zinc-100 dark:bg-zinc-800 rounded-full flex items-center justify-center text-zinc-400 mb-2">
              <Users size={40} />
            </div>
            <p className="text-lg font-black tracking-tight">
              Encara no formes part de cap grup.
            </p>
            <p className="text-sm text-zinc-500">
              Crea un grup nou o demana que t&apos;hi convidin des del teu perfil.
            </p>
          </div>
        ) : (
          <SwipeContainer activeView={currentView}>
            {/* WEEKEND VIEW */}
            <div className="space-y-6">
              <section>
                <WeekendSelector />
              </section>

              <div className="flex flex-col gap-6">
                <Suspense
                  fallback={
                    <div className="h-24 w-full bg-background border border-zinc-100 dark:border-zinc-800 animate-pulse rounded-[2rem]" />
                  }
                >
                  <WeatherCard date={selectedDateStr} />
                </Suspense>

                <VotingSection
                  key={`${selectedDateStr}-${groupId}`}
                  userId={user.id}
                  groupId={groupId}
                  weekendDate={selectedDateStr}
                  initialStatus={userStatus}
                  initialComment={userComment}
                  displayDate={displayDate}
                />
              </div>

              <div className="space-y-4">
                <h3 className="text-sm font-black uppercase tracking-[0.2em] text-zinc-400 px-2">
                  Qui ve?
                </h3>
                <Suspense
                  fallback={
                    <div className="space-y-3">
                      <div className="h-16 w-full bg-background border border-zinc-100 dark:border-zinc-800 animate-pulse rounded-2xl" />
                      <div className="h-16 w-full bg-background border border-zinc-100 dark:border-zinc-800 animate-pulse rounded-2xl opacity-50" />
                    </div>
                  }
                >
                  <AttendanceList weekendDate={selectedDateStr} groupId={groupId} />
                </Suspense>
              </div>

              <hr className="border-zinc-200 dark:border-zinc-800 mx-4" />

              <div className="space-y-4">
                <h3 className="text-sm font-black uppercase tracking-[0.2em] text-zinc-400 px-2">
                  Plans
                </h3>
                <Suspense
                  fallback={
                    <div className="h-24 w-full bg-background border border-zinc-100 dark:border-zinc-800 animate-pulse rounded-3xl" />
                  }
                >
                  <ActivityBoard
                    weekendDate={selectedDateStr}
                    currentUserId={user.id}
                    groupId={groupId}
                  />
                </Suspense>
              </div>

              <hr className="border-zinc-200 dark:border-zinc-800 mx-4" />

              <div className="pb-8">
                <Suspense
                  fallback={
                    <div className="h-40 w-full bg-background border border-zinc-100 dark:border-zinc-800 animate-pulse rounded-3xl" />
                  }
                >
                  <HallOfFame groupId={groupId} />
                </Suspense>
              </div>
            </div>

            {/* PLANS HUB VIEW */}
            <div>
              <Suspense
                fallback={
                  <div className="space-y-4 py-12">
                    <div className="h-24 w-full bg-background border border-zinc-100 dark:border-zinc-800 animate-pulse rounded-3xl" />
                    <div className="h-24 w-full bg-background border border-zinc-100 dark:border-zinc-800 animate-pulse rounded-3xl opacity-50" />
                  </div>
                }
              >
                <PlansHub currentUserId={user.id} groupId={groupId} />
              </Suspense>
            </div>
          </SwipeContainer>
        )}
      </div>

      <footer className="mt-auto py-12 text-zinc-400 text-[10px] font-bold uppercase tracking-widest text-center">
        KONNECTA v1.0
      </footer>
    </main>
  );
}
