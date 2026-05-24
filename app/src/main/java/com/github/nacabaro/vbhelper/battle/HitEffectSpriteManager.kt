package com.github.nacabaro.vbhelper.battle

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Environment
import java.io.File

class HitEffectSpriteManager(private val context: Context) {
    private val spriteCache = mutableMapOf<String, Bitmap>()
    
    // Get the external storage directory for hit effect sprites
    private fun getHitSpritesDir(): File {
        val externalDir = android.os.Environment.getExternalStorageDirectory()
        return File(externalDir, "VBHelper/battle_sprites/extracted_hit_sprites")
    }
    
    /**
     * Load a hit sprite (hit_01.png, hit_02.png, hit_02_white.png)
     * @param spriteName The sprite name (e.g., "hit_01", "hit_02", "hit_02_white")
     * @return Bitmap of the hit sprite, or null if not found
     */
    fun loadHitSprite(spriteName: String): Bitmap? {
        val cacheKey = "hit_$spriteName"
        
        // Check cache first
        if (spriteCache.containsKey(cacheKey)) {
            return spriteCache[cacheKey]
        }
        
        try {
            val hitSpritesDir = getHitSpritesDir()
            val spriteFile = File(hitSpritesDir, "$spriteName.png")
            
            if (!spriteFile.exists()) {
                println("Hit sprite file not found: ${spriteFile.absolutePath}")
                return null
            }
            
            val bitmap = BitmapFactory.decodeFile(spriteFile.absolutePath)
            if (bitmap == null) {
                println("Failed to decode hit sprite file: ${spriteFile.absolutePath}")
                return null
            }
            
            // Cache the result
            spriteCache[cacheKey] = bitmap
            
            return bitmap
            
        } catch (e: Exception) {
            println("Error loading hit sprite: ${e.message}")
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * Load a damage effect sprite from spritesheet
     * @param spritesheetName The spritesheet name (e.g., "dmg_ef1", "dmg_ef2")
     * @param frameIndex The frame index (0-3 for dmg_ef1 and dmg_ef2, 0 for dmg_ef3)
     * @return Bitmap of the damage effect frame, or null if not found
     */
    fun loadDamageEffectSprite(spritesheetName: String, frameIndex: Int = 0): Bitmap? {
        val cacheKey = "dmg_${spritesheetName}_frame_${frameIndex}"
        
        // Check cache first
        if (spriteCache.containsKey(cacheKey)) {
            return spriteCache[cacheKey]
        }
        
        try {
            val spritesheetFile = File(getHitSpritesDir(), "$spritesheetName.png")
            
            if (!spritesheetFile.exists()) {
                println("Damage effect spritesheet not found: ${spritesheetFile.absolutePath}")
                return null
            }
            
            val spritesheet = BitmapFactory.decodeFile(spritesheetFile.absolutePath)
            if (spritesheet == null) {
                println("Failed to decode damage effect spritesheet: ${spritesheetFile.absolutePath}")
                return null
            }
            
            // Extract frame from spritesheet
            val frameBitmap = when (spritesheetName) {
                "dmg_ef1", "dmg_ef2" -> {
                    // These are 2x2 spritesheets (4 frames)
                    val frameWidth = spritesheet.width / 2
                    val frameHeight = spritesheet.height / 2
                    val row = frameIndex / 2
                    val col = frameIndex % 2
                    val x = col * frameWidth
                    val y = row * frameHeight
                    Bitmap.createBitmap(spritesheet, x, y, frameWidth, frameHeight)
                }
                "dmg_ef3" -> {
                    // This is a single sprite
                    spritesheet
                }
                else -> {
                    println("Unknown spritesheet name: $spritesheetName")
                    return null
                }
            }
            
            println("Successfully loaded damage effect frame: $spritesheetName frame $frameIndex (${frameBitmap.width}x${frameBitmap.height})")
            
            // Cache the result
            spriteCache[cacheKey] = frameBitmap
            
            return frameBitmap
            
        } catch (e: Exception) {
            println("Error loading damage effect sprite: ${e.message}")
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * Get all available hit sprites
     * @return List of hit sprite names (without .png extension)
     */
    fun getAvailableHitSprites(): List<String> {
        val hitSpritesDir = getHitSpritesDir()
        
        if (!hitSpritesDir.exists()) {
            return emptyList()
        }
        
        return hitSpritesDir.listFiles { file ->
            file.name.startsWith("hit_") && file.name.endsWith(".png")
        }?.map { file ->
            file.name.substringBefore(".png")
        }?.sorted() ?: emptyList()
    }
    
    /**
     * Get available damage effect spritesheet names
     * @return List of available damage effect spritesheet names
     */
    fun getAvailableDamageEffectSpritesheets(): List<String> {
        try {
            if (!getHitSpritesDir().exists()) {
                return emptyList()
            }
            
            val dmgFiles = getHitSpritesDir().listFiles { file ->
                file.name.startsWith("dmg_ef") && file.name.endsWith(".png")
            } ?: emptyArray()
            
            return dmgFiles.map { file ->
                file.name.substringBefore(".png")
            }.sorted()
            
        } catch (e: Exception) {
            println("Error getting available damage effect spritesheets: ${e.message}")
            e.printStackTrace()
            return emptyList()
        }
    }
    
    /**
     * Clear the sprite cache
     */
    fun clearCache() {
        spriteCache.clear()
    }
}
