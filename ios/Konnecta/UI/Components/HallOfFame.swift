import SwiftUI

struct HallOfFame: View {
    let winners: [LeaderboardEntry]
    
    var body: some View {
        if !winners.isEmpty {
            VStack(alignment: .leading, spacing: 16) {
                HStack(spacing: 8) {
                    Text("🏆")
                        .font(.system(size: 18))
                    Text("ELS FIXES")
                        .font(.system(size: 10, weight: .black))
                        .kerning(2)
                        .foregroundColor(.secondary)
                }
                .padding(.horizontal, 8)
                
                VStack(spacing: 16) {
                    ForEach(0..<winners.count, id: \.self) { index in
                        let user = winners[index]
                        HStack {
                            HStack(spacing: 12) {
                                ZStack {
                                    Circle()
                                        .fill(Color.gray.opacity(0.2))
                                        .frame(width: 40, height: 40)
                                    
                                    Text(String(user.full_name?.first ?? user.email.first!))
                                        .font(.system(size: 16, weight: .bold))
                                }
                                
                                Text(user.full_name ?? user.email.components(separatedBy: "@")[0])
                                    .font(.system(size: 14, weight: .bold))
                            }
                            
                            Spacer()
                            
                            Text("\(user.visit_count) \(user.visit_count == 1 ? "VISITA" : "VISITES")")
                                .font(.system(size: 9, weight: .black))
                                .kerning(1)
                                .foregroundColor(.secondary)
                                .padding(.horizontal, 12)
                                .padding(.vertical, 6)
                                .background(Color.white)
                                .cornerRadius(12)
                        }
                    }
                }
                .padding(24)
                .background(Color(.systemGray6).opacity(0.5))
                .cornerRadius(32)
            }
            .padding(.horizontal, 16)
        }
    }
}
