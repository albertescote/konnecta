import SwiftUI

struct PlansHubView: View {
    @StateObject private var viewModel = PlansHubViewModel()
    let groupId: String
    
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
                Text("PROPERS ESDEVENIMENTS")
                    .font(.system(size: 12, weight: .black))
                    .kerning(2)
                    .foregroundColor(.secondary)
                    .padding(.horizontal, 24)
                    .padding(.top, 24)
                
                if viewModel.activities.isEmpty && !viewModel.isLoading {
                    Text("No hi ha cap pla futur encara...")
                        .font(.system(size: 14, weight: .medium))
                        .italic()
                        .foregroundColor(.secondary)
                        .padding(.horizontal, 24)
                } else {
                    VStack(spacing: 16) {
                        ForEach(viewModel.activities) { activity in
                            ActivityCard(activity: activity)
                        }
                    }
                    .padding(.horizontal, 16)
                }
                
                Spacer().frame(height: 32)
            }
        }
        .refreshable {
            viewModel.loadFutureActivities(groupId: groupId)
        }
        .onAppear {
            viewModel.loadFutureActivities(groupId: groupId)
        }
    }
}

#Preview {
    PlansHubView(groupId: "preview-group")
}
