package com.univ.energymonitor.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.univ.energymonitor.ui.theme.BackgroundGray
import com.univ.energymonitor.ui.theme.DarkGreen
import com.univ.energymonitor.ui.theme.HintGray
import com.univ.energymonitor.ui.theme.IconGray
import com.univ.energymonitor.ui.theme.LightDivider
import com.univ.energymonitor.ui.theme.LightGreen
import com.univ.energymonitor.ui.theme.PrimaryGreen
import com.univ.energymonitor.ui.theme.TextGray
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: (String, String) -> Unit,
    onCreateAccount: () -> Unit = {}
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var usernameError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    fun attemptLogin() {
        usernameError = ""; passwordError = ""
        if (username.isBlank()) { usernameError = "Username is required"; return }
        if (password.isBlank()) { passwordError = "Password is required"; return }
        if (password.length < 4) { passwordError = "Password too short"; return }
        isLoading = true
        scope.launch {
            delay(600)
            isLoading = false
            onLoginSuccess(username.trim(), password)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(48.dp))

            Box(
                modifier = Modifier.size(72.dp).background(PrimaryGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("⚡", fontSize = 34.sp)
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Lebanon Energy Monitor",
                color = DarkGreen,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                "Residential Energy Efficiency & CO₂ Tracker",
                color = TextGray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp, bottom = 32.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    Text(
                        "Sign In",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Username
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; usernameError = "" },
                        label = { Text("Username") },
                        placeholder = { Text("Enter your username", color = HintGray) },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = PrimaryGreen) },
                        isError = usernameError.isNotEmpty(),
                        supportingText = if (usernameError.isNotEmpty()) {
                            { Text(usernameError, color = MaterialTheme.colorScheme.error) }
                        } else null,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            focusedLabelColor = PrimaryGreen,
                            cursorColor = PrimaryGreen
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; passwordError = "" },
                        label = { Text("Password") },
                        placeholder = { Text("Enter your password", color = HintGray) },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = PrimaryGreen) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    null,
                                    tint = IconGray
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = passwordError.isNotEmpty(),
                        supportingText = if (passwordError.isNotEmpty()) {
                            { Text(passwordError, color = MaterialTheme.colorScheme.error) }
                        } else null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            attemptLogin()
                        }),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            focusedLabelColor = PrimaryGreen,
                            cursorColor = PrimaryGreen
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    TextButton(
                        onClick = {
                            Toast.makeText(
                                context,
                                "Contact your administrator to reset password.",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Forgot password?", color = PrimaryGreen, fontSize = 12.sp)
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = { focusManager.clearFocus(); attemptLogin() },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen,
                            disabledContainerColor = LightGreen
                        )
                    ) {
                        if (isLoading) CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                        else Text(
                            "SIGN IN",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = LightDivider)
                        Text("  or  ", color = IconGray, fontSize = 12.sp)
                        HorizontalDivider(modifier = Modifier.weight(1f), color = LightDivider)
                    }

                    Spacer(Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = onCreateAccount,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreen)
                    ) {
                        Text(
                            "CREATE ACCOUNT",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Text(
                "Demo: admin / admin123",
                color = IconGray,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 20.dp, bottom = 48.dp)
            )
        }
    }
}