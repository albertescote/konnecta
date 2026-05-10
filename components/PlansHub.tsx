import { createClient } from "@/lib/supabase/server";
import ActivityCard from "./ActivityCard";
import NewActivityForm from "./NewActivityForm";
import { Activity } from "@/types";
import { formatDbDate } from "@/lib/utils";

export default async function PlansHub({
  currentUserId,
  groupId,
}: {
  currentUserId: string;
  groupId: string;
}) {
  const supabase = await createClient();
  const today = formatDbDate(new Date());

  // Fetch all activities from today onwards
  const { data: activities } = await supabase
    .from("activities")
    .select(
      `
      *,
      activity_participants (
        user_id,
        additional_participants,
        profiles (
          full_name,
          avatar_url,
          email
        )
      )
    `,
    )
    .eq("group_id", groupId)
    .gte("start_date", today)
    .order("start_date", { ascending: true });

  const sortedActivities = (activities as unknown as Activity[]) || [];

  return (
    <div className="space-y-8 pb-12">
      <div className="space-y-4">
        <h3 className="text-sm font-black uppercase tracking-[0.2em] text-zinc-400 px-2">
          Propers Esdeveniments
        </h3>
        
        {sortedActivities.length === 0 ? (
          <div className="py-12 text-center bg-zinc-50 dark:bg-zinc-900/50 rounded-[2rem] border border-dashed border-zinc-200 dark:border-zinc-800">
            <p className="text-zinc-500 font-medium italic">No hi ha cap pla futur encara...</p>
          </div>
        ) : (
          <div className="grid gap-4">
            {sortedActivities.map((activity) => (
              <ActivityCard
                key={activity.id}
                activity={activity}
                currentUserId={currentUserId}
              />
            ))}
          </div>
        )}
      </div>

      <div className="pt-4">
        <NewActivityForm weekendDate={today} groupId={groupId} />
      </div>
    </div>
  );
}
