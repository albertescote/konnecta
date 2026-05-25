import Foundation
import EventKit
import UIKit

class CalendarManager {
    static let shared = CalendarManager()
    private let eventStore = EKEventStore()
    
    func addEvent(activity: ActivityWithParticipants) {
        eventStore.requestAccess(to: .event) { granted, error in
            if granted && error == nil {
                DispatchQueue.main.async {
                    let event = EKEvent(eventStore: self.eventStore)
                    event.title = "\(activity.title) [Konnecta]"
                    event.location = "Valls"
                    event.notes = activity.description
                    
                    let dateFormatter = DateFormatter()
                    dateFormatter.dateFormat = "yyyy-MM-dd HH:mm"
                    let startDate = dateFormatter.date(from: "\(activity.weekend_date) \(activity.start_time ?? "10:00")") ?? Date()
                    
                    event.startDate = startDate
                    event.endDate = Calendar.current.date(byAdding: .hour, value: 2, to: startDate)
                    event.calendar = self.eventStore.defaultCalendarForNewEvents
                    
                    // Here we'd typically use EKEventEditViewController to let user confirm
                    // For simplicity, we'll try to save directly or show a message
                    do {
                        try self.eventStore.save(event, span: .thisEvent)
                        print("Event saved to calendar")
                    } catch {
                        print("Error saving event: \(error)")
                    }
                }
            }
        }
    }
}
