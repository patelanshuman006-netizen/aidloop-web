package com.example.data.repository

import android.util.Log
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.firebase.FirebaseManager
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class IcchaRepository(private val db: AppDatabase) {

    companion object {
        private val idCounter = java.util.concurrent.atomic.AtomicInteger(0)
    }

    private val userDao = db.userDao()
    private val wishDao = db.wishDao()
    private val offerDao = db.offerDao()
    private val chatMessageDao = db.chatMessageDao()
    private val anonPostDao = db.anonPostDao()
    private val blockedUserDao = db.blockedUserDao()
    private val wishCommentDao = db.wishCommentDao()

    private val activeListeners = mutableListOf<ListenerRegistration>()

    // --- Real-time Bidirectional Firestore Synchronization Engine ---
    fun startRealtimeSync(scope: CoroutineScope) {
        if (!FirebaseManager.isInitialized) {
            Log.e("FirebaseSync", "Firebase is not initialized. Sync engine skipped.")
            return
        }

        try {
            stopRealtimeSync() // Clean up any stale listeners first

            // 1. Synchronize Users collection
            val userReg = FirebaseManager.firestore.collection("users")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("FirebaseSync", "Users list sync error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        scope.launch {
                            for (doc in snapshot.documents) {
                                val user = doc.toObject(User::class.java)
                                if (user != null) {
                                    userDao.insertUser(user)
                                }
                            }
                        }
                    }
                }
            activeListeners.add(userReg)

            // 2. Synchronize Wishes (Request Feed) collection
            val wishReg = FirebaseManager.firestore.collection("wishes")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("FirebaseSync", "Wishes list sync error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        scope.launch {
                            for (doc in snapshot.documents) {
                                val wish = doc.toObject(Wish::class.java)
                                if (wish != null) {
                                    wishDao.insertWish(wish)
                                }
                            }
                        }
                    }
                }
            activeListeners.add(wishReg)

            // 3. Synchronize Offers collection
            val offerReg = FirebaseManager.firestore.collection("offers")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("FirebaseSync", "Offers list sync error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        scope.launch {
                            for (doc in snapshot.documents) {
                                val offer = doc.toObject(Offer::class.java)
                                if (offer != null) {
                                    offerDao.insertOffer(offer)
                                }
                            }
                        }
                    }
                }
            activeListeners.add(offerReg)

            // 4. Synchronize Chat Messages collection
            val chatReg = FirebaseManager.firestore.collection("chat_messages")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("FirebaseSync", "Chat messages list sync error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        scope.launch {
                            for (doc in snapshot.documents) {
                                val msg = doc.toObject(ChatMessage::class.java)
                                if (msg != null) {
                                    chatMessageDao.insertMessage(msg)
                                }
                            }
                        }
                    }
                }
            activeListeners.add(chatReg)

            // 5. Synchronize Wish Comments collection
            val commentReg = FirebaseManager.firestore.collection("wish_comments")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("FirebaseSync", "Wish comments list sync error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        scope.launch {
                            for (doc in snapshot.documents) {
                                val comm = doc.toObject(WishComment::class.java)
                                if (comm != null) {
                                    wishCommentDao.insertComment(comm)
                                }
                            }
                        }
                    }
                }
            activeListeners.add(commentReg)

            Log.d("FirebaseSync", "Successfully established real-time bidirectional cloud sync.")
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error setting up snapshots: ${e.message}", e)
        }
    }

    fun stopRealtimeSync() {
        for (reg in activeListeners) {
            reg.remove()
        }
        activeListeners.clear()
        Log.d("FirebaseSync", "Real-time cloud database snapshot listeners detached cleanly.")
    }

    // --- User Profile Commands ---
    fun getUserFlow(userId: String): Flow<User?> = userDao.getUserById(userId)
    
    fun getAllUsersFlow(): Flow<List<User>> = userDao.getAllUsers()

    suspend fun getUserSync(userId: String): User? = userDao.getUserByIdSync(userId)

    suspend fun saveUserProfile(user: User) {
        userDao.insertUser(user)
        if (FirebaseManager.isInitialized) {
            try {
                FirebaseManager.firestore.collection("users").document(user.userId).set(user)
            } catch (e: Exception) {
                Log.e("FirebaseSave", "Firestore user profiles write error: ${e.message}")
            }
        }
    }

    suspend fun deductCoins(userId: String, amount: Int): Boolean {
        val user = userDao.getUserByIdSync(userId) ?: return false
        if (user.coinBalance >= amount) {
            val updatedUser = user.copy(coinBalance = user.coinBalance - amount)
            userDao.insertUser(updatedUser)
            if (FirebaseManager.isInitialized) {
                try {
                    FirebaseManager.firestore.collection("users").document(userId).set(updatedUser)
                } catch (e: Exception) {
                    Log.e("FirebaseSave", "Coins deduction error: ${e.message}")
                }
            }
            return true
        }
        return false
    }

    suspend fun addReputationPoints(userId: String, points: Int) {
        val user = userDao.getUserByIdSync(userId) ?: return
        val updatedUser = user.copy(reputationPoints = user.reputationPoints + points)
        userDao.insertUser(updatedUser)
        if (FirebaseManager.isInitialized) {
            try {
                FirebaseManager.firestore.collection("users").document(userId).set(updatedUser)
            } catch (e: Exception) {
                Log.e("FirebaseSave", "Reputation points bump error: ${e.message}")
            }
        }
    }

    suspend fun upgradeVerification(userId: String, level: Int, cost: Int): Boolean {
        val user = userDao.getUserByIdSync(userId) ?: return false
        if (user.coinBalance >= cost) {
            val updatedUser = user.copy(
                coinBalance = user.coinBalance - cost,
                isVerified = true,
                verificationLevel = level
            )
            userDao.insertUser(updatedUser)
            if (FirebaseManager.isInitialized) {
                try {
                    FirebaseManager.firestore.collection("users").document(userId).set(updatedUser)
                } catch (e: Exception) {
                    Log.e("FirebaseSave", "Verification Level write error: ${e.message}")
                }
            }
            return true
        }
        return false
    }

    // --- Wish List Instructions ---
    val allWishes: Flow<List<Wish>> = wishDao.getAllWishes()

    suspend fun getAllWishesSync(): List<Wish> = wishDao.getAllWishesSync()

    fun getWishFlow(id: Int): Flow<Wish?> = wishDao.getWishById(id)

    suspend fun getWishSync(id: Int): Wish? = wishDao.getWishByIdSync(id)

    suspend fun createWish(
        userId: String,
        title: String,
        content: String,
        category: String,
        mediaFormat: String,
        mediaUrl: String? = null,
        mediaDurationSec: Int = 0,
        location: String? = "Pune",
        postType: String = "REQUEST",
        loopName: String = "General",
        urgencyLevel: String = "CASUAL",
        expiryDurationHours: Int = 0
    ): Long {
        val user = userDao.getUserByIdSync(userId)
        val creatorName = user?.fullName ?: "Anonymous Helper"
        val creatorImage = user?.profilePicUrl ?: "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=150&q=80"
        val creatorVerified = user?.isVerified ?: false
        val creatorVerificationLevel = user?.verificationLevel ?: 0

        val localId = (System.currentTimeMillis().toInt() and 0x7FFFFFFF) + idCounter.getAndIncrement()
        val isEmergencyCategory = category.equals("Emergency", ignoreCase = true) || urgencyLevel.equals("EMERGENCY", ignoreCase = true)
        
        val expiryTime = if (expiryDurationHours > 0) {
            System.currentTimeMillis() + (expiryDurationHours * 3600_000L)
        } else {
            0L
        }

        val newWish = Wish(
            id = localId,
            creatorId = userId,
            creatorName = creatorName,
            creatorImage = creatorImage,
            creatorVerified = creatorVerified,
            creatorVerificationLevel = creatorVerificationLevel,
            title = title,
            content = content,
            category = category,
            mediaFormat = mediaFormat,
            mediaUrl = mediaUrl,
            mediaDurationSec = mediaDurationSec,
            location = location,
            timestamp = System.currentTimeMillis(),
            isEmergency = isEmergencyCategory,
            
            postType = postType,
            loopName = loopName,
            urgencyLevel = urgencyLevel,
            expiryTimestamp = expiryTime,
            reactionsJson = "{}",
            bookmarked = false,
            commentsCount = 0,
            city = user?.city ?: "Pune",
            college = user?.college ?: "Pune University (SPPU)",
            area = user?.area ?: "Kothrud",
            locality = user?.locality ?: "Ideal Colony"
        )

        wishDao.insertWish(newWish)

        if (FirebaseManager.isInitialized) {
            try {
                FirebaseManager.firestore.collection("wishes").document(localId.toString()).set(newWish)
            } catch (e: Exception) {
                Log.e("FirebaseSave", "Create wish upload error: ${e.message}")
            }
        }
        return localId.toLong()
    }

    suspend fun toggleLikeWish(wishId: Int): Wish? {
        val wish = wishDao.getWishByIdSync(wishId) ?: return null
        val updated = wish.copy(
            userLiked = !wish.userLiked,
            likesCount = if (wish.userLiked) wish.likesCount - 1 else wish.likesCount + 1
        )
        wishDao.insertWish(updated)
        if (FirebaseManager.isInitialized) {
            try {
                FirebaseManager.firestore.collection("wishes").document(wishId.toString()).set(updated)
            } catch (e: Exception) {
                Log.e("FirebaseSave", "Like write error: ${e.message}")
            }
        }
        return updated
    }

    suspend fun performReaction(wishId: Int, reactionType: String): Wish? {
        val wish = wishDao.getWishByIdSync(wishId) ?: return null
        val currentJson = wish.reactionsJson.ifBlank { "{}" }
        val currentVal = try {
            val regex = """"$reactionType"\s*:\s*(\d+)""".toRegex()
            val match = regex.find(currentJson)
            match?.groupValues?.get(1)?.toInt() ?: 0
        } catch (e: Exception) { 0 }

        val newVal = currentVal + 1
        val updatedJson = if (currentJson.contains("\"$reactionType\"")) {
            currentJson.replace(""""$reactionType"\s*:\s*\d+""".toRegex(), "\"$reactionType\":$newVal")
        } else {
            if (currentJson == "{}") "{\"$reactionType\":$newVal}"
            else "${currentJson.dropLast(1)}, \"$reactionType\":$newVal}"
        }

        val updated = wish.copy(
            reactionsJson = updatedJson,
            likesCount = wish.likesCount + 1
        )
        wishDao.insertWish(updated)
        if (FirebaseManager.isInitialized) {
            try {
                FirebaseManager.firestore.collection("wishes").document(wishId.toString()).set(updated)
            } catch (e: Exception) {
                Log.e("FirebaseSave", "Reaction write error: ${e.message}")
            }
        }
        return updated
    }

    suspend fun toggleBookmark(wishId: Int): Wish? {
        val wish = wishDao.getWishByIdSync(wishId) ?: return null
        val updated = wish.copy(bookmarked = !wish.bookmarked)
        wishDao.insertWish(updated)
        if (FirebaseManager.isInitialized) {
            try {
                FirebaseManager.firestore.collection("wishes").document(wishId.toString()).set(updated)
            } catch (e: Exception) {
                Log.e("FirebaseSave", "Bookmark write error: ${e.message}")
            }
        }
        return updated
    }

    fun getCommentsForWish(wishId: Int): Flow<List<WishComment>> = wishCommentDao.getCommentsForWish(wishId)

    suspend fun submitWishComment(
        wishId: Int,
        senderId: String,
        senderName: String,
        senderImage: String,
        commentText: String
    ): WishComment {
        val commentId = (System.currentTimeMillis() and 0x7FFFFFFF).toInt()
        val newComment = WishComment(
            id = commentId,
            wishId = wishId,
            senderId = senderId,
            senderName = senderName,
            senderImage = senderImage,
            commentText = commentText,
            timestamp = System.currentTimeMillis()
        )
        wishCommentDao.insertComment(newComment)

        val wish = wishDao.getWishByIdSync(wishId)
        if (wish != null) {
            val updatedWish = wish.copy(commentsCount = wish.commentsCount + 1)
            wishDao.insertWish(updatedWish)
            if (FirebaseManager.isInitialized) {
                try {
                    FirebaseManager.firestore.collection("wishes").document(wishId.toString()).set(updatedWish)
                } catch (e: Exception) {
                    Log.e("FirebaseSave", "Comment count update fail: ${e.message}")
                }
            }
        }

        if (FirebaseManager.isInitialized) {
            try {
                FirebaseManager.firestore.collection("wish_comments").document(commentId.toString()).set(newComment)
            } catch (e: Exception) {
                Log.e("FirebaseSave", "Comment write error: ${e.message}")
            }
        }
        return newComment
    }

    // --- Offer Submission ---
    fun getOffersForWish(wishId: Int): Flow<List<Offer>> = offerDao.getOffersForWish(wishId)

    suspend fun submitOffer(
        wishId: Int,
        providerId: String,
        message: String,
        attachmentType: String = "NONE",
        attachmentUrl: String? = null
    ): Boolean {
        val provider = userDao.getUserByIdSync(providerId) ?: return false
        val offerId = System.currentTimeMillis().toInt() and 0x7FFFFFFF
        val newOffer = Offer(
            id = offerId,
            wishId = wishId,
            providerId = providerId,
            providerName = provider.fullName,
            providerImage = provider.profilePicUrl,
            providerVerified = provider.isVerified,
            providerPoints = provider.reputationPoints,
            offerMessage = message,
            attachmentType = attachmentType,
            attachmentUrl = attachmentUrl,
            timestamp = System.currentTimeMillis()
        )
        offerDao.insertOffer(newOffer)

        if (FirebaseManager.isInitialized) {
            try {
                FirebaseManager.firestore.collection("offers").document(offerId.toString()).set(newOffer)
            } catch (e: Exception) {
                Log.e("FirebaseSave", "Submit offer upload error: ${e.message}")
            }
        }

        // Increment offer count in Wish
        val wish = wishDao.getWishByIdSync(wishId)
        if (wish != null) {
            val updatedWish = wish.copy(offerCount = wish.offerCount + 1)
            wishDao.insertWish(updatedWish)
            if (FirebaseManager.isInitialized) {
                try {
                    FirebaseManager.firestore.collection("wishes").document(wishId.toString()).set(updatedWish)
                } catch (e: Exception) {
                    Log.e("FirebaseSave", "Wish offer counter increment error: ${e.message}")
                }
            }
        }
        return true
    }

    suspend fun acceptOffer(offerId: Int, wishId: Int): Boolean {
        val offer = offerDao.getOfferByIdSync(offerId) ?: return false
        val wish = wishDao.getWishByIdSync(wishId) ?: return false

        // Update offer to accepted state on Cloud + Local
        val updatedOffer = offer.copy(isAccepted = true)
        offerDao.insertOffer(updatedOffer)

        // Update wish status
        val updatedWish = wish.copy(
            fulfillStatus = "ACCEPTED",
            fulfillerId = offer.providerId,
            fulfillerName = offer.providerName
        )
        wishDao.insertWish(updatedWish)

        if (FirebaseManager.isInitialized) {
            try {
                FirebaseManager.firestore.collection("offers").document(offerId.toString()).set(updatedOffer)
                FirebaseManager.firestore.collection("wishes").document(wishId.toString()).set(updatedWish)
            } catch (e: Exception) {
                Log.e("FirebaseSave", "Offer acceptance upload error: ${e.message}")
            }
        }
        return true
    }

    suspend fun markWishAsFulfilled(wishId: Int): Boolean {
        val wish = wishDao.getWishByIdSync(wishId) ?: return false
        val fulfillerId = wish.fulfillerId ?: return false

        // Update wish status to FULFILLED locally & cloud
        val updatedWish = wish.copy(fulfillStatus = "FULFILLED")
        wishDao.insertWish(updatedWish)

        if (FirebaseManager.isInitialized) {
            try {
                FirebaseManager.firestore.collection("wishes").document(wishId.toString()).set(updatedWish)
            } catch (e: Exception) {
                Log.e("FirebaseSave", "Fulfill request update error: ${e.message}")
            }
        }

        // Give reputation rewards to the provider (+100) and creator (+10)
        addReputationPoints(fulfillerId, 100)
        addReputationPoints(wish.creatorId, 10)
        return true
    }

    // --- Chat Room Communications ---
    fun getChatMessagesFlow(wishId: Int): Flow<List<ChatMessage>> = chatMessageDao.getMessagesForWish(wishId)

    suspend fun sendChatMessage(
        wishId: Int,
        senderId: String,
        receiverId: String,
        text: String,
        isAudio: Boolean = false,
        audioDuration: Int = 0,
        audioUrl: String? = null
    ) {
        val msgId = System.currentTimeMillis().toInt() and 0x7FFFFFFF
        val chatMessage = ChatMessage(
            id = msgId,
            wishId = wishId,
            senderId = senderId,
            receiverId = receiverId,
            text = text,
            isAudio = isAudio,
            audioDurationSec = audioDuration,
            audioUrl = audioUrl,
            timestamp = System.currentTimeMillis()
        )
        chatMessageDao.insertMessage(chatMessage)

        if (FirebaseManager.isInitialized) {
            try {
                FirebaseManager.firestore.collection("chat_messages").document(msgId.toString()).set(chatMessage)
            } catch (e: Exception) {
                Log.e("FirebaseSave", "Send Chat message upload error: ${e.message}")
            }
        }
    }

    // --- Block User Methods ---
    fun getBlockedUsersFlow(blockerId: String): Flow<List<BlockedUser>> = blockedUserDao.getBlockedUsersFor(blockerId)
    suspend fun getBlockedUsersSync(blockerId: String): List<BlockedUser> = blockedUserDao.getBlockedUsersForSync(blockerId)
    
    suspend fun blockUser(blockerId: String, blockedId: String) {
        blockedUserDao.insertBlockedUser(BlockedUser(blockerId, blockedId))
    }

    suspend fun unblockUser(blockerId: String, blockedId: String) {
        blockedUserDao.deleteBlockedUser(blockerId, blockedId)
    }

    // --- Trust & Safety Updates ---
    suspend fun reportWish(wishId: Int, reason: String): Wish? {
        val wish = wishDao.getWishByIdSync(wishId) ?: return null
        val updated = wish.copy(
            isReported = true,
            reportReason = reason,
            reportCount = wish.reportCount + 1
        )
        wishDao.insertWish(updated)
        
        // Also deduct minor creator reputation (-10) for being reported
        val creator = userDao.getUserByIdSync(wish.creatorId)
        if (creator != null) {
            val updatedCreator = creator.copy(
                reputationPoints = maxOf(0, creator.reputationPoints - 10),
                reportCount = creator.reportCount + 1,
                isSuspicious = creator.reportCount + 1 >= 2 // mark suspicious after 2+ reports
            )
            userDao.insertUser(updatedCreator)
        }

        if (FirebaseManager.isInitialized) {
            try {
                FirebaseManager.firestore.collection("wishes").document(wishId.toString()).set(updated)
            } catch (e: Exception) {
                Log.e("FirebaseSave", "Report Wish upload error: ${e.message}")
            }
        }
        return updated
    }

    suspend fun reportUser(userId: String, reason: String) {
        val user = userDao.getUserByIdSync(userId) ?: return
        val updated = user.copy(
            reportCount = user.reportCount + 1,
            isSuspicious = true,
            suspiciousReason = reason,
            reputationPoints = maxOf(0, user.reputationPoints - 30) // Deduct reputation
        )
        userDao.insertUser(updated)
        if (FirebaseManager.isInitialized) {
            try {
                FirebaseManager.firestore.collection("users").document(userId).set(updated)
            } catch (e: Exception) {
                Log.e("FirebaseSave", "Report User write error: ${e.message}")
            }
        }
    }

    suspend fun resolveWishReport(wishId: Int, deleteWish: Boolean) {
        if (deleteWish) {
            wishDao.deleteWishById(wishId)
            if (FirebaseManager.isInitialized) {
                try {
                    FirebaseManager.firestore.collection("wishes").document(wishId.toString()).delete()
                } catch (e: Exception) {
                    Log.e("FirebaseSave", "Delete Wish error: ${e.message}")
                }
            }
        } else {
            val wish = wishDao.getWishByIdSync(wishId) ?: return
            val updated = wish.copy(isReported = false, reportReason = null)
            wishDao.insertWish(updated)
            if (FirebaseManager.isInitialized) {
                try {
                    FirebaseManager.firestore.collection("wishes").document(wishId.toString()).set(updated)
                } catch (e: Exception) {
                    Log.e("FirebaseSave", "Resolve report write error: ${e.message}")
                }
            }
        }
    }

    suspend fun resolveUserSuspicion(userId: String, clearSuspicion: Boolean) {
        val user = userDao.getUserByIdSync(userId) ?: return
        val updated = if (clearSuspicion) {
            user.copy(isSuspicious = false, suspiciousReason = null, reportCount = 0)
        } else {
            // Permanently flagged suspicious 
            user.copy(isSuspicious = true, suspiciousReason = "Permanently flagged by moderation staff", reputationPoints = 0)
        }
        userDao.insertUser(updated)
        if (FirebaseManager.isInitialized) {
            try {
                FirebaseManager.firestore.collection("users").document(userId).set(updated)
            } catch (e: Exception) {
                Log.e("FirebaseSave", "Resolve user exception write error: ${e.message}")
            }
        }
    }

    suspend fun markWishExpired(wishId: Int, expired: Boolean = true) {
        val wish = wishDao.getWishByIdSync(wishId) ?: return
        val updated = wish.copy(isExpired = expired)
        wishDao.insertWish(updated)
        if (FirebaseManager.isInitialized) {
            try {
                FirebaseManager.firestore.collection("wishes").document(wishId.toString()).set(updated)
            } catch (e: Exception) {
                Log.e("FirebaseSave", "Wish expiration write error: ${e.message}")
            }
        }
    }

    suspend fun updateWishAIAnalysis(wishId: Int, isSpam: Boolean, spamScore: Int, isFake: Boolean, fakeScore: Int, expl: String) {
        val wish = wishDao.getWishByIdSync(wishId) ?: return
        val updated = wish.copy(
            isSpamDetected = isSpam,
            spamScore = spamScore,
            isFakeDetected = isFake,
            fakeScore = fakeScore,
            detectionExplanation = expl
        )
        wishDao.insertWish(updated)
        if (FirebaseManager.isInitialized) {
            try {
                FirebaseManager.firestore.collection("wishes").document(wishId.toString()).set(updated)
            } catch (e: Exception) {
                Log.e("FirebaseSave", "Wish AI analysis write error: ${e.message}")
            }
        }
    }
}
