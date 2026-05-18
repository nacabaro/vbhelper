package com.github.nacabaro.vbhelper.battle

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

class HitEffectSpriteManager {
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
    
}
