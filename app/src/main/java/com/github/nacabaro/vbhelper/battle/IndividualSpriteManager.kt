package com.github.nacabaro.vbhelper.battle

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import java.io.File

class IndividualSpriteManager(private val context: Context) {
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
    
    /**
     * Get all available sprite frames for a character
     * @param characterId The character ID
     * @return List of frame numbers (1-12) that exist for this character
     */
    fun getAvailableFrames(characterId: String): List<Int> {
        val spriteBaseDir = getSpriteBaseDir()
        val characterDir = File(spriteBaseDir, characterId)
        
        if (!characterDir.exists()) {
            return emptyList()
        }
        
        val spriteFiles = characterDir.listFiles { file ->
            file.name.startsWith("${characterId}_") && file.name.endsWith(".png")
        } ?: emptyArray()
        
        return spriteFiles.mapNotNull { file ->
            val fileName = file.name
            val frameMatch = Regex("${characterId}_(\\d{2})\\.png").find(fileName)
            frameMatch?.groupValues?.get(1)?.toIntOrNull()
        }.sorted()
    }
    
    /**
     * Get all available character IDs
     * @return List of character IDs that have sprite directories
     */
    fun getAvailableCharacters(): List<String> {
        val spriteBaseDir = getSpriteBaseDir()
        
        if (!spriteBaseDir.exists()) {
            return emptyList()
        }
        
        return spriteBaseDir.listFiles { file ->
            file.isDirectory && file.listFiles()?.any { it.name.endsWith(".png") } == true
        }?.map { it.name }?.sorted() ?: emptyList()
    }
    
    /**
     * Clear the sprite cache
     */
    fun clearCache() {
        spriteCache.clear()
    }
    
    /**
     * Check if a character has sprite files
     * @param characterId The character ID to check
     * @return true if the character has sprite files, false otherwise
     */
    fun hasCharacterSprites(characterId: String): Boolean {
        val characterDir = File(getSpriteBaseDir(), characterId)
        if (!characterDir.exists()) {
            return false
        }
        
        val spriteFiles = characterDir.listFiles { file ->
            file.name.startsWith("${characterId}_") && file.name.endsWith(".png")
        } ?: emptyArray()
        
        return spriteFiles.isNotEmpty()
    }
} 