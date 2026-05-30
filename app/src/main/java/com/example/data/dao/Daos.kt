package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    fun getUserById(userId: String): Flow<User?>

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserByIdSync(userId: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>
}

@Dao
interface WishDao {
    @Query("SELECT * FROM wishes ORDER BY timestamp DESC")
    fun getAllWishes(): Flow<List<Wish>>

    @Query("SELECT * FROM wishes ORDER BY timestamp DESC")
    suspend fun getAllWishesSync(): List<Wish>

    @Query("SELECT * FROM wishes WHERE id = :id LIMIT 1")
    fun getWishById(id: Int): Flow<Wish?>

    @Query("SELECT * FROM wishes WHERE id = :id LIMIT 1")
    suspend fun getWishByIdSync(id: Int): Wish?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWish(wish: Wish): Long

    @Update
    suspend fun updateWish(wish: Wish)

    @Query("DELETE FROM wishes WHERE id = :id")
    suspend fun deleteWishById(id: Int)
}

@Dao
interface OfferDao {
    @Query("SELECT * FROM offers WHERE wishId = :wishId ORDER BY timestamp ASC")
    fun getOffersForWish(wishId: Int): Flow<List<Offer>>

    @Query("SELECT * FROM offers WHERE id = :id LIMIT 1")
    suspend fun getOfferByIdSync(id: Int): Offer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffer(offer: Offer)

    @Update
    suspend fun updateOffer(offer: Offer)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE wishId = :wishId ORDER BY timestamp ASC")
    fun getMessagesForWish(wishId: Int): Flow<List<ChatMessage>>

    @Query("SELECT DISTINCT wishId FROM chat_messages WHERE senderId = :userId OR receiverId = :userId")
    fun getActiveChatWishIds(userId: String): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)
}

@Dao
interface AnonPostDao {
    @Query("SELECT * FROM anon_posts ORDER BY timestamp DESC")
    fun getAllAnonPosts(): Flow<List<AnonPost>>

    @Query("SELECT * FROM anon_posts WHERE id = :id LIMIT 1")
    fun getAnonPostById(id: Int): Flow<AnonPost?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnonPost(post: AnonPost): Long

    @Update
    suspend fun updateAnonPost(post: AnonPost)

    @Query("SELECT * FROM anon_comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsForPost(postId: Int): Flow<List<AnonComment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: AnonComment)
}

@Dao
interface BlockedUserDao {
    @Query("SELECT * FROM blocked_users WHERE blockerId = :blockerId")
    fun getBlockedUsersFor(blockerId: String): Flow<List<BlockedUser>>

    @Query("SELECT * FROM blocked_users WHERE blockerId = :blockerId")
    suspend fun getBlockedUsersForSync(blockerId: String): List<BlockedUser>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedUser(blocked: BlockedUser)

    @Query("DELETE FROM blocked_users WHERE blockerId = :blockerId AND blockedId = :blockedId")
    suspend fun deleteBlockedUser(blockerId: String, blockedId: String)
}

@Dao
interface WishCommentDao {
    @Query("SELECT * FROM wish_comments WHERE wishId = :wishId ORDER BY timestamp ASC")
    fun getCommentsForWish(wishId: Int): Flow<List<WishComment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: WishComment)
}
