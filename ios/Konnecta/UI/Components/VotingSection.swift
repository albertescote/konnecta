import SwiftUI

struct VotingSection: View {
    @Binding var currentStatus: String?
    let onStatusChange: (String) -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("VINS AQUEST CAP DE SETMANA?")
                .font(.system(size: 12, weight: .black))
                .kerning(2)
                .foregroundColor(.secondary)
            
            HStack(spacing: 8) {
                VoteButton(text: "SÍ", isSelected: currentStatus == "going", activeColor: .green) {
                    onStatusChange("going")
                }
                VoteButton(text: "NO", isSelected: currentStatus == "not_going", activeColor: .red) {
                    onStatusChange("not_going")
                }
                VoteButton(text: "POTSER", isSelected: currentStatus == "pending", activeColor: .orange) {
                    onStatusChange("pending")
                }
            }
        }
        .padding(24)
        .background(Color(.systemGray6))
        .cornerRadius(32)
        .padding(16)
    }
}

struct VoteButton: View {
    let text: String
    let isSelected: Bool
    let activeColor: Color
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Text(text)
                .font(.system(size: 16, weight: .black))
                .frame(maxWidth: .infinity)
                .frame(height: 56)
                .background(isSelected ? activeColor : Color.white.opacity(0.5))
                .foregroundColor(isSelected ? .white : .secondary)
                .cornerRadius(20)
        }
    }
}
