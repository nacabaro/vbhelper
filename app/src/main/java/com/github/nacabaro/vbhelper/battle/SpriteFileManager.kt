package com.github.nacabaro.vbhelper.battle

import java.io.File

class SpriteFileManager {
    private fun getExternalSpriteBaseDir(): File {
        val externalDir = android.os.Environment.getExternalStorageDirectory()
        return File(externalDir, "VBHelper/battle_sprites")
    }

    fun checkSpriteFilesExist(): Boolean {
        val battleSpritesDir = getExternalSpriteBaseDir()
        val extractedAssetsDir = File(battleSpritesDir, "extracted_assets")
        val extractedStatsDir = File(battleSpritesDir, "extracted_digimon_stats")
        val atkspritesDir = File(battleSpritesDir, "extracted_atksprites")
        val battlebgsDir = File(battleSpritesDir, "extracted_battlebgs")
        
        val battleSpritesExist = battleSpritesDir.exists() && battleSpritesDir.listFiles()?.isNotEmpty() == true
        val assetsExist = extractedAssetsDir.exists() && extractedAssetsDir.listFiles()?.isNotEmpty() == true
        val statsExist = extractedStatsDir.exists() && extractedStatsDir.listFiles()?.isNotEmpty() == true
        val atkspritesExist = atkspritesDir.exists() && atkspritesDir.listFiles()?.isNotEmpty() == true
        val battlebgsExist = battlebgsDir.exists() && battlebgsDir.listFiles()?.isNotEmpty() == true

        /*
        println("Checking sprite files exist in external storage:")
        println("  battle_sprites exists: $battleSpritesExist")
        println("  extracted_assets exists: $assetsExist")
        println("  extracted_digimon_stats exists: $statsExist")
        println("  extracted_atksprites exists: $atkspritesExist")
        println("  extracted_battlebgs exists: $battlebgsExist")
        */
        
        return battleSpritesExist && assetsExist && statsExist && atkspritesExist && battlebgsExist
    }
}