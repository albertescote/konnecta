import SwiftUI

struct ProfileSheet: View {
    let profile: Profile?
    let onSignOut: () -> Void
    let onDeleteAccount: () -> Void
    let onDismiss: () -> Void
    
    @State private var showDeleteConfirmation = false
    
    var body: some View {
        VStack(spacing: 32) {
            Text("EL MEU PERFIL")
                .font(.system(size: 12, weight: .black))
                .kerning(2)
                .foregroundColor(.secondary)
            
            // User Info
            VStack(spacing: 16) {
                ZStack {
                    Circle()
                        .fill(Color.gray.opacity(0.2))
                        .frame(width: 80, height: 80)
                    
                    Text(String(profile?.full_name?.first ?? "?"))
                        .font(.system(size: 32, weight: .bold))
                }
                
                VStack(spacing: 4) {
                    Text(profile?.full_name ?? "Usuari")
                        .font(.system(size: 20, weight: .black))
                    Text(profile?.email ?? "")
                        .font(.system(size: 14))
                        .foregroundColor(.secondary)
                }
            }
            
            VStack(spacing: 16) {
                Button(action: onSignOut) {
                    Text("TANCAR SESSIÓ")
                        .font(.system(size: 14, weight: .bold))
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 16)
                        .background(Color(.systemGray6))
                        .cornerRadius(20)
                }
                .foregroundColor(.primary)
                
                Button(action: { showDeleteConfirmation = true }) {
                    Text("ELIMINAR COMPTE")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(.red.opacity(0.6))
                }
            }
            
            Spacer()
        }
        .padding(32)
        .alert(isPresented: $showDeleteConfirmation) {
            Alert(
                title: Text("Eliminar compte?"),
                message: Text("Aquesta acció és permanent i no es pot desfer. Es perdran totes les teves dades."),
                primaryButton: .destructive(Text("ELIMINAR"), action: onDeleteAccount),
                secondaryButton: .cancel(Text("CANCEL·LAR"))
            )
        }
    }
}
