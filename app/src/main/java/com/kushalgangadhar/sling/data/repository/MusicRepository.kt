package com.kushalgangadhar.sling.data.repository

import com.kushalgangadhar.sling.data.local.MusicDao
import com.kushalgangadhar.sling.data.local.PlaylistEntity
import com.kushalgangadhar.sling.data.local.PlaylistSongCrossRef
import com.kushalgangadhar.sling.data.local.PlaylistWithSongs
import com.kushalgangadhar.sling.data.local.SongEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MusicRepository(private val musicDao: MusicDao) {

    // --------------------------------------------------------
    // READ OPERATIONS (Flows automatically run on background threads in Room)
    // --------------------------------------------------------
    
    val allSongs: Flow<List<SongEntity>> = musicDao.getAllSongs()
    
    val allPlaylists: Flow<List<PlaylistEntity>> = musicDao.getAllPlaylists()

    fun getPlaylistWithSongs(playlistId: Long): Flow<PlaylistWithSongs> {
        return musicDao.getPlaylistWithSongs(playlistId)
    }

    // --------------------------------------------------------
    // WRITE OPERATIONS (We explicitly use Dispatchers.IO here)
    // --------------------------------------------------------

    suspend fun insertSongs(songs: List<SongEntity>) {
        withContext(Dispatchers.IO) {
            musicDao.insertSongs(songs)
        }
    }

    suspend fun createPlaylist(name: String): Long {
        return withContext(Dispatchers.IO) {
            musicDao.createPlaylist(PlaylistEntity(name = name))
        }
    }

    suspend fun addSongToPlaylist(playlistId: Long, songUri: String, position: Int) {
        withContext(Dispatchers.IO) {
            val crossRef = PlaylistSongCrossRef(
                playlistId = playlistId,
                uriString = songUri,
                position = position
            )
            musicDao.addSongToPlaylist(crossRef)
        }
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songUri: String) {
        withContext(Dispatchers.IO) {
            musicDao.removeSongFromPlaylist(playlistId, songUri)
        }
    }
}
