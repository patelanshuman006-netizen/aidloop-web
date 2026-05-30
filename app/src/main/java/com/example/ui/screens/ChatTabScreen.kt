package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.*
import com.example.ui.viewmodel.IcchaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTabScreen(
    viewModel: IcchaViewModel,
    onNavigateToChat: (Int) -> Unit
) {
    val currentUser by viewModel.currentUserState.collectAsState()
    val activeChatWishes by viewModel.activeChatWishesState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // Mock direct contacts online for quick onboarding
    val directContacts = remember(currentUser?.city) {
        val city = currentUser?.city ?: "Pune"
        when {
            city.contains("Pune", ignoreCase = true) -> listOf(
                Pair("Dr. Priya Deshmukh", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=150&q=80"),
                Pair("Rajesh G.", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=150&q=80"),
                Pair("Reema Sen", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=150&q=80")
            )
            else -> listOf(
                Pair("Amit Sharma", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=150&q=80"),
                Pair("Sneha Mishra", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80")
            )
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
                                .background(Brush.linearGradient(listOf(AccentPurple, PrimaryAmber))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Chat, "chats", tint = Color.Black, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Bartaalaap Console",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = TextLight
                            )
                            Text(
                                text = "Realtime Local Discussions & Direct Chats",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
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
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Search Bar for Chats
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search chats or contacts...", color = TextMuted, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = TextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chat_search_bar"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight,
                        focusedBorderColor = PrimaryAmber,
                        unfocusedBorderColor = CozyCard
                    ),
                    singleLine = true
                )
            }

            // Section: Online Now Active Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "🟢 Active Nearby Now",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(directContacts) { contact ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        // Auto-open chat thread with wish id 1 ( Priya ) or 2 ( Rajesh )
                                        val targetId = if (contact.first.contains("Priya")) 1 else 2
                                        viewModel.selectWish(targetId)
                                        onNavigateToChat(targetId)
                                    }
                                    .padding(horizontal = 4.dp)
                            ) {
                                Box(modifier = Modifier.size(52.dp)) {
                                    AsyncImage(
                                        model = contact.second,
                                        contentDescription = contact.first,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .border(1.5.dp, PrimaryAmber, CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    // Live Green pulse
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(ValidGreen)
                                            .border(2.dp, CozyCard, CircleShape)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = contact.first.substringBefore(" "),
                                    color = TextLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Section: Conversations header
            item {
                Text(
                    text = "📥 Direct Conversations",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 13.sp
                )
            }

            // Combined active DB chats and simulated pre-seeds for complete look
            val filteredDbChats = activeChatWishes.filter { wish ->
                wish.title.contains(searchQuery, ignoreCase = true) ||
                        wish.creatorName.contains(searchQuery, ignoreCase = true)
            }

            if (filteredDbChats.isEmpty()) {
                // If there are no active DB conversations yet, supply beautifully simulated real ones matching initial seed data!
                val sampleConversations = listOf(
                    Triple(
                        1, 
                        "Dr. Priya Deshmukh",
                        "I have a spare verified 10L oxygen concentrator in Deccan Gymkhana. Let's Chat."
                    ),
                    Triple(
                        2, 
                        "Rajesh G.",
                        "I am free on Sunday and can drive over with some extra tech kits! 📚"
                    )
                ).filter {
                    it.second.contains(searchQuery, ignoreCase = true)
                }

                if (sampleConversations.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("no_chats_clue"),
                            colors = CardDefaults.cardColors(containerColor = CozyCard.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.ChatBubbleOutline, null, tint = TextMuted, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No conversations found", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    items(sampleConversations) { conv ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectWish(conv.first)
                                    onNavigateToChat(conv.first)
                                }
                                .testTag("sim_chat_item_${conv.first}"),
                            colors = CardDefaults.cardColors(containerColor = CozyCard)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(42.dp)) {
                                    AsyncImage(
                                        model = if (conv.first == 1) "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=150&q=80" else "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=150&q=80",
                                        contentDescription = conv.second,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(ValidGreen)
                                            .border(1.5.dp, CozyCard, CircleShape)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = conv.second,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "Just Now",
                                            color = TextMuted,
                                            fontSize = 9.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = conv.third,
                                        color = TextLight,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Glow Unread Badge (1 notification)
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryAmber),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "1",
                                        color = Color.Black,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                items(filteredDbChats) { wish ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectWish(wish.id)
                                onNavigateToChat(wish.id)
                            }
                            .testTag("db_chat_item_${wish.id}"),
                        colors = CardDefaults.cardColors(containerColor = CozyCard)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(42.dp)) {
                                AsyncImage(
                                    model = wish.creatorImage,
                                    contentDescription = wish.creatorName,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(ValidGreen)
                                        .border(1.5.dp, CozyCard, CircleShape)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = wish.creatorName,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Active",
                                        color = TextMuted,
                                        fontSize = 9.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Post: ${wish.title}",
                                    color = TextLight,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Glow notification count
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryAmber),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "1",
                                    color = Color.Black,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) } // nav buffer
        }
    }
}
