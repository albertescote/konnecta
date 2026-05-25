import Foundation
import Supabase

struct LeaderboardEntry: Codable, Identifiable {
    var id: String { email }
    val full_name: String?
    val avatar_url: String?
    val email: String
    val visit_count: Int
}

private struct PlanWithProfileOnly: Codable {
    val user_id: String
    val profiles: Profile
}

class LeaderboardService {
    private let client = SupabaseManager.shared.client
    
    func getLeaderboard(groupId: String) async -> [LeaderboardEntry] {
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd"
        let today = dateFormatter.string(from: Date())
        
        do {
            let plans: [PlanWithProfileOnly] = try await client.database
                .from("weekend_plans")
                .select(columns: "user_id, profiles(id, full_name, avatar_url, email, updated_at)")
                .eq(column: "status", value: "going")
                .eq(column: "group_id", value: groupId)
                .lt(column: "weekend_date", value: today)
                .execute()
                .value
            
            let grouped = Dictionary(grouping: plans, by: { $0.user_id })
            let entries = grouped.map { (userId, userPlans) -> LeaderboardEntry in
                let profile = userPlans.first!.profiles
                return LeaderboardEntry(
                    full_name = profile.full_name,
                    avatar_url = profile.avatar_url,
                    email = profile.email,
                    visit_count = userPlans.count
                )
            }
            
            return entries.sorted(by: { $0.visit_count > $1.visit_count }).prefix(5).map { $0 }
        } catch {
            print("Leaderboard error: \(error)")
            return []
        }
    }
}
