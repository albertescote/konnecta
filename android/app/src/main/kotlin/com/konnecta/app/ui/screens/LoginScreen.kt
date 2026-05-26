package com.konnecta.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.konnecta.app.R // Make sure to import your R class!
import com.konnecta.app.data.remote.SupabaseClient
import com.konnecta.app.ui.components.AppFooter
import com.konnecta.app.ui.components.ThemeToggle
import com.konnecta.app.ui.viewmodel.AuthViewModel
import io.github.jan.supabase.compose.auth.composeAuth
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    val googleAction = SupabaseClient.client.composeAuth.rememberSignInWithGoogle(
        onResult = { /* The session is automatically handled by the SDK */ }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "KONNECTA",
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp,
                    letterSpacing = (-1.5).sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "BENVINGUT",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
            ThemeToggle()
        }

        Spacer(modifier = Modifier.height(24.dp))

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = Color.LightGray.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Central Login Box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Inicia sessió per connectar amb els teus amics.",
                fontSize = 18.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
                lineHeight = 26.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { googleAction.startFlow() },
                modifier = Modifier
                    .width(260.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface
                ),
                enabled = !state.isLoading
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {

                    // Replaced the Text "G" Box with the actual Image resource
                    Image(
                        painter = painterResource(id = R.drawable.ic_google_logo),
                        contentDescription = "Google Logo",
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = if (state.isLoading) "CONNECTANT..." else "INICIA SESSIÓ",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Footer
        AppFooter()
    }
}
