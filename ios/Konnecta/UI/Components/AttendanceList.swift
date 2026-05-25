import SwiftUI

struct UserAttendanceCard: View {
    let profile: Profile
    let comment: String?
    
    var body: some View {
        HStack(spacing: 12) {
            // Avatar Placeholder
            ZStack {
                Circle()
                    .fill(Color.gray.opacity(0.3))
                    .frame(width: 40, height: 40)
                
                Text(String(profile.full_name?.first ?? "?"))
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.white)
            }
            
            VStack(alignment: .leading, spacing: 2) {
                Text(profile.full_name ?? "Usuari")
                    .font(.system(size: 14, weight: .bold))
                
                if let comment = comment, !comment.isEmpty {
                    Text(comment)
                        .font(.system(size: 12))
                        .foregroundColor(.secondary)
                }
            }
            
            Spacer()
        }
        .padding(12)
        .background(Color(.systemGray6).opacity(0.5))
        .cornerRadius(20)
    }
}

struct AttendanceSection: View {
    let title: String
    let users: [(Profile, String?)]
    let titleColor: Color
    
    var body: some View {
        if !users.isEmpty {
            VStack(alignment: .leading, spacing: 8) {
                Text("\(title) (\(users.count))")
                    .font(.system(size: 12, weight: .heavy))
                    .foregroundColor(titleColor)
                    .padding(.horizontal, 4)
                
                ForEach(0..<users.count, id: \.self) { index in
                    UserAttendanceCard(profile: users[index].0, comment: users[index].1)
                }
            }
        }
    }
}
