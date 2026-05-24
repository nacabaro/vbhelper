package com.github.nacabaro.vbhelper.battle

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import com.google.gson.Gson
import java.io.File

data class CharacterData(
    val name: String,
    val charaId: String,
    val smalefilename: String,
    val laugeFileName: String
)

data class CharacterDataResponse(
    val name: String,
    val type: String,
    val source_file: String,
    val collection: String,
    val unity_collection_id: String,
    val relative_path: String,
    val all_attributes: CharacterDataAttributes
)

data class CharacterDataAttributes(
    val DataList: List<String>
)

class AttackSpriteManager(private val context: Context) {
    private val gson = Gson()
    private val characterDataCache = mutableMapOf<String, CharacterData>()
    
    // Get the external storage directory for attack sprites
    private fun getAttackTexturesBaseDir(): File {
        val externalDir = android.os.Environment.getExternalStorageDirectory()
        return File(externalDir, "VBHelper/battle_sprites/extracted_atksprites")
    }
    
    fun getAttackSprite(characterId: String, isLarge: Boolean = false): Bitmap? {
        println("AttackSpriteManager: Getting attack sprite for characterId=$characterId, isLarge=$isLarge")
        try {
            // Get character data
            val characterData = getCharacterData(characterId) ?: return null
            // Determine which attack file to use
            val attackFileName = if (isLarge) {
                characterData.laugeFileName
            } else {
                characterData.smalefilename
            }
            
            // Skip if no attack file
            if (attackFileName == "0") {
                println("AttackSpriteManager: Skipping attack file (filename is '0')")
                return null
            }
            
            // Load the attack sprite from external storage
            val attackFile = File(getAttackTexturesBaseDir(), "$attackFileName.png")
            
            return if (attackFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(attackFile.absolutePath)
                bitmap
            } else {
                println("AttackSpriteManager: Attack file does not exist")
                null
            }
        } catch (e: Exception) {
            println("AttackSpriteManager: Exception occurred: ${e.message}")
            e.printStackTrace()
            return null
        }
    }
    
    private fun getCharacterData(characterId: String): CharacterData? {
        // Check cache first
        if (characterDataCache.containsKey(characterId)) {
            return characterDataCache[characterId]
        }
        
        try {
            // Load character data from JSON file in external storage
            val externalDir = android.os.Environment.getExternalStorageDirectory()
            val characterDataFile = File(externalDir, "VBHelper/battle_sprites/extracted_digimon_stats/character_data/CharacterData.json")
            
            if (!characterDataFile.exists()) {
                println("AttackSpriteManager: Character data file does not exist, using default data")
                // For now, return a default character data
                val characterData = CharacterData(
                    name = characterId,
                    charaId = characterId,
                    smalefilename = "atk_s_02", // Default small attack
                    laugeFileName = "atk_l_04"   // Default large attack
                )
                
                characterDataCache[characterId] = characterData
                return characterData
            }
            
            val jsonContent = characterDataFile.readText()
            
            // Parse the JSON response
            val response = gson.fromJson(jsonContent, CharacterDataResponse::class.java)
            
            // Search through the DataList for the matching characterId
            for (characterString in response.all_attributes.DataList) {
                // Extract charaId from the string format: "<UnknownObject<Character> id=0, charaId='dim000_mon03', ...>"
                val charaIdMatch = Regex("charaId='([^']+)'").find(characterString)
                if (charaIdMatch != null) {
                    val foundCharaId = charaIdMatch.groupValues[1]
                    if (foundCharaId == characterId) {
                        // Extract smalefilename and laugeFileName
                        val smallFileMatch = Regex("smalefilename='([^']+)'").find(characterString)
                        val largeFileMatch = Regex("laugeFileName='([^']+)'").find(characterString)
                        
                        val smallFileName = smallFileMatch?.groupValues?.get(1) ?: "0"
                        val largeFileName = largeFileMatch?.groupValues?.get(1) ?: "0"
                        
                        val characterData = CharacterData(
                            name = characterId,
                            charaId = characterId,
                            smalefilename = smallFileName,
                            laugeFileName = largeFileName
                        )
                        
                        characterDataCache[characterId] = characterData
                        return characterData
                    }
                }
            }
            
            // If character not found, return default data
            println("AttackSpriteManager: Character not found in JSON, using default data")
            val characterData = CharacterData(
                name = characterId,
                charaId = characterId,
                smalefilename = "atk_s_02", // Default small attack
                laugeFileName = "atk_l_04"   // Default large attack
            )
            
            characterDataCache[characterId] = characterData
            return characterData
            
        } catch (e: Exception) {
            println("AttackSpriteManager: Exception in getCharacterData: ${e.message}")
            e.printStackTrace()
            return null
        }
    }
} 