/*
 * Login and registration screen for BudgetBoss.
 * It collects email and password input, then calls the ViewModel to register or sign in the user.
 */
package com.example.budgetboss.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgetboss.ui.components.ItemCardShape

@Composable
fun AuthScreen(
    statusMessage: String?,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String) -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isRegister by rememberSaveable { mutableStateOf(false) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 24.dp, vertical = 56.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    "Budget",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    "BOSS",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 6.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Box(
                    modifier = Modifier
                        .size(width = 48.dp, height = 3.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)),
                )
                Text(
                    "Take Control Like a Boss",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                )
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = !isRegister,
                    onClick = {
                        isRegister = false
                        emailError = null
                        passwordError = null
                    },
                    label = { Text("Login") },
                )
                FilterChip(
                    selected = isRegister,
                    onClick = {
                        isRegister = true
                        emailError = null
                        passwordError = null
                    },
                    label = { Text("Register") },
                )
            }

            if (!statusMessage.isNullOrBlank()) {
                Card(
                    shape = ItemCardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Text(statusMessage, modifier = Modifier.padding(16.dp))
                }
            }

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailError != null,
                supportingText = {
                    if (emailError != null) {
                        Text(emailError!!)
                    }
                },
            )
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = passwordError != null,
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle password visibility",
                        )
                    }
                },
                supportingText = {
                    if (passwordError != null) {
                        Text(passwordError!!)
                    }
                },
            )
            Spacer(modifier = Modifier.size(4.dp))
            Button(
                onClick = {
                    if (isRegister) {
                        emailError = when {
                            email.trim().isBlank() -> "Email is required."
                            !isValidEmail(email) -> "Enter a valid email address."
                            else -> null
                        }
                        passwordError = when {
                            password.isBlank() -> "Password is required."
                            !isStrongPassword(password) -> "Use 8+ chars with upper, lower, and number."
                            else -> null
                        }
                        if (emailError == null && passwordError == null) {
                            onRegister(email, password)
                        }
                    } else {
                        emailError = when {
                            email.trim().isBlank() -> "Email is required."
                            !isValidEmail(email) -> "Enter a valid email address."
                            else -> null
                        }
                        passwordError = if (password.trim().isBlank()) "Password is required." else null
                        if (emailError == null && passwordError == null) {
                            onLogin(email, password)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(0.7f),
            ) {
                Text(if (isRegister) "Create Account" else "Login")
            }
        }
    }
}

private fun isStrongPassword(password: String): Boolean {
    return password.length >= 8 &&
        password.any { it.isUpperCase() } &&
        password.any { it.isLowerCase() } &&
        password.any { it.isDigit() }
}

private fun isValidEmail(value: String): Boolean {
    val trimmed = value.trim()
    return trimmed.contains("@") && trimmed.substringAfter("@").contains(".")
}

