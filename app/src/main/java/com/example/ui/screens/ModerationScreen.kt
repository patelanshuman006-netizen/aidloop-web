package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.User
import com.example.data.model.Wish
import com.example.ui.theme.*
import com.example.ui.viewmodel.IcchaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModerationScreen(
    viewModel: IcchaViewModel,
    onBack: () -> Unit
) {
    val wishes by viewModel.allWishesState.collectAsState()
    val users by viewModel.allUsersState.collectAsState()

    var activeTab by remember { mutableStateOf("WISHES") } // "WISHES" or "USERS"

    val reportedWishes = wishes.filter { it.isReported || it.isSpamDetected || it.isFakeDetected }
    val suspiciousUsers = users.filter { it.isSuspicious || it.reportCount > 0 || it.reputationPoints <= 40 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = PrimaryGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Admin Moderation Portal",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLight
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_button_moderation")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepSurface,
                    titleContentColor = TextLight
                )
            )
        },
        containerColor = DarkGrayBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Summary Widgets Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepSurface),
                    border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Flagged Needs", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = reportedWishes.size.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = ErrorRed
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepSurface),
                    border = BorderStroke(1.dp, PrimaryAmber.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Suspicious Users", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = suspiciousUsers.size.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryAmber
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepSurface),
                    border = BorderStroke(1.dp, ValidGreen.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Active Core Users", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = users.size.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = ValidGreen
                        )
                    }
                }
            }

            // Tab Selector Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DeepSurface)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeTab = "WISHES" }
                        .background(if (activeTab == "WISHES") PrimaryAmber else Color.Transparent)
                        .padding(vertical = 10.dp)
                        .testTag("mod_tab_wishes"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Flags Needs (${reportedWishes.size})",
                        color = if (activeTab == "WISHES") Color.Black else TextLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeTab = "USERS" }
                        .background(if (activeTab == "USERS") PrimaryAmber else Color.Transparent)
                        .padding(vertical = 10.dp)
                        .testTag("mod_tab_users"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Suspicious Accounts (${suspiciousUsers.size})",
                        color = if (activeTab == "USERS") Color.Black else TextLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Listing Area
            if (activeTab == "WISHES") {
                if (reportedWishes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = ValidGreen, modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("All Needs Cleared", color = TextLight, fontWeight = FontWeight.Bold)
                            Text("No flagged requests, spam, or scam logs present in Pune system.", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(reportedWishes) { wish ->
                            ModWishCard(
                                wish = wish,
                                onDismiss = { viewModel.resolveWishReport(wish.id, deleteWish = false) },
                                onDelete = { viewModel.resolveWishReport(wish.id, deleteWish = true) },
                                onScanAI = { viewModel.runSafetyCheck(wish.id) },
                                isEngineChecking = viewModel.isCheckingSafety
                            )
                        }
                    }
                }
            } else {
                if (suspiciousUsers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                            Icon(Icons.Default.Mood, contentDescription = null, tint = ValidGreen, modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Zero Warned Accounts", color = TextLight, fontWeight = FontWeight.Bold)
                            Text("No Pune AidLoop accounts flagged as suspicious. Excellent community conduct!", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(suspiciousUsers) { user ->
                            ModUserCard(
                                user = user,
                                onApprove = { viewModel.resolveUserSuspicion(user.userId, clearSuspicion = true) },
                                onBan = { viewModel.resolveUserSuspicion(user.userId, clearSuspicion = false) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModWishCard(
    wish: Wish,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onScanAI: () -> Unit,
    isEngineChecking: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeepSurface),
        border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(24.dp).clip(CircleShape)) {
                        AsyncImage(model = wish.creatorImage, contentDescription = null, contentScale = ContentScale.Crop)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(wish.creatorName, color = TextLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Gray.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(wish.category.uppercase(), fontSize = 8.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(wish.title, color = TextLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(wish.content, color = TextMuted, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)

            // Show Flags Details
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(10.dp)
            ) {
                if (wish.isReported) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("User Report Flag: ${wish.reportReason ?: "Spam suspicion"}", color = ErrorRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (wish.isSpamDetected || wish.isFakeDetected) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = PrimaryGold, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AI Threat Assessment Score: Spam ${wish.spamScore}% | Fake ${wish.fakeScore}%",
                            color = PrimaryGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = wish.detectionExplanation ?: "Local pattern check flagged advance transaction security hazard.",
                        color = TextMuted,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onScanAI,
                    enabled = !isEngineChecking,
                    colors = ButtonDefaults.buttonColors(containerColor = CozyCard),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(12.dp), tint = PrimaryAmber)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AI Safety Scan", fontSize = 10.sp, color = TextLight)
                    }
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ValidGreen.copy(alpha = 0.15f)),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Dismiss", fontSize = 10.sp, color = ValidGreen, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.15f)),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete Post", fontSize = 10.sp, color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ModUserCard(
    user: User,
    onApprove: () -> Unit,
    onBan: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeepSurface),
        border = BorderStroke(1.dp, PrimaryAmber.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(44.dp).clip(CircleShape)) {
                    AsyncImage(model = user.profilePicUrl, contentDescription = null, contentScale = ContentScale.Crop)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(user.fullName, color = TextLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(PrimaryGold.copy(alpha = 0.15f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("${user.reputationPoints} Rep", fontSize = 9.sp, color = PrimaryGold, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text("@${user.username}", color = TextMuted, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.2f))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = PrimaryAmber, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Received Reports: ${user.reportCount}",
                        color = PrimaryAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.suspiciousReason ?: "System Flag: Multi-report threshold triggered or low reputation status.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = ValidGreen.copy(alpha = 0.2f)),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = ValidGreen)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear Suspicion", fontSize = 10.sp, color = ValidGreen, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onBan,
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.2f)),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(12.dp), tint = ErrorRed)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Flag Permanently", fontSize = 10.sp, color = ErrorRed, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
