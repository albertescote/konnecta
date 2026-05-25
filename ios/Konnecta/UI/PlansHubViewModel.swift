import Foundation

@MainActor
class PlansHubViewModel: ObservableObject {
    private let activityService = ActivityService()
    
    @Published var activities: [ActivityWithParticipants] = []
    @Published var isLoading = false
    
    func loadFutureActivities(groupId: String) {
        isLoading = true
        Task {
            do {
                let dateFormatter = DateFormatter()
                dateFormatter.dateFormat = "yyyy-MM-dd"
                let today = dateFormatter.string(from: Date())
                
                let fetched = try await activityService.getFutureActivities(today: today, groupId: groupId)
                self.activities = fetched.sorted { ($0.start_date ?? $0.weekend_date) < ($1.start_date ?? $1.weekend_date) }
                isLoading = false
            } catch {
                print("Error loading future activities: \(error)")
                isLoading = false
            }
        }
    }
}
