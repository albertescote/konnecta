import SwiftUI

struct WeekendSelector: View {
    let dates: [String]
    @Binding var selectedDate: String
    
    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(dates, id: \.self) { date in
                    Text(date)
                        .font(.system(size: 14, weight: .bold))
                        .padding(.horizontal, 16)
                        .padding(.vertical, 12)
                        .background(selectedDate == date ? Color.blue : Color(.systemGray6))
                        .foregroundColor(selectedDate == date ? .white : .primary)
                        .clipShape(Capsule())
                        .onTapGesture {
                            selectedDate = date
                        }
                }
            }
            .padding(.horizontal, 16)
        }
    }
}
