package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.IcchaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(
    viewModel: IcchaViewModel,
    onSuccess: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var locationInput by remember { mutableStateOf("Pune") }
    var selectedCategory by remember { mutableStateOf("Community") }
    
    // Evolved Spark Post Metadata
    var selectedPostType by remember { mutableStateOf("REQUEST") } // "REQUEST", "ADVICE", "LISTING", "OFFER", "UPDATE"
    var selectedLoop by remember { mutableStateOf("General Loop") } // e.g. "Student Loop", "BHU Loop"
    var selectedUrgency by remember { mutableStateOf("CASUAL") } // "CASUAL", "NEEDED_SOON", "URGENT", "EMERGENCY"
    var selectedExpiryHours by remember { mutableStateOf(0) } // 0 = never, 6, 24, 72

    // Choose custom image or stick to high-quality preset matching the category
    var useCustomImage by remember { mutableStateOf(false) }
    var customImageUrl by remember { mutableStateOf("") }

    // Cloud upload states
    var isUploadingAsset by remember { mutableStateOf(false) }
    var cloudUploadStatus by remember { mutableStateOf("") }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploadingAsset = true
            cloudUploadStatus = "Uploading chosen live asset to Firebase Storage..."
            viewModel.uploadImageToCloud(uri) { url ->
                isUploadingAsset = false
                if (url != null) {
                    customImageUrl = url
                    useCustomImage = true
                    cloudUploadStatus = "Uploaded successfully to Firebase! 🌟"
                } else {
                    customImageUrl = uri.toString()
                    useCustomImage = true
                    cloudUploadStatus = "Using local caching asset container (Sandbox mode)"
                }
            }
        }
    }

    val categories = listOf("Community", "Volunteering", "Gratitude", "Support", "Loops", "Emergency")

    val postTypes = listOf(
        "REQUEST" to "Request 🤝",
        "ADVICE" to "Advice/Q&A 💡",
        "LISTING" to "Listing 📦",
        "OFFER" to "My Offer 🎁",
        "UPDATE" to "Loop Update 📢"
    )

    val urgencyLevels = listOf(
        "CASUAL" to "Casual 💬",
        "NEEDED_SOON" to "Needed Soon ⏳",
        "URGENT" to "Urgent 🔶",
        "EMERGENCY" to "Emergency 🚨"
    )

    val expiryOptions = listOf(
        0 to "Never Expires",
        6 to "6 Hours",
        24 to "24 Hours",
        72 to "3 Days"
    )

    // Automatically get the Unsplash image matching the category from our ViewModel
    val activePresetImage = viewModel.categoryPresets[selectedCategory] ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create a Community Post", fontWeight = FontWeight.Bold, color = TextLight, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepSurface)
            )
        },
        containerColor = DarkGrayBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(DarkGrayBg)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Helper explanation tip Card (Glassmorphic)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CozyCard.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AccentPurple.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Stars, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(20.dp))
                    }
                    Text(
                        text = "Broadcast listings, request support, offer help or start community updates. Positive and local, your post instantly notifies members inside your Local Loop.",
                        fontSize = 11.sp,
                        color = TextMuted,
                        lineHeight = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Post Type Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "What kind of post are you creating?",
                    color = TextLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(postTypes) { (typeKey, typeLabel) ->
                        val isSelected = selectedPostType == typeKey
                        OptionChip(
                            label = typeLabel,
                            isSelected = isSelected,
                            selectedColor = PrimaryAmber,
                            onClick = { selectedPostType = typeKey }
                        )
                    }
                }
            }

            // Loop Group Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Select Community Circle (Loop):",
                    color = TextLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(viewModel.availableLoops) { loop ->
                        val isSelected = selectedLoop == loop
                        OptionChip(
                            label = "⭕ $loop",
                            isSelected = isSelected,
                            selectedColor = AccentPurple,
                            onClick = { selectedLoop = loop }
                        )
                    }
                }
            }

            // Urgency Level Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Urgency Level Badge:",
                    color = TextLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(urgencyLevels) { (urgencyKey, urgencyLabel) ->
                        val isSelected = selectedUrgency == urgencyKey
                        val activeColor = when (urgencyKey) {
                            "EMERGENCY" -> ErrorRed
                            "URGENT" -> Color(0xFFFF5722)
                            "NEEDED_SOON" -> PrimaryAmber
                            else -> Color(0xFF2196F3)
                        }
                        OptionChip(
                            label = urgencyLabel,
                            isSelected = isSelected,
                            selectedColor = activeColor,
                            onClick = { selectedUrgency = urgencyKey }
                        )
                    }
                }
            }

            // Expiry Option Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Automatic Lifetime Expiry:",
                    color = TextLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(expiryOptions) { (hours, label) ->
                        val isSelected = selectedExpiryHours == hours
                        OptionChip(
                            label = "⏱️ $label",
                            isSelected = isSelected,
                            selectedColor = Color(0xFF00BCD4),
                            onClick = { selectedExpiryHours = hours }
                        )
                    }
                }
            }

            // Category Horizontal Line picker
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Core Category Filter Tag:",
                    color = TextLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        val colorTheme = when (cat) {
                            "Emergency" -> ErrorRed
                            "Community" -> Color(0xFF2196F3)
                            "Volunteering" -> Color(0xFF4CAF50)
                            "Gratitude" -> Color(0xFFFF9800)
                            "Support" -> Color(0xFF9C27B0)
                            "Loops" -> Color(0xFF00BCD4)
                            else -> TextLight
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) colorTheme else CozyCard)
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("post_category_$cat"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.Black else TextLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Title input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Post short summary Title", color = TextMuted) },
                placeholder = { Text("E.g., Free Web Mentorship Loop or BHU student books exchange", color = TextMuted) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("post_title_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryAmber,
                    unfocusedBorderColor = CozyCard,
                    focusedTextColor = TextLight,
                    unfocusedTextColor = TextLight,
                    focusedContainerColor = DeepSurface,
                    unfocusedContainerColor = DeepSurface
                ),
                singleLine = true
            )

            // Location Input
            OutlinedTextField(
                value = locationInput,
                onValueChange = { locationInput = it },
                label = { Text("Active Location Circle", color = TextMuted) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("post_location_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryAmber,
                    unfocusedBorderColor = CozyCard,
                    focusedTextColor = TextLight,
                    unfocusedTextColor = TextLight,
                    focusedContainerColor = DeepSurface,
                    unfocusedContainerColor = DeepSurface
                ),
                leadingIcon = { Icon(Icons.Default.LocationOn, "loc", tint = PrimaryAmber) },
                singleLine = true
            )

            // Description input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Tell the community your plans, ideas or resources", color = TextMuted) },
                placeholder = { Text("Explain clearly how community members can collaborate, support, or pick up listings.", color = TextMuted) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .testTag("post_desc_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryAmber,
                    unfocusedBorderColor = CozyCard,
                    focusedTextColor = TextLight,
                    unfocusedTextColor = TextLight,
                    focusedContainerColor = DeepSurface,
                    unfocusedContainerColor = DeepSurface
                )
            )

            // Image attachment selection (Curated category Stock vs Custom Cloud Upload)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CozyCard),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "📷 Visual Attachment (Optional)",
                        fontWeight = FontWeight.Bold,
                        color = TextLight,
                        fontSize = 13.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { photoLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.UploadFile, "upload")
                            Spacer(Modifier.width(6.dp))
                            Text("Upload Device Photo", fontSize = 11.sp, color = TextLight, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                useCustomImage = !useCustomImage 
                                if (!useCustomImage) {
                                    customImageUrl = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (useCustomImage) PrimaryAmber else DeepSurface
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Link, "link")
                            Spacer(Modifier.width(6.dp))
                            Text(if (useCustomImage) "Custom Link Active" else "Use Link URL", fontSize = 11.sp, color = if (useCustomImage) Color.Black else TextLight, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (cloudUploadStatus.isNotBlank()) {
                        Text(
                            text = cloudUploadStatus,
                            color = if (cloudUploadStatus.contains("success") || cloudUploadStatus.contains("🌟") || cloudUploadStatus.contains("Sandbox")) ValidGreen else PrimaryAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    if (useCustomImage) {
                        OutlinedTextField(
                            value = customImageUrl,
                            onValueChange = { 
                                customImageUrl = it
                                cloudUploadStatus = ""
                            },
                            label = { Text("Image Link / URL path", color = TextMuted) },
                            placeholder = { Text("E.g., https://unsplash.com/your-image.jpg", color = TextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextLight,
                                unfocusedTextColor = TextLight,
                                focusedBorderColor = PrimaryAmber,
                                unfocusedBorderColor = CozyCard
                            ),
                            singleLine = true
                        )
                    }

                    // Display active preview card
                    val activeImagePreview = if (useCustomImage && customImageUrl.isNotBlank()) customImageUrl else activePresetImage
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DeepSurface)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AsyncImage(
                            model = activeImagePreview,
                            contentDescription = "Active Preset image",
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Live Attachment Preview",
                                color = if (useCustomImage) PrimaryAmber else ValidGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = if (useCustomImage) "Displaying custom uploaded asset URL beautifully in the feed container." else "Displaying recommended category preset image.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Publish button
            Button(
                onClick = {
                    if (title.isNotBlank() && description.isNotBlank()) {
                        val finalImage = if (useCustomImage && customImageUrl.isNotBlank()) {
                            customImageUrl
                        } else {
                            activePresetImage
                        }
                        
                        viewModel.publishSparkPost(
                            title = title,
                            content = description,
                            category = selectedCategory,
                            postType = selectedPostType,
                            loopName = selectedLoop,
                            urgencyLevel = selectedUrgency,
                            expiryDurationHours = selectedExpiryHours,
                            mediaUrl = finalImage,
                            location = locationInput
                        )
                        onSuccess()
                    }
                },
                enabled = title.isNotBlank() && description.isNotBlank() && !isUploadingAsset,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryAmber,
                    disabledContainerColor = CozyCard
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("publish_wish_button")
            ) {
                if (isUploadingAsset) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Publish, contentDescription = "publish", tint = if (title.isNotBlank()) Color.Black else TextMuted)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Publish Post",
                        color = if (title.isNotBlank()) Color.Black else TextMuted,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun OptionChip(
    label: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) selectedColor.copy(alpha = 0.22f) else CozyCard)
            .border(
                1.dp,
                if (isSelected) selectedColor else Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) selectedColor else TextLight,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal
        )
    }
}
