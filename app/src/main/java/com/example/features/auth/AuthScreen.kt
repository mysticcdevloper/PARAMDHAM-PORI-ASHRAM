package com.example.features.auth

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.models.MemberProfile
import com.example.core.models.MemberStatus
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.RajShyamaLogo
import com.example.ui.viewmodel.AppViewModel

@Composable
fun AuthScreen(
  viewModel: AppViewModel,
  onAuthSuccess: () -> Unit
) {
  var isRegisterMode by remember { mutableStateOf(false) }
  var selectedLoginMethod by remember { mutableStateOf(0) } // 0 = Phone, 1 = Email, 2 = Google

  // Inputs
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var phoneNumber by remember { mutableStateOf("") }
  var otpCode by remember { mutableStateOf("") }
  var isOtpSent by remember { mutableStateOf(false) }

  // Registration Form Fields
  var regPhotoPath by remember { mutableStateOf<String?>(null) }
  var regName by remember { mutableStateOf("") }
  var regPhone by remember { mutableStateOf("") }
  var regEmail by remember { mutableStateOf("") }
  var regGender by remember { mutableStateOf("Male") }
  var regDob by remember { mutableStateOf("1995-01-01") }
  var regCity by remember { mutableStateOf("") }
  var regState by remember { mutableStateOf("") }
  var regCountry by remember { mutableStateOf("India") }
  var regAshramId by remember { mutableStateOf("") }
  var regOccupation by remember { mutableStateOf("") }
  var regBio by remember { mutableStateOf("") }
  var regEmergency by remember { mutableStateOf("") }
  var regInviteCode by remember { mutableStateOf("") }

  // Processing indicators
  var isProcessing by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var successMessage by remember { mutableStateOf<String?>(null) }

  // Modal selector for photos
  var showPhotoSelector by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
          )
        )
      )
      .padding(horizontal = 24.dp, vertical = 40.dp)
      .testTag("auth_screen_root"),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Spacer(modifier = Modifier.height(20.dp))
    RajShyamaLogo(size = 72.dp)
    Spacer(modifier = Modifier.height(12.dp))
    Text(
      text = "Paramdham Podhi Ashram",
      fontSize = 24.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
      fontFamily = FontFamily.Serif
    )
    Text(
      text = "Secure Member Gateway",
      fontSize = 12.sp,
      fontWeight = FontWeight.Light,
      color = MaterialTheme.colorScheme.secondary,
      letterSpacing = 1.5.sp
    )

    Spacer(modifier = Modifier.height(30.dp))

    // Form errors
    if (errorMessage != null) {
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
      ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
          Spacer(modifier = Modifier.width(8.dp))
          Text(errorMessage!!, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 12.sp)
        }
      }
    }

    if (!isRegisterMode) {
      // --- LOGIN VIEW ---
      GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
      ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "Welcome Seekers",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
          )

          TabRow(
            selectedTabIndex = selectedLoginMethod,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth().height(44.dp).padding(bottom = 20.dp)
          ) {
            Tab(selected = selectedLoginMethod == 0, onClick = { selectedLoginMethod = 0 }) {
              Text("Phone OTP", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = selectedLoginMethod == 1, onClick = { selectedLoginMethod = 1 }) {
              Text("Email", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = selectedLoginMethod == 2, onClick = { selectedLoginMethod = 2 }) {
              Text("Google", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }

          when (selectedLoginMethod) {
            0 -> { // Phone OTP
              if (!isOtpSent) {
                OutlinedTextField(
                  value = phoneNumber,
                  onValueChange = { phoneNumber = it },
                  label = { Text("Mobile Number") },
                  leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                  placeholder = { Text("+91 XXXXX XXXXX") },
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                  onClick = {
                    if (phoneNumber.length < 10) {
                      errorMessage = "Enter a valid mobile phone number."
                    } else {
                      isOtpSent = true
                      errorMessage = null
                      viewModel.showToast("Simulated OTP sent to $phoneNumber! Code: 123456")
                    }
                  },
                  modifier = Modifier.fillMaxWidth().height(48.dp),
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Text("Get OTP Code", fontWeight = FontWeight.Bold)
                }
              } else {
                Text(
                  text = "Enter the 6-digit OTP sent to $phoneNumber",
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.secondary,
                  modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                  value = otpCode,
                  onValueChange = { otpCode = it },
                  label = { Text("OTP Code (Use '123456')") },
                  leadingIcon = { Icon(Icons.Default.LockOpen, contentDescription = null) },
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                  onClick = {
                    isProcessing = true
                    errorMessage = null
                    viewModel.loginWithPhoneOTP(phoneNumber, otpCode) { success, err ->
                      isProcessing = false
                      if (success) {
                        onAuthSuccess()
                      } else {
                        errorMessage = err ?: "Invalid OTP code."
                      }
                    }
                  },
                  modifier = Modifier.fillMaxWidth().height(48.dp),
                  shape = RoundedCornerShape(12.dp)
                ) {
                  if (isProcessing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                  } else {
                    Text("Verify & Enter Portal", fontWeight = FontWeight.Bold)
                  }
                }
                TextButton(onClick = { isOtpSent = false }) {
                  Text("Change Phone Number")
                }
              }
            }
            1 -> { // Email Login
              OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
              )
              Spacer(modifier = Modifier.height(12.dp))
              OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
              )
              Spacer(modifier = Modifier.height(16.dp))
              Button(
                onClick = {
                  isProcessing = true
                  errorMessage = null
                  viewModel.loginWithEmail(email, password) { success, err ->
                    isProcessing = false
                    if (success) {
                      onAuthSuccess()
                    } else {
                      errorMessage = err ?: "Login failed. Check your password."
                    }
                  }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
              ) {
                if (isProcessing) {
                  CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                  Text("Sign In", fontWeight = FontWeight.Bold)
                }
              }
            }
            2 -> { // Google Login
              Text(
                text = "Access the private portal instantly with your Google account credentials safely verified by JWT tokens.",
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Button(
                onClick = {
                  isProcessing = true
                  errorMessage = null
                  viewModel.loginWithGoogle("google_mock_id_token") { success, err ->
                    isProcessing = false
                    if (success) {
                      onAuthSuccess()
                    } else {
                      errorMessage = err ?: "Google authentication failed."
                    }
                  }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.AccountCircle, contentDescription = null)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Continue with Google", fontWeight = FontWeight.Bold)
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))
          TextButton(onClick = { isRegisterMode = true }) {
            Text("First Time Member? Register Profile")
          }
        }
      }
    } else {
      // --- REGISTRATION FORM ---
      GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Create Member Profile",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
          )

          // Profile Photo Selector
          Box(
            modifier = Modifier.size(90.dp).align(Alignment.CenterHorizontally).clickable { showPhotoSelector = true },
            contentAlignment = Alignment.Center
          ) {
            if (regPhotoPath == null) {
              Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape).border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Outlined.PhotoCamera, contentDescription = "Camera", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
              }
            } else {
              Box(
                modifier = Modifier.fillMaxSize().background(Color(0xFFFFB300), CircleShape).border(2.dp, Color(0xFFFFB300), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Text("🌸", fontSize = 36.sp)
              }
            }
            Box(
              modifier = Modifier.size(26.dp).background(MaterialTheme.colorScheme.primary, CircleShape).align(Alignment.BottomEnd),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
          }
          Text(
            "Profile Photo (Camera/Gallery supported)",
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 16.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
          )

          // Form fields
          OutlinedTextField(
            value = regName,
            onValueChange = { regName = it },
            label = { Text("Full Name *") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
          )

          OutlinedTextField(
            value = regPhone,
            onValueChange = { regPhone = it },
            label = { Text("Phone Number *") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
          )

          OutlinedTextField(
            value = regEmail,
            onValueChange = { regEmail = it },
            label = { Text("Email Address *") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
          )

          // Gender dropdown simulator
          Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            listOf("Male", "Female", "Other").forEach { g ->
              val isSelected = regGender == g
              Button(
                onClick = { regGender = g },
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
              ) {
                Text(g, fontSize = 11.sp, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface)
              }
            }
          }

          OutlinedTextField(
            value = regDob,
            onValueChange = { regDob = it },
            label = { Text("Date of Birth (YYYY-MM-DD) *") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
          )

          Row(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
            OutlinedTextField(
              value = regCity,
              onValueChange = { regCity = it },
              label = { Text("City *") },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.weight(1f).padding(end = 4.dp)
            )
            OutlinedTextField(
              value = regState,
              onValueChange = { regState = it },
              label = { Text("State *") },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.weight(1f).padding(start = 4.dp)
            )
          }

          OutlinedTextField(
            value = regCountry,
            onValueChange = { regCountry = it },
            label = { Text("Country") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
          )

          OutlinedTextField(
            value = regAshramId,
            onValueChange = { regAshramId = it },
            label = { Text("Ashram Member ID (optional)") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
          )

          OutlinedTextField(
            value = regOccupation,
            onValueChange = { regOccupation = it },
            label = { Text("Occupation (optional)") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
          )

          OutlinedTextField(
            value = regBio,
            onValueChange = { regBio = it },
            label = { Text("Short Spiritual Bio") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
          )

          OutlinedTextField(
            value = regEmergency,
            onValueChange = { regEmergency = it },
            label = { Text("Emergency Contact (Relative/Friend) *") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
          )

          OutlinedTextField(
            value = regInviteCode,
            onValueChange = { regInviteCode = it },
            label = { Text("Invite Code (Optional for Quick Approval)") },
            placeholder = { Text("e.g. GURU77") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
          )

          Button(
            onClick = {
              if (regName.isEmpty() || regPhone.isEmpty() || regEmail.isEmpty() || regCity.isEmpty() || regState.isEmpty()) {
                errorMessage = "All asterisk (*) marked fields are mandatory."
              } else {
                isProcessing = true
                errorMessage = null
                val profile = MemberProfile(
                  id = "member_${System.currentTimeMillis()}",
                  fullName = regName,
                  phoneNumber = regPhone,
                  email = regEmail,
                  gender = regGender,
                  dob = regDob,
                  city = regCity,
                  state = regState,
                  country = regCountry,
                  ashramMemberId = if (regAshramId.isNotEmpty()) regAshramId else null,
                  occupation = if (regOccupation.isNotEmpty()) regOccupation else null,
                  bio = regBio,
                  emergencyContact = regEmergency,
                  status = MemberStatus.PENDING, // Pending state by default
                  profilePhotoUrl = regPhotoPath ?: "avatar"
                )
                viewModel.registerNewMember(profile, regInviteCode) { success, err ->
                  isProcessing = false
                  if (success) {
                    onAuthSuccess()
                  } else {
                    errorMessage = err
                  }
                }
              }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
          ) {
            if (isProcessing) {
              CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
              Text("Submit & Await Approval", fontWeight = FontWeight.Bold)
            }
          }

          TextButton(
            onClick = { isRegisterMode = false },
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Already registered? Go to Login")
          }
        }
      }
    }

    // Photo Dialog simulation (Crop / Compress)
    if (showPhotoSelector) {
      AlertDialog(
        onDismissRequest = { showPhotoSelector = false },
        title = { Text("Upload Profile Photo", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Crop & Compress optimization will automatically execute before uploading to 'profile-images' bucket.", fontSize = 12.sp)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Button(
                onClick = {
                  regPhotoPath = "camera_simulated_image_cropped_compressed.jpg"
                  showPhotoSelector = false
                  viewModel.showToast("Camera image cropped & compressed (150kb).")
                },
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.Camera, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Camera", fontSize = 11.sp)
              }

              Button(
                onClick = {
                  regPhotoPath = "gallery_simulated_image_cropped_compressed.jpg"
                  showPhotoSelector = false
                  viewModel.showToast("Gallery image cropped & compressed (120kb).")
                },
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Gallery", fontSize = 11.sp)
              }
            }
          }
        },
        confirmButton = {
          TextButton(onClick = { showPhotoSelector = false }) { Text("Cancel") }
        }
      )
    }
  }
}
