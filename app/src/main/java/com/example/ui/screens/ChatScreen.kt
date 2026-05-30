package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ChatMessage
import com.example.ui.theme.*
import com.example.ui.viewmodel.IcchaViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: IcchaViewModel,
    onBack: () -> Unit
) {
    val wish by viewModel.activeWishFlow.collectAsState()
    val messages by viewModel.activeMessagesFlow.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var isRecordingAudioMessage by remember { mutableStateOf(false) }
    var audioTimer by remember { mutableStateOf(0) }

    // Audio recorder simulation loop
    LaunchedEffect(isRecordingAudioMessage) {
        if (isRecordingAudioMessage) {
            audioTimer = 0
            while (isRecordingAudioMessage && audioTimer < 15) {
                delay(1000)
                audioTimer++
            }
            if (isRecordingAudioMessage) {
                isRecordingAudioMessage = false
                viewModel.sendChatMessage(
                    text = "🎤 Voice message (${audioTimer}s)",
                    isAudio = true,
                    audioSec = audioTimer,
                    audioUrl = "simulated_voice_clip.mp3"
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    wish?.let { currentWish ->
                        Column {
                            Text(
                                text = "Bartaalaap (Direct Chat)",
                                fontWeight = FontWeight.Bold,
                                color = TextLight,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Wish: ${currentWish.title}",
                                color = TextMuted,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    } ?: Text("Direct Chat", color = TextLight)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepSurface)
            )
        },
        containerColor = DarkGrayBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
        ) {
            // Chat Message List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    // Context tip
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CozyCard.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Secure status flag", tint = PrimaryAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Personal communication. Discuss pickup/deal criteria ethically.",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No chats started yet! Text or hold mic to communicate.",
                                color = TextMuted,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(messages) { message ->
                        val isMe = message.senderId == viewModel.currentUserId
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 12.dp,
                                            topEnd = 12.dp,
                                            bottomStart = if (isMe) 12.dp else 2.dp,
                                            bottomEnd = if (isMe) 2.dp else 12.dp
                                        )
                                    )
                                    .background(if (isMe) AccentPurple else CozyCard)
                                    .border(1.dp, if (isMe) AccentPurple else CozyCard)
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .widthIn(max = 260.dp)
                            ) {
                                Column {
                                    if (message.isAudio) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Simulated voice player",
                                                tint = PrimaryGold,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Simulated Voice Note",
                                                color = TextLight,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "${message.audioDurationSec}s",
                                                color = TextMuted,
                                                fontSize = 11.sp
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = message.text,
                                            color = TextLight,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            // Input Row Panel with Simulated Microphone for voice replies
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepSurface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mic Icon for Voice Note simulation
                IconButton(
                    onClick = {
                        if (isRecordingAudioMessage) {
                            // Stop and send
                            isRecordingAudioMessage = false
                            viewModel.sendChatMessage(
                                text = "🎤 Voice message (${audioTimer}s)",
                                isAudio = true,
                                audioSec = audioTimer,
                                audioUrl = "simulated_voice_clip.mp3"
                            )
                        } else {
                            // Start
                            isRecordingAudioMessage = true
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isRecordingAudioMessage) Color.Red else CozyCard)
                        .testTag("chat_voice_mic_button")
                ) {
                    Icon(
                        imageVector = if (isRecordingAudioMessage) Icons.Default.Pause else Icons.Default.Mic,
                        contentDescription = "voice note creator",
                        tint = if (isRecordingAudioMessage) Color.White else PrimaryGold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (isRecordingAudioMessage) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.Red.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🔴 Recording Voice message: ${audioTimer}s ... Tap pause to send",
                            color = Color.Red,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Write personal message...", color = TextMuted, fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("chat_message_input"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentPurple,
                            unfocusedBorderColor = CozyCard,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight,
                            focusedContainerColor = CosmicThemeTextBg,
                            unfocusedContainerColor = CosmicThemeTextBg
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.sendChatMessage(textInput)
                            textInput = ""
                        }
                    },
                    enabled = textInput.isNotBlank() && !isRecordingAudioMessage,
                    modifier = Modifier.testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "send message",
                        tint = if (textInput.isNotBlank()) AccentPurple else TextMuted
                    )
                }
            }
        }
    }
}
