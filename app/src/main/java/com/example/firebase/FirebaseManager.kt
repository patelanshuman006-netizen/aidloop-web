package com.example.firebase

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object FirebaseManager {
    private const val TAG = "FirebaseManager"
    
    var isInitialized = false
        private set

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            var appInitialized = false
            // Check if FirebaseApp is already initialized (e.g. via google-services.json)
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                Log.d(TAG, "Firebase initialized automatically via plugin/metadata")
                appInitialized = true
            } else {
                // Retrieve credentials injected from Secrets
                val apiKey = BuildConfig.FIREBASE_API_KEY
                val appId = BuildConfig.FIREBASE_APPLICATION_ID
                val projectId = BuildConfig.FIREBASE_PROJECT_ID
                val storageBucket = BuildConfig.FIREBASE_STORAGE_BUCKET

                if (apiKey.isNullOrBlank() || apiKey.startsWith("placeholder") ||
                    appId.isNullOrBlank() || appId.startsWith("1:000000000000") ||
                    projectId.isNullOrBlank() || projectId.startsWith("placeholder")
                ) {
                    Log.w(TAG, "Using sandbox/local testing options for manual Firebase initialization.")
                    // Set fallback options so SDK doesn't crash on instantiation if user hasn't put credentials yet
                    val fallbackOptions = FirebaseOptions.Builder()
                        .setApiKey("placeholder-api-key-pune-aidloop-1090382012")
                        .setApplicationId("1:000000000000:android:0000000000000000")
                        .setProjectId("placeholder-aidloop")
                        .setStorageBucket("placeholder-aidloop.appspot.com")
                        .build()
                    FirebaseApp.initializeApp(context, fallbackOptions)
                    appInitialized = true
                } else {
                    val options = FirebaseOptions.Builder()
                        .setApiKey(apiKey)
                        .setApplicationId(appId)
                        .setProjectId(projectId)
                        .apply {
                            if (!storageBucket.isNullOrBlank()) {
                                setStorageBucket(storageBucket)
                            }
                        }
                        .build()
                    FirebaseApp.initializeApp(context, options)
                    appInitialized = true
                    Log.d(TAG, "Firebase programmatically initialized with user credentials.")
                }
            }

            if (appInitialized) {
                // Verify that we can obtain the Firebase services without throwing class loader, SDK, or runtime exceptions
                try {
                    FirebaseAuth.getInstance()
                    FirebaseFirestore.getInstance()
                    FirebaseStorage.getInstance()
                    isInitialized = true
                    Log.d(TAG, "Firebase services validated and verified successfully!")
                } catch (e: Throwable) {
                    Log.e(TAG, "Firebase service instance validation failed: ${e.message}. Disabling Firebase connection to fallback on local storage.", e)
                    isInitialized = false
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed loading Firebase app or config credentials: ${e.message}", e)
            isInitialized = false
        }
    }

    val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    val firestore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    val storage: FirebaseStorage
        get() = FirebaseStorage.getInstance()
}
