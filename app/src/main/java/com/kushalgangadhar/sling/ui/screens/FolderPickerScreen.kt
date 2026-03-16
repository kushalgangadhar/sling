package com.kushalgangadhar.sling.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun FolderPickerScreen(playerController: PlayerController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val musicScanner = remember { MusicScanner(context) }
    
    // State to hold our list of found songs
    var songs by remember { mutableStateOf<List<LocalSong>>(emptyList()) }
    var isScanning by remember { mutableStateOf(false) }

    // The SAF Launcher to open the system folder picker
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            isScanning = true
            // Scan the folder on a background thread so we don't freeze the UI
            scope.launch {
                val foundSongs = withContext(Dispatchers.IO) {
                    musicScanner.scanFolderForMusic(selectedUri)
                }
                songs = foundSongs
                isScanning = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Top Section: Button to pick a folder
        Button(
            onClick = { folderPickerLauncher.launch(null) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Icon(Icons.Default.Folder, contentDescription = "Pick Folder")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Select Music Folder")
        }

        if (isScanning) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        // Bottom Section: List of Songs
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(songs) { song ->
                SongListItem(
                    song = song,
                    onClick = {
                        // Pass the URI string to our PlayerController!
                        playerController.playSong(song.uri.toString())
                    }
                )
                Divider() // Adds a nice line between items
            }
        }
    }
}

@Composable
fun SongListItem(song: LocalSong, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = "Music Icon",
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = song.title,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
