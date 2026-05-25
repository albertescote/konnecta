import Foundation
import Supabase
import GoTrue

import OneSignalFramework

@MainActor
class AuthViewModel: ObservableObject {
    private let client = SupabaseManager.shared.client
    
    @Published var session: Session? = nil
    @Published var isLoading = false
    @Published var errorMessage: String? = nil
    
    init() {
        Task {
            // Listen for auth state changes
            for await status in client.auth.authStateNames {
                if status == .signedIn {
                    self.session = try? await client.auth.session
                    if let userId = self.session?.user.id {
                        OneSignal.login(userId.uuidString)
                    }
                } else {
                    self.session = nil
                    OneSignal.logout()
                }
            }
        }
    }
    
    func signInWithGoogle() {
        isLoading = true
        Task {
            do {
                // For native apps, this usually opens a web browser for OAuth
                let url = try await client.auth.getOAuthSignInURL(provider: .google, redirectTo: URL(string: "konnecta://login-callback"))
                // Here you would use ASWebAuthenticationSession to open the URL
                isLoading = false
            } catch {
                errorMessage = error.localizedDescription
                isLoading = false
            }
        }
    }
    
    func signInWithMagicLink(email: String) {
        isLoading = true
        Task {
            do {
                try await client.auth.signInWithOTP(email: email, redirectTo: URL(string: "konnecta://login-callback"))
                isLoading = false
            } catch {
                errorMessage = error.localizedDescription
                isLoading = false
            }
        }
    }
    
    func signOut() {
        Task {
            try? await client.auth.signOut()
        }
    }
    
    func deleteAccount() {
        Task {
            // Store compliance requires an obvious deletion path.
            // For Supabase client-side, we sign out.
            // Best practice: Link to a web deletion form or call a custom function.
            try? await client.auth.signOut()
        }
    }
}
