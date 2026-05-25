import SwiftUI

struct ActivityCard: View {
    let activity: ActivityWithParticipants
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 4) {
                    Text(activity.title.uppercased())
                        .font(.system(size: 18, weight: .black))
                        .kerning(-0.5)
                    
                    let timeText = activity.start_time != nil 
                        ? "\(activity.day_of_week.capitalized) a les \(activity.start_time!)"
                        : activity.day_of_week.capitalized
                    
                    Text(timeText)
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(.blue)
                }
                Spacer()

                HStack(spacing: 8) {
                    Button(action: { CalendarManager.shared.addEvent(activity: activity) }) {
                        ZStack {
                            Circle()
                                .fill(Color.white.opacity(0.5))
                                .frame(width: 40, height: 40)
                            Text("📅")
                                .font(.system(size: 16))
                        }
                    }

                    Button(action: { shareActivity(activity: activity) }) {
                        ZStack {
                            Circle()
                                .fill(Color.white.opacity(0.5))
                                .frame(width: 40, height: 40)
                            Text("🔗")
                                .font(.system(size: 16))
                        }
                    }
                }
            }
            
            if let description = activity.description, !description.isEmpty {
                Text(description)
                    .font(.system(size: 14))
                    .foregroundColor(.secondary)
                    .lineLimit(3)
            }
            
            // Participants
            HStack(spacing: -10) {
                ForEach(activity.activity_participants.prefix(5), id: \.user_id) { participant in
                    ZStack {
                        Circle()
                            .fill(Color.white)
                            .frame(width: 34, height: 34)
                        
                        Circle()
                            .fill(Color.gray.opacity(0.3))
                            .frame(width: 30, height: 30)
                        
                        Text(String(participant.profiles.full_name?.first ?? "?"))
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(.white)
                    }
                }
                
                if activity.activity_participants.count > 5 {
                    Text("+\(activity.activity_participants.count - 5)")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(.secondary)
                        .padding(.leading, 16)
                }
            }
        }
        .padding(20)
        .background(Color(.systemGray6).opacity(0.5))
        .cornerRadius(24)
    }
}
