import SwiftUI

struct NewActivitySheet: View {
    let weekendDate: String
    let groupId: String
    let onDismiss: () -> Void
    let onSuccess: () -> Void
    
    @State private var title = ""
    @State private var description = ""
    @State private var selectedDay = "dissabte"
    @State private var isPending = false
    
    private let activityService = ActivityService()
    
    var body: some View {
        NavigationView {
            VStack(alignment: .leading, spacing: 24) {
                Text("NOU PLA")
                    .font(.system(size: 24, weight: .black))
                    .kerning(-0.5)
                
                VStack(spacing: 16) {
                    TextField("Títol del pla", text: $title)
                        .padding()
                        .background(Color(.systemGray6))
                        .cornerRadius(20)
                        .font(.system(size: 16, weight: .bold))
                    
                    TextEditor(text: $description)
                        .frame(height: 100)
                        .padding(12)
                        .background(Color(.systemGray6))
                        .cornerRadius(20)
                        .overlay(
                            Group {
                                if description.isEmpty {
                                    Text("Detalls (opcional)")
                                        .foregroundColor(Color(.placeholderText))
                                        .padding(.horizontal, 16)
                                        .padding(.vertical, 20)
                                }
                            },
                            alignment: .topLeading
                        )
                }
                
                // Day Selector
                HStack(spacing: 4) {
                    ForEach(["divendres", "dissabte", "diumenge"], id: \.self) { day in
                        let isSelected = selectedDay == day
                        Button(action: { selectedDay = day }) {
                            Text(day.prefix(3).uppercased())
                                .font(.system(size: 10, weight: .black))
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 12)
                                .background(isSelected ? Color.white : Color.clear)
                                .foregroundColor(isSelected ? .blue : .gray)
                                .cornerRadius(12)
                                .shadow(color: isSelected ? .black.opacity(0.1) : .clear, radius: 4)
                        }
                    }
                }
                .padding(4)
                .background(Color(.systemGray6))
                .cornerRadius(16)
                
                Spacer()
                
                Button(action: {
                    createActivity()
                }) {
                    Text(isPending ? "CREANT..." : "AFEGIR PLA")
                        .font(.system(size: 16, weight: .black))
                        .kerning(1)
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 64)
                        .background(title.isEmpty ? Color.gray : Color.black)
                        .cornerRadius(24)
                }
                .disabled(title.isEmpty || isPending)
            }
            .padding(24)
            .navigationBarItems(trailing: Button("Tanca") { onDismiss() })
        }
    }
    
    private func createActivity() {
        guard !title.isEmpty else { return }
        isPending = true
        
        Task {
            do {
                _ = try await activityService.createActivity(activity: Activity(
                    id: UUID().uuidString,
                    title: title,
                    description: description,
                    group_id: groupId,
                    start_date: nil,
                    end_date: nil,
                    start_time: "19:00",
                    end_time: nil,
                    creator_id: "your-user-id",
                    weekend_date: weekendDate,
                    day_of_week: selectedDay
                ))
                onSuccess()
            } catch {
                print("Error creating activity: \(error)")
            }
            isPending = false
        }
    }
}

#Preview {
    NewActivitySheet(weekendDate: "2024-05-24", groupId: "preview", onDismiss: {}, onSuccess: {})
}
