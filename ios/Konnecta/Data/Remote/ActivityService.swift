import Foundation
import Supabase

struct ParticipantWithProfile: Codable {
    val user_id: String
    val additional_participants: Int
    val profiles: Profile
}

struct ActivityWithParticipants: Codable, Identifiable {
    val id: String
    val title: String
    val description: String?
    val group_id: String
    val start_date: String?
    val end_date: String?
    val start_time: String?
    val end_time: String?
    val creator_id: String
    val weekend_date: String
    val day_of_week: String
    val activity_participants: [ParticipantWithProfile]
}

class ActivityService {
    private let client = SupabaseManager.shared.client
    
    func getActivities(weekendDate: String, groupId: String) async throws -> [ActivityWithParticipants] {
        return try await client.database
            .from("activities")
            .select(columns: "*, activity_participants(user_id, additional_participants, profiles(*))")
            .eq(column: "weekend_date", value: weekendDate)
            .eq(column: "group_id", value: groupId)
            .execute()
            .value
    }

    func getFutureActivities(today: String, groupId: String) async throws -> [ActivityWithParticipants] {
        return try await client.database
            .from("activities")
            .select(columns: "*, activity_participants(user_id, additional_participants, profiles(*))")
            .gte(column: "start_date", value: today)
            .eq(column: "group_id", value: groupId)
            .execute()
            .value
    }

    func createActivity(activity: Activity) async throws -> Activity {
        return try await client.database
            .from("activities")
            .insert(activity)
            .execute()
            .value
    }
}
