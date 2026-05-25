import Foundation

@MainActor
class DashboardViewModel: ObservableObject {
    private let attendanceService = AttendanceService()
    private let activityService = ActivityService()
    private let weatherService = WeatherService()
    private let leaderboardService = LeaderboardService()
    private let groupService = GroupService()
    
    @Published var going: [(Profile, String?)] = []
    @Published var notGoing: [(Profile, String?)] = []
    @Published var pending: [(Profile, String?)] = []
    @Published var unanswered: [(Profile, String?)] = []
    @Published var activities: [ActivityWithParticipants] = []
    @Published var leaderboard: [LeaderboardEntry] = []
    @Published var weather: WeatherDay? = nil
    
    @Published var userGroups: [Group] = []
    @Published var activeGroup: Group? = nil
    @Published var isLoading = false
    
    private let dayOrder = ["divendres": 1, "dissabte": 2, "diumenge": 3]
    
    func loadInitialData(userId: String) {
        isLoading = true
        Task {
            let groups = await groupService.getUserGroups(userId: userId)
            self.userGroups = groups
            if let first = groups.first {
                self.activeGroup = first
                self.loadDashboardData(weekendDate: "2024-05-24", groupId: first.id)
            } else {
                self.isLoading = false
            }
        }
    }
    
    func switchGroup(groupId: String, weekendDate: String) {
        if let group = userGroups.first(where: { $0.id == groupId }) {
            self.activeGroup = group
            loadDashboardData(weekendDate: weekendDate, groupId: groupId)
        }
    }
    
    func joinGroup(token: String, userId: String) {
        Task {
            let success = await groupService.joinGroupByToken(token: token, userId: userId)
            if success {
                let groups = await groupService.getUserGroups(userId: userId)
                self.userGroups = groups
                if let newGroup = groups.first(where: { g in !userGroups.contains(where: { $0.id == g.id }) }) ?? groups.first {
                    self.switchGroup(groupId: newGroup.id, weekendDate: "2024-05-24")
                }
            }
        }
    }
    
    func loadDashboardData(weekendDate: String, groupId: String) {
        isLoading = true
        Task {
            do {
                async let plansRequest = attendanceService.getAttendance(weekendDate: weekendDate, groupId: groupId)
                async let membersRequest = attendanceService.getGroupMembers(groupId: groupId)
                async let activitiesRequest = activityService.getActivities(weekendDate: weekendDate, groupId: groupId)
                async let weatherRequest = weatherService.getWeekendWeather(fridayDateStr: weekendDate)
                async let leaderboardRequest = leaderboardService.getLeaderboard(groupId: groupId)
                
                let (plans, allMembers, fetchedActivities, weatherForecast, leaderboard) = try await (plansRequest, membersRequest, activitiesRequest, weatherRequest, leaderboardRequest)
                
                let answeredIds = Set(plans.map { $0.user_id })
                
                self.going = plans.filter { $0.status == "going" }.map { ($0.profiles, $0.comment) }
                self.notGoing = plans.filter { $0.status == "not_going" }.map { ($0.profiles, $0.comment) }
                self.pending = plans.filter { $0.status == "pending" }.map { ($0.profiles, $0.comment) }
                self.unanswered = allMembers.filter { !answeredIds.contains($0.id) }.map { ($0, nil) }
                
                self.activities = fetchedActivities.sorted { a, b in
                    if dayOrder[a.day_of_week] != dayOrder[b.day_of_week] {
                        return (dayOrder[a.day_of_week] ?? 99) < (dayOrder[b.day_of_week] ?? 99)
                    }
                    return (a.start_time ?? "").compare(b.start_time ?? "") == .orderedAscending
                }
                
                self.weather = weatherForecast?.summary
                self.leaderboard = leaderboard
                
                self.isLoading = false
            } catch {
                print("Error loading dashboard data: \(error)")
                self.isLoading = false
            }
        }
    }
}
