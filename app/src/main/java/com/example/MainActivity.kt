package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.firebase.FirebaseManager
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.IcchaViewModel

enum class Screen {
    FEED, CHAT_LIST, EXPLORE, PROFILE, POST, DETAIL, CHAT, GUIDELINES, MODERATION
}

class MainActivity : ComponentActivity() {
    private val viewModel: IcchaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseManager.initialize(applicationContext)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContainer(viewModel)
            }
        }
    }
}

@Composable
fun MainAppContainer(viewModel: IcchaViewModel) {
    var currentScreen by remember { mutableStateOf(Screen.FEED) }
    var previousTabScreen by remember { mutableStateOf(Screen.FEED) } // To remember back location

    val activeWishId by viewModel.selectedWishId.collectAsState()
    val currentUser by viewModel.currentUserState.collectAsState()

    // Synced reactive updates for deep detailing
    LaunchedEffect(activeWishId) {
        if (activeWishId != null) {
            currentScreen = Screen.DETAIL
        }
    }

    if (currentUser == null) {
        // Force User authentication onboarding first
        AuthScreen(
            viewModel = viewModel,
            onAuthSuccess = {
                currentScreen = Screen.FEED
            }
        )
    } else {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isExpanded = maxWidth > 840.dp
            
            // Smart district zone quick selector dialog state flow
            var showZoneDialogBySidebar by remember { mutableStateOf(false) }
            if (showZoneDialogBySidebar) {
                ZoneSelectorDesktopDialog(
                    viewModel = viewModel,
                    onDismiss = { showZoneDialogBySidebar = false }
                )
            }

            if (isExpanded) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkGrayBg)
                ) {
                    // LEFT SIDEBAR Menu
                    DesktopLeftSidebar(
                        currentScreen = currentScreen,
                        viewModel = viewModel,
                        onNavigate = { screen ->
                            viewModel.selectWish(null)
                            currentScreen = screen
                            if (screen == Screen.FEED || screen == Screen.CHAT_LIST || screen == Screen.EXPLORE || screen == Screen.PROFILE) {
                                previousTabScreen = screen
                            }
                        }
                    )
                    
                    // CENTER PORTAL PANEL (Framed design card)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                            .padding(vertical = 12.dp, horizontal = 4.dp)
                    ) {
                        Surface(
                            color = DeepSurface,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            ActiveScreenView(
                                currentScreen = currentScreen,
                                viewModel = viewModel,
                                previousTabScreen = previousTabScreen,
                                onNavigate = { screen ->
                                    currentScreen = screen
                                },
                                onNavigateBack = { screen ->
                                    currentScreen = screen
                                }
                            )
                        }
                    }
                    
                    // RIGHT SIDEBAR Menu (Shown for wide monitors > 1120.dp)
                    if (this@BoxWithConstraints.maxWidth > 1120.dp) {
                        DesktopRightSidebar(
                            viewModel = viewModel,
                            onSwitchZone = { showZoneDialogBySidebar = true }
                        )
                    }
                }
            } else {
                // Portrait Mobile Scaffolding layout with bottom bar
                Scaffold(
                    bottomBar = {
                        if (currentScreen == Screen.FEED || currentScreen == Screen.CHAT_LIST || currentScreen == Screen.EXPLORE || currentScreen == Screen.PROFILE) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Transparent)
                                    .navigationBarsPadding()
                                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                            ) {
                                Surface(
                                    color = DeepSurface.copy(alpha = 0.94f),
                                    shape = RoundedCornerShape(20.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.12f),
                                                Color.White.copy(alpha = 0.04f)
                                            )
                                        )
                                    ),
                                    shadowElevation = 12.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    NavigationBar(
                                        containerColor = Color.Transparent,
                                        tonalElevation = 0.dp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(72.dp)
                                            .testTag("app_bottom_navigation")
                                    ) {
                                        // Feed tab
                                        NavigationBarItem(
                                            selected = currentScreen == Screen.FEED,
                                            onClick = {
                                                viewModel.selectWish(null)
                                                currentScreen = Screen.FEED
                                                previousTabScreen = Screen.FEED
                                            },
                                            icon = {
                                                Icon(
                                                    imageVector = if (currentScreen == Screen.FEED) Icons.Default.Forum else Icons.Outlined.Forum,
                                                    contentDescription = "Community Feed"
                                                )
                                            },
                                            label = { Text("Feed", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = Color.Black,
                                                selectedTextColor = PrimaryAmber,
                                                indicatorColor = PrimaryAmber,
                                                unselectedIconColor = TextMuted,
                                                unselectedTextColor = TextMuted
                                            ),
                                            modifier = Modifier.testTag("nav_feed_tab")
                                        )

                                        // Chat tab
                                        NavigationBarItem(
                                            selected = currentScreen == Screen.CHAT_LIST,
                                            onClick = {
                                                viewModel.selectWish(null)
                                                currentScreen = Screen.CHAT_LIST
                                                previousTabScreen = Screen.CHAT_LIST
                                            },
                                            icon = {
                                                Box {
                                                    Icon(
                                                        imageVector = if (currentScreen == Screen.CHAT_LIST) Icons.Default.Chat else Icons.Outlined.Chat,
                                                        contentDescription = "Messaging Hub"
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.TopEnd)
                                                            .offset(x = 6.dp, y = (-4).dp)
                                                            .size(10.dp)
                                                            .clip(CircleShape)
                                                            .background(PrimaryAmber)
                                                            .border(1.dp, DeepSurface, CircleShape)
                                                    )
                                                }
                                            },
                                            label = { Text("Chat", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = Color.Black,
                                                selectedTextColor = PrimaryAmber,
                                                indicatorColor = PrimaryAmber,
                                                unselectedIconColor = TextMuted,
                                                unselectedTextColor = TextMuted
                                            ),
                                            modifier = Modifier.testTag("nav_chat_tab")
                                        )

                                        // Explore tab
                                        NavigationBarItem(
                                            selected = currentScreen == Screen.EXPLORE,
                                            onClick = {
                                                viewModel.selectWish(null)
                                                currentScreen = Screen.EXPLORE
                                                previousTabScreen = Screen.EXPLORE
                                            },
                                            icon = {
                                                Icon(
                                                    imageVector = if (currentScreen == Screen.EXPLORE) Icons.Default.Explore else Icons.Outlined.Explore,
                                                    contentDescription = "Radar Explore"
                                                )
                                            },
                                            label = { Text("Explore", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = Color.Black,
                                                selectedTextColor = PrimaryAmber,
                                                indicatorColor = PrimaryAmber,
                                                unselectedIconColor = TextMuted,
                                                unselectedTextColor = TextMuted
                                            ),
                                            modifier = Modifier.testTag("nav_explore_tab")
                                        )

                                        // Profile tab
                                        NavigationBarItem(
                                            selected = currentScreen == Screen.PROFILE,
                                            onClick = {
                                                viewModel.selectWish(null)
                                                currentScreen = Screen.PROFILE
                                                previousTabScreen = Screen.PROFILE
                                            },
                                            icon = {
                                                Icon(
                                                    imageVector = if (currentScreen == Screen.PROFILE) Icons.Default.Person else Icons.Outlined.Person,
                                                    contentDescription = "Profile"
                                                )
                                            },
                                            label = { Text("Profile", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = Color.Black,
                                                selectedTextColor = PrimaryAmber,
                                                indicatorColor = PrimaryAmber,
                                                unselectedIconColor = TextMuted,
                                                unselectedTextColor = TextMuted
                                            ),
                                            modifier = Modifier.testTag("nav_profile_tab")
                                        )
                                    }
                                }
                            }
                        }
                    },
                    containerColor = DarkGrayBg
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        ActiveScreenView(
                            currentScreen = currentScreen,
                            viewModel = viewModel,
                            previousTabScreen = previousTabScreen,
                            onNavigate = { screen ->
                                currentScreen = screen
                            },
                            onNavigateBack = { screen ->
                                currentScreen = screen
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveScreenView(
    currentScreen: Screen,
    viewModel: IcchaViewModel,
    previousTabScreen: Screen,
    onNavigate: (Screen) -> Unit,
    onNavigateBack: (Screen) -> Unit
) {
    when (currentScreen) {
        Screen.FEED -> {
            FeedScreen(
                viewModel = viewModel,
                onNavigateToDetail = { wishId ->
                    viewModel.selectWish(wishId)
                    onNavigate(Screen.DETAIL)
                },
                onNavigateToGuidelines = {
                    onNavigate(Screen.GUIDELINES)
                },
                onNavigateToModeration = {
                    onNavigate(Screen.MODERATION)
                },
                onNavigateToPost = {
                    onNavigate(Screen.POST)
                }
            )
        }
        Screen.CHAT_LIST -> {
            ChatTabScreen(
                viewModel = viewModel,
                onNavigateToChat = { wishId ->
                    viewModel.selectWish(wishId)
                    onNavigate(Screen.CHAT)
                }
            )
        }
        Screen.EXPLORE -> {
            ExploreScreen(
                viewModel = viewModel,
                onNavigateToDetail = { wishId ->
                    viewModel.selectWish(wishId)
                    onNavigate(Screen.DETAIL)
                }
            )
        }
        Screen.POST -> {
            PostScreen(
                viewModel = viewModel,
                onSuccess = {
                    onNavigate(Screen.FEED)
                }
            )
        }
        Screen.PROFILE -> {
            ProfileScreen(viewModel = viewModel)
        }
        Screen.DETAIL -> {
            WishDetailScreen(
                viewModel = viewModel,
                onBack = {
                    viewModel.selectWish(null)
                    onNavigateBack(previousTabScreen)
                },
                onNavigateToChat = {
                    onNavigate(Screen.CHAT)
                }
            )
        }
        Screen.CHAT -> {
            ChatScreen(
                viewModel = viewModel,
                onBack = {
                    onNavigate(Screen.DETAIL)
                }
            )
        }
        Screen.GUIDELINES -> {
            GuidelinesScreen(
                onBack = {
                    onNavigate(Screen.FEED)
                }
            )
        }
        Screen.MODERATION -> {
            ModerationScreen(
                viewModel = viewModel,
                onBack = {
                    onNavigate(Screen.FEED)
                }
            )
        }
    }
}

@Composable
fun RowScope.DesktopLeftSidebar(
    currentScreen: Screen,
    viewModel: IcchaViewModel,
    onNavigate: (Screen) -> Unit
) {
    val currentUser by viewModel.currentUserState.collectAsState()
    val scrollState = rememberScrollState()
    val globalNavCategory = viewModel.globalNavCategory
    
    Surface(
        modifier = Modifier
            .width(260.dp)
            .fillMaxHeight()
            .padding(12.dp),
        color = DeepSurface.copy(alpha = 0.85f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Spark Logo Header 
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.horizontalGradient(listOf(PrimaryAmber, AccentPurple))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Loop,
                            contentDescription = "AidLoop",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "AidLoop",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Hyperlocal Social",
                            color = TextMuted,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Profile Capsule Card
                currentUser?.let { user ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(containerColor = CozyCard.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AsyncImage(
                                model = user.profilePicUrl,
                                contentDescription = "Profile Pic",
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, PrimaryAmber, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.fullName,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "@${user.username}",
                                    color = TextMuted,
                                    fontSize = 8.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            
                            // Spark reputation stars
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PrimaryAmber.copy(alpha = 0.15f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = PrimaryGold,
                                        modifier = Modifier.size(8.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${user.reputationPoints}",
                                        color = PrimaryGold,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.05f))

                // Menu items scroll
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val isFeedActive = currentScreen == Screen.FEED && globalNavCategory == "Nearby"
                    SidebarNavigationRow(
                        icon = Icons.Default.Forum,
                        label = "Community Feed",
                        active = isFeedActive,
                        onClick = {
                            viewModel.globalNavCategory = "Nearby"
                            onNavigate(Screen.FEED)
                        }
                    )

                    val isDiscussionActive = currentScreen == Screen.FEED && globalNavCategory == "Discussions"
                    SidebarNavigationRow(
                        icon = Icons.Default.ChatBubble,
                        label = "Local Discussions",
                        active = isDiscussionActive,
                        onClick = {
                            viewModel.globalNavCategory = "Discussions"
                            onNavigate(Screen.FEED)
                        }
                    )

                    val isRequestsActive = currentScreen == Screen.FEED && globalNavCategory == "Requests"
                    SidebarNavigationRow(
                        icon = Icons.Default.Handshake,
                        label = "Mutual Aid Requests",
                        active = isRequestsActive,
                        onClick = {
                            viewModel.globalNavCategory = "Requests"
                            onNavigate(Screen.FEED)
                        }
                    )

                    val isListingsActive = currentScreen == Screen.FEED && globalNavCategory == "Listings"
                    SidebarNavigationRow(
                        icon = Icons.Default.Storefront,
                        label = "Local Exchange",
                        active = isListingsActive,
                        onClick = {
                            viewModel.globalNavCategory = "Listings"
                            onNavigate(Screen.FEED)
                        }
                    )

                    val isLoopsActive = currentScreen == Screen.FEED && globalNavCategory == "Loops"
                    SidebarNavigationRow(
                        icon = Icons.Default.Loop,
                        label = "Subscribed Loops",
                        active = isLoopsActive,
                        onClick = {
                            viewModel.globalNavCategory = "Loops"
                            onNavigate(Screen.FEED)
                        }
                    )

                    val isExploreActive = currentScreen == Screen.EXPLORE
                    SidebarNavigationRow(
                        icon = Icons.Default.Explore,
                        label = "Location Radar",
                        active = isExploreActive,
                        onClick = { onNavigate(Screen.EXPLORE) }
                    )

                    val isChatActive = currentScreen == Screen.CHAT_LIST || currentScreen == Screen.CHAT
                    SidebarNavigationRow(
                        icon = Icons.Default.Chat,
                        label = "Conversations & DMs",
                        active = isChatActive,
                        onClick = { onNavigate(Screen.CHAT_LIST) }
                    )

                    val isGuidelinesActive = currentScreen == Screen.GUIDELINES
                    SidebarNavigationRow(
                        icon = Icons.Default.Shield,
                        label = "Rules & Guidelines",
                        active = isGuidelinesActive,
                        onClick = { onNavigate(Screen.GUIDELINES) }
                    )

                    val isModerationActive = currentScreen == Screen.MODERATION
                    SidebarNavigationRow(
                        icon = Icons.Default.AdminPanelSettings,
                        label = "Moderator Panel",
                        active = isModerationActive,
                        onClick = { onNavigate(Screen.MODERATION) }
                    )

                    val isProfileActive = currentScreen == Screen.PROFILE
                    SidebarNavigationRow(
                        icon = Icons.Default.Person,
                        label = "My Profile Hub",
                        active = isProfileActive,
                        onClick = { onNavigate(Screen.PROFILE) }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Post spark action card CTA
                Button(
                    onClick = { onNavigate(Screen.POST) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("sidebar_spark_post_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAmber),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Spark Local Need", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                }
            }
            
            // Logout anchor button at bottom 
            SidebarNavigationRow(
                icon = Icons.Default.Logout,
                label = "Log Out Account",
                active = false,
                onClick = { viewModel.logout() }
            )
        }
    }
}

@Composable
fun SidebarNavigationRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = if (active) PrimaryAmber.copy(alpha = 0.15f) else Color.Transparent,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (active) PrimaryAmber else TextMuted,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                color = if (active) Color.White else TextMuted,
                fontSize = 11.sp,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RowScope.DesktopRightSidebar(
    viewModel: IcchaViewModel,
    onSwitchZone: () -> Unit
) {
    val joinedLoops by viewModel.joinedLoops.collectAsState()
    val liveActivities by viewModel.liveActivities.collectAsState()
    val currentUser by viewModel.currentUserState.collectAsState()
    
    val availableLoops = listOf(
        Pair("Kalyani Nagar Loop", "🏡 Hyperlocal Mutual Aid circle"),
        Pair("FC Road Tech Loop", "💻 Academic & tech gear sharing"),
        Pair("Baner Green Loop", "🌱 Composting & organic food swap"),
        Pair("Pune Medical Response", "🩺 Clinical & emergency supply network")
    )
    
    Surface(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
            .padding(12.dp),
        color = DeepSurface.copy(alpha = 0.85f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Smart Location Privacy Widget
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "SMART LOCATION HUB",
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                )
                
                currentUser?.let { user ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(containerColor = CozyCard.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.NearMe, null, tint = PrimaryAmber, modifier = Modifier.size(11.dp))
                                Text(
                                    text = "${user.locality}, ${user.area}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = "Privacy Shield is ACTIVE. Only sharing approximate distances on our hyperlocal map radar.",
                                color = TextMuted,
                                fontSize = 8.sp,
                                lineHeight = 11.sp
                            )
                            Button(
                                onClick = onSwitchZone,
                                modifier = Modifier.fillMaxWidth().height(26.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CozyCard),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Switch Enclave Zone", color = PrimaryAmber, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            
            Divider(color = Color.White.copy(alpha = 0.05f))

            // Popular loops toggle indicators
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "NEIGHBORHOOD LOOPS",
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    availableLoops.forEach { (name, desc) ->
                        val joined = joinedLoops.contains(name)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CozyCard.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
                                .padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                                Text(
                                    text = name,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = desc,
                                    color = TextMuted,
                                    fontSize = 8.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (joined) ValidGreen.copy(alpha = 0.15f) else PrimaryAmber.copy(alpha = 0.15f))
                                    .clickable { viewModel.toggleJoinLoop(name) }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (joined) "Joined" else "+ Join",
                                    color = if (joined) ValidGreen else PrimaryAmber,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            Divider(color = Color.White.copy(alpha = 0.05f))

            // Realtime Pulse Stream Activities
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "LIVE DISTRICT PULSE",
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    liveActivities.take(3).forEach { act ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CozyCard.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                .padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(ValidGreen)
                            )
                            Text(
                                text = act,
                                color = Color.White,
                                fontSize = 9.sp,
                                lineHeight = 11.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoneSelectorDesktopDialog(
    viewModel: IcchaViewModel,
    onDismiss: () -> Unit
) {
    val currentUser by viewModel.currentUserState.collectAsState()
    var selectedArea by remember { mutableStateOf(currentUser?.area ?: "Baner") }
    var selectedLocality by remember { mutableStateOf(currentUser?.locality ?: "Pancard Club Road") }
    var selectedCollege by remember { mutableStateOf(currentUser?.college ?: "COEP Tech University") }
    
    val areaOptions = listOf("Baner", "Kothrud", "Kalyani Nagar", "Deccan Gymkhana", "Viman Nagar")
    val campusOptions = listOf("COEP Tech University", "Pune University (SPPU)", "Symbiosis Campus", "MIT WPU", "None")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Smart Hyperlocal Zone Settings", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select your primary community zone below. Only approximate distances are used for privacy protection.", color = TextMuted, fontSize = 11.sp, lineHeight = 14.sp)
                
                Text("Pune Neighbourhood Core:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    areaOptions.take(3).forEach { area ->
                        val active = selectedArea == area
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (active) PrimaryAmber else CozyCard)
                                .clickable { selectedArea = area }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(area, color = if (active) Color.Black else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    areaOptions.drop(3).forEach { area ->
                        val active = selectedArea == area
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (active) PrimaryAmber else CozyCard)
                                .clickable { selectedArea = area }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(area, color = if (active) Color.Black else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                Text("Street Sector / landmark:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = selectedLocality,
                    onValueChange = { selectedLocality = it },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 11.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryAmber,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                        focusedContainerColor = CozyCard.copy(alpha = 0.5f),
                        unfocusedContainerColor = CozyCard.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                Text("Active Student Campus:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    campusOptions.take(3).forEach { campus ->
                        val active = selectedCollege == campus
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (active) PrimaryAmber.copy(alpha = 0.12f) else Color.Transparent)
                                .clickable { selectedCollege = campus }
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = active, 
                                onClick = { selectedCollege = campus }, 
                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryAmber, unselectedColor = TextMuted)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(campus, color = Color.White, fontSize = 10.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.updateUserLocation(
                        city = currentUser?.city ?: "Pune",
                        college = selectedCollege,
                        area = selectedArea,
                        locality = selectedLocality
                    )
                    viewModel.addLiveUpdate("District zone locked to $selectedLocality, $selectedArea")
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAmber)
            ) {
                Text("Lock Settings", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted, fontSize = 11.sp)
            }
        },
        containerColor = DeepSurface,
        shape = RoundedCornerShape(16.dp)
    )
}
