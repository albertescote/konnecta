import SwiftUI

struct ContentView: View {
    @StateObject private var viewModel = DashboardViewModel()
    @State private var selectedTab = 0
    @State private var showGroupSelector = false
    @State private var showProfile = false
    
    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Button(action: { showProfile = true }) {
                    Text("👤")
                        .font(.system(size: 20))
                        .padding(10)
                        .background(Color(.systemGray6))
                        .clipShape(Circle())
                }
                
                Spacer()
                
                if let activeGroup = viewModel.activeGroup {
                    Button(action: { showGroupSelector = true }) {
                        VStack(spacing: 2) {
                            Text(activeGroup.name)
                                .font(.system(size: 16, weight: .bold))
                            Text("CANVIA DE GRUP ▾")
                                .font(.system(size: 10, weight: .black))
                                .foregroundColor(.secondary)
                        }
                    }
                    .foregroundColor(.primary)
                }
                
                Spacer()
                
                // Placeholder to balance the profile icon
                Color.clear.frame(width: 40, height: 40)
            }
            .padding(.horizontal, 24)
            .padding(.top, 8)
            .padding(.bottom, 8)
            .background(Color(.systemBackground))
            
            TabView(selection: $selectedTab) {
                DashboardView(viewModel: viewModel)
                    .tabItem {
                        Label("Inici", systemImage: "house.fill")
                    }
                    .tag(0)
                
                PlansHubView(groupId: viewModel.activeGroup?.id ?? "")
                    .tabItem {
                        Label("Plans", systemImage: "calendar")
                    }
                    .tag(1)
            }
        }
        .accentColor(.primary)
        .sheet(isPresented: $showGroupSelector) {
            GroupSelectorSheet(
                groups: viewModel.userGroups,
                activeGroupId: viewModel.activeGroup?.id ?? "",
                onGroupSelected: { groupId in
                    viewModel.switchGroup(groupId: groupId, weekendDate: "2024-05-24")
                },
                onDismiss: { showGroupSelector = false }
            )
            .presentationDetents([.medium, .large])
        }
        .sheet(isPresented: $showProfile) {
            ProfileSheet(
                profile: nil,
                onSignOut: { showProfile = false },
                onDeleteAccount: { showProfile = false },
                onDismiss: { showProfile = false }
            )
            .presentationDetents([.medium])
        }
        .onAppear {
            viewModel.loadInitialData(userId: "your-user-id")
        }
        .onOpenURL { url in
            if url.scheme == "konnecta" && url.host == "join" {
                let token = url.lastPathComponent
                viewModel.joinGroup(token: token, userId: "your-user-id")
            }
        }
    }
}

struct DashboardView: View {
    @ObservedObject var viewModel: DashboardViewModel
    @State private var selectedDate = "2024-05-24"
    @State private var currentStatus: String? = nil
    @State private var showNewActivitySheet = false

    let dates = ["2024-05-24", "2024-05-31", "2024-06-07", "2024-06-14"]

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                Text("KONNECTA")
                    .font(.system(size: 34, weight: .black))
                    .padding(.horizontal, 24)
                    .padding(.top, 16)

                Spacer().frame(height: 24)

                WeekendSelector(dates: dates, selectedDate: $selectedDate)

                Spacer().frame(height: 24)

                WeatherCard(weather: viewModel.weather)

                Spacer().frame(height: 24)

                VotingSection(currentStatus: $currentStatus) { status in
                    currentStatus = status
                }

                // Attendance
                VStack(alignment: .leading, spacing: 24) {
                    Text("QUI VE?")
                        .font(.system(size: 12, weight: .black))
                        .kerning(2)
                        .foregroundColor(.secondary)

                    AttendanceSection(title: "SÍ", users: viewModel.going, titleColor: .green)
                    AttendanceSection(title: "NO", users: viewModel.notGoing, titleColor: .red)
                    AttendanceSection(title: "POTSER", users: viewModel.pending, titleColor: .gray)
                    AttendanceSection(title: "PENDENT", users: viewModel.unanswered, titleColor: .gray.opacity(0.5))
                }
                .padding(16)

                Divider()
                    .padding(.vertical, 16)
                    .padding(.horizontal, 24)

                // Activity Board
                VStack(alignment: .leading, spacing: 24) {
                    Text("PLANS")
                        .font(.system(size: 12, weight: .black))
                        .kerning(2)
                        .foregroundColor(.secondary)

                    if viewModel.activities.isEmpty {
                        Text("No hi ha cap pla encara...")
                            .font(.system(size: 14, weight: .medium))
                            .italic()
                            .foregroundColor(.secondary)
                            .padding(8)
                    } else {
                        ForEach(viewModel.activities) { activity in
                            ActivityCard(activity: activity)
                        }
                    }

                    Button(action: { showNewActivitySheet = true }) {
                        HStack {
                            Image(systemName: "plus")
                            Text("PROPOSA UN PLA")
                        }
                        .font(.system(size: 14, weight: .black))
                        .foregroundColor(.secondary)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 16)
                        .overlay(
                            RoundedRectangle(cornerRadius: 24)
                                .stroke(Color.gray.opacity(0.3), style: StrokeStyle(lineWidth: 2, dash: [4]))
                        )
                    }
                    .sheet(isPresented: $showNewActivitySheet) {
                        NewActivitySheet(
                            weekendDate: selectedDate,
                            groupId: viewModel.activeGroup?.id ?? "",
                            onDismiss: { showNewActivitySheet = false },
                            onSuccess: {
                                showNewActivitySheet = false
                                viewModel.loadDashboardData(weekendDate: selectedDate, groupId: viewModel.activeGroup?.id ?? "")
                            }
                        )
                    }
                }
                .padding(16)

                Divider()
                    .padding(.vertical, 16)
                    .padding(.horizontal, 24)

                // Hall of Fame
                HallOfFame(winners: viewModel.leaderboard)

                Spacer().frame(height: 32)
            }
        }
        .refreshable {
            if let groupId = viewModel.activeGroup?.id {
                viewModel.loadDashboardData(weekendDate: selectedDate, groupId: groupId)
            }
        }
        .onChange(of: selectedDate) { newDate in
            if let groupId = viewModel.activeGroup?.id {
                viewModel.loadDashboardData(weekendDate: newDate, groupId: groupId)
            }
        }
    }
}
