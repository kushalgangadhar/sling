package com.kushalgangadhar.sling.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors


class PlayerController(private val context: Context) {
    
    private var controllerFuture: ListenableFuture<MediaController>? = null
    var mediaController: MediaController? = null
        private set

    // Call this when your App or ViewModel starts
    fun initialize() {
        val sessionToken = SessionToken(
            context, 
            ComponentName(context, PlaybackService::class.java)
        )
        
        // Build the controller asynchronously 
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            // Once connected, assign it to our local variable
            mediaController = controllerFuture?.get()
        }, MoreExecutors.directExecutor())
    }

    // Helper function to play a specific URI
    fun playSong(uriString: String) {
        val mediaItem = MediaItem.fromUri(uriString)
        mediaController?.setMediaItem(mediaItem)
        mediaController?.prepare()
        mediaController?.play()
    }
    
    // Helper function to pause
    fun pause() {
        mediaController?.pause()
    }

    // Call this when your App or ViewModel is destroyed to prevent memory leaks
    fun release() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
}
