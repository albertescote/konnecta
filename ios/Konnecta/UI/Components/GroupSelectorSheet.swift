import SwiftUI

struct GroupSelectorSheet: View {
    let groups: [Group]
    let activeGroupId: String
    let onGroupSelected: (String) -> Void
    let onDismiss: () -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 24) {
            Text("ELS MEUS GRUPS")
                .font(.system(size: 12, weight: .black))
                .kerning(2)
                .foregroundColor(.secondary)
            
            ScrollView {
                VStack(spacing: 12) {
                    ForEach(groups) { group in
                        let isSelected = group.id == activeGroupId
                        Button(action: {
                            onGroupSelected(group.id)
                            onDismiss()
                        }) {
                            HStack {
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(group.name)
                                        .font(.system(size: 16, weight: .bold))
                                        .foregroundColor(isSelected ? .blue : .primary)
                                    Text(group.role?.uppercased() ?? "MEMBER")
                                        .font(.system(size: 10, weight: .black))
                                        .foregroundColor(.secondary)
                                }
                                Spacer()
                                if isSelected {
                                    Image(systemName: "checkmark.circle.fill")
                                        .foregroundColor(.blue)
                                }
                            }
                            .padding(16)
                            .background(isSelected ? Color.blue.opacity(0.1) : Color(.systemGray6).opacity(0.5))
                            .cornerRadius(20)
                        }
                    }
                }
            }
            
            Button(action: { /* Manage Logic */ }) {
                Text("GESTIONAR GRUPS")
                    .font(.system(size: 14, weight: .bold))
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
                    .overlay(
                        RoundedRectangle(cornerRadius: 20)
                            .stroke(Color.gray.opacity(0.3), lineWidth: 1)
                    )
            }
            .foregroundColor(.primary)
        }
        .padding(24)
    }
}
