package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Comment
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Wish
import com.example.ui.theme.*
import com.example.ui.viewmodel.IcchaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: IcchaViewModel,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToGuidelines: () -> Unit,
    onNavigateToModeration: () -> Unit,
    onNavigateToPost: () -> Unit
) {
    val wishes by viewModel.allWishesState.collectAsState()
    val currentUser by viewModel.currentUserState.collectAsState()
    val blockedUsers by viewModel.blockedUsersState.collectAsState()
    val joinedLoops by viewModel.joinedLoops.collectAsState()

    val blockedIds = blockedUsers.map { it.blockedId }

    var searchQuery by remember { mutableStateOf("") }
    var selectedNavigationFilter by remember { mutableStateOf(viewModel.globalNavCategory) }
    
    // Hyperlocal states
    var showLocationEditDialogOnFeed by remember { mutableStateOf(false) }
    var selectedLocationRangeFilter by remember { mutableStateOf(viewModel.globalLocationRangeFilter) } // "ALL", "SAME_NEIGHBORHOOD", "CAMPUS", "CLOSEBY"

    LaunchedEffect(viewModel.globalNavCategory) {
        selectedNavigationFilter = viewModel.globalNavCategory
    }
    LaunchedEffect(selectedNavigationFilter) {
        viewModel.globalNavCategory = selectedNavigationFilter
    }

    LaunchedEffect(viewModel.globalLocationRangeFilter) {
        selectedLocationRangeFilter = viewModel.globalLocationRangeFilter
    }
    LaunchedEffect(selectedLocationRangeFilter) {
        viewModel.globalLocationRangeFilter = selectedLocationRangeFilter
    }

    // Simulating loading skeletons on filter/search transition
    var isSimulatingLoading by remember { mutableStateOf(false) }
    LaunchedEffect(selectedNavigationFilter, searchQuery, selectedLocationRangeFilter) {
        isSimulatingLoading = true
        kotlinx.coroutines.delay(450)
        isSimulatingLoading = false
    }

    // Dialog state management
    var wishToReport by remember { mutableStateOf<Wish?>(null) }
    var userToBlock by remember { mutableStateOf<Wish?>(null) }
    var selectedReportReason by remember { mutableStateOf("Advance Payment Scam suspicion") }

    val filteredWishes = wishes.filter { wish ->
        // Exclude wishes posted by blocked users
        val isNotBlocked = !blockedIds.contains(wish.creatorId)
        
        val matchesSearch = wish.title.contains(searchQuery, ignoreCase = true) || 
                            wish.content.contains(searchQuery, ignoreCase = true) ||
                            (wish.location?.contains(searchQuery, ignoreCase = true) == true)
        
        val matchesNavigation = when (selectedNavigationFilter) {
            "Nearby" -> true
            "Discussions" -> wish.postType == "ADVICE" || wish.postType == "UPDATE" || wish.category.equals("Community", ignoreCase = true) || wish.category.equals("Discussions", ignoreCase = true)
            "Requests" -> wish.postType == "REQUEST" || wish.category.equals("Support", ignoreCase = true) || wish.category.equals("Emergency", ignoreCase = true)
            "Listings" -> wish.postType == "LISTING" || wish.postType == "OFFER"
            "Loops" -> wish.loopName != "General"
            else -> true
        }

        isNotBlocked && matchesSearch && matchesNavigation
    }

    // 1. Filter by location radius (hyperlocal filters)
    val locationFilteredWishes = remember(filteredWishes, selectedLocationRangeFilter, currentUser) {
        val userLocal = currentUser
        if (userLocal == null) {
            filteredWishes
        } else {
            filteredWishes.filter { wish ->
                when (selectedLocationRangeFilter) {
                    "ALL" -> true
                    "SAME_NEIGHBORHOOD" -> wish.area.equals(userLocal.area, ignoreCase = true)
                    "CAMPUS" -> wish.college.equals(userLocal.college, ignoreCase = true) && userLocal.college != "None"
                    "CLOSEBY" -> estimateDistanceKm(userLocal.area, wish.area) <= 4.0
                    else -> true
                }
            }
        }
    }

    // 2. Prioritize showing relevant nearby activities (distance sorts: campus -> neighborhood -> nearest)
    val prioritizedWishes = remember(locationFilteredWishes, currentUser) {
        val userLocal = currentUser
        if (userLocal == null) {
            locationFilteredWishes
        } else {
            locationFilteredWishes.sortedWith(compareBy<Wish> { wish ->
                // Sort by campus first
                if (wish.college.equals(userLocal.college, ignoreCase = true) && userLocal.college != "None") 0 else 1
            }.thenBy { wish ->
                // Sort by same neighborhood second
                if (wish.area.equals(userLocal.area, ignoreCase = true)) 0 else 1
            }.thenBy { wish ->
                // Sort by distance in km ascending
                estimateDistanceKm(userLocal.area, wish.area)
            })
        }
    }

    // Report Dialog popup
    if (wishToReport != null) {
        val reasons = listOf(
            "Advance Payment Scam suspicion",
            "Spam / Commercial advertising",
            "Duplicate / Fake threat alert",
            "Abusive or Offensive content outside Pune rules"
        )
        AlertDialog(
            onDismissRequest = { wishToReport = null },
            title = { Text("Report Community Need", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column {
                    Text("Select a safety reason to flag this request to Pune AidLoop Staff:", color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    reasons.forEach { r ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedReportReason = r }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = selectedReportReason == r,
                                onClick = { selectedReportReason = r },
                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryAmber)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(r, color = TextLight, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        wishToReport?.let { viewModel.reportRequest(it.id, selectedReportReason) }
                        wishToReport = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Flag / Report", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { wishToReport = null }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = DeepSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Block Confirmation Dialog
    if (userToBlock != null) {
        AlertDialog(
            onDismissRequest = { userToBlock = null },
            title = { Text("Block @${userToBlock?.creatorName}?", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = {
                Text(
                    text = "You will instantly hide all current and future community requests, reports, and real-time chat messages from this creator.",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        userToBlock?.let { viewModel.blockUser(it.creatorId) }
                        userToBlock = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Block Creator", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToBlock = null }) {
                    Text("Go Back", color = TextMuted)
                }
            },
            containerColor = DeepSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Dialog for on-the-fly Feed Location updating
    if (showLocationEditDialogOnFeed && currentUser != null) {
        val user = currentUser!!
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
            onDismissRequest = { showLocationEditDialogOnFeed = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EditLocationAlt, contentDescription = null, tint = PrimaryAmber)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Hyperlocal Zone", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
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
                        showLocationEditDialogOnFeed = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAmber)
                ) {
                    Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationEditDialogOnFeed = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = DeepSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    var showNotificationDialog by remember { mutableStateOf(false) }

    if (showNotificationDialog) {
        val simulatedNotifications = listOf(
            "🔔 Rohan commented on Deccan Gymkhana clean drive",
            "🤝 Neha accepted your book sharing request",
            "⚡ Urgent medicine request posted 1.2km from you",
            "🏆 Earned +10 reputation for verifying a local water supply issue"
        )
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            title = { Text("Local Activity notifications", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    simulatedNotifications.forEach { notif ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CozyCard, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = notif,
                                color = TextLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showNotificationDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAmber)
                ) {
                    Text("OK", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DeepSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(DeepSurface)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Sleek, Compact One-Line Top Bar Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    // Logo and Small Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Brush.horizontalGradient(listOf(PrimaryAmber, AccentPurple))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AllInclusive,
                                contentDescription = "Logo",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "AidLoop",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = TextLight,
                            letterSpacing = 0.2.sp
                        )
                    }

                    // Compact Inline Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search feed...", color = TextMuted, fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted, modifier = Modifier.size(12.dp)) },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .testTag("wish_search_bar"),
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryAmber.copy(alpha = 0.5f),
                            unfocusedBorderColor = CozyCard,
                            focusedContainerColor = CozyCard,
                            unfocusedContainerColor = CozyCard,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        ),
                        singleLine = true
                    )

                    // Compact Utility Controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Reputation Points Mini Badge
                        currentUser?.let { user ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PrimaryAmber.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stars,
                                    contentDescription = "Reputation Points",
                                    tint = PrimaryGold,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${user.reputationPoints}",
                                    color = PrimaryGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        // Notifications Icon button with Alert indicator
                        IconButton(
                            onClick = { showNotificationDialog = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = TextLight,
                                    modifier = Modifier.size(16.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryAmber)
                                )
                            }
                        }

                        // Guidelines Shortcut Icon Button
                        IconButton(
                            onClick = onNavigateToGuidelines,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("btn_safety_guidelines")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Safety Guidelines",
                                tint = PrimaryAmber,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        // Moderation Portal Shortcut Icon Button
                        IconButton(
                            onClick = onNavigateToModeration,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("btn_moderation_portal")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Moderation Portal",
                                tint = PrimaryGold,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable Category Filters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val navigationCategories = listOf("Nearby", "Discussions", "Requests")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(navigationCategories) { cat ->
                            val isSelected = selectedNavigationFilter == cat
                            val icon = when (cat) {
                                "Nearby" -> Icons.Default.NearMe
                                "Discussions" -> Icons.Default.Forum
                                "Requests" -> Icons.Default.Handshake
                                else -> Icons.Default.AllInclusive
                            }
                            val badgeColor = when (cat) {
                                "Nearby" -> Color(0xFF4CAF50)
                                "Discussions" -> Color(0xFF2196F3)
                                "Requests" -> ErrorRed
                                else -> TextLight
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PrimaryAmber else CozyCard.copy(alpha = 0.5f))
                                    .clickable { selectedNavigationFilter = cat }
                                    .border(
                                        BorderStroke(
                                            1.dp,
                                            if (isSelected) PrimaryAmber else CozyCard.copy(alpha = 0.3f)
                                        ),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                    .testTag("filter_chip_$cat")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = cat,
                                        tint = if (isSelected) Color.Black else badgeColor,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = cat,
                                        color = if (isSelected) Color.Black else TextLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // A premium mini filter toggle button to access Listings, Loops and Location filters
                    Box {
                        var showMoreFiltersDropdown by remember { mutableStateOf(false) }
                        val isSecondaryFilterActive = selectedNavigationFilter == "Listings" || 
                                                     selectedNavigationFilter == "Loops" || 
                                                     selectedLocationRangeFilter != "ALL"

                        IconButton(
                            onClick = { showMoreFiltersDropdown = true },
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSecondaryFilterActive) AccentPurple.copy(alpha = 0.2f) else CozyCard.copy(alpha = 0.5f))
                                .border(1.dp, if (isSecondaryFilterActive) AccentPurple else CozyCard.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .testTag("btn_more_filters")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Advanced Filters",
                                tint = if (isSecondaryFilterActive) AccentPurple else TextLight,
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMoreFiltersDropdown,
                            onDismissRequest = { showMoreFiltersDropdown = false },
                            modifier = Modifier
                                .background(DeepSurface)
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        ) {
                            // Sub-Feeds section header
                            Text(
                                "SUB-FEEDS",
                                color = TextMuted,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )

                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.Storefront, null, tint = Color(0xFFFF9800), modifier = Modifier.size(12.dp))
                                        Text("📦 Community Exchange / Listings", color = Color.White, fontSize = 11.sp)
                                    }
                                },
                                onClick = {
                                    selectedNavigationFilter = "Listings"
                                    showMoreFiltersDropdown = false
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.Loop, null, tint = Color(0xFF9C27B0), modifier = Modifier.size(12.dp))
                                        Text("⭕ Subscribed Neighborhood Loops", color = Color.White, fontSize = 11.sp)
                                    }
                                },
                                onClick = {
                                    selectedNavigationFilter = "Loops"
                                    showMoreFiltersDropdown = false
                                }
                            )

                            Divider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                            // Local Range section header
                            Text(
                                "LOCATION RADAR",
                                color = TextMuted,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )

                            val radiusOptions = listOf(
                                Pair("ALL", "🌍 All Pune"),
                                Pair("SAME_NEIGHBORHOOD", "🏡 My Neighborhood"),
                                Pair("CAMPUS", "🎓 Campus Only"),
                                Pair("CLOSEBY", "📍 Nearby (< 4km)")
                            )

                            radiusOptions.forEach { pair ->
                                val isSelectedRadius = selectedLocationRangeFilter == pair.first
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(pair.second, color = if (isSelectedRadius) PrimaryAmber else Color.White, fontSize = 11.sp)
                                            if (isSelectedRadius) {
                                                Icon(Icons.Default.Check, null, tint = PrimaryAmber, modifier = Modifier.size(10.dp))
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedLocationRangeFilter = pair.first
                                        showMoreFiltersDropdown = false
                                    }
                                )
                            }

                            Divider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                            // Settings/Edit location
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.EditLocation, null, tint = PrimaryAmber, modifier = Modifier.size(12.dp))
                                        Text("Edit Home Location/Campus", color = PrimaryAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                },
                                onClick = {
                                    showMoreFiltersDropdown = false
                                    showLocationEditDialogOnFeed = true
                                }
                            )
                        }
                    }
                }

                // If secondary filter is chosen, show a premium inline chip to easily clear it
                val isSecondaryNavActive = selectedNavigationFilter == "Listings" || selectedNavigationFilter == "Loops"
                val isRadiusActive = selectedLocationRangeFilter != "ALL"
                if (isSecondaryNavActive || isRadiusActive) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(AccentPurple.copy(alpha = 0.08f))
                            .border(BorderStroke(1.dp, AccentPurple.copy(alpha = 0.15f)), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Info, null, tint = AccentPurple, modifier = Modifier.size(10.dp))
                            val filterLabelText = buildString {
                                if (isSecondaryNavActive) append("Feed: $selectedNavigationFilter ")
                                if (isRadiusActive) {
                                    if (isNotEmpty()) append("• ")
                                    val radiusText = when (selectedLocationRangeFilter) {
                                        "SAME_NEIGHBORHOOD" -> "My Neighborhood"
                                        "CAMPUS" -> "My Campus"
                                        "CLOSEBY" -> "Nearby (< 4km)"
                                        else -> ""
                                    }
                                    append(radiusText)
                                }
                            }
                            Text(
                                text = filterLabelText,
                                color = AccentPurple,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Reset ✕",
                            color = PrimaryAmber,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier
                                .clickable {
                                    if (isSecondaryNavActive) selectedNavigationFilter = "Nearby"
                                    selectedLocationRangeFilter = "ALL"
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToPost,
                containerColor = PrimaryAmber.copy(alpha = 0.85f),
                contentColor = Color.Black,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp,
                    hoveredElevation = 3.dp,
                    focusedElevation = 3.dp
                ),
                shape = RoundedCornerShape(14.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = "New Post", modifier = Modifier.size(16.dp)) },
                text = { Text("New Post", fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 0.4.sp) },
                modifier = Modifier
                    .padding(bottom = 76.dp)
                    .height(38.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                    .testTag("floating_ask_help_btn")
            )
        },
        containerColor = DarkGrayBg
    ) { paddingValues ->
        if (isSimulatingLoading) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(3) {
                    SkeletonCard()
                }
            }
        } else if (filteredWishes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AllInclusive,
                        contentDescription = "Empty",
                        tint = TextMuted.copy(alpha = 0.3f),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Requests Found",
                        color = TextLight,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Be the first to post a community need in Pune or surrounding cities!",
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Ticker ribbon of active nearby notifications
                item {
                    val liveMessageStream by viewModel.liveActivities.collectAsState()
                    var tickerIndex by remember { mutableStateOf(0) }
                    
                    LaunchedEffect(liveMessageStream) {
                        while (true) {
                            kotlinx.coroutines.delay(4500)
                            if (liveMessageStream.isNotEmpty()) {
                                tickerIndex = (tickerIndex + 1) % liveMessageStream.size
                            }
                        }
                    }
                    val currentMsg = if (liveMessageStream.isNotEmpty() && tickerIndex < liveMessageStream.size) {
                        liveMessageStream[tickerIndex]
                    } else {
                        "Radar actively scanning Pune Deccan Gymkhana..."
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .testTag("live_activity_ticker_ribbon"),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = AccentPurple.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, AccentPurple.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF34C759))
                            )
                            Icon(Icons.Default.Campaign, null, tint = AccentPurple, modifier = Modifier.size(11.dp))
                            
                            AnimatedContent(
                                targetState = currentMsg,
                                transitionSpec = {
                                    slideInVertically { h -> h } + fadeIn() togetherWith
                                            slideOutVertically { h -> -h } + fadeOut()
                                },
                                label = "ticker_fade"
                            ) { targetMsg ->
                                Text(
                                    text = targetMsg,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Mini Elegant Intro Card on "Nearby" or general stream
                if (selectedNavigationFilter == "Nearby") {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = CozyCard.copy(alpha = 0.35f)),
                            border = BorderStroke(1.dp, PrimaryAmber.copy(alpha = 0.15f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NearMe,
                                        contentDescription = null,
                                        tint = PrimaryAmber,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "Connecting Deccan Gymkhana community",
                                        color = PrimaryAmber,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Connect with nearby people, discussions and opportunities around Pune instantly.",
                                    color = TextLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 15.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                // Compact Inline Stats
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(ValidGreen))
                                        Text("12 Active Helpers", color = ValidGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Icon(Icons.Default.Forum, null, tint = PrimaryGold, modifier = Modifier.size(9.dp))
                                        Text("Kothrud Loop trending", color = PrimaryGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Icon(Icons.Default.Bolt, null, tint = AccentPurple, modifier = Modifier.size(9.dp))
                                        Text("3 Live channels", color = AccentPurple, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Feed Section Header
                item {
                    val streamHeader = when (selectedNavigationFilter) {
                        "Nearby" -> "⚡ Deccan Radar Stream"
                        "Discussions" -> "💬 Trending Discussions"
                        "Requests" -> "🤝 Help Connections"
                        "Listings" -> "📦 Community Exchange"
                        "Loops" -> "⭕ Subscribed Loops"
                        else -> "⚡ Deccan Radar Stream"
                    }
                    Text(
                        text = streamHeader,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 13.sp,
                        letterSpacing = 0.2.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }

                // In-Feed Stream Items + Natural Community Organic Insertions!
                itemsIndexed(prioritizedWishes) { index, wish ->
                    val creatorUser = viewModel.allUsersState.value.find { it.userId == wish.creatorId }
                    val isSuspicious = creatorUser?.isSuspicious == true

                    AidRequestCard(
                        wish = wish,
                        currentUser = currentUser,
                        onHelpClick = { onNavigateToDetail(wish.id) },
                        onLikeClick = { viewModel.toggleLikeWish(wish.id) },
                        onReportClick = { wishToReport = wish },
                        onBlockClick = { userToBlock = wish },
                        isCreatorSuspicious = isSuspicious,
                        onQuickReply = { draft ->
                            viewModel.quickReplyToWish(wish.id, draft)
                            viewModel.addLiveUpdate("You initiated helper chat shortcut on \"${wish.title}\"")
                        }
                    )

                    // Organic Placement 1: Suggested Local Loops (after first item)
                    if (index == 0 && (selectedNavigationFilter == "Nearby" || selectedNavigationFilter == "Loops")) {
                        Spacer(modifier = Modifier.height(10.dp))
                        SuggestedLoopsOrganicInsertion(joinedLoops, viewModel)
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Organic Placement 2: Trending Discussions (after second item)
                    if (index == 1 && (selectedNavigationFilter == "Nearby" || selectedNavigationFilter == "Discussions")) {
                        Spacer(modifier = Modifier.height(10.dp))
                        TrendingDiscussionsOrganicInsertion()
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Organic Placement 3: Active Nearby Helpers (after fourth item)
                    if (index == 3 && selectedNavigationFilter == "Nearby") {
                        Spacer(modifier = Modifier.height(10.dp))
                        ActivePeopleNearbyOrganicInsertion(viewModel)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SuggestedLoopsOrganicInsertion(
    joinedLoops: Set<String>,
    viewModel: IcchaViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CozyCard.copy(alpha = 0.45f)),
        border = BorderStroke(1.dp, AccentPurple.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("🏡", fontSize = 14.sp)
                    Text(
                        text = "Suggested Loops Near You",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
                Text(
                    text = "Hyperlocal",
                    color = AccentPurple,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(AccentPurple.copy(alpha = 0.15f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            
            val loopsList = listOf(
                com.example.ui.screens.ActiveLoopItem("Kothrud Local Loop", "4.2k active", Icons.Default.Handshake, Color(0xFF9C27B0)),
                com.example.ui.screens.ActiveLoopItem("Baner Green Loop", "1.8k members", Icons.Default.Favorite, Color(0xFF4CAF50)),
                com.example.ui.screens.ActiveLoopItem("Hinjawadi Tech Loop", "3.1k members", Icons.Default.Star, Color(0xFF2196F3))
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(loopsList) { loop ->
                    val isJoined = joinedLoops.contains(loop.name)
                    Box(
                        modifier = Modifier
                            .width(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DeepSurface.copy(alpha = 0.9f))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .clickable { viewModel.toggleJoinLoop(loop.name) }
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(loop.hue.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(loop.icon, null, tint = loop.hue, modifier = Modifier.size(14.dp))
                                }
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(if (isJoined) Color(0xFF34C759) else Color.Gray)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = loop.name,
                                color = TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = loop.stats,
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.toggleJoinLoop(loop.name) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isJoined) Color(0xFF34C759).copy(alpha = 0.15f) else PrimaryAmber,
                                    contentColor = if (isJoined) Color(0xFF34C759) else Color.Black
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp)
                            ) {
                                Text(
                                    text = if (isJoined) "Joined ✓" else "Join",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrendingDiscussionsOrganicInsertion() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CozyCard.copy(alpha = 0.45f)),
        border = BorderStroke(1.dp, PrimaryGold.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("💬", fontSize = 14.sp)
                    Text(
                        text = "Trending Local Debates",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
                Text(
                    text = "Trending",
                    color = PrimaryGold,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(PrimaryGold.copy(alpha = 0.15f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            val conversations = listOf(
                com.example.ui.screens.TrendingTopicItem("Adopt community eco-clearing for Deccan public parks?", 42, "Eco Care"),
                com.example.ui.screens.TrendingTopicItem("Set up coding camps for literacy at Kothrud school?", 19, "Education")
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                conversations.forEach { topic ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DeepSurface.copy(alpha = 0.8f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PrimaryAmber.copy(alpha = 0.1f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(topic.tag, color = PrimaryAmber, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(topic.text, color = TextLight, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Comment, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                            Text("${topic.replies} chats", color = TextMuted, fontSize = 8.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActivePeopleNearbyOrganicInsertion(viewModel: IcchaViewModel) {
    val users by viewModel.allUsersState.collectAsState()
    val activeNearbyUsers = remember(users) {
        users.filter { !it.isSuspicious && it.reputationPoints > 10 }.take(3)
    }
    
    if (activeNearbyUsers.isEmpty()) return
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CozyCard.copy(alpha = 0.45f)),
        border = BorderStroke(1.dp, Color(0xFF34C759).copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("👥", fontSize = 14.sp)
                    Text(
                        text = "Active Helpers Nearby",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF34C759))
                    )
                    Text("12 Online", color = Color(0xFF34C759), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                activeNearbyUsers.forEach { user ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DeepSurface.copy(alpha = 0.8f))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(modifier = Modifier.size(34.dp)) {
                                AsyncImage(
                                    model = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80",
                                    contentDescription = user.username,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .border(1.dp, PrimaryAmber, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF34C759))
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = user.username,
                                color = TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "🏆 ${user.reputationPoints} Rep",
                                color = PrimaryGold,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun AidRequestCard(
    wish: Wish,
    currentUser: com.example.data.model.User?,
    onHelpClick: () -> Unit,
    onLikeClick: () -> Unit,
    onReportClick: () -> Unit,
    onBlockClick: () -> Unit,
    isCreatorSuspicious: Boolean,
    onQuickReply: (String) -> Unit
) {
    val categoryColor = when (wish.category) {
        "Emergency" -> ErrorRed
        "Community" -> Color(0xFF2196F3)
        "Volunteering" -> Color(0xFF4CAF50)
        "Gratitude" -> Color(0xFFFF9800)
        "Support" -> Color(0xFF9C27B0)
        "Loops" -> Color(0xFF00BCD4)
        else -> TextMuted
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("wish_card_${wish.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepSurface.copy(alpha = 0.7f) // Glassmorphic look
        ),
        border = BorderStroke(
            1.dp,
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.08f),
                    Color.White.copy(alpha = 0.01f)
                )
            )
        )
    ) {
        Column {
            // Suspicious user warning banner
            if (isCreatorSuspicious) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ErrorRed.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "⚠️ SUSPICIOUS USER: Suspicious transactions reported, proceed with caution",
                            color = ErrorRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Emergency alert banner
            if (wish.isEmergency) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(ErrorRed.copy(alpha = 0.8f), ErrorRed.copy(alpha = 0.2f))))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CRITICAL EMERGENCY IN PUNECITY",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Expiry banner if request is old or flagged expired
            if (wish.isExpired) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Gray.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HourglassDisabled, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "EXPIRED: This request has expired and is now read-only",
                            color = TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Header: Creator identity, Location, Status, Dropdown
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.size(40.dp)) {
                        AsyncImage(
                            model = wish.creatorImage,
                            contentDescription = "Profile",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(1.dp, PrimaryAmber, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        if (wish.creatorVerified) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(ValidGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Verified Flag",
                                    tint = Color.Black,
                                    modifier = Modifier.size(9.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = wish.creatorName,
                                fontWeight = FontWeight.Bold,
                                color = TextLight,
                                fontSize = 13.sp
                            )
                            if (wish.creatorVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (wish.creatorVerificationLevel == 3) Icons.Default.Stars else Icons.Default.Verified,
                                    contentDescription = "Verified level: ${wish.creatorVerificationLevel}",
                                    tint = when (wish.creatorVerificationLevel) {
                                        2 -> GoldBadge
                                        3 -> PlatinumBadge
                                        else -> ValidGreen
                                    },
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryAmber, modifier = Modifier.size(10.dp))
                            
                            val displayLoc = buildString {
                                if (!wish.locality.isNullOrEmpty() && !wish.area.isNullOrEmpty()) {
                                    append("${wish.locality}, ${wish.area}")
                                } else {
                                    append(wish.location ?: "Pune")
                                }
                                
                                if (!wish.college.isNullOrBlank() && wish.college != "None") {
                                    append(" • ${wish.college}")
                                }
                            }
                            
                            Text(
                                text = displayLoc,
                                color = TextMuted,
                                fontSize = 10.sp,
                                maxLines = 1
                            )

                            if (currentUser != null) {
                                val dist = estimateDistanceKm(currentUser.area, wish.area)
                                val distText = when {
                                    wish.college.equals(currentUser.college, ignoreCase = true) && currentUser.college != "None" -> " • 🎓 On Campus"
                                    wish.area.equals(currentUser.area, ignoreCase = true) -> " • 🏡 Same Neighborhood (~0.5 km)"
                                    else -> " • 📍 ~$dist km away"
                                }
                                Text(
                                    text = distText,
                                    color = if (distText.contains("On Campus") || distText.contains("Same Neighborhood")) PrimaryAmber else TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Options menu dropdown
                    var expandedMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(
                            onClick = { expandedMenu = true },
                            modifier = Modifier.testTag("request_options_${wish.id}")
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = TextMuted)
                        }
                        DropdownMenu(
                            expanded = expandedMenu,
                            onDismissRequest = { expandedMenu = false },
                            modifier = Modifier.background(DeepSurface)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Flag, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Report Need", color = TextLight, fontSize = 11.sp)
                                    }
                                },
                                onClick = {
                                    expandedMenu = false
                                    onReportClick()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Block, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Block Creator", color = TextLight, fontSize = 11.sp)
                                    }
                                },
                                onClick = {
                                    expandedMenu = false
                                    onBlockClick()
                                }
                            )
                        }
                    }

                    if (!wish.isExpired) {
                        Spacer(modifier = Modifier.width(6.dp))
                        // Fulfill Status Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when (wish.fulfillStatus) {
                                        "OPEN" -> Color.Gray.copy(alpha = 0.15f)
                                        "ACCEPTED" -> PrimaryAmber.copy(alpha = 0.15f)
                                        "FULFILLED" -> ValidGreen.copy(alpha = 0.15f)
                                        else -> Color.Gray.copy(alpha = 0.15f)
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = wish.fulfillStatus,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = when (wish.fulfillStatus) {
                                    "OPEN" -> TextMuted
                                    "ACCEPTED" -> PrimaryAmber
                                    "FULFILLED" -> ValidGreen
                                    else -> TextMuted
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Post Badges (Category, Post Type, Loop, Urgency & Expiry) - Simplified to One Primary Tag
                val primaryTagLabel = when {
                    wish.urgencyLevel == "EMERGENCY" -> "🚨 EMERGENCY"
                    wish.urgencyLevel == "URGENT" -> "⚡ URGENT"
                    wish.postType == "REQUEST" -> "🤝 SUPPORT REQUEST"
                    wish.postType == "ADVICE" -> "💡 KNOWLEDGE & DISCUSSION"
                    wish.postType == "LISTING" -> "📦 LOCAL EXCHANGE"
                    wish.postType == "OFFER" -> "🎁 RESOURCE OFFER"
                    wish.postType == "UPDATE" -> "📢 LIVE UPDATE"
                    else -> "📝 ${wish.category.uppercase()}"
                }
                
                val primaryTagColor = when {
                    wish.urgencyLevel == "EMERGENCY" || wish.category == "Emergency" -> ErrorRed
                    wish.urgencyLevel == "URGENT" -> Color(0xFFFF5722)
                    wish.postType == "REQUEST" || wish.category == "Support" -> Color(0xFF9C27B0)
                    wish.postType == "LISTING" -> Color(0xFFFF9800)
                    wish.postType == "OFFER" -> Color(0xFF4CAF50)
                    wish.category == "Community" || wish.category == "Discussions" -> Color(0xFF2196F3)
                    else -> TextMuted
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(primaryTagColor.copy(alpha = 0.08f))
                            .border(1.dp, primaryTagColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = primaryTagLabel,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            color = primaryTagColor,
                            letterSpacing = 0.2.sp
                        )
                    }

                    // Optional tiny loop group indication if not "General", shown as elegant small text
                    if (!wish.loopName.equals("General", ignoreCase = true)) {
                        Text(
                            text = "in 🏡 ${wish.loopName}",
                            color = AccentPurple.copy(alpha = 0.7f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Time left / time ago as clean, minimal trailing text instead of a boxy chip
                    if (wish.expiryTimestamp > 0L) {
                        val hoursLeft = ((wish.expiryTimestamp - System.currentTimeMillis()) / 3600_000L).coerceAtLeast(0L)
                        val isExpired = hoursLeft <= 0
                        Text(
                            text = if (isExpired) "• ⏱️ Expired" else "• ⏱️ ${hoursLeft}h left",
                            color = if (isExpired) TextMuted else Color(0xFF00BCD4),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Post Title
                Text(
                    text = wish.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    lineHeight = 20.sp,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Post Content
                Text(
                    text = wish.content,
                    fontSize = 13.sp,
                    color = Color(0xFFE2E8F0),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 19.sp
                )

                // Optional Image centered like Instagram/Reddit
                if (!wish.mediaUrl.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    AsyncImage(
                        model = wish.mediaUrl,
                        contentDescription = "Attached Need Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, CozyCard, RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Footer Section: Likes/Love tally, and prominent HELP button!
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Interactive Mini-Reactions
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Reaction ❤️ (synthesizes with main Like click)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (wish.userLiked) ErrorRed.copy(alpha = 0.15f) else CozyCard)
                                .clickable { onLikeClick() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "❤️", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = wish.likesCount.toString(),
                                color = if (wish.userLiked) ErrorRed else TextLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Reaction 👏 Claps
                        var clapsCount by remember(wish.id) { mutableStateOf(wish.likesCount / 2 + 1) }
                        var hasClapped by remember(wish.id) { mutableStateOf(false) }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (hasClapped) PrimaryAmber.copy(alpha = 0.15f) else CozyCard)
                                .clickable {
                                    hasClapped = !hasClapped
                                    if (hasClapped) clapsCount++ else clapsCount--
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "👏", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = clapsCount.toString(),
                                color = if (hasClapped) PrimaryAmber else TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Reaction 🤝 Support
                        var supportsCount by remember(wish.id) { mutableStateOf(wish.offerCount + 2) }
                        var hasSupported by remember(wish.id) { mutableStateOf(false) }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (hasSupported) ValidGreen.copy(alpha = 0.15f) else CozyCard)
                                .clickable {
                                    hasSupported = !hasSupported
                                    if (hasSupported) supportsCount++ else supportsCount--
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "🤝", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = supportsCount.toString(),
                                color = if (hasSupported) ValidGreen else TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Reaction 💡 Insight
                        var insightsCount by remember(wish.id) { mutableStateOf(wish.commentsCount + 1) }
                        var hasInsight by remember(wish.id) { mutableStateOf(false) }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (hasInsight) AccentPurple.copy(alpha = 0.15f) else CozyCard)
                                .clickable {
                                    hasInsight = !hasInsight
                                    if (hasInsight) insightsCount++ else insightsCount--
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "💡", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = insightsCount.toString(),
                                color = if (hasInsight) AccentPurple else TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Help Call-To-Action Button
                    Button(
                        onClick = onHelpClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (wish.fulfillStatus == "OPEN" && !wish.isExpired) PrimaryAmber else CozyCard,
                            disabledContainerColor = CozyCard
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (wish.fulfillStatus == "OPEN" && !wish.isExpired) Icons.Default.Handshake else Icons.Default.ArrowForward,
                                contentDescription = "Help Action",
                                tint = if (wish.fulfillStatus == "OPEN" && !wish.isExpired) Color.Black else TextLight,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (wish.fulfillStatus == "OPEN" && !wish.isExpired) "Help/Offer Support" else "View Details",
                                color = if (wish.fulfillStatus == "OPEN" && !wish.isExpired) Color.Black else TextLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                // Centralised Chat-First Direct Reply Shortcut drawer
                Spacer(modifier = Modifier.height(12.dp))
                
                var showQuickReplyInput by remember { mutableStateOf(false) }
                var quickReplyText by remember { mutableStateOf("") }
                
                if (showQuickReplyInput) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CozyCard.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = quickReplyText,
                            onValueChange = { quickReplyText = it },
                            placeholder = { Text("Ask or offer support directly...", color = TextMuted, fontSize = 11.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_reply_field_${wish.id}"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextLight,
                                unfocusedTextColor = TextLight,
                                focusedBorderColor = PrimaryAmber,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                if (quickReplyText.isNotBlank()) {
                                    onQuickReply(quickReplyText)
                                    quickReplyText = ""
                                    showQuickReplyInput = false
                                }
                            },
                            enabled = quickReplyText.isNotBlank(),
                            modifier = Modifier.testTag("send_quick_reply_btn_${wish.id}")
                        ) {
                            Icon(Icons.Default.Send, "Send Chat", tint = if (quickReplyText.isNotBlank()) PrimaryAmber else TextMuted)
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { showQuickReplyInput = true },
                        border = BorderStroke(1.dp, PrimaryAmber.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .testTag("quick_reply_toggle_${wish.id}"),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Chat, "Chat", tint = PrimaryAmber, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start Live Chat Shortcut", color = PrimaryAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SkeletonCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepSurface.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CozyCard)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.width(110.dp).height(12.dp).clip(RoundedCornerShape(4.dp)).background(CozyCard))
                    Box(modifier = Modifier.width(70.dp).height(8.dp).clip(RoundedCornerShape(4.dp)).background(CozyCard))
                }
            }
            Box(modifier = Modifier.fillMaxWidth(0.9f).height(16.dp).clip(RoundedCornerShape(6.dp)).background(CozyCard))
            Box(modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(4.dp)).background(CozyCard))
            Box(modifier = Modifier.fillMaxWidth(0.6f).height(12.dp).clip(RoundedCornerShape(4.dp)).background(CozyCard))
        }
    }
}

// Spark social companion data models
data class ActiveLoopItem(
    val name: String,
    val stats: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val hue: Color
)

data class TrendingTopicItem(
    val text: String,
    val replies: Int,
    val tag: String
)

fun getAreaCoordinates(area: String?): Pair<Double, Double> {
    if (area == null) return Pair(18.5204, 73.8567)
    return when (area.trim()) {
        "Kothrud" -> Pair(18.5074, 73.8077)
        "Baner" -> Pair(18.5590, 73.7797)
        "Hinjawadi" -> Pair(18.5913, 73.7389)
        "Kalyani Nagar" -> Pair(18.5463, 73.9033)
        "Viman Nagar" -> Pair(18.5679, 73.9143)
        "Deccan Gymkhana" -> Pair(18.5168, 73.8402)
        "Shivaji Nagar" -> Pair(18.5308, 73.8474)
        "Katraj" -> Pair(18.4529, 73.8540)
        "South Mumbai" -> Pair(18.9220, 72.8347)
        "Indiranagar" -> Pair(12.9716, 77.5946)
        "Connaught Place" -> Pair(28.6139, 77.2090)
        else -> Pair(18.5204, 73.8567) // default center Pune
    }
}

fun estimateDistanceKm(area1: String?, area2: String?): Double {
    if (area1 == null || area2 == null) return 5.0
    val a1 = area1.trim()
    val a2 = area2.trim()
    if (a1.equals(a2, ignoreCase = true)) {
        return 0.5 // same neighborhood estimate
    }
    val coords1 = getAreaCoordinates(a1)
    val coords2 = getAreaCoordinates(a2)
    
    // Haversine formula
    val earthRadiusKm = 6371.0
    val dLat = Math.toRadians(coords2.first - coords1.first)
    val dLon = Math.toRadians(coords2.second - coords1.second)
    
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(coords1.first)) * Math.cos(Math.toRadians(coords2.first)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    val d = earthRadiusKm * c
    return if (d.isNaN()) 0.0 else Math.round(d * 10.0) / 10.0 // 1 decimal place
}

