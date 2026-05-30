package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a user in the AidLoop Platform.
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey val userId: String = "",
    val username: String = "",
    val fullName: String = "",
    val bio: String = "",
    val profilePicUrl: String = "",
    val reputationPoints: Int = 0,
    val isVerified: Boolean = false,
    val verificationLevel: Int = 0, // 0 = None, 1 = Silver, 2 = Gold, 3 = Cosmic Platinum
    val coinBalance: Int = 500, // Demo physical balance for verification unlock simulation
    val isSuspicious: Boolean = false,
    val suspiciousReason: String? = null,
    val reportCount: Int = 0,
    
    // Social & Gamification enhancements
    val streakCount: Int = 0,
    val bookmarkedIdsJson: String = "[]",
    val achievementsJson: String = "[]", // E.g., '["First Spark", "Active Citizen", "Local Savior"]'
    
    // Hyperlocal select configuration (Approximate and private matching)
    val city: String = "Pune",
    val college: String = "Pune University (SPPU)",
    val area: String = "Kothrud",
    val locality: String = "Ideal Colony"
)

/**
 * Represents a Spark post or activity inside a loop.
 */
@Entity(tableName = "wishes")
data class Wish(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val creatorId: String = "",
    val creatorName: String = "",
    val creatorImage: String = "",
    val creatorVerified: Boolean = false,
    val creatorVerificationLevel: Int = 0,
    val title: String = "",
    val content: String = "",
    val category: String = "", 
    val mediaFormat: String = "TEXT", 
    val mediaUrl: String? = null, 
    val mediaDurationSec: Int = 0, 
    val fulfillStatus: String = "OPEN", // "OPEN", "ACCEPTED", "FULFILLED", "CLOSED"
    val fulfillerId: String? = null,
    val fulfillerName: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val userLiked: Boolean = false,
    val offerCount: Int = 0,
    val location: String? = "Mumbai",
    val isReported: Boolean = false,
    val reportReason: String? = null,
    val reportCount: Int = 0,
    val isSpamDetected: Boolean = false,
    val spamScore: Int = 0, // 0 to 100 percentage
    val isFakeDetected: Boolean = false,
    val fakeScore: Int = 0, // 0 to 100 percentage
    val detectionExplanation: String? = null,
    val isEmergency: Boolean = false, // Deprecated, mapped to urgencyLevel == "EMERGENCY"
    val isExpired: Boolean = false,
    
    // Core Evolved Metadata
    val postType: String = "REQUEST", // "REQUEST", "ADVICE", "LISTING", "OFFER", "UPDATE"
    val loopName: String = "General", // Loop grouping e.g. "Student Loop", "BHU Loop", " Bihar Loop"
    val urgencyLevel: String = "CASUAL", // "EMERGENCY", "URGENT", "NEEDED_SOON", "CASUAL"
    val expiryTimestamp: Long = 0L, // 0 means never expires, otherwise timestamp
    val reactionsJson: String = "{}", // Map of reactions: E.g., '{"love":0, "fire":0, "clap":0, "support":0}'
    val bookmarked: Boolean = false,
    val commentsCount: Int = 0,

    // Hyperlocal parameters mapped from creator's active bubble or manually selected
    val city: String = "Pune",
    val college: String = "Pune University (SPPU)",
    val area: String = "Kothrud",
    val locality: String = "Ideal Colony"
)

/**
 * Represents a user blocking another user.
 */
@Entity(tableName = "blocked_users", primaryKeys = ["blockerId", "blockedId"])
data class BlockedUser(
    val blockerId: String,
    val blockedId: String
)

/**
 * Represents an offer/solution submitted to fulfill a specific post (e.g. volunteering or help).
 */
@Entity(tableName = "offers")
data class Offer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val wishId: Int = 0,
    val providerId: String = "",
    val providerName: String = "",
    val providerImage: String = "",
    val providerVerified: Boolean = false,
    val providerPoints: Int = 0,
    val offerMessage: String = "",
    val attachmentType: String = "NONE", // "NONE", "IMAGE", "LINK"
    val attachmentUrl: String? = null,
    val isAccepted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Represents a chat message between sender and receiver.
 */
@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val wishId: Int = 0,
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isAudio: Boolean = false,
    val audioDurationSec: Int = 0,
    val audioUrl: String? = null
)

/**
 * Represents a comment on a Spark/Wish post.
 */
@Entity(tableName = "wish_comments")
data class WishComment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val wishId: Int = 0,
    val senderId: String = "",
    val senderName: String = "",
    val senderImage: String = "",
    val commentText: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Represents an anonymous discussion post (kept for backwards-compatibility unit tests or fallback discussions).
 */
@Entity(tableName = "anon_posts")
data class AnonPost(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String,
    val anonymousNickname: String, 
    val avatarColorSeed: Int, 
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val userLiked: Boolean = false,
    val commentCount: Int = 0
)

/**
 * Represents comments on anonymous discussion threads.
 */
@Entity(tableName = "anon_comments")
data class AnonComment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val postId: Int,
    val content: String,
    val anonymousNickname: String,
    val avatarColorSeed: Int,
    val timestamp: Long = System.currentTimeMillis()
)
