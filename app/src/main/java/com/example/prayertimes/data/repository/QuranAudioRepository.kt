package com.example.prayertimes.data.repository

import android.content.Context
import com.example.prayertimes.data.model.Reciter
import com.example.prayertimes.data.model.getGlobalAyahNumber
import java.io.File

class QuranAudioRepository(private val context: Context) {
    
    private val cacheDir = File(context.filesDir, "quran_audio")
    
    init { cacheDir.mkdirs() }
    
    fun getAudioUrl(reciter: Reciter, surahNumber: Int, ayahNumber: Int, quality: String): String {
        val globalAyah = getGlobalAyahNumber(surahNumber, ayahNumber)
        return "https://cdn.islamic.network/quran/audio/$quality/${reciter.identifier}/$globalAyah.mp3"
    }
    
    fun getCachedFile(reciter: Reciter, surahNumber: Int, ayahNumber: Int, quality: String): File {
        return File(cacheDir, "${reciter.identifier}_${quality}_${surahNumber}_${ayahNumber}.mp3")
    }
    
    fun isAyahCached(reciter: Reciter, surahNumber: Int, ayahNumber: Int, quality: String): Boolean {
        return getCachedFile(reciter, surahNumber, ayahNumber, quality).exists()
    }
    
    fun getCacheSizeMB(): Float {
        return try {
            if (!cacheDir.exists()) return 0f
            cacheDir.walkTopDown().sumOf { it.length() } / (1024f * 1024f)
        } catch (e: Exception) {
            0f
        }
    }
    
    fun getDownloadedSurahs(reciter: Reciter): Set<Int> {
        if (!cacheDir.exists()) return emptySet()
        val files = cacheDir.listFiles()?.filter { it.isFile && it.extension == "mp3" && it.name.startsWith(reciter.identifier) } ?: return emptySet()
        val downloaded = mutableSetOf<Int>()
        for (file in files) {
            val parts = file.nameWithoutExtension.split("_")
            if (parts.size == 3) {
                downloaded.add(parts[1].toInt())
            }
        }
        return downloaded
    }
    
    fun clearCache() {
        if (cacheDir.exists()) {
            cacheDir.deleteRecursively()
            cacheDir.mkdirs()
        }
    }
    
    suspend fun downloadAndCacheAudio(reciter: Reciter, surahNumber: Int, ayahNumber: Int, quality: String): File? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val file = getCachedFile(reciter, surahNumber, ayahNumber, quality)
            if (file.exists() && file.length() > 0) return@withContext file
            
            val url = java.net.URL(getAudioUrl(reciter, surahNumber, ayahNumber, quality))
            val connection = url.openConnection()
            connection.connect()
            
            val inputStream = connection.getInputStream()
            val tempFile = File(cacheDir, "${file.name}.tmp")
            tempFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            tempFile.renameTo(file)
            
            manageCacheSize()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun manageCacheSize() {
        if (!cacheDir.exists()) return
        val maxSizeBytes = 200 * 1024 * 1024L
        val files = cacheDir.listFiles()?.filter { it.isFile && it.extension == "mp3" } ?: return
        
        var currentSize = files.sumOf { it.length() }
        if (currentSize <= maxSizeBytes) return
        
        // Sort by last modified (oldest first)
        val sortedFiles = files.sortedBy { it.lastModified() }
        for (file in sortedFiles) {
            if (currentSize <= maxSizeBytes) break
            val size = file.length()
            if (file.delete()) {
                currentSize -= size
            }
        }
    }
}
