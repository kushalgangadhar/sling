package com.kushalgangadhar.sling.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

// 1. The Song Table
@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val uriString: String, // The URI is guaranteed to be unique
    val title: String,
    val artist: String = "Unknown Artist",
    val durationMs: Long = 0L
)

// 2. The Playlist Table
@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val playlistId: Long = 0,
    val name: String
)

// 3. The Mapping Table (Links Songs to Playlists)
@Entity(
    tableName = "playlist_song_cross_ref",
    primaryKeys = ["playlistId", "uriString"]
)
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val uriString: String,
    val position: Int // Keeps track of the song order in the playlist
)

// 4. A helper class to fetch a Playlist AND all its Songs in one go
data class PlaylistWithSongs(
    @Embedded val playlist: PlaylistEntity,
    @Relation(
        parentColumn = "playlistId",
        entityColumn = "uriString",
        associateBy = Junction(PlaylistSongCrossRef::class)
    )
    val songs: List<SongEntity>
)
