package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.firebase.FirebaseManager
import com.example.ui.theme.*
import com.example.ui.viewmodel.IcchaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: IcchaViewModel,
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    var authMode by remember { mutableStateOf("LANDING") } // "LANDING", "LOGIN", "REGISTER", "FAST_DEMO"
    
    // Core inputs
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Pune") }
    
    // UI Helpers
    var isUploading by remember { mutableStateOf(false) }
    var selectedAvatar by remember { mutableStateOf(viewModel.availableAvatars[0]) }

    val gradientBg = Brush.radialGradient(
        colors = listOf(
            Color(0xFF2E244B), // deep violet
            Color(0xFF0F0C1B), // cosmic dark
            Color(0xFF0C0C12)  // pure midnight
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBg)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        if (authMode == "LANDING") {
            LandingPageView(
                onJoin = { authMode = "LOGIN" },
                onDemo = { authMode = "FAST_DEMO" },
                viewModel = viewModel
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 480.dp)
                    .padding(vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // App Branding Logo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(listOf(PrimaryAmber, AccentPurple))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Loop,
                        contentDescription = "AidLoop Logo",
                        tint = Color.Black,
                        modifier = Modifier.size(34.dp)
                    )
                }
                Text(
                    text = "AidLoop",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    color = TextLight,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Pune's Realtime Mutual Aid Network",
                    fontSize = 12.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }

            // Glassmorphism Container Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(
                            1.dp,
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.12f),
                                    Color.White.copy(alpha = 0.02f)
                                )
                            )
                        ),
                        RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1C2E).copy(alpha = 0.55f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (authMode) {
                        "LOGIN" -> {
                            Text(
                                text = "Sign In to AidLoop",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextLight,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email Address", color = TextMuted) },
                                placeholder = { Text("E.g., amit@example.com", color = TextMuted) },
                                modifier = Modifier.fillMaxWidth().testTag("login_email_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Email, null, tint = TextMuted) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextLight,
                                    unfocusedTextColor = TextLight,
                                    focusedBorderColor = PrimaryAmber,
                                    unfocusedBorderColor = CozyCard
                                )
                            )

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password", color = TextMuted) },
                                modifier = Modifier.fillMaxWidth().testTag("login_password_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                leadingIcon = { Icon(Icons.Default.Lock, null, tint = TextMuted) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextLight,
                                    unfocusedTextColor = TextLight,
                                    focusedBorderColor = PrimaryAmber,
                                    unfocusedBorderColor = CozyCard
                                )
                            )

                            Button(
                                onClick = {
                                    if (email.isNotBlank() && password.isNotBlank()) {
                                        isUploading = true
                                        viewModel.loginWithEmailAndPassword(
                                            email = email.trim(),
                                            password = password,
                                            onSuccess = {
                                                isUploading = false
                                                onAuthSuccess()
                                            },
                                            onFailure = { err ->
                                                isUploading = false
                                                Toast.makeText(context, "Auth Error: $err", Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    }
                                },
                                enabled = email.isNotBlank() && password.isNotBlank() && !isUploading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryAmber,
                                    disabledContainerColor = CozyCard
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("login_action_button")
                            ) {
                                if (isUploading) {
                                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                                } else {
                                    Icon(Icons.Default.Login, contentDescription = "login", tint = Color.Black)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Log In Securely", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Google Sign In Button
                            Button(
                                onClick = {
                                    // Simulated Google Authentication wrapper link
                                    isUploading = true
                                    viewModel.loginWithGoogleCredential(
                                        idToken = "google-sign-in-token-manually-triggered-key",
                                        onSuccess = {
                                            isUploading = false
                                            Toast.makeText(context, "Google Sign-In Session Connected!", Toast.LENGTH_SHORT).show()
                                            onAuthSuccess()
                                        },
                                        onFailure = { err ->
                                            isUploading = false
                                            Toast.makeText(context, "Google Error: $err", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("google_login_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.GTranslate, "google", tint = Color(0xFF4285F4))
                                    Text("Continue with Google", color = Color(0xFF5F6368), fontWeight = FontWeight.Bold)
                                }
                            }

                            Divider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { authMode = "REGISTER" }) {
                                    Text("Create Account", color = PrimaryAmber, fontSize = 13.sp)
                                }
                                TextButton(onClick = { authMode = "FAST_DEMO" }) {
                                    Text("Try Fast Demo Mode", color = TextMuted, fontSize = 13.sp)
                                }
                            }
                        }

                        "REGISTER" -> {
                            Text(
                                text = "Create AidLoop Account",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextLight,
                                modifier = Modifier.align(Alignment.Start)
                            )

                            // Avatar Picker
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Choose Profile Avatar",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                ) {
                                    viewModel.availableAvatars.forEach { av ->
                                        val isPicked = av == selectedAvatar
                                        AsyncImage(
                                            model = av,
                                            contentDescription = "Avatar",
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .border(
                                                    width = if (isPicked) 2.dp else 1.dp,
                                                    color = if (isPicked) PrimaryAmber else CozyCard,
                                                    shape = CircleShape
                                                )
                                                .clickable { selectedAvatar = av },
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email", color = TextMuted) },
                                modifier = Modifier.fillMaxWidth().testTag("reg_email"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight, focusedBorderColor = PrimaryAmber, unfocusedBorderColor = CozyCard)
                            )

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password", color = TextMuted) },
                                modifier = Modifier.fillMaxWidth().testTag("reg_pwd"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight, focusedBorderColor = PrimaryAmber, unfocusedBorderColor = CozyCard)
                            )

                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("Display/Full Name", color = TextMuted) },
                                modifier = Modifier.fillMaxWidth().testTag("reg_name_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight, focusedBorderColor = PrimaryAmber, unfocusedBorderColor = CozyCard)
                            )

                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                label = { Text("Username", color = TextMuted) },
                                modifier = Modifier.fillMaxWidth().testTag("reg_username_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight, focusedBorderColor = PrimaryAmber, unfocusedBorderColor = CozyCard)
                            )

                            OutlinedTextField(
                                value = bio,
                                onValueChange = { bio = it },
                                label = { Text("Brief Bio", color = TextMuted) },
                                modifier = Modifier.fillMaxWidth().testTag("reg_bio_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight, focusedBorderColor = PrimaryAmber, unfocusedBorderColor = CozyCard)
                            )

                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text("Your Location / City", color = TextMuted) },
                                modifier = Modifier.fillMaxWidth().testTag("reg_location_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextLight, focusedBorderColor = PrimaryAmber, unfocusedBorderColor = CozyCard)
                            )

                            Button(
                                onClick = {
                                    if (email.isNotBlank() && password.isNotBlank() && username.isNotBlank() && fullName.isNotBlank()) {
                                        isUploading = true
                                        viewModel.registerUserWithEmailAndPassword(
                                            email = email.trim(),
                                            password = password,
                                            username = username,
                                            fullName = fullName,
                                            bio = bio,
                                            location = city,
                                            picUrl = selectedAvatar,
                                            onSuccess = {
                                                isUploading = false
                                                onAuthSuccess()
                                            },
                                            onFailure = { err ->
                                                isUploading = false
                                                Toast.makeText(context, "Registration Fail: $err", Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    }
                                },
                                enabled = email.isNotBlank() && password.isNotBlank() && username.isNotBlank() && fullName.isNotBlank() && !isUploading,
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAmber, disabledContainerColor = CozyCard),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("do_register_button")
                            ) {
                                if (isUploading) {
                                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                                } else {
                                    Text("Register & Enter", fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }

                            TextButton(onClick = { authMode = "LOGIN" }) {
                                Text("Already have an account? Sign In", color = TextMuted, fontSize = 12.sp)
                            }
                        }

                        "FAST_DEMO" -> {
                            Text(
                                text = "Developer Fast-Track Access",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextLight,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Text(
                                text = "Bypass manual email validation with pre-configured developer templates.",
                                fontSize = 12.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )

                            Button(
                                onClick = {
                                    viewModel.registerOrLogin("patel_anshuman", "Anshuman Patel", "Coordinating support in Pune", "Pune", viewModel.availableAvatars[0])
                                    onAuthSuccess()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAmber),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("fast_login_button")
                            ) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = "demo", tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Enter as Anshuman (Demo)", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                            }

                            Button(
                                onClick = {
                                    viewModel.registerOrLogin("priya_medical", "Dr. Priya Deshmukh", "Resident clinician at Pune Community Health", "Pune", viewModel.availableAvatars[2])
                                    onAuthSuccess()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) {
                                Icon(Icons.Default.MedicalServices, contentDescription = "demo", tint = TextLight)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Enter as Dr. Priya (Medical)", color = TextLight, fontWeight = FontWeight.Bold)
                            }

                            TextButton(onClick = { authMode = "LOGIN" }) {
                                Text("← Back to Secure Email Login", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun LandingPageView(
    onJoin: () -> Unit,
    onDemo: () -> Unit,
    viewModel: IcchaViewModel
) {
    val scrollState = rememberScrollState()
    val joinedLoops by viewModel.joinedLoops.collectAsState()
    
    // Sandbox Mock state for interactable mockup preview
    var mockUpvotes by remember { mutableStateOf(42) }
    var mockClickedUpvote by remember { mutableStateOf(false) }
    
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val isWide = maxWidth > 750.dp
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Header Nav Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.linearGradient(listOf(PrimaryAmber, AccentPurple))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Loop,
                            contentDescription = "AidLoop Logo",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = "AidLoop",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(onClick = onJoin) {
                        Text("Sign In", color = PrimaryAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Button(
                        onClick = onDemo,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Instant Try", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
            
            // Hero + Interactive Simulated App Preview Section
            if (isWide) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1.2f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryAmber.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "NEXT-GEN HYPERLOCAL GRAPH 🌐",
                                color = PrimaryAmber,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        
                        Text(
                            text = "Connect. Collaborate.\nCo-elevate.",
                            color = Color.White,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black,
                            lineHeight = 48.sp
                        )
                        
                        Text(
                            text = "AidLoop is a hyperlocal social platform where nearby people connect, discuss ideas, discover opportunities and collaborate with local communities.",
                            color = TextMuted,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = onJoin,
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAmber),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(44.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("Join Pune Hub", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 13.sp)
                                    Icon(imageVector = Icons.Default.ArrowForward, null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                }
                            }
                            Button(
                                onClick = onDemo,
                                colors = ButtonDefaults.buttonColors(containerColor = CozyCard),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(44.dp)
                            ) {
                                Text("Bypass using Demo Session", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                    
                    // Web browser / App Preview Frame Mockup
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(340.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF1E1B2E).copy(alpha = 0.5f))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(24.dp))
                            .padding(16.dp)
                    ) {
                        InteractiveAppSimulator(
                            mockUpvotes = mockUpvotes,
                            mockClickedUpvote = mockClickedUpvote,
                            onToggleUpvote = {
                                mockClickedUpvote = !mockClickedUpvote
                                if (mockClickedUpvote) mockUpvotes++ else mockUpvotes--
                            }
                        )
                    }
                }
            } else {
                // Portrait stack for mobile landing
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryAmber.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "A WEB SOCIAL REVOLUTION",
                            color = PrimaryAmber,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    
                    Text(
                        text = "AidLoop Hyperlocal Ecosystem",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = "AidLoop is a hyperlocal social platform where nearby people connect, discuss ideas, discover opportunities and collaborate with local communities.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onJoin,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAmber),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Text("Get Started", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Button(
                            onClick = onDemo,
                            colors = ButtonDefaults.buttonColors(containerColor = CozyCard),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Text("Fast Demo Setup", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                    
                    // Simple Interactive Simulator Card for mobile
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF1E1B2E).copy(alpha = 0.5f))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(18.dp))
                            .padding(12.dp)
                    ) {
                        InteractiveAppSimulator(
                            mockUpvotes = mockUpvotes,
                            mockClickedUpvote = mockClickedUpvote,
                            onToggleUpvote = {
                                mockClickedUpvote = !mockClickedUpvote
                                if (mockClickedUpvote) mockUpvotes++ else mockUpvotes--
                            }
                        )
                    }
                }
            }
            
            // Grid section: Previews of Trending Loops and Discussions side-by-side on wide screens
            if (isWide) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "⭕ TRENDING NEIGHBORHOOD LOOPS",
                            color = PrimaryAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        TrendingLoopsGrid(viewModel)
                    }
                    Column(modifier = Modifier.weight(1.2f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "💬 NEARBY DISCUSSIONS PREVIEW",
                            color = AccentPurple,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        NearbyDiscussionsPreview()
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        "⭕ POPULAR LOOPS PREVIEW",
                        color = PrimaryAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    TrendingLoopsGrid(viewModel)
                    
                    Text(
                        "💬 LIVE DISCUSSIONS PREVIEW",
                        color = AccentPurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    NearbyDiscussionsPreview()
                }
            }
            
            // Testimonial & Social Trust Banner Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = "HEAR FROM OUR COMMUNITY",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(CozyCard.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                "“Borrowing moving crates near Baner took less than 15 mins. Genuinely beautiful local experience.”",
                                color = TextLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("— Rohan S., Deccan Resident", color = PrimaryAmber, fontSize = 9.sp)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(CozyCard.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                "“Offering local medical advice has connected me to wonderful neighbors. Absolute credit to the safety flow.”",
                                color = TextLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("— Dr. Priya D., Health General", color = PrimaryAmber, fontSize = 9.sp)
                        }
                    }
                }
            }
            
            // Footer Branding
            Text(
                text = "© 2026 AidLoop Inc. Hyperlocal Social Engine. Engineered for secure, responsive community development.",
                color = TextMuted,
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

@Composable
fun InteractiveAppSimulator(
    mockUpvotes: Int,
    mockClickedUpvote: Boolean,
    onToggleUpvote: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // App header bar mock
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(ValidGreen))
                Text("Baner Green Loop", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Text("Simulation Live", color = PrimaryAmber, fontSize = 8.sp, fontWeight = FontWeight.Black)
        }
        
        // Post content card mockup
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = CozyCard.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=80&q=80",
                        contentDescription = "Priya profile",
                        modifier = Modifier.size(24.dp).clip(CircleShape).border(1.dp, PrimaryAmber, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Column {
                        Text("Ananya Deshmukh", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("📍 Pancard Club Road (200m away)", color = TextMuted, fontSize = 8.sp)
                    }
                }
                
                Text(
                    text = "🌱 Free Organic Composting Workshop Sunday!",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Bringing premium compost starter kits for anyone registered in the Baner Loop. Tap click upvote if you're attending!",
                    color = TextMuted,
                    fontSize = 9.sp,
                    lineHeight = 12.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (mockClickedUpvote) PrimaryAmber.copy(alpha = 0.2f) else CozyCard)
                            .clickable { onToggleUpvote() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "upvote",
                            tint = if (mockClickedUpvote) PrimaryAmber else TextMuted,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("$mockUpvotes", color = if (mockClickedUpvote) PrimaryAmber else TextLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.ChatBubble, null, tint = TextMuted, modifier = Modifier.size(11.dp))
                        Text("18 replies", color = TextMuted, fontSize = 8.sp)
                    }
                }
            }
        }
        
        // Chat feedback notification simulation bubble
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AccentPurple.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                .border(1.dp, AccentPurple.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                .padding(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(AccentPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Chat, null, tint = Color.White, modifier = Modifier.size(10.dp))
                }
                Text(
                    text = "Live DM Ping: Neha S. requested to join Deccan Composting group.",
                    color = TextLight,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun TrendingLoopsGrid(viewModel: IcchaViewModel) {
    val joinedState by viewModel.joinedLoops.collectAsState()
    val preLoops = listOf(
        Pair("Kalyani Nagar Loop", "https://images.unsplash.com/photo-1511632765486-a01980e01a18?auto=format&fit=crop&w=150&q=80"),
        Pair("FC Road Tech Loop", "https://images.unsplash.com/photo-1559027615-cd4628902d4a?auto=format&fit=crop&w=150&q=80"),
        Pair("Baner Green Loop", "https://images.unsplash.com/photo-1544640808-32ca72ac7f37?auto=format&fit=crop&w=150&q=80"),
        Pair("Pune Medical Response", "https://images.unsplash.com/photo-1516550893923-42d28e5677af?auto=format&fit=crop&w=150&q=80")
    )
    
    // Renders 4 adaptive boxes inside a Row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        preLoops.forEach { (name, img) ->
            val isJoined = joinedState.contains(name)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E1B2E).copy(alpha = 0.4f))
                    .border(BorderStroke(1.dp, if (isJoined) PrimaryAmber else Color.White.copy(alpha = 0.05f)), RoundedCornerShape(12.dp))
                    .clickable { viewModel.toggleJoinLoop(name) }
                    .padding(8.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AsyncImage(
                        model = img,
                        contentDescription = name,
                        modifier = Modifier.size(36.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Text(name, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        text = if (isJoined) "Joined ✓" else "+ Tap Join",
                        color = if (isJoined) PrimaryAmber else TextMuted,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun NearbyDiscussionsPreview() {
    val mockDiscussions = listOf(
        Triple("FC Road Library", "📚 Anyone want to co-study Android/Kotlin tomorrow morning?", "3 replies"),
        Triple("Koregaon Park", "🌱 Setting up community compost exchange behind Lane 5", "12 replies"),
        Triple("Baner District", "🏥 Dr. Priya: Free clinic screening happening this saturday", "8 replies")
    )
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        mockDiscussions.forEach { (loc, pitch, comments) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CozyCard.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(imageVector = Icons.Default.NearMe, null, tint = AccentPurple, modifier = Modifier.size(9.dp))
                        Text(loc, color = TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(pitch, color = TextLight, fontSize = 9.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CozyCard)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(comments, color = TextLight, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

