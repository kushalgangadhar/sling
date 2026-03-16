
package com.kushalgangadhar.sling

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.kushalgangadhar.sling.data.local.AppDatabase
import com.kushalgangadhar.sling.data.repository.MusicRepository
import com.kushalgangadhar.sling.player.PlayerController
import com.kushalgangadhar.sling.ui.screens.FolderPickerScreen
import com.kushalgangadhar.sling.viewmodels.LibraryViewModel
import com.kushalgangadhar.sling.viewmodels.LibraryViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var playerController: PlayerController
    private lateinit var repository: MusicRepository
    private lateinit var libraryViewModel: LibraryViewModel

    // Launcher for asking permissions from the user
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // We can handle specific denials here later if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialize the audio engine controller
        playerController = PlayerController(this)
        playerController.initialize()

        // 2. Initialize Database and Repository
        val database = AppDatabase.getDatabase(this)
        repository = MusicRepository(database.musicDao())

        // 3. Initialize the ViewModel
        val factory = LibraryViewModelFactory(repository)
        libraryViewModel = ViewModelProvider(this, factory)[LibraryViewModel::class.java]

        // 4. Request necessary runtime permissions
        requestAppPermissions()

        // 5. Set the Compose UI
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Launching directly into the Folder Picker for testing.
                    // Later, you can wrap this in a Compose Navigation Graph!
                    FolderPickerScreen(playerController = playerController)
                }
            }
        }
    }

    private fun requestAppPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // Permission required for the FFT Visualizer to read the audio session
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }

        // Permission required for Media Notifications (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // CRITICAL: Release the player when the app is completely destroyed
        // to prevent memory leaks and "ghost" audio playing in the background.
        playerController.release()
    }
}