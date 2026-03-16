package com.kushalgangadhar.sling.utils

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile


data class LocalSong(
    val title: String,
    val uri: Uri
)

class MusicScanner(private val context: Context) {

    fun scanFolderForMusic(folderUri: Uri): List<LocalSong> {
        val songList = mutableListOf<LocalSong>()
        
        // Take persistable permission so we don't have to ask again if the app restarts
        val takeFlags: Int = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
        context.contentResolver.takePersistableUriPermission(folderUri, takeFlags)

        // Convert the Uri to a DocumentFile to traverse the directory
        val directory = DocumentFile.fromTreeUri(context, folderUri)
        
        if (directory != null && directory.isDirectory) {
            // Loop through all files in the selected folder
            for (file in directory.listFiles()) {
                // Check if the file is an audio file
                if (file.isFile && file.type?.startsWith("audio/") == true) {
                    val title = file.name ?: "Unknown Track"
                    songList.add(LocalSong(title = title, uri = file.uri))
                }
            }
        }
        return songList
    }
}
