package com.example.firebaseatenticacaogoogle


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.firebaseatenticacaogoogle.ui.theme.FirebaseAtenticacaoGoogleTheme
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat


class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        setContent {
            FirebaseAtenticacaoGoogleTheme {
                var loggedIn by remember { mutableStateOf(false) }
                var userEmail by remember { mutableStateOf("") }

                if (loggedIn) {
                    WelcomeScreen(email = userEmail, onLogout = {
                        loggedIn = false
                        userEmail = ""
                        showLogoutNotification()
                    })
                } else {
                    AuthenticationScreen { emailAddress ->
                        userEmail = emailAddress
                        loggedIn = true
                        showLoginNotification(emailAddress)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AuthenticationScreen(onLoginSuccess: (String) -> Unit) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var message by remember { mutableStateOf("") }
        var isLogin by remember { mutableStateOf(true) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isLogin) "Login" else "Criar Conta",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Exibe a mensagem de erro ou sucesso
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                color = if (message.startsWith("Erro")) Color(0xFFB00020) else Color(0xFF1B5E20),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isLogin) {
                        if (validateInput(email, password)) {
                            loginUser(email, password) { success, errorMessage ->
                                message = if (success) {
                                    onLoginSuccess(email) // Chama o callback com o email
                                    "Login bem-sucedido!"
                                } else {
                                    errorMessage ?: "Erro desconhecido."
                                }
                            }
                        } else {
                            message = "Por favor, insira um e-mail válido e uma senha de pelo menos 6 caracteres."
                        }
                    } else {
                        if (validateInput(email, password)) {
                            createUser(email, password) { success, errorMessage ->
                                message = if (success) {
                                    "Conta criada com sucesso!"
                                } else {
                                    errorMessage ?: "Erro desconhecido."
                                }
                            }
                        } else {
                            message = "Por favor, insira um e-mail válido e uma senha de pelo menos 6 caracteres."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(if (isLogin) "Login" else "Criar Conta", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botão para alternar entre Login e Cadastro
            TextButton(onClick = {
                isLogin = !isLogin
                email = ""
                password = ""
                message = ""
            }) {
                Text(if (isLogin) "Não tem uma conta? Criar conta" else "Já tem uma conta? Fazer login")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    @Composable
    fun WelcomeScreen(email: String, onLogout: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Bem-vindo!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = "Email logado: $email",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Logout", color = Color.White)
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.length >= 6
    }

    private fun createUser(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i(TAG, "onCreate: Sucesso")
                    callback(true, null)
                } else {
                    Log.i(TAG, "onCreate: Falha -> ${task.exception}")
                    callback(false, task.exception?.message ?: "Erro desconhecido")
                }
            }
    }

    private fun loginUser(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i(TAG, "Login: Sucesso")
                    callback(true, null)
                } else {
                    Log.i(TAG, "Login: Falha -> ${task.exception}")
                    callback(false, task.exception?.message ?: "Erro desconhecido")
                }
            }
    }

    private fun showLoginNotification(email: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "login_channel"
        val channelName = "Login Notifications"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Coloque um ícone apropriado
            .setContentTitle("Login Bem-Sucedido")
            .setContentText("Você está logado como: $email")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(1, notificationBuilder.build())
    }

    private fun showLogoutNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "logout_channel"
        val channelName = "Logout Notifications"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Coloque um ícone apropriado
            .setContentTitle("Logout")
            .setContentText("Você foi desconectado.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(2, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
