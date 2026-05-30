package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class GuidelineRule(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidelinesScreen(onBack: () -> Unit) {
    val rules = listOf(
        GuidelineRule(
            title = "Strictly Non-Commercial Aid",
            description = "AidLoop is a pure mutual aid network. You cannot sell items, ask for security deposits, or post rental requests. No commercial marketing or loans are tolerated on this platform.",
            icon = Icons.Default.MonetizationOn,
            color = ErrorRed
        ),
        GuidelineRule(
            title = "Zero Advance Transfers (Anti-Fraud)",
            description = "Never transfer any money (via GPay, Paytm, or PhonePe) to anyone promising material or medical aid beforehand. Verified help should be free or coordinated face-to-face in busy public areas safely.",
            icon = Icons.Default.Security,
            color = ValidGreen
        ),
        GuidelineRule(
            title = "Safe Public Meets",
            description = "When carrying out drops in Pune (or nearby centers), prefer well-lit populated meetups. JM Road, Swargate, Phoenix Marketcity, or local railway/bus terminals of Pune are recommended spots.",
            icon = Icons.Default.LocationOn,
            color = PrimaryAmber
        ),
        GuidelineRule(
            title = "Honest Needs posting",
            description = "Each request must contain precise, verified details. Posting fake distress calls or duplicate spam requests is caught by the AidLoop AI Safety Scanner and will drop your Community Reputation to zero.",
            icon = Icons.Default.CheckCircle,
            color = AccentPurple
        ),
        GuidelineRule(
            title = "Reputation & Silver/Gold Badges",
            description = "Earn badging levels (Silver, Gold, and Cosmic Platinum) by getting your profile verified with local coordinates and receiving multiple positive helper reviews dynamically.",
            icon = Icons.Default.Stars,
            color = PrimaryGold
        )
    )

    var expandedIndex by remember { mutableStateOf<Int?>(-1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Safety & Guidelines",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_button_guidelines")
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Intro Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DeepSurface.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.1f), Color.Transparent))
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrimaryAmber.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Shield",
                                tint = PrimaryAmber,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Pune's Safe Mutual-Aid Network",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = TextLight
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "AidLoop is backed by a proactive reputation network. Follow these community guidelines to protect yourself and ensure solidarian trust.",
                            color = TextMuted,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Core Rules & Security Protocol",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryAmber,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            // Accordion list of guidelines
            items(rules.size) { index ->
                val r = rules[index]
                val isExpanded = expandedIndex == index

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expandedIndex = if (isExpanded) -1 else index
                        }
                        .testTag("guideline_rule_$index"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isExpanded) CozyCard else DeepSurface.copy(alpha = 0.6f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isExpanded) r.color.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(r.color.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = r.icon,
                                        contentDescription = r.title,
                                        tint = r.color,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = r.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextLight
                                )
                            }
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Collapse Toggle",
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = r.description,
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Trust Pledge Footer
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CozyCard.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = "Pledge",
                            tint = PrimaryGold,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = " Pune AidLoop Solidarian Pledge",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextLight
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "By listing and assisting, you pledge to respect physical, clinical, and transactional boundaries. Let's keep Pune safe.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
