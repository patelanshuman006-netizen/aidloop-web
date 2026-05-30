package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.*
import com.example.data.model.*

@Database(
    entities = [
        User::class,
        Wish::class,
        Offer::class,
        ChatMessage::class,
        AnonPost::class,
        AnonComment::class,
        BlockedUser::class,
        WishComment::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun wishDao(): WishDao
    abstract fun offerDao(): OfferDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun anonPostDao(): AnonPostDao
    abstract fun blockedUserDao(): BlockedUserDao
    abstract fun wishCommentDao(): WishCommentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "iccha_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
