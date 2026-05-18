package com.github.nacabaro.vbhelper.battle

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

class IndividualSpriteManager {
    private val spriteCache = mutableMapOf<String, Bitmap>()
    
    // Get the external storage directory for sprite files
    private fun getSpriteBaseDir(): File {
        val externalDir = android.os.Environment.getExternalStorageDirectory()
        return File(externalDir, "VBHelper/battle_sprites/extracted_assets/sprites")
    }
    
    /**
     * Load a specific sprite frame for a character
     * @param characterId The character ID (e.g., "dim012_mon03")
     * @param frameNumber The frame number (1-12)
     * @return Bitmap of the sprite frame, or null if not found
     */
    fun loadSpriteFrame(characterId: String, frameNumber: Int): Bitmap? {
        val cacheKey = "${characterId}_frame_${frameNumber}"
        
        // Check cache first
        if (spriteCache.containsKey(cacheKey)) {
            return spriteCache[cacheKey]
        }
        
        // Debug: Check if base directory exists
        val spriteBaseDir = getSpriteBaseDir()
        if (!spriteBaseDir.exists()) {
            println("Sprite base directory does not exist: ${spriteBaseDir.absolutePath}")
            return null
        }
        
        try {
            // Construct the sprite file path
            val spriteFileName = "${characterId}_${String.format("%02d", frameNumber)}.png"
            val spriteFile = File(spriteBaseDir, "$characterId/$spriteFileName")
            
            if (!spriteFile.exists()) {
                println("Sprite file not found: ${spriteFile.absolutePath}")
                return null
            }
            
            // Load the PNG file directly
            val bitmap = BitmapFactory.decodeFile(spriteFile.absolutePath)
            if (bitmap == null) {
                println("Failed to decode sprite file: ${spriteFile.absolutePath}")
                return null
            }
            
            // Cache the result
            spriteCache[cacheKey] = bitmap
            
            return bitmap
            
        } catch (e: Exception) {
            println("Error loading sprite frame: ${e.message}")
            e.printStackTrace()
            return null
        }
    }
    
}