package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.User
import com.example.ui.theme.*
import com.example.ui.viewmodel.IcchaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: IcchaViewModel) {
    val currentUser by viewModel.currentUserState.collectAsState()
    val wishes by viewModel.allWishesState.collectAsState()
    val joinedLoops by viewModel.joinedLoops.collectAsState()

    var showLocationDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Loop Profile", fontWeight = FontWeight.Bold, color = TextLight, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepSurface),
                actions = {
                    // Sign out trigger
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("logout_profile_button")
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "Sign Out", tint = ErrorRed)
                    }
                }
            )
        },
        containerColor = DarkGrayBg
    ) { paddingValues ->
        if (currentUser == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryGold)
            }
            return@Scaffold
        }

        val user = currentUser!!
        val userWishes = wishes.filter { w -> w.creatorId == user.userId }
        val fulfilledCount = userWishes.count { w -> w.fulfillStatus == "FULFILLED" }

        // Localized Hyperlocal Selection Dialog with safety approximations
        if (showLocationDialog) {
            var tempCity by remember { mutableStateOf(user.city) }
            var tempCollege by remember { mutableStateOf(user.college) }
            var tempArea by remember { mutableStateOf(user.area) }
            var tempLocality by remember { mutableStateOf(user.locality) }

            val activeLocalities = when (tempArea) {
                "Kothrud" -> listOf("Ideal Colony", "Mayur Colony", "Bhusari Colony")
                "Baner" -> listOf("Pancard Club Road", "Balewadi High St", "Veerbhadra Nagar")
                "Hinjawadi" -> listOf("Phase 1 Tech Court", "Phase 2 IT Hub", "Megapolis")
                "Kalyani Nagar" -> listOf("Gold Adlabs", "Koregaon Park Jnc", "Central Avenue")
                "Viman Nagar" -> listOf("Phoenix Mall Enclave", "Dutta Mandir Chowk", "Symbiosis Road")
                "Deccan Gymkhana" -> listOf("FC Road Campus", "Bhandarkar Road", "Goodluck Cafe Area")
                "Shivaji Nagar" -> listOf("Shivaji Nagar Stn", "Model Colony", "JM Road Lane")
                "Katraj" -> listOf("Katraj Lake Side", "Bharati Vidyapeeth", "Katraj Chowk")
                else -> listOf("Main Block", "Central Enclave")
            }

            AlertDialog(
                onDismissRequest = { showLocationDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EditLocationAlt, contentDescription = null, tint = PrimaryAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Hyperlocal Zone", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Select your approximate district. We never expose your exact live coordinates to ensure your absolute safety and privacy.", color = TextMuted, fontSize = 11.sp, lineHeight = 15.sp)
                        
                        // City Input
                        Column {
                            Text("City Selection:", color = PrimaryAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                listOf("Pune", "Mumbai", "Bangalore", "Delhi").forEach { c ->
                                    val sel = tempCity == c
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (sel) PrimaryAmber.copy(alpha = 0.2f) else CozyCard)
                                            .border(1.dp, if (sel) PrimaryAmber else Color.Transparent, RoundedCornerShape(8.dp))
                                            .clickable { 
                                                tempCity = c 
                                                if (c != "Pune") {
                                                    tempCollege = "None"
                                                    tempArea = if (c == "Mumbai") "South Mumbai" else if (c == "Bangalore") "Indiranagar" else "Connaught Place"
                                                    tempLocality = if (c == "Mumbai") "Gateway Enclave" else if (c == "Bangalore") "100 Feet Road" else "Rajiv Chowk Sector"
                                                } else {
                                                    tempArea = "Kothrud"
                                                    tempLocality = "Ideal Colony"
                                                    tempCollege = "None"
                                                }
                                            }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(c, color = if (sel) PrimaryAmber else TextLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        if (tempCity == "Pune") {
                            // Area Selection
                            Column {
                                Text("Area / District (Pune):", color = PrimaryAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                val areas = listOf("Kothrud", "Baner", "Hinjawadi", "Kalyani Nagar", "Viman Nagar", "Deccan Gymkhana", "Shivaji Nagar", "Katraj")
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    var expanded by remember { mutableStateOf(false) }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(CozyCard)
                                            .clickable { expanded = true }
                                            .padding(10.dp)
                                    ) {
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Text(tempArea, color = TextLight, fontSize = 12.sp)
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextMuted)
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = expanded, 
                                        onDismissRequest = { expanded = false },
                                        modifier = Modifier.background(DeepSurface)
                                    ) {
                                        areas.forEach { a ->
                                            DropdownMenuItem(
                                                text = { Text(a, color = Color.White) },
                                                onClick = {
                                                    tempArea = a
                                                    tempLocality = when (a) {
                                                        "Kothrud" -> "Ideal Colony"
                                                        "Baner" -> "Pancard Club Road"
                                                        "Hinjawadi" -> "Phase 1 Tech Court"
                                                        "Kalyani Nagar" -> "Gold Adlabs"
                                                        "Viman Nagar" -> "Phoenix Mall Enclave"
                                                        "Deccan Gymkhana" -> "FC Road Campus"
                                                        "Shivaji Nagar" -> "Shivaji Nagar Stn"
                                                        "Katraj" -> "Katraj Lake Side"
                                                        else -> "Main Block"
                                                    }
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Locality Selection
                            Column {
                                Text("Sub-locality (Safe approximate):", color = PrimaryAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                    activeLocalities.forEach { loc ->
                                        val sel = tempLocality == loc
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (sel) AccentPurple.copy(alpha = 0.2f) else CozyCard)
                                                .border(1.dp, if (sel) AccentPurple else Color.Transparent, RoundedCornerShape(8.dp))
                                                .clickable { tempLocality = loc }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(loc, color = if (sel) AccentPurple else TextLight, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // College Campus
                            Column {
                                Text("College or Campus association:", color = PrimaryAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                val colleges = listOf("Pune University (SPPU)", "COEP Tech University", "Symbiosis University (SIU)", "MIT World Peace University (MIT-WPU)", "Fergusson College", "None")
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    var expandedCol by remember { mutableStateOf(false) }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(CozyCard)
                                            .clickable { expandedCol = true }
                                            .padding(10.dp)
                                    ) {
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Text(tempCollege, color = TextLight, fontSize = 12.sp)
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextMuted)
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = expandedCol, 
                                        onDismissRequest = { expandedCol = false },
                                        modifier = Modifier.background(DeepSurface)
                                    ) {
                                        colleges.forEach { col ->
                                            DropdownMenuItem(
                                                text = { Text(col, color = Color.White) },
                                                onClick = {
                                                    tempCollege = col
                                                    expandedCol = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateUserLocation(tempCity, tempCollege, tempArea, tempLocality)
                            showLocationDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryAmber)
                    ) {
                        Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLocationDialog = false }) {
                        Text("Cancel", color = TextMuted)
                    }
                },
                containerColor = DeepSurface,
                shape = RoundedCornerShape(16.dp)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(DarkGrayBg),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card (Glassmorphic)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DeepSurface.copy(alpha = 0.7f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.1f),
                                Color.White.copy(alpha = 0.02f)
                            )
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(modifier = Modifier.size(90.dp)) {
                            AsyncImage(
                                model = user.profilePicUrl,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .border(2.dp, PrimaryAmber, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            if (user.isVerified) {
                                val badgeColor = when (user.verificationLevel) {
                                    1 -> SilverBadge
                                    2 -> GoldBadge
                                    3 -> PlatinumBadge
                                    else -> PrimaryGold
                                }
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(badgeColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Verified Flag",
                                        tint = Color.Black,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = user.fullName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextLight
                        )

                        Text(
                            text = "@${user.username}",
                            fontSize = 13.sp,
                            color = PrimaryAmber,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (user.isSuspicious) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.15f)),
                                border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = user.suspiciousReason ?: "This profile has been flagged for safety compliance audits.",
                                        color = ErrorRed,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = user.bio ?: "Pune Helper ready to support",
                            fontSize = 12.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Quick Board
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Reputation Points
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = CozyCard)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Stars, contentDescription = null, tint = PrimaryGold, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${user.reputationPoints}",
                                        color = TextLight,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Rep Points",
                                        color = TextMuted,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Coin Balance
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = CozyCard)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Token, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${user.coinBalance}",
                                        color = TextLight,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Loop Tokens",
                                        color = TextMuted,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Contributions / Created needs
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = CozyCard)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.DoneAll, contentDescription = null, tint = ValidGreen, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${userWishes.size}",
                                        color = TextLight,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Broadcasts",
                                        color = TextMuted,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Hyperlocal Location Settings Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("hyperlocal_location_settings_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CozyCard),
                    border = BorderStroke(1.dp, PrimaryAmber.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "📍 Hyperlocal Neighborhood Settings",
                            fontWeight = FontWeight.Bold,
                            color = PrimaryAmber,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Customize your approximate city, campus, and county zones. We use this to curate nearby activities with absolute privacy.",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Current settings board
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1.5f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationCity, contentDescription = null, tint = PrimaryGold, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("City: ${user.city}", color = TextLight, fontSize = 12.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.School, contentDescription = null, tint = PrimaryGold, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Campus: ${user.college}", color = TextLight, fontSize = 12.sp)
                                }
                            }
                            Column(modifier = Modifier.weight(1.5f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Map, contentDescription = null, tint = PrimaryGold, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Area: ${user.area}", color = TextLight, fontSize = 12.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.HomeWork, contentDescription = null, tint = PrimaryGold, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Locality: ${user.locality}", color = TextLight, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { showLocationDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CozyCard.copy(alpha = 0.5f)),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Icon(Icons.Default.EditLocationAlt, contentDescription = null, tint = PrimaryAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Modify Hyperlocal Setup", color = TextLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Earn Coins Interactive Simulator
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CozyCard),
                    border = BorderStroke(1.dp, PrimaryAmber.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎁 Pune Loop Rewards Hub",
                            fontWeight = FontWeight.Bold,
                            color = PrimaryAmber,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add simulator cash to unlock premium verifications, build credibility, and inspire trust in your requests.",
                            fontSize = 11.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.addSimulatedCoins(400) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAmber),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "add", tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Claim +400 Loop Tokens (Daily Check-in)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Trust Verification & Badger Level shop
            item {
                Text(
                    text = "Request Trust badge level",
                    fontWeight = FontWeight.Bold,
                    color = TextLight,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = "Trust Icon", tint = PrimaryGold, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Your Rank: " + when (user.verificationLevel) {
                                    1 -> "Silver Tier Helper"
                                    2 -> "Gold Tier Coordinator"
                                    3 -> "Cosmic Platinum Specialist"
                                    else -> "Basic Community Account"
                                },
                                fontWeight = FontWeight.Bold,
                                color = TextLight,
                                fontSize = 13.sp
                            )
                        }

                        Divider(color = CozyCard, thickness = 1.dp)

                        // Tier Upgrades
                        val tiers = listOf(
                            Triple(1, "Silver Verification Badge", "Unlocks Silver badge on requests • Cost: 200 Coins"),
                            Triple(2, "Gold Coordinator Badge", "Unlocks Gold badge + priority feed sorting • Cost: 400 Coins"),
                            Triple(3, "Cosmic Specialist Seal", "Platinum badge + official regional moderator label • Cost: 700 Coins")
                        )

                        tiers.forEach { (level, title, desc) ->
                            val isUnlocked = user.isVerified && user.verificationLevel >= level
                            val isNext = level == user.verificationLevel + 1
                            val cost = when (level) {
                                1 -> 200
                                2 -> 400
                                3 -> 700
                                else -> 0
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isUnlocked) ValidGreen.copy(alpha = 0.05f) else Color.Transparent)
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        color = if (isUnlocked) ValidGreen else TextLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = desc,
                                        color = TextMuted,
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp
                                    )
                                }

                                if (isUnlocked) {
                                    Icon(Icons.Default.Check, contentDescription = "Unlocked", tint = ValidGreen)
                                } else {
                                    Button(
                                        onClick = { viewModel.buyVerificationUpgrade(level) },
                                        enabled = isNext && user.coinBalance >= cost,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = PrimaryAmber,
                                            disabledContainerColor = CozyCard
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text(
                                            text = "Buy",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isNext && user.coinBalance >= cost) Color.Black else TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section: Reputation Badges & Achievements
            item {
                Text(
                    text = "🏆 Achievements & Badges",
                    fontWeight = FontWeight.Bold,
                    color = TextLight,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepSurface),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PrimaryAmber.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = PrimaryAmber, modifier = Modifier.size(22.dp))
                            }
                            Column {
                                Text(
                                    text = "Elite Pune Pioneer",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Granted to contributors maintaining >100 rep points.",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Divider(color = CozyCard)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ValidGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Favorite, contentDescription = null, tint = ValidGreen, modifier = Modifier.size(22.dp))
                            }
                            Column {
                                Text(
                                    text = "Super Community Connector",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Awarded for participating actively in community loops.",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            // Section: My Joined Active Loops
            item {
                Text(
                    text = "🏡 My Active Neighborhood Loops",
                    fontWeight = FontWeight.Bold,
                    color = TextLight,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                if (joinedLoops.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CozyCard.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "You haven't joined any Loops yet.",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap the 'Join' button inside target Loops on your Home feed to synchronize with local circle projects.",
                                color = TextMuted.copy(alpha = 0.7f),
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DeepSurface),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            joinedLoops.forEach { loopName ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(ValidGreen)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = loopName,
                                            color = TextLight,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }

                                    Button(
                                        onClick = { viewModel.toggleJoinLoop(loopName) },
                                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.15f), contentColor = ErrorRed),
                                        modifier = Modifier.height(26.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Leave", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Modern Visual Activity History Timeline Card
            item {
                Text(
                    text = "Activity History & Audit Trails",
                    fontWeight = FontWeight.Bold,
                    color = TextLight,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepSurface),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val activities = listOf(
                            Triple("Completed Identity Verification Audit", "AI Security Hub - Cleared", ValidGreen),
                            Triple("Acquired First Reputation Badge Tokens", "Pune Regional Coordinator", PrimaryGold),
                            Triple("Registered Safe Pune Emergency Location", "Assigned: Shivaji Nagar Zone", PrimaryAmber),
                            Triple("Initial Platform Registration Completed", "Joined AidLoop India", TextMuted)
                        )

                        activities.forEachIndexed { idx, (actTitle, actDetail, actColor) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(20.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(actColor)
                                    )
                                    if (idx < activities.size - 1) {
                                        Box(
                                            modifier = Modifier
                                                .width(1.5.dp)
                                                .height(34.dp)
                                                .background(Color.White.copy(alpha = 0.15f))
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = actTitle,
                                        color = TextLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        lineHeight = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = actDetail,
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // My posted needs feed preview
            item {
                Text(
                    text = "My Community Posts (${userWishes.size})",
                    fontWeight = FontWeight.Bold,
                    color = TextLight,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                if (userWishes.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DeepSurface)
                    ) {
                        Text(
                            text = "You haven't published any community posts yet.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }
            }

            items(userWishes.size) { index ->
                val wish = userWishes[index]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepSurface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = wish.category.uppercase(),
                                color = PrimaryAmber,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = wish.fulfillStatus,
                                color = if (wish.fulfillStatus == "FULFILLED") ValidGreen else TextLight,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = wish.title,
                            color = TextLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
