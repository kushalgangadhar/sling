package com.kushalgangadhar.sling.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kushalgangadhar.sling.data.local.PlaylistEntity
import com.kushalgangadhar.sling.data.local.SongEntity
import com.kushalgangadhar.sling.data.repository.MusicRepository
import com.kushalgangadhar.sling.utils.MusicScanner
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val repository: MusicRepository
) : ViewModel() {

    // 1. Convert Repository Flows into Compose-friendly StateFlows.
    // The UI will collect these and instantly redraw when the database changes.
    val allSongs: StateFlow<List<SongEntity>> = repository.allSongs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val playlists: StateFlow<List<PlaylistEntity>> = repository.allPlaylists
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 2. Handle scanning a folder and saving the results to Room
    fun scanAndSaveMusic(folderUri: Uri, scanner: MusicScanner) {
        viewModelScope.launch {
            // Scan the folder using our utility class
            val localSongs = scanner.scanFolderForMusic(folderUri)
            
            // Map the LocalSong objects to database Entities
            val songEntities = localSongs.map { localSong ->
                SongEntity(
                    uriString = localSong.uri.toString(),
                    title = localSong.title
                )
            }
            
            // Save them to the database via the Repository
            repository.insertSongs(songEntities)
        }
    }

    // 3. Handle creating a new playlist
    fun createNewPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }

    // Example of seeding a default playlist if you wanted to test it
    fun createDefaultPlaylist() {
        viewModelScope.launch {
            repository.createPlaylist("Kannada Classics")
        }
    }
}

// ------------------------------------------------------------------------
// THE FACTORY: Android needs this to know how to inject the Repository
// ------------------------------------------------------------------------
class LibraryViewModelFactory(
    private val repository: MusicRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LibraryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LibraryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
