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
import androidx.compose.material.icons.outlined.*
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
import com.example.data.model.Offer
import com.example.data.model.Wish
import com.example.ui.theme.*
import com.example.ui.viewmodel.IcchaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishDetailScreen(
    viewModel: IcchaViewModel,
    onBack: () -> Unit,
    onNavigateToChat: () -> Unit
) {
    val wish by viewModel.activeWishFlow.collectAsState()
    val offers by viewModel.activeOffersFlow.collectAsState()
    val currentUser by viewModel.currentUserState.collectAsState()
    val comments by viewModel.activeCommentsFlow.collectAsState()

    var offerMessage by remember { mutableStateOf("") }

    if (wish == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryGold)
        }
        return
    }

    val currentWish = wish!!
    val isOwner = currentWish.creatorId == viewModel.currentUserId

    val categoryColor = when (currentWish.category) {
        "Emergency" -> ErrorRed
        "Community" -> Color(0xFF2196F3)
        "Volunteering" -> Color(0xFF4CAF50)
        "Gratitude" -> Color(0xFFFF9800)
        "Support" -> Color(0xFF9C27B0)
        "Loops" -> Color(0xFF00BCD4)
        else -> TextMuted
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post Details", fontWeight = FontWeight.Bold, color = TextLight, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go back", tint = TextLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepSurface)
            )
        },
        containerColor = DarkGrayBg
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Requester Header Card
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(54.dp)) {
                            AsyncImage(
                                model = currentWish.creatorImage,
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .border(1.5.dp, PrimaryAmber, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            if (currentWish.creatorVerified) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(ValidGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Verified Flag",
                                        tint = Color.Black,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentWish.creatorName,
                                    color = TextLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                if (currentWish.creatorVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified Seal",
                                        tint = ValidGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryAmber, modifier = Modifier.size(10.dp))
                                Text(
                                    text = "Location: ${currentWish.location ?: "Pune"}",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Fulfiller Status badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when (currentWish.fulfillStatus) {
                                        "OPEN" -> CozyCard
                                        "ACCEPTED" -> PrimaryAmber.copy(alpha = 0.2f)
                                        "FULFILLED" -> ValidGreen.copy(alpha = 0.2f)
                                        else -> Color.Gray.copy(alpha = 0.2f)
                                    }
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = currentWish.fulfillStatus,
                                color = when (currentWish.fulfillStatus) {
                                    "OPEN" -> TextMuted
                                    "ACCEPTED" -> PrimaryAmber
                                    "FULFILLED" -> ValidGreen
                                    else -> TextMuted
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Beautiful interactive request progress indicator timeline card
            item {
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Need Life Cycle Tracker",
                            color = TextLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 0.3.sp
                        )

                        val status = currentWish.fulfillStatus
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val activeColor = ValidGreen
                            val inactiveColor = TextMuted.copy(alpha = 0.25f)
                            
                            // 1. Safe Scan Passed
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = activeColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Safe Scan", color = TextLight, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                Text("Cleared", color = ValidGreen, fontSize = 8.sp, fontWeight = FontWeight.Medium)
                            }
                            
                            // Line separator 1 (always active)
                            Box(modifier = Modifier.width(16.dp).height(2.dp).background(activeColor))
                            
                            // 2. Broadcasted Need
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.Campaign,
                                    contentDescription = null,
                                    tint = activeColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Broadcast", color = TextLight, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                Text("Live in Pune", color = ValidGreen, fontSize = 8.sp, fontWeight = FontWeight.Medium)
                            }

                            // Line separator 2
                            val line2Color = if (status == "ACCEPTED" || status == "FULFILLED") activeColor else inactiveColor
                            Box(modifier = Modifier.width(16.dp).height(2.dp).background(line2Color))

                            // 3. Connected
                            val isConnected = status == "ACCEPTED" || status == "FULFILLED"
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.Handshake,
                                    contentDescription = null,
                                    tint = if (isConnected) activeColor else inactiveColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Connected", color = if (isConnected) TextLight else TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                Text(
                                    text = if (isConnected) "Helper Found" else "Waiting...", 
                                    color = if (isConnected) ValidGreen else TextMuted, 
                                    fontSize = 8.sp, 
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Line separator 3
                            val line3Color = if (status == "FULFILLED") activeColor else inactiveColor
                            Box(modifier = Modifier.width(16.dp).height(2.dp).background(line3Color))

                            // 4. Resolved
                            val isResolved = status == "FULFILLED"
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isResolved) activeColor else inactiveColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Resolved", color = if (isResolved) TextLight else TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                Text(
                                    text = if (isResolved) "Aid Succeeded" else "Incomplete", 
                                    color = if (isResolved) ValidGreen else TextMuted, 
                                    fontSize = 8.sp, 
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Description details and Optional Image presentation card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CozyCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Category Tag
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(categoryColor.copy(alpha = 0.15f))
                                    .border(1.dp, categoryColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = currentWish.category.uppercase(),
                                    color = categoryColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = currentWish.title,
                            color = TextLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            lineHeight = 26.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = currentWish.content,
                            color = TextLight.copy(alpha = 0.9f),
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        )

                        // If media selection preset contains Unsplash URLs, display center card beautifully
                        if (!currentWish.mediaUrl.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            AsyncImage(
                                model = currentWish.mediaUrl,
                                contentDescription = "Attached Need photo",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, CozyCard, RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Interactive Reactions & Bookmark row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val reactionTypes = listOf(
                                "love" to "❤️",
                                "fire" to "🔥",
                                "clap" to "👏",
                                "support" to "🤝"
                            )
                            
                            reactionTypes.forEach { (type, emoji) ->
                                val count = try {
                                    val regex = """"$type"\s*:\s*(\d+)""".toRegex()
                                    val match = regex.find(currentWish.reactionsJson)
                                    match?.groupValues?.get(1)?.toInt() ?: 0
                                } catch(e: Exception) { 0 }
                                
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DeepSurface)
                                        .clickable { viewModel.reactToWish(currentWish.id, type) }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(emoji, fontSize = 11.sp)
                                        Text(count.toString(), color = TextLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            // Bookmark toggle Button
                            IconButton(onClick = { viewModel.toggleBookmarkWish(currentWish.id) }) {
                                Icon(
                                    imageVector = if (currentWish.bookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Bookmark",
                                    tint = if (currentWish.bookmarked) PrimaryAmber else TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Direct Communication / Chat Link if state is ACCEPTED or FULFILLED
            if (currentWish.fulfillStatus == "ACCEPTED" || currentWish.fulfillStatus == "FULFILLED") {
                item {
                    val isActiveParticipant = isOwner || currentWish.fulfillerId == viewModel.currentUserId
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActiveParticipant) AccentPurple.copy(alpha = 0.15f) else CozyCard
                        ),
                        border = if (isActiveParticipant) BorderStroke(1.dp, AccentPurple) else null
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Forum,
                                contentDescription = "Active communication channel",
                                tint = PrimaryGold,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Communication Thread is Active!",
                                color = TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Accepted collaborator: ${currentWish.fulfillerName ?: "Reliable solver"}",
                                color = TextMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            if (isActiveParticipant) {
                                Button(
                                    onClick = onNavigateToChat,
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                                    modifier = Modifier.testTag("chat_communication_button")
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = "chat")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Enter Coordinator Chat Room", fontWeight = FontWeight.Bold)
                                }

                                if (isOwner && currentWish.fulfillStatus == "ACCEPTED") {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.fulfillWish(currentWish.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = ValidGreen),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Celebration, contentDescription = "complete")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Mark as Fulfilled (Reward +100 Rep!)", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                Text(
                                    text = "Only the Need broadcaster and accepted provider can join this coordination room.",
                                    color = TextMuted.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Solutions/Offers Section Title
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text(
                        text = "Initiative Collaborations & Suggestions",
                        fontWeight = FontWeight.Bold,
                        color = TextLight,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(PrimaryAmber)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = offers.size.toString(),
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Offers List
            if (offers.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CozyCard)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "No offers yet",
                                tint = TextMuted.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No solutions or aid packages proposed yet. Be the first to volunteer and earn 100 reputation points!",
                                color = TextMuted,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            } else {
                items(offers) { offer ->
                    OfferCard(
                        offer = offer,
                        isOwnerOfWish = isOwner,
                        wishStatus = currentWish.fulfillStatus,
                        onAccept = { viewModel.acceptOffer(offer.id, currentWish.id) }
                    )
                }
            }

            // Write and Submit Solution offering form if NOT self-owned and Request is OPEN
            if (!isOwner && currentWish.fulfillStatus == "OPEN") {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DeepSurface),
                        border = BorderStroke(1.dp, PrimaryAmber.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Propose Community Handshake / Aid",
                                fontWeight = FontWeight.Bold,
                                color = PrimaryAmber,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Detail how you intend to help. Do you have the materials, transport vehicle, or medical tips? Building trust on requests leads to solid coordinate chat.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = offerMessage,
                                onValueChange = { offerMessage = it },
                                placeholder = { Text("Describe details of what you have/how you coordinate...", color = TextMuted) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .testTag("solution_offer_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryAmber,
                                    unfocusedBorderColor = CozyCard,
                                    focusedContainerColor = CosmicThemeTextBg,
                                    unfocusedContainerColor = CosmicThemeTextBg,
                                    focusedTextColor = TextLight,
                                    unfocusedTextColor = TextLight
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (offerMessage.isNotBlank()) {
                                        viewModel.submitOffer(currentWish.id, offerMessage)
                                        offerMessage = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAmber),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_solution_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.RocketLaunch, contentDescription = "Submit help", tint = Color.Black)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Contribute & Align Loop", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Q&A / Advice Discussion Room Section Header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 4.dp)
                ) {
                    Text(
                        text = "💬 Q&A & Advice Discussion Room",
                        fontWeight = FontWeight.Bold,
                        color = TextLight,
                        fontSize = 17.sp
                    )
                }
            }

            // Comments List Render
            if (comments.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CozyCard.copy(alpha = 0.4f))
                    ) {
                        Box(
                            modifier = Modifier.padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No comments or suggestions yet. Ask a question, recommend, or post advice inside this Loop!",
                                color = TextMuted,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(comments) { comment ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CozyCard.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = comment.senderImage,
                                    contentDescription = "Avatar",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .border(0.8.dp, PrimaryAmber, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = comment.senderName,
                                    color = TextLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "Pune Loop",
                                    color = AccentPurple,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = comment.commentText,
                                color = TextLight.copy(alpha = 0.95f),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Comment Submission Bar
            item {
                var commentText by remember { mutableStateOf("") }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepSurface),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(6.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            placeholder = { Text("Comment, offer advice, recommendations...", color = TextMuted, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f).testTag("comment_input_box"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextLight,
                                unfocusedTextColor = TextLight,
                                focusedBorderColor = PrimaryAmber,
                                unfocusedBorderColor = CozyCard,
                                focusedContainerColor = CosmicThemeTextBg,
                                unfocusedContainerColor = CosmicThemeTextBg
                            )
                        )
                        IconButton(
                            onClick = {
                                if (commentText.isNotBlank()) {
                                    viewModel.addCommentToWish(currentWish.id, commentText)
                                    commentText = ""
                                }
                            },
                            enabled = commentText.isNotBlank(),
                            colors = IconButtonDefaults.iconButtonColors(contentColor = PrimaryAmber)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Publish advice",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun OfferCard(
    offer: Offer,
    isOwnerOfWish: Boolean,
    wishStatus: String,
    onAccept: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (offer.isAccepted) Color(0xFF2E7D32).copy(alpha = 0.15f) else CozyCard
        ),
        border = if (offer.isAccepted) BorderStroke(1.dp, ValidGreen) else null
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Offer Provider Profile details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model = offer.providerImage,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(1.dp, PrimaryAmber, CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = offer.providerName,
                            color = TextLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (offer.providerVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified provider",
                                tint = ValidGreen,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = "${offer.providerPoints} Rep Points",
                        color = PrimaryGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Accept status button or tag
                if (offer.isAccepted) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ValidGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = "Accepted", tint = ValidGreen, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ACCEPTED", color = ValidGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (isOwnerOfWish && wishStatus == "OPEN") {
                    Button(
                        onClick = onAccept,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryAmber),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("accept_offer_button_${offer.id}")
                    ) {
                        Text("Accept", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = offer.offerMessage,
                color = TextLight.copy(alpha = 0.9f),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}
