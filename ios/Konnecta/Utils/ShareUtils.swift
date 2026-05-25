import SwiftUI
import UIKit

struct ShareSheet: UIViewControllerRepresentable {
    let activityItems: [Any]
    
    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: activityItems, applicationActivities: nil)
    }
    
    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}

extension View {
    func shareActivity(activity: ActivityWithParticipants) {
        let timeText = activity.start_time != nil ? " a les \(activity.start_time!)" : ""
        let text = """
            Ei! Estem organitzant això per KONNECTA:
            
            *\(activity.title.uppercased())*
            📅 \(activity.day_of_week.uppercased()), \(activity.weekend_date)\(timeText)
            
            Anima't i apunta't a l'app!
        """
        
        let av = UIActivityViewController(activityItems: [text], applicationActivities: nil)
        
        if let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let rootVC = scene.windows.first?.rootViewController {
            rootVC.present(av, animated: true)
        }
    }
}
