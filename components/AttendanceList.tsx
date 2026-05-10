import { createClient } from "@/lib/supabase/server";
import UserAttendanceCard from "./UserAttendanceCard";
import { Profile } from "@/types";

interface Props {
  weekendDate: string;
  groupId: string;
}

export default async function AttendanceList({ weekendDate, groupId }: Props) {
  const supabase = await createClient();

  const [plansResponse, profilesResponse] = await Promise.all([
    supabase
      .from("weekend_plans")
      .select(
        `
        user_id,
        status,
        comment,
        profiles (
          id,
          full_name,
          avatar_url,
          email,
          updated_at
        )
      `,
      )
      .eq("weekend_date", weekendDate)
      .eq("group_id", groupId),
    supabase
      .from("group_memberships")
      .select("profiles (*)")
      .eq("group_id", groupId),
  ]);

  const plans = plansResponse.data || [];
  const allProfiles = (profilesResponse.data?.map((m: any) => m.profiles) || []) as Profile[];

  const going = plans.filter((p) => p.status === "going") as unknown as {
    profiles: Profile;
    comment?: string | null;
    status: string;
  }[];
  const notGoing = plans.filter((p) => p.status === "not_going") as unknown as {
    profiles: Profile;
    comment?: string | null;
    status: string;
  }[];
  const pending = plans.filter((p) => p.status === "pending") as unknown as {
    profiles: Profile;
    comment?: string | null;
    status: string;
  }[];

  const answeredUserIds = new Set(plans.map((p) => p.user_id));
  const unanswered = allProfiles
    .filter((profile) => !answeredUserIds.has(profile.id))
    .map((profile) => ({
      profiles: profile,
      status: "unanswered",
      comment: null,
    }));

  return (
    <div className="w-full max-w-md space-y-8">
      <Section title="SÍ" users={going} color="text-green-500" groupId={groupId} />
      <Section title="NO" users={notGoing} color="text-red-500" groupId={groupId} />
      <Section title="POTSER" users={pending} color="text-zinc-500" groupId={groupId} />
      <Section
        title="PENDENT"
        users={unanswered}
        color="text-zinc-400"
        opacity="opacity-60"
        groupId={groupId}
      />
    </div>
  );
}

function Section({
  title,
  users,
  color,
  opacity,
  groupId,
}: {
  title: string;
  users: { profiles: Profile; comment?: string | null; status: string }[];
  color: string;
  opacity?: string;
  groupId: string;
}) {
  if (users.length === 0) return null;

  return (
    <div
      className={`space-y-3 animate-in fade-in slide-in-from-bottom-2 duration-500 ${opacity || ""}`}
    >
      <h3 className={`text-sm font-bold uppercase tracking-wider ${color}`}>
        {title} ({users.length})
      </h3>
      <div className="grid gap-2">
        {users.map((plan, i) => (
          <UserAttendanceCard
            key={i}
            profile={plan.profiles}
            comment={plan.comment}
            groupId={groupId}
          />
        ))}
      </div>
    </div>
  );
}
