import SwiftUI

struct LoginView: View {
    @StateObject private var viewModel = AuthViewModel()
    @State private var email = ""
    
    var body: some View {
        VStack(spacing: 0) {
            Spacer()
            
            VStack(alignment: .leading, spacing: 8) {
                Text("KONNECTA")
                    .font(.system(size: 40, weight: .black))
                    .kerning(-2)
                
                Text("Connecta amb els amics.")
                    .font(.system(size: 18, weight: .medium))
                    .foregroundColor(.secondary)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 24)
            
            Spacer().frame(height: 48)
            
            VStack(spacing: 16) {
                Button(action: {
                    viewModel.signInWithGoogle()
                }) {
                    Text("CONTINUA AMB GOOGLE")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                        .background(Color.black)
                        .cornerRadius(20)
                }
                
                HStack {
                    Rectangle().frame(height: 1).foregroundColor(Color(.systemGray5))
                    Text("o").font(.caption).foregroundColor(.secondary)
                    Rectangle().frame(height: 1).foregroundColor(Color(.systemGray5))
                }
                .padding(.vertical, 8)
                
                TextField("Correu electrònic", text = $email)
                    .padding()
                    .frame(height: 56)
                    .background(Color(.systemGray6))
                    .cornerRadius(20)
                    .keyboardType(.emailAddress)
                    .autocapitalization(.none)
                
                Button(action: {
                    viewModel.signInWithMagicLink(email: email)
                }) {
                    Text("ENVIA ENLLAÇ MÀGIC")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.primary)
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                        .background(Color(.systemGray5))
                        .cornerRadius(20)
                }
            }
            .padding(.horizontal, 24)
            
            Spacer()
        }
    }
}

#Preview {
    LoginView()
}
