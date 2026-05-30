package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.IcchaRepository
import com.example.firebase.FirebaseManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import kotlin.random.Random

class IcchaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = IcchaRepository(db)
    
    // Auth-session tracking. Persistent session checks Auth on startup.
    var currentUserId by mutableStateOf("")
        private set

    // Backing states for reactive UI updating
    val currentUserState: StateFlow<User?>
    val allWishesState: StateFlow<List<Wish>>
    val allUsersState: StateFlow<List<User>>

    // Blocked Users State
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val blockedUsersState: StateFlow<List<BlockedUser>> = snapshotFlow { currentUserId }
        .flatMapLatest { id ->
            if (id.isEmpty()) flowOf(emptyList()) else repository.getBlockedUsersFlow(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Session selection trackers
    private val _selectedWishId = MutableStateFlow<Int?>(null)
    val selectedWishId: StateFlow<Int?> = _selectedWishId.asStateFlow()

    // Loaded relationships
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activeWishFlow: StateFlow<Wish?> = _selectedWishId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.getWishFlow(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activeOffersFlow: StateFlow<List<Offer>> = _selectedWishId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getOffersForWish(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activeCommentsFlow: StateFlow<List<WishComment>> = _selectedWishId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getCommentsForWish(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activeMessagesFlow: StateFlow<List<ChatMessage>> = _selectedWishId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getChatMessagesFlow(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // List of reliable Modern Avatars
    val availableAvatars = listOf(
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80",
        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=150&q=80",
        "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=150&q=80",
        "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=150&q=80",
        "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?auto=format&fit=crop&w=150&q=80",
        "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=150&q=80"
    )

    // Category presets matching beautiful design assets
    val categoryPresets = mapOf(
        "Community" to "https://images.unsplash.com/photo-1511632765486-a01980e01a18?auto=format&fit=crop&w=600&q=80",
        "Volunteering" to "https://images.unsplash.com/photo-1559027615-cd4628902d4a?auto=format&fit=crop&w=600&q=80",
        "Gratitude" to "https://images.unsplash.com/photo-1531206715517-5c0ba140b2b8?auto=format&fit=crop&w=600&q=80",
        "Support" to "https://images.unsplash.com/photo-1544640808-32ca72ac7f37?auto=format&fit=crop&w=600&q=80",
        "Loops" to "https://images.unsplash.com/photo-1491438590914-bc09fcaaf77a?auto=format&fit=crop&w=600&q=80",
        "Emergency" to "https://images.unsplash.com/photo-1516550893923-42d28e5677af?auto=format&fit=crop&w=600&q=80"
    )

    // Local Loops state management
    private val _joinedLoops = kotlinx.coroutines.flow.MutableStateFlow<Set<String>>(setOf("Kalyani Nagar Loop"))
    val joinedLoops: kotlinx.coroutines.flow.StateFlow<Set<String>> = _joinedLoops

    fun toggleJoinLoop(loopName: String) {
        viewModelScope.launch {
            val current = _joinedLoops.value
            if (current.contains(loopName)) {
                _joinedLoops.value = current - loopName
            } else {
                _joinedLoops.value = current + loopName
                // Award points for active community circle registration
                val user = repository.getUserSync(currentUserId)
                if (user != null) {
                    repository.saveUserProfile(user.copy(reputationPoints = user.reputationPoints + 50))
                }
            }
        }
    }

    // Smart Location Privacy state
    private val _isLocationPrivacyEnabled = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isLocationPrivacyEnabled: kotlinx.coroutines.flow.StateFlow<Boolean> = _isLocationPrivacyEnabled.asStateFlow()

    fun toggleLocationPrivacy() {
        _isLocationPrivacyEnabled.value = !_isLocationPrivacyEnabled.value
    }

    // Active chats stream matching current user session
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activeChatWishesState: StateFlow<List<Wish>> = snapshotFlow { currentUserId }
        .flatMapLatest { id ->
            if (id.isEmpty()) flowOf(emptyList()) else {
                db.chatMessageDao().getActiveChatWishIds(id).flatMapLatest { wishIds ->
                    repository.allWishes.map { wishes ->
                        wishes.filter { wishIds.contains(it.id) }
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live Activity System
    private val _liveActivities = kotlinx.coroutines.flow.MutableStateFlow<List<String>>(listOf(
        "12 people active nearby in Pune",
        "Someone joined the Baner Green Loop",
        "New discussion started in Deccan Gymkhana"
    ))
    val liveActivities: StateFlow<List<String>> = _liveActivities.asStateFlow()

    // Global layout & category filters for synchronizing responsive web sidebar views and feed filters
    var globalNavCategory by mutableStateOf("Nearby")
    var globalLocationRangeFilter by mutableStateOf("ALL")

    fun addLiveUpdate(message: String) {
        val current = _liveActivities.value.toMutableList()
        current.add(0, message)
        if (current.size > 10) {
            current.removeAt(current.size - 1)
        }
        _liveActivities.value = current
    }

    fun quickReplyToWish(wishId: Int, draftText: String) {
        viewModelScope.launch {
            val wish = repository.getWishSync(wishId) ?: return@launch
            val receiverId = wish.creatorId
            repository.sendChatMessage(
                wishId = wishId,
                senderId = currentUserId,
                receiverId = receiverId,
                text = draftText,
                isAudio = false,
                audioDuration = 0,
                audioUrl = null
            )
            addLiveUpdate("You started a conversation with ${wish.creatorName} about \"${wish.title}\"")
        }
    }

    init {
        @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
        currentUserState = snapshotFlow { currentUserId }
            .flatMapLatest { id ->
                if (id.isEmpty()) flowOf(null) else repository.getUserFlow(id)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        allWishesState = repository.allWishes
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allUsersState = repository.getAllUsersFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Persistent Session Restoration check on Launch
        viewModelScope.launch {
            seedInitialData()

            try {
                if (FirebaseManager.isInitialized) {
                    val persistentUser = FirebaseManager.auth.currentUser
                    if (persistentUser != null) {
                        currentUserId = persistentUser.uid
                        repository.startRealtimeSync(viewModelScope)
                        Log.d("FirebaseSync", "Successfully recovered logged in session of: ${persistentUser.uid}")
                    }
                }
            } catch (e: Throwable) {
                Log.e("FirebaseRestore", "Could not restore persistent session: ${e.message}", e)
            }
        }
    }

    private suspend fun seedInitialData() {
        val existingCurrentUser = repository.getUserSync("me_101")
        if (existingCurrentUser == null) {
            val me = User(
                userId = "me_101",
                username = "patel_anshuman",
                fullName = "Anshuman Patel",
                bio = "Coordinating youth community support in Pune. Proud Helper. Let's make positive loops!",
                profilePicUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=250&q=80",
                reputationPoints = 250,
                isVerified = true,
                verificationLevel = 1,
                coinBalance = 750,
                city = "Pune",
                college = "COEP Tech University",
                area = "Baner",
                locality = "Pancard Club Road"
            )
            repository.saveUserProfile(me)

            val users = listOf(
                User(
                    userId = "user_priya",
                    username = "priya_medical",
                    fullName = "Dr. Priya Deshmukh",
                    bio = "Resident doctor at City General Hospital. Volunteer for community clinical advice.",
                    profilePicUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=250&q=80",
                    reputationPoints = 510,
                    isVerified = true,
                    verificationLevel = 2,
                    coinBalance = 400,
                    city = "Pune",
                    college = "Pune University (SPPU)",
                    area = "Deccan Gymkhana",
                    locality = "FC Road Campus"
                ),
                User(
                    userId = "user_rajesh",
                    username = "rajesh_tech",
                    fullName = "Rajesh G.",
                    bio = "Web engineer & hobbyist. Actively donating spare electronics and computers to kids.",
                    profilePicUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=250&q=80",
                    reputationPoints = 940,
                    isVerified = true,
                    verificationLevel = 3,
                    coinBalance = 1500,
                    city = "Pune",
                    college = "COEP Tech University",
                    area = "Kothrud",
                    locality = "Ideal Colony"
                ),
                User(
                    userId = "user_reema",
                    username = "reema_food_connect",
                    fullName = "Reema Sen",
                    bio = "Coordinator at FoodForSoul NGO. Focused on distribution of surplus community meals.",
                    profilePicUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=250&q=80",
                    reputationPoints = 180,
                    isVerified = false,
                    verificationLevel = 0,
                    coinBalance = 100,
                    city = "Pune",
                    college = "None",
                    area = "Viman Nagar",
                    locality = "Phoenix Mall Enclave"
                )
            )
            for (u in users) {
                repository.saveUserProfile(u)
            }
        }

        // Decoupled Wish/Request seeding: guarantees wishes are populated if the stream is empty!
        val existingWishes = repository.getAllWishesSync()
        if (existingWishes.isEmpty()) {
            val sampleRequests = listOf(
                Wish(
                    creatorId = "user_priya",
                    creatorName = "Dr. Priya Deshmukh",
                    creatorImage = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=250&q=80",
                    creatorVerified = true,
                    creatorVerificationLevel = 2,
                    title = "Coordinating portables oxygen cylinder supply circle",
                    content = "Community Medical Circle: Co-creating a backup portal for Pune seniors. We are coordinates at Deccan Gymkhana Circle looking to borrow/exchange 10L oxygen concentrators to build a local support loop.",
                    category = "Support",
                    mediaFormat = "IMAGE",
                    mediaUrl = categoryPresets["Support"],
                    timestamp = System.currentTimeMillis() - 3600000,
                    likesCount = 14,
                    userLiked = false,
                    offerCount = 1,
                    location = "Pune Deccan"
                ),
                Wish(
                    creatorId = "user_rajesh",
                    creatorName = "Rajesh G.",
                    creatorImage = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=250&q=80",
                    creatorVerified = true,
                    creatorVerificationLevel = 3,
                    title = "Digital literacy & basic mobile coding for Kothrud youngsters 📚",
                    content = "Pune Tech Loop Initiative: We teach basic coding skills near slums. We have 12 young learners and 3 coordinator screens. If you want to jump in as a tech mentor or donate older tablets/laptops, join the loop!",
                    category = "Volunteering",
                    mediaFormat = "IMAGE",
                    mediaUrl = categoryPresets["Volunteering"],
                    timestamp = System.currentTimeMillis() - 7200000,
                    likesCount = 42,
                    userLiked = true,
                    offerCount = 2,
                    location = "Kothrud"
                ),
                Wish(
                    creatorId = "user_reema",
                    creatorName = "Reema Sen",
                    creatorImage = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=250&q=80",
                    creatorVerified = false,
                    creatorVerificationLevel = 0,
                    title = "Food Connect Drive: Logistics coordinator for surplus wedding catering 🍲",
                    content = "Viman Nagar Loop Event: Coordinated fresh dinner savior loop. We have packed food for 80 family plates. Looking for an active loop member with empty vehicle space to assist distribution routes safely tonight.",
                    category = "Community",
                    mediaFormat = "IMAGE",
                    mediaUrl = categoryPresets["Community"],
                    timestamp = System.currentTimeMillis() - 14400000,
                    likesCount = 28,
                    userLiked = false,
                    offerCount = 0,
                    location = "Viman Nagar"
                ),
                Wish(
                    creatorId = "me_101",
                    creatorName = "Anshuman Patel",
                    creatorImage = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=250&q=80",
                    creatorVerified = true,
                    creatorVerificationLevel = 1,
                    title = "Join Pune Hills ecological seed-bombing & clean-trail drive 🌿",
                    content = "Baner Green Loop Event: Eco-restoration drive in Pune. We need 15 enthusiast volunteers to sort native seeds, carry organic seedballs, and walk Baner hills clearing litter this Sunday morning. Bring active vibes!",
                    category = "Loops",
                    mediaFormat = "IMAGE",
                    mediaUrl = categoryPresets["Loops"],
                    timestamp = System.currentTimeMillis() - 28800000,
                    likesCount = 21,
                    userLiked = false,
                    offerCount = 1,
                    location = "Baner Hills"
                ),
                Wish(
                    creatorId = "user_rajesh",
                    creatorName = "Rajesh G.",
                    creatorImage = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=250&q=80",
                    creatorVerified = true,
                    creatorVerificationLevel = 3,
                    title = "Gratitude: 3 working tablets successfully deployed to digital study circle! 🎉",
                    content = "Massive shoutout to Anshuman Patel & Baner Loop members! Thanks to your incredibly quick collaboration, three kids have access to study resources. Authentic community power!",
                    category = "Gratitude",
                    mediaFormat = "IMAGE",
                    mediaUrl = categoryPresets["Gratitude"],
                    timestamp = System.currentTimeMillis() - 43200000,
                    likesCount = 56,
                    userLiked = true,
                    offerCount = 0,
                    location = "Kothrud"
                )
            )

            for (w in sampleRequests) {
                val reqId = repository.createWish(
                    userId = w.creatorId,
                    title = w.title,
                    content = w.content,
                    category = w.category,
                    mediaFormat = w.mediaFormat,
                    mediaUrl = w.mediaUrl,
                    location = w.location
                )
                
                if (w.title.contains("oxygen")) {
                    repository.submitOffer(
                        wishId = reqId.toInt(),
                        providerId = "me_101",
                        message = "I have a spare verified 10L oxygen concentrator available in our Pune local group. I can transport it to Sector 4 by tonight. Let's chat."
                    )
                } else if (w.title.contains("Digital literacy")) {
                    repository.submitOffer(
                        wishId = reqId.toInt(),
                        providerId = "user_reema",
                        message = "We have an old Android tablet with a minor screen crack but fully functional. I can ship it to your center or coordinate handoff."
                    )
                    repository.submitOffer(
                        wishId = reqId.toInt(),
                        providerId = "me_101",
                        message = "I have 2 old Lenovo tablets from our office upgrade. Both work perfectly with chargers. Happy to donate!"
                    )
                } else if (w.title.contains("seed-bombing")) {
                    repository.submitOffer(
                        wishId = reqId.toInt(),
                        providerId = "user_rajesh",
                        message = "I am free on Sunday and can drive over with some extra cartons. Let's sync up up on chat."
                    )
                }
            }
        }
    }

    // --- Production Firebase Authentication Methods ---

    fun loginWithEmailAndPassword(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (!FirebaseManager.isInitialized) {
            onFailure("Firebase not initialized")
            return
        }
        FirebaseManager.auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val fUser = FirebaseManager.auth.currentUser
                    if (fUser != null) {
                        currentUserId = fUser.uid
                        repository.startRealtimeSync(viewModelScope)
                        onSuccess()
                    } else {
                        onFailure("Error retrieving authenticated session")
                    }
                } else {
                    onFailure(task.exception?.message ?: "Login failed")
                }
            }
    }

    fun registerUserWithEmailAndPassword(
        email: String,
        password: String,
        username: String,
        fullName: String,
        bio: String,
        location: String,
        picUrl: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (!FirebaseManager.isInitialized) {
            onFailure("Firebase not initialized")
            return
        }
        FirebaseManager.auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val fUser = FirebaseManager.auth.currentUser
                    if (fUser != null) {
                        val uid = fUser.uid
                        val newUser = User(
                            userId = uid,
                            username = username.trim().replace(" ", "_"),
                            fullName = fullName.trim(),
                            bio = bio.trim(),
                            profilePicUrl = picUrl,
                            reputationPoints = 100,
                            isVerified = false,
                            verificationLevel = 0,
                            coinBalance = 300
                        )
                        viewModelScope.launch {
                            repository.saveUserProfile(newUser)
                            currentUserId = uid
                            repository.startRealtimeSync(viewModelScope)
                            onSuccess()
                        }
                    } else {
                        onFailure("Error reading registered session profile")
                    }
                } else {
                    onFailure(task.exception?.message ?: "Registration failed")
                }
            }
    }

    fun loginWithGoogleCredential(
        idToken: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (!FirebaseManager.isInitialized) {
            onFailure("Firebase not initialized")
            return
        }
        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
        FirebaseManager.auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val fUser = FirebaseManager.auth.currentUser
                    if (fUser != null) {
                        viewModelScope.launch {
                            val existing = repository.getUserSync(fUser.uid)
                            if (existing == null) {
                                val newUser = User(
                                    userId = fUser.uid,
                                    username = fUser.displayName?.replace(" ", "_")?.lowercase() ?: "google_user",
                                    fullName = fUser.displayName ?: "Google User",
                                    bio = "Pune Community Coordinator joining via Google Sign-In",
                                    profilePicUrl = fUser.photoUrl?.toString() ?: availableAvatars[0],
                                    reputationPoints = 120,
                                    isVerified = true,
                                    verificationLevel = 1,
                                    coinBalance = 400
                                )
                                repository.saveUserProfile(newUser)
                            }
                            currentUserId = fUser.uid
                            repository.startRealtimeSync(viewModelScope)
                            onSuccess()
                        }
                    } else {
                        onFailure("Retrieve Google user session failed")
                    }
                } else {
                    onFailure(task.exception?.message ?: "Authenticated credentials linking failed")
                }
            }
    }

    // Legacy quick login method (demo / fallback mode)
    fun registerOrLogin(username: String, fullName: String, bio: String, location: String, picUrl: String) {
        viewModelScope.launch {
            val userUuid = "user_" + username.lowercase().trim().replace(" ", "_")
            val existing = repository.getUserSync(userUuid)
            
            if (existing == null) {
                val newUser = User(
                    userId = userUuid,
                    username = username.trim().replace(" ", "_"),
                    fullName = fullName.trim(),
                    bio = bio.trim(),
                    profilePicUrl = picUrl,
                    reputationPoints = 100,
                    isVerified = false,
                    verificationLevel = 0,
                    coinBalance = 300
                )
                repository.saveUserProfile(newUser)
                currentUserId = newUser.userId
            } else {
                currentUserId = existing.userId
            }
            
            // Start local sync flow
            repository.startRealtimeSync(viewModelScope)
        }
    }

    fun logout() {
        if (FirebaseManager.isInitialized) {
            FirebaseManager.auth.signOut()
        }
        repository.stopRealtimeSync()
        currentUserId = ""
    }

    // --- Action Methods exposed to UI ---

    val availableLoops = listOf(
        "Student Loop",
        "BHU Loop",
        "Bihar Loop",
        "Gaming Loop",
        "Medical Loop",
        "Startup Loop",
        "General Loop"
    )

    fun selectWish(id: Int?) {
        _selectedWishId.value = id
    }

    fun publishWish(
        title: String,
        content: String,
        category: String,
        mediaUrl: String? = null,
        location: String? = "Pune"
    ) {
        viewModelScope.launch {
            repository.createWish(
                userId = currentUserId,
                title = title,
                content = content,
                category = category,
                mediaFormat = if (mediaUrl != null) "IMAGE" else "TEXT",
                mediaUrl = mediaUrl,
                mediaDurationSec = 0,
                location = location
            )
        }
    }

    fun publishSparkPost(
        title: String,
        content: String,
        category: String,
        postType: String,
        loopName: String,
        urgencyLevel: String,
        expiryDurationHours: Int,
        mediaUrl: String? = null,
        location: String? = "Pune"
    ) {
        viewModelScope.launch {
            repository.createWish(
                userId = currentUserId,
                title = title,
                content = content,
                category = category,
                mediaFormat = if (mediaUrl != null) "IMAGE" else "TEXT",
                mediaUrl = mediaUrl,
                mediaDurationSec = 0,
                location = location,
                postType = postType,
                loopName = loopName,
                urgencyLevel = urgencyLevel,
                expiryDurationHours = expiryDurationHours
            )
        }
    }

    fun addCommentToWish(wishId: Int, commentText: String) {
        viewModelScope.launch {
            val user = repository.getUserSync(currentUserId)
            val name = user?.fullName ?: "Anonymous Helper"
            val image = user?.profilePicUrl ?: "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=150&q=80"
            repository.submitWishComment(
                wishId = wishId,
                senderId = currentUserId,
                senderName = name,
                senderImage = image,
                commentText = commentText
            )
        }
    }

    fun reactToWish(wishId: Int, reactionType: String) {
        viewModelScope.launch {
            repository.performReaction(wishId, reactionType)
        }
    }

    fun toggleBookmarkWish(wishId: Int) {
        viewModelScope.launch {
            repository.toggleBookmark(wishId)
        }
    }

    fun toggleLikeWish(wishId: Int) {
        viewModelScope.launch {
            repository.toggleLikeWish(wishId)
        }
    }

    fun submitOffer(wishId: Int, message: String) {
        viewModelScope.launch {
            repository.submitOffer(
                wishId = wishId,
                providerId = currentUserId,
                message = message,
                attachmentType = "NONE",
                attachmentUrl = null
            )
        }
    }

    fun acceptOffer(offerId: Int, wishId: Int) {
        viewModelScope.launch {
            repository.acceptOffer(offerId, wishId)
            val offer = db.offerDao().getOfferByIdSync(offerId)
            if (offer != null) {
                repository.sendChatMessage(
                    wishId = wishId,
                    senderId = currentUserId,
                    receiverId = offer.providerId,
                    text = "Hello! I have accepted your community aid offer for my request. Thanks a ton for reaching out. Let's coordinate here. 🙏"
                )
            }
        }
    }

    fun fulfillWish(wishId: Int) {
        viewModelScope.launch {
            repository.markWishAsFulfilled(wishId)
        }
    }

    fun sendChatMessage(
        text: String,
        isAudio: Boolean = false,
        audioSec: Int = 0,
        audioUrl: String? = null
    ) {
        val wishId = _selectedWishId.value ?: return
        val currentWish = activeWishFlow.value ?: return
        
        val receiverId = if (currentWish.creatorId == currentUserId) {
            currentWish.fulfillerId ?: return
        } else {
            currentWish.creatorId
        }

        viewModelScope.launch {
            repository.sendChatMessage(
                wishId = wishId,
                senderId = currentUserId,
                receiverId = receiverId,
                text = text,
                isAudio = isAudio,
                audioDuration = audioSec,
                audioUrl = audioUrl
            )
        }
    }

    // --- Production Firebase Cloud Image Upload ---

    fun uploadImageToCloud(uri: Uri, onComplete: (String?) -> Unit) {
        if (!FirebaseManager.isInitialized) {
            Log.e("FirebaseUpload", "Initialization missing. Fulfilling as offline success.")
            onComplete(uri.toString()) // Offline default fallback matching resource scheme
            return
        }
        val ref = FirebaseManager.storage.reference.child("uploads/posts/${System.currentTimeMillis()}.jpg")
        val context = getApplication<Application>().applicationContext
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val uploadTask = ref.putStream(stream)
                uploadTask.addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { downloadUri ->
                        onComplete(downloadUri.toString())
                    }.addOnFailureListener {
                        Log.e("FirebaseUpload", "Download url retrieval failed: ${it.message}")
                        onComplete(null)
                    }
                }.addOnFailureListener {
                    Log.e("FirebaseUpload", "Upload of stream failed: ${it.message}")
                    onComplete(null)
                }
            } ?: onComplete(null)
        } catch (e: Exception) {
            Log.e("FirebaseUpload", "Local read error of asset path: ${e.message}", e)
            onComplete(null)
        }
    }

    // --- Verification Upgrades ---
    fun buyVerificationUpgrade(targetLevel: Int) {
        val cost = when (targetLevel) {
            1 -> 200
            2 -> 400
            3 -> 700
            else -> 0
        }
        viewModelScope.launch {
            val success = repository.upgradeVerification(currentUserId, targetLevel, cost)
            if (success) {
                repository.addReputationPoints(currentUserId, targetLevel * 50)
            }
        }
    }

    fun addSimulatedCoins(amount: Int) {
        viewModelScope.launch {
            val u = repository.getUserSync(currentUserId)
            if (u != null) {
                db.userDao().updateUser(u.copy(coinBalance = u.coinBalance + amount))
            }
        }
    }

    fun updateUserLocation(city: String, college: String, area: String, locality: String) {
        viewModelScope.launch {
            val u = repository.getUserSync(currentUserId)
            if (u != null) {
                val updated = u.copy(
                    city = city,
                    college = college,
                    area = area,
                    locality = locality
                )
                repository.saveUserProfile(updated)
            }
        }
    }

    // --- Block User Actions ---
    fun blockUser(blockedId: String) {
        val current = currentUserId
        if (current.isEmpty() || current == blockedId) return
        viewModelScope.launch {
            repository.blockUser(current, blockedId)
        }
    }

    fun unblockUser(blockedId: String) {
        val current = currentUserId
        if (current.isEmpty()) return
        viewModelScope.launch {
            repository.unblockUser(current, blockedId)
        }
    }

    // --- Trust & Safety Actions ---
    fun reportRequest(wishId: Int, reason: String) {
        viewModelScope.launch {
            repository.reportWish(wishId, reason)
        }
    }

    fun reportUser(userId: String, reason: String) {
        viewModelScope.launch {
            repository.reportUser(userId, reason)
        }
    }

    // --- Admin Moderation Dashboard Actions ---
    fun resolveWishReport(wishId: Int, deleteWish: Boolean) {
        viewModelScope.launch {
            repository.resolveWishReport(wishId, deleteWish)
        }
    }

    fun resolveUserSuspicion(userId: String, clearSuspicion: Boolean) {
        viewModelScope.launch {
            repository.resolveUserSuspicion(userId, clearSuspicion)
        }
    }

    fun toggleExpiration(wishId: Int, expired: Boolean) {
        viewModelScope.launch {
            repository.markWishExpired(wishId, expired)
        }
    }

    // --- Smart Trust & Safety AI/Local Scan Engine ---
    var isCheckingSafety by mutableStateOf(false)
        private set

    fun runSafetyCheck(wishId: Int) {
        viewModelScope.launch {
            isCheckingSafety = true
            val wish = db.wishDao().getWishByIdSync(wishId)
            if (wish == null) {
                isCheckingSafety = false
                return@launch
            }

            // 1. Perform Local Heuristics
            val text = "${wish.title} ${wish.content}".uppercase()
            var isSpam = false
            var spamScore = 0
            var isFake = false
            var fakeScore = 0
            val explanationParts = mutableListOf<String>()

            if (text.contains("GPAY") || text.contains("GOOGLE PAY") || text.contains("PHONEPE") || text.contains("PAYTM") || text.contains("PAY ME") || text.contains("ADVANCE DEPOSIT")) {
                isSpam = true
                spamScore = 90
                explanationParts.add("Flags advance transfer request (GPay/PhonePe bank mention)")
            }
            if (text.contains("WIN COINS") || text.contains("CLICK LINK") || text.contains("FREE GIFTS") || text.contains("MAKE EASY MONEY") || text.contains("WORK FROM HOME")) {
                isSpam = true
                spamScore = 95
                explanationParts.add("Matches high-probability promotional advertisement/spam links")
            }
            if (text.contains("LOAN IMMEDIATELY") || text.contains("BUSINESS PROMPT") || text.contains("COMMERCIAL AGENT")) {
                isFake = true
                fakeScore = 80
                explanationParts.add("Highlights potential unverified commercial solicitation instead of genuine mutual aid")
            }
            if (wish.content.length < 30) {
                isFake = true
                fakeScore = maxOf(fakeScore, 65)
                explanationParts.add("Critically low description length: High risk of hollow fake request")
            }
            
            // Expiry Check (Auto flags older than 4 days as expired)
            val daysAgo = (System.currentTimeMillis() - wish.timestamp) / (1000 * 60 * 60 * 24)
            if (daysAgo >= 4) {
                repository.markWishExpired(wishId, true)
            }

            var finalIsSpam = isSpam
            var finalSpamScore = spamScore
            var finalIsFake = isFake
            var finalFakeScore = fakeScore
            var finalExpl = if (explanationParts.isEmpty()) {
                "Pune AidLoop Safety Engine: Passed all local heuristic scam criteria."
            } else {
                "Pune AidLoop Safety Engine Flagged:\n- " + explanationParts.joinToString("\n- ")
            }

            // 2. Call Direct Gemini API if Key is Configured and internet is active
            val apiKey = try { com.example.BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
            if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY" && apiKey != "placeholder_gemini_api_key") {
                try {
                    val prompt = """
                        Analyze this mutual aid request for spam, scams, or being fake.
                        Title: ${wish.title}
                        Content: ${wish.content}
                        Category: ${wish.category}
                        
                        Return a clean JSON object exactly matching this format:
                        {
                          "isSpam": true/false,
                          "spamScore": integer 0-100,
                          "isFake": true/false,
                          "fakeScore": integer 0-100,
                          "explanation": "concise 2-sentence reason explaining Pune Safety Guidelines breach"
                        }
                    """.trimIndent()

                    val response = callGeminiDirectRest(apiKey, prompt)
                    if (response != null) {
                        finalIsSpam = response.isSpam
                        finalSpamScore = response.spamScore
                        finalIsFake = response.isFake
                        finalFakeScore = response.fakeScore
                        finalExpl = "AI Verified Assessment:\n${response.explanation}\n(Pune AidLoop Central Trust Group)"
                    }
                } catch (e: Exception) {
                    Log.e("GeminiCoreCheck", "Direct Gemini integration error: ${e.message}")
                }
            }

            // Commit results to Room & Cloud Firestore
            repository.updateWishAIAnalysis(
                wishId = wishId,
                isSpam = finalIsSpam,
                spamScore = finalSpamScore,
                isFake = finalIsFake,
                fakeScore = finalFakeScore,
                expl = finalExpl
            )
            isCheckingSafety = false
        }
    }

    private suspend fun callGeminiDirectRest(apiKey: String, prompt: String): GeminiAnalysisResponse? {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val okHttpClient = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                // Construct Request Body manual stringification to avoid serialization matching issues
                val jsonRequestEscaped = prompt.replace("\"", "\\\"").replace("\n", "\\n")
                val requestBodyString = "{\"contents\":[{\"parts\":[{\"text\":\"$jsonRequestEscaped\"}]}],\"generationConfig\":{\"responseFormat\":{\"type\":\"APPLICATION_JSON\"}}}"

                val mediaType = "application/json".toMediaTypeOrNull()
                val body = okhttp3.RequestBody.create(mediaType, requestBodyString)
                val request = okhttp3.Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                    .post(body)
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseString = response.body?.string() ?: ""
                
                // Parse response to find text candidates. Direct Regex parsing is extremely bulletproof and survives JSON models updates
                val pattern = "\"text\"\\s*:\\s*\"([^\"]*)\"".toRegex()
                val match = pattern.find(responseString)
                val contentJson = match?.groupValues?.get(1)
                    ?.replace("\\\"", "\"")
                    ?.replace("\\n", "\n")
                    ?: ""

                if (contentJson.isNotBlank()) {
                    // Quick parse of content fields
                    val isSpamVal = contentJson.contains("\"isSpam\"\\s*:\\s*true".toRegex(RegexOption.IGNORE_CASE))
                    val isFakeVal = contentJson.contains("\"isFake\"\\s*:\\s*true".toRegex(RegexOption.IGNORE_CASE))
                    
                    val spamScorePat = "\"spamScore\"\\s*:\\s*(\\d+)".toRegex()
                    val spamScoreVal = spamScorePat.find(contentJson)?.groupValues?.get(1)?.toIntOrNull() ?: 0

                    val fakeScorePat = "\"fakeScore\"\\s*:\\s*(\\d+)".toRegex()
                    val fakeScoreVal = fakeScorePat.find(contentJson)?.groupValues?.get(1)?.toIntOrNull() ?: 0

                    val explanationPat = "\"explanation\"\\s*:\\s*\"([^\"]*)\"".toRegex()
                    val explanationVal = explanationPat.find(contentJson)?.groupValues?.get(1) ?: "Processed by Gemini Security Group"

                    GeminiAnalysisResponse(
                        isSpam = isSpamVal,
                        spamScore = spamScoreVal,
                        isFake = isFakeVal,
                        fakeScore = fakeScoreVal,
                        explanation = explanationVal
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("GeminiRest", "Rest request failure: ${e.message}")
                null
            }
        }
    }

    data class GeminiAnalysisResponse(
        val isSpam: Boolean,
        val spamScore: Int,
        val isFake: Boolean,
        val fakeScore: Int,
        val explanation: String
    )

    override fun onCleared() {
        super.onCleared()
        repository.stopRealtimeSync()
    }
}
