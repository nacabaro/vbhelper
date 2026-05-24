package com.github.nacabaro.vbhelper.battle

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import com.google.gson.Gson
import java.io.File

data class SpriteMapping(
    val atlas_name: String,
    val atlas_file: String,
    val texture: TextureInfo,
    val sprites: List<String>
)

data class TextureInfo(
    val name: String,
    val file: String,
    val path_id: Long
)

data class SpriteData(
    val name: String,
    val atlas_name: String,
    val m_Name: String,
    val texture_rect: TextureRect
)

data class TextureRect(
    val height: Float,
    val width: Float,
    val x: Float,
    val y: Float
)

class BattleSpriteManager(private val context: Context) {
    private val gson = Gson()
    private val spriteCache = mutableMapOf<String, Bitmap>()
    
    // Get the external storage directory for sprite files
    private fun getSpriteBaseDir(): File {
        val externalDir = android.os.Environment.getExternalStorageDirectory()
        return File(externalDir, "VBHelper/battle_sprites/extracted_assets")
    }
    
    fun loadSprite(spriteName: String, atlasName: String): Bitmap? {
        val cacheKey = "${spriteName}_${atlasName}"
        
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
        
        println("Sprite base directory exists: ${spriteBaseDir.absolutePath}")
        println("Available directories: ${spriteBaseDir.listFiles()?.map { it.name }}")
        
        try {
            // Load the PNG texture file directly using the atlas name
            val textureFile = File(spriteBaseDir, "extracted_textures/${atlasName}.png")
            
            if (!textureFile.exists()) {
                println("Texture file not found: ${textureFile.absolutePath}")
                return null
            }
            
            val fullBitmap = BitmapFactory.decodeFile(textureFile.absolutePath)
            if (fullBitmap == null) {
                println("Failed to decode texture file: ${textureFile.absolutePath}")
                return null
            }
            
            // Load the specific sprite data file
            val spriteDataFile = File(spriteBaseDir, "sprites/${spriteName}.json")
            if (!spriteDataFile.exists()) {
                println("Sprite data file not found: ${spriteDataFile.absolutePath}")
                return null
            }
            
            val spriteDataJson = spriteDataFile.readText()
            val spriteData = gson.fromJson(spriteDataJson, SpriteData::class.java)
            
            // Debug: Print sprite coordinates
            println("Sprite coordinates: x=${spriteData.texture_rect.x}, y=${spriteData.texture_rect.y}, width=${spriteData.texture_rect.width}, height=${spriteData.texture_rect.height}")
            println("Texture dimensions: width=${fullBitmap.width}, height=${fullBitmap.height}")
            
            // Calculate the correct Y coordinate (inverted coordinate system)
            val correctedY = fullBitmap.height - spriteData.texture_rect.y.toInt() - spriteData.texture_rect.height.toInt()
            
            // Extract the sprite from the atlas using texture_rect coordinates
            val spriteBitmap = Bitmap.createBitmap(
                fullBitmap,
                spriteData.texture_rect.x.toInt(),
                correctedY,
                spriteData.texture_rect.width.toInt(),
                spriteData.texture_rect.height.toInt()
            )
            
            // Ensure the bitmap is not scaled and has proper quality
            val finalBitmap = if (spriteBitmap.width != spriteData.texture_rect.width.toInt() || 
                                  spriteBitmap.height != spriteData.texture_rect.height.toInt()) {
                // If the bitmap was scaled during creation, create a new one with exact dimensions
                Bitmap.createScaledBitmap(spriteBitmap, 
                    spriteData.texture_rect.width.toInt(), 
                    spriteData.texture_rect.height.toInt(), 
                    false) // false = no filtering/interpolation
            } else {
                spriteBitmap
            }
            
            println("Extracted sprite dimensions: ${finalBitmap.width}x${finalBitmap.height}")
            
            // Cache the result
            spriteCache[cacheKey] = finalBitmap
            
            return finalBitmap
            
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    fun clearCache() {
        spriteCache.clear()
    }
    
    // Helper method to get available sprites for an atlas
    fun getAvailableSprites(atlasName: String): List<String> {
        try {
            val spritesDir = File(getSpriteBaseDir(), "sprites")
            if (!spritesDir.exists()) {
                return emptyList()
            }
            
            val spriteFiles = spritesDir.listFiles { file ->
                file.name.startsWith("${atlasName}_sprite_") && file.name.endsWith(".json")
            } ?: emptyArray()
            
            return spriteFiles.map { file ->
                // Extract sprite number from filename (e.g., "dim000_mon01_sprite_00.json" -> "00")
                val spriteNumber = file.name.substringAfter("_sprite_").substringBefore(".json")
                spriteNumber
            }.sorted()
            
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }
    
    // Helper method to get available atlases
    fun getAvailableAtlases(): List<String> {
        try {
            val texturesDir = File(getSpriteBaseDir(), "extracted_textures")
            if (!texturesDir.exists()) {
                return emptyList()
            }
            
            val textureFiles = texturesDir.listFiles { file ->
                file.name.endsWith(".png")
            } ?: emptyArray()
            
            return textureFiles.map { file ->
                // Extract atlas name from filename (e.g., "dim000_mon01.png" -> "dim000_mon01")
                file.name.substringBefore(".png")
            }.sorted()
            
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }
}