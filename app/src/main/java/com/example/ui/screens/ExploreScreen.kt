package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.*
import com.example.ui.viewmodel.IcchaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Nearby People mock model for active presence
data class ActiveNearbyUser(
    val id: String,
    val name: String,
    val role: String,
    val distance: String,
    val isOnline: Boolean,
    val isTyping: Boolean,
    val avatar: String,
    val currentWishId: Int? = null,
    val currentWishTitle: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: IcchaViewModel,
    onNavigateToDetail: (Int) -> Unit
) {
    val currentUser by viewModel.currentUserState.collectAsState()
    val isPrivateMode by viewModel.isLocationPrivacyEnabled.collectAsState()
    val joinedLoops by viewModel.joinedLoops.collectAsState()
    val allWishes by viewModel.allWishesState.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showGPSPermissionDialog by remember { mutableStateOf(false) }
    var isCheckingGPS by remember { mutableStateOf(false) }
    var showQuickChatDialog by remember { mutableStateOf<ActiveNearbyUser?>(null) }
    var quickChatMessageText by remember { mutableStateOf("") }
    
    // Switch Location states
    var showSwitchCityDialog by remember { mutableStateOf(false) }

    // Nearby people pre-set based on selected city
    val activePeople = remember(currentUser?.city) {
        val city = currentUser?.city ?: "Pune"
        when {
            city.contains("Pune", ignoreCase = true) -> listOf(
                ActiveNearbyUser("user_priya", "Dr. Priya Deshmukh", "Clinician • Medical Advice Group", "1.2 km away", true, false, "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=150&q=80", 1, " ऑक्सीजन Cylinder backup portal supply"),
                ActiveNearbyUser("user_rajesh", "Rajesh G.", "Engineer • Tablet Donor Circle", "0.4 km away", true, true, "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=150&q=80", 2, "Digital mobile coding for Kothrud youngsters"),
                ActiveNearbyUser("user_reema", "Reema Sen", "Food Connect Organizer", "2.8 km away", false, false, "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=150&q=80", 3, "Logistics coordinator for wedding catering surplus")
            )
            city.contains("Varanasi", ignoreCase = true) || city.contains("BHU", ignoreCase = true) -> listOf(
                ActiveNearbyUser("bhu_amit", "Amit Sharma", "Lanka Tech Coordinator", "0.2 km away", true, true, "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=150&q=80"),
                ActiveNearbyUser("bhu_sneha", "Sneha Mishra", "Literature Student Lead", "0.7 km away", true, false, "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80"),
                ActiveNearbyUser("bhu_vikram", "Vikram Rathore", "Ghat Cleaner Volunteer", "1.5 km away", false, false, "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?auto=format&fit=crop&w=150&q=80")
            )
            else -> listOf(
                ActiveNearbyUser("bihar_ramesh", "Ramesh Jha", "Darbhanga Youth Circle President", "0.9 km away", true, false, "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=150&q=80"),
                ActiveNearbyUser("bihar_komal", "Komal Singh", "Mithila Creators Advisor", "0.5 km away", true, true, "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=150&q=80")
            )
        }
    }

    // Dynamic suggested loops preset
    val suggestedLoops = remember(currentUser?.city) {
        val city = currentUser?.city ?: "Pune"
        when {
            city.contains("Pune", ignoreCase = true) -> listOf(
                Pair("Pune Startup Loop", "3.4k founders"),
                Pair("Baner Green Loop", "1.8k eco-members"),
                Pair("Kothrud Local Loop", "4.2k active"),
                Pair("Hinjawadi Tech Loop", "3.1k techies")
            )
            city.contains("Varanasi", ignoreCase = true) || city.contains("BHU", ignoreCase = true) -> listOf(
                Pair("BHU Student Loop", "5.8k active scholars"),
                Pair("Assi Ghat Clean Circle", "1.2k volunteers"),
                Pair("Varanasi Food Connect", "850 saviors"),
                Pair("Lanka Tech Innovators", "420 devs")
            )
            else -> listOf(
                Pair("Bihar Student Community", "8.9k members"),
                Pair("Darbhanga Community Circle", "2.1k active"),
                Pair("Mithila Creators Loop", "1.1k artisans"),
                Pair("Patna Youth Network", "4.4k scholars")
            )
        }
    }

    // Interactive custom simulated location triggers
    fun handleLocationAutoDetect() {
        showGPSPermissionDialog = false
        isCheckingGPS = true
        scope.launch {
            delay(1500)
            isCheckingGPS = false
            // Update to a highly tailored mock based on current state or preset
            viewModel.updateUserLocation(
                city = "Pune",
                college = "COEP Tech University",
                area = "Deccan Gymkhana",
                locality = "FC Road Campus"
            )
            viewModel.addLiveUpdate("Auto-detected Deccan Gymkhana campus region via GPS safely!")
            Toast.makeText(context, "Hyperlocal location detected!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.linearGradient(listOf(PrimaryAmber, AccentPurple))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Explore, "explore", tint = Color.Black, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Hyperlocal Explore",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = TextLight
                            )
                            Text(
                                text = "Active Around ${currentUser?.area ?: "Nearby"}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showGPSPermissionDialog = true },
                        modifier = Modifier.testTag("detect_gps_btn")
                    ) {
                        Icon(Icons.Default.MyLocation, "Detect GPS", tint = PrimaryAmber)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepSurface)
            )
        },
        containerColor = DarkGrayBg
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header spacer spacing
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Live Active Badge Ribbon
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = CozyCard.copy(alpha = 0.40f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(ValidGreen)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${activePeople.size * 4} active members nearby",
                                        color = ValidGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(PrimaryAmber.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "LIVE UPDATES",
                                        color = PrimaryAmber,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Current Location: ${currentUser?.locality ?: "Empty"}, ${currentUser?.area ?: "Empty"}, ${currentUser?.city ?: "Pune"}",
                                color = TextLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { showSwitchCityDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAmber),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(Icons.Default.EditLocation, null, tint = Color.Black, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Switch Region", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.toggleLocationPrivacy() },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, if (isPrivateMode) ValidGreen else Color.White.copy(alpha = 0.2f)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPrivateMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = if (isPrivateMode) ValidGreen else TextLight,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isPrivateMode) "Private Mode On" else "Go Private",
                                        color = if (isPrivateMode) ValidGreen else TextLight,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // SIMULATOR LOADER STATUS
                if (isCheckingGPS) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CozyCard)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = PrimaryAmber, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    "Simulating high-precision GPS coordinate polling...",
                                    color = TextLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Canvas Stylized Local Map Grid representation
                item {
                    Text(
                        "🗺️ Live Hyperlocal Radar",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF07040D))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    ) {
                        // Drawing futuristic grid lines in background
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            // Vertical grid lines
                            val gridCount = 8
                            for (i in 1 until gridCount) {
                                val x = w * i / gridCount
                                drawLine(
                                    color = Color.White.copy(alpha = 0.04f),
                                    start = androidx.compose.ui.geometry.Offset(x, 0f),
                                    end = androidx.compose.ui.geometry.Offset(x, h),
                                    strokeWidth = 1f
                                )
                                val y = h * i / gridCount
                                drawLine(
                                    color = Color.White.copy(alpha = 0.04f),
                                    start = androidx.compose.ui.geometry.Offset(0f, y),
                                    end = androidx.compose.ui.geometry.Offset(w, y),
                                    strokeWidth = 1f
                                )
                            }
                            // Radar waves starting from center
                            drawCircle(
                                color = PrimaryAmber.copy(alpha = 0.03f),
                                radius = 200f,
                                center = androidx.compose.ui.geometry.Offset(w / 2, h / 2)
                            )
                            drawCircle(
                                color = PrimaryAmber.copy(alpha = 0.06f),
                                radius = 100f,
                                center = androidx.compose.ui.geometry.Offset(w / 2, h / 2)
                            )
                        }

                        // Glowing pulsing center "YOU"
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(PrimaryAmber.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryAmber)
                            )
                        }
                        Text(
                            text = if (isPrivateMode) "You (Approximate)" else "You (FC Road)",
                            color = PrimaryAmber,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(top = 34.dp)
                        )

                        // Floating map avatars matching nearby users
                        activePeople.forEachIndexed { idx, person ->
                            // Calculate simple static asymmetric alignments on radar
                            val alignment = when (idx) {
                                0 -> Alignment.TopStart
                                1 -> Alignment.BottomEnd
                                else -> Alignment.TopEnd
                            }
                            val paddingOffset = when (idx) {
                                0 -> Modifier.padding(start = 24.dp, top = 20.dp)
                                1 -> Modifier.padding(end = 40.dp, bottom = 20.dp)
                                else -> Modifier.padding(end = 24.dp, top = 30.dp)
                            }
                            Box(
                                modifier = Modifier
                                    .align(alignment)
                                    .then(paddingOffset)
                                    .clickable { showQuickChatDialog = person }
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .border(
                                                width = 1.5.dp,
                                                color = if (person.isOnline) ValidGreen else Color.Gray,
                                                shape = CircleShape
                                            )
                                    ) {
                                        AsyncImage(
                                            model = person.avatar,
                                            contentDescription = person.name,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        if (person.isTyping) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(PrimaryGold)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = person.name.substringBefore(" "),
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 1: Active Users Around You
                item {
                    Text(
                        "👥 Active Around You",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(activePeople) { person ->
                            Card(
                                modifier = Modifier
                                    .width(160.dp)
                                    .clip(RoundedCornerShape(14.dp)),
                                colors = CardDefaults.cardColors(containerColor = CozyCard)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(modifier = Modifier.size(46.dp)) {
                                        AsyncImage(
                                            model = person.avatar,
                                            contentDescription = person.name,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                        // Status Dot
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(if (person.isOnline) ValidGreen else Color.Gray)
                                                .border(2.dp, CozyCard, CircleShape)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = person.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = person.distance,
                                        color = PrimaryAmber,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (person.isTyping) "💬 typing..." else person.role,
                                        color = if (person.isTyping) PrimaryGold else TextMuted,
                                        fontSize = 9.sp,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = { showQuickChatDialog = person },
                                        colors = ButtonDefaults.buttonColors(containerColor = CozyCard.copy(alpha = 0.5f)),
                                        border = BorderStroke(1.dp, PrimaryAmber.copy(alpha = 0.40f)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(28.dp)
                                    ) {
                                        Icon(Icons.Default.Chat, null, tint = PrimaryAmber, modifier = Modifier.size(11.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Chat", color = PrimaryAmber, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 2: Popular Nearby Loops
                item {
                    Text(
                        "🏡 Suggested Loops Near You",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(suggestedLoops) { loop ->
                            val isJoined = joinedLoops.contains(loop.first)
                            Card(
                                modifier = Modifier
                                    .width(180.dp)
                                    .clip(RoundedCornerShape(14.dp)),
                                colors = CardDefaults.cardColors(containerColor = CozyCard)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Groups,
                                            contentDescription = null,
                                            tint = AccentPurple,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (isJoined) ValidGreen else Color.Gray)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = loop.first,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = loop.second,
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = { viewModel.toggleJoinLoop(loop.first) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isJoined) Color(0xFF34C759).copy(alpha = 0.2f) else PrimaryAmber,
                                            contentColor = if (isJoined) Color(0xFF34C759) else Color.Black
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(28.dp)
                                    ) {
                                        Text(
                                            text = if (isJoined) "Joined ✓" else "Join Loop",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 3: Nearby Active Opportunities
                item {
                    Text(
                        "💡 Nearby Opportunities & Advice",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val locationWishes = allWishes.filter { wish ->
                        wish.city.contains(currentUser?.city ?: "Pune", ignoreCase = true)
                    }

                    if (locationWishes.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CozyCard.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "No matching posts inside ${currentUser?.city ?: "this region"} yet! Choose 'Switch Region' or build a new post.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(14.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            locationWishes.take(4).forEach { wish ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToDetail(wish.id) },
                                    colors = CardDefaults.cardColors(containerColor = CozyCard)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(PrimaryAmber.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = when (wish.postType) {
                                                    "REQUEST" -> Icons.Default.Handshake
                                                    "ADVICE" -> Icons.Default.Forum
                                                    "LISTING" -> Icons.Default.Storefront
                                                    else -> Icons.Default.Campaign
                                                },
                                                contentDescription = null,
                                                tint = PrimaryAmber,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = wish.title,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "In ${wish.locality ?: "Nearby"} • ${wish.commentsCount} replies",
                                                color = TextMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Icon(Icons.Default.ChevronRight, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(72.dp)) } // navigation buffer
            }

            // SIMULATED GPS POPUP DIALOG
            if (showGPSPermissionDialog) {
                AlertDialog(
                    onDismissRequest = { showGPSPermissionDialog = false },
                    title = { Text("Request Location Permission 🗺️", color = TextLight, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                    text = {
                        Text(
                            text = "AidLoop will use approximate GPS coordinates to scan active loops, trending discussions and nearby users in real-time. Exact coordinates are never exposed.",
                            color = TextLight,
                            fontSize = 12.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { handleLocationAutoDetect() },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAmber)
                        ) {
                            Text("Grant & Detect", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showGPSPermissionDialog = false }) {
                            Text("Cancel", color = TextMuted)
                        }
                    },
                    containerColor = CozyCard
                )
            }

            // REGION SWITCH DISPATCH MODE
            if (showSwitchCityDialog) {
                AlertDialog(
                    onDismissRequest = { showSwitchCityDialog = false },
                    title = { Text("Switch Active Region", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                Triple("Pune", "COEP Tech Campus", "Kothrud"),
                                Triple("Varanasi", "Banaras Hindu University", "Lanka"),
                                Triple("Darbhanga Bihar", "LNMU Campus", "Kathalbari"),
                                Triple("Patna Bihar", "Patna University", "Boring Road")
                            ).forEach { locationSet ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.updateUserLocation(
                                                city = locationSet.first,
                                                college = locationSet.second,
                                                area = locationSet.third,
                                                locality = "${locationSet.third} Gate"
                                            )
                                            viewModel.addLiveUpdate("Switched location to ${locationSet.first} (${locationSet.second})!")
                                            showSwitchCityDialog = false
                                        },
                                    colors = CardDefaults.cardColors(containerColor = CozyCard.copy(alpha = 0.5f)),
                                    border = BorderStroke(1.dp, if (currentUser?.city == locationSet.first) PrimaryAmber else Color.Transparent)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(locationSet.first, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("${locationSet.second} • ${locationSet.third}", color = TextMuted, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showSwitchCityDialog = false }) {
                            Text("Done", color = PrimaryAmber)
                        }
                    },
                    containerColor = DeepSurface
                )
            }

            // QUICK CHAT POPUP WITH ACTIVE USERS
            showQuickChatDialog?.let { person ->
                AlertDialog(
                    onDismissRequest = { showQuickChatDialog = null },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(34.dp)) {
                                AsyncImage(
                                    model = person.avatar,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(ValidGreen)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(person.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(person.role, color = TextMuted, fontSize = 9.sp)
                            }
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (person.currentWishId != null) {
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        showQuickChatDialog = null
                                        viewModel.selectWish(person.currentWishId)
                                    },
                                    colors = CardDefaults.cardColors(containerColor = PrimaryAmber.copy(alpha = 0.08f)),
                                    border = BorderStroke(1.dp, PrimaryAmber.copy(alpha = 0.15f))
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("Active Post by ${person.name.substringBefore(" ")}:", color = PrimaryAmber, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text(person.currentWishTitle ?: "", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                        Text("Tap to view details & comment", color = TextMuted, fontSize = 9.sp)
                                    }
                                }
                            }

                            Text("Send a quick helper message to start a local direct chat:", color = TextMuted, fontSize = 11.sp)
                            OutlinedTextField(
                                value = quickChatMessageText,
                                onValueChange = { quickChatMessageText = it },
                                placeholder = { Text("E.g., Hey! I can help you coordinate with this nearby.", color = TextMuted, fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextLight,
                                    unfocusedTextColor = TextLight,
                                    focusedBorderColor = PrimaryAmber,
                                    unfocusedBorderColor = CozyCard
                                )
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (quickChatMessageText.isNotBlank()) {
                                    // Start conversation inside the active post if any, or general index
                                    val targetWishId = person.currentWishId ?: 1
                                    viewModel.quickReplyToWish(targetWishId, quickChatMessageText)
                                    quickChatMessageText = ""
                                    showQuickChatDialog = null
                                    Toast.makeText(context, "Direct message dispatched!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAmber),
                            enabled = quickChatMessageText.isNotBlank()
                        ) {
                            Text("Send Message", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showQuickChatDialog = null }) {
                            Text("Cancel", color = TextMuted)
                        }
                    },
                    containerColor = DeepSurface
                )
            }
        }
    }
}
