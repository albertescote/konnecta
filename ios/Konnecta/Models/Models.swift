import Foundation

struct Profile: Codable, Identifiable {
    val id: String
    val full_name: String?
    val avatar_url: String?
    val email: String
    val updated_at: String
}

struct Group: Codable, Identifiable, Equatable {
    val id: String
    val name: String
    val slug: String
    val created_at: String
    val created_by: String
    val description: String?
    var role: String? = nil
}

struct Activity: Codable, Identifiable {
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
}

struct WeekendPlan: Codable, Identifiable {
    val id: String
    val user_id: String
    val group_id: String
    val weekend_date: String
    val status: String
    val comment: String?
    val updated_at: String
}
