package com.github.nacabaro.vbhelper.screens.settingsScreen.controllers

import android.util.Log
import com.github.cfogrady.vb.dim.card.BemCard
import com.github.cfogrady.vb.dim.card.DimCard
import com.github.cfogrady.vb.dim.card.DimReader
import com.github.cfogrady.vbnfc.data.NfcCharacter
import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.domain.card.Card
import com.github.nacabaro.vbhelper.domain.card.CardCharacter
import com.github.nacabaro.vbhelper.domain.card.CardProgress
import com.github.nacabaro.vbhelper.domain.characters.Sprite
import java.io.InputStream

class CardImportController(
    private val database: AppDatabase
) {
    suspend fun importCard(
        fileReader: InputStream?
    ) {
        val dimReader = DimReader()
        val card = dimReader.readCard(fileReader, false)

        val cardModel = Card(
            cardId = card.header.dimId,
            logo = card.spriteData.sprites[0].pixelData,
            name = card.spriteData.text,
            stageCount = card.adventureLevels.levels.size,
            logoHeight = card.spriteData.sprites[0].height,
            logoWidth = card.spriteData.sprites[0].width,
            isBEm = card is BemCard
        )

        val cardId = database
            .cardDao()
            .insertNewCard(cardModel)

        updateCardProgress(cardId = cardId)

        importCharacterData(cardId, card)

        importEvoData(cardId, card)

        importAdventureMissions(cardId, card)

        importCardFusions(cardId, card)
    }

    private fun updateCardProgress(
        cardId: Long,
    ) {
        database
            .cardProgressDao()
            .insertCardProgress(
                CardProgress(
                    cardId = cardId,
                    currentStage = 1,
                    unlocked = false
                )
            )
    }

    private suspend fun importCharacterData(
        cardId: Long,
        card: com.github.cfogrady.vb.dim.card.Card<*, *, *, *, *, *>
    ) {
        var spriteCounter = when (card is BemCard) {
            true -> 54
            false -> 10
        }

        val domainCharacters = mutableListOf<CardCharacter>()

        val characters = card
            .characterStats
            .characterEntries

        for (index in 0 until characters.size) {
            var domainSprite: Sprite?
            if (index < 2 && card is DimCard) {
                domainSprite = Sprite(
                    width = card.spriteData.sprites[spriteCounter + 1].spriteDimensions.width,
                    height = card.spriteData.sprites[spriteCounter + 1].spriteDimensions.height,
                    spriteIdle1 = card.spriteData.sprites[spriteCounter + 1].pixelData,
                    spriteIdle2 = card.spriteData.sprites[spriteCounter + 2].pixelData,
                    spriteWalk1 = card.spriteData.sprites[spriteCounter + 1].pixelData,
                    spriteWalk2 = card.spriteData.sprites[spriteCounter + 3].pixelData,
                    spriteRun1 = card.spriteData.sprites[spriteCounter + 1].pixelData,
                    spriteRun2 = card.spriteData.sprites[spriteCounter + 3].pixelData,
                    spriteTrain1 = card.spriteData.sprites[spriteCounter + 1].pixelData,
                    spriteTrain2 = card.spriteData.sprites[spriteCounter + 3].pixelData,
                    spriteHappy = card.spriteData.sprites[spriteCounter + 4].pixelData,
                    spriteSleep = card.spriteData.sprites[spriteCounter + 5].pixelData,
                    spriteAttack = card.spriteData.sprites[spriteCounter + 2].pixelData,
                    spriteDodge = card.spriteData.sprites[spriteCounter + 3].pixelData
                )
            } else {
                domainSprite = Sprite(
                    width = card.spriteData.sprites[spriteCounter + 1].spriteDimensions.width,
                    height = card.spriteData.sprites[spriteCounter + 1].spriteDimensions.height,
                    spriteIdle1 = card.spriteData.sprites[spriteCounter + 1].pixelData,
                    spriteIdle2 = card.spriteData.sprites[spriteCounter + 2].pixelData,
                    spriteWalk1 = card.spriteData.sprites[spriteCounter + 3].pixelData,
                    spriteWalk2 = card.spriteData.sprites[spriteCounter + 4].pixelData,
                    spriteRun1 = card.spriteData.sprites[spriteCounter + 5].pixelData,
                    spriteRun2 = card.spriteData.sprites[spriteCounter + 6].pixelData,
                    spriteTrain1 = card.spriteData.sprites[spriteCounter + 7].pixelData,
                    spriteTrain2 = card.spriteData.sprites[spriteCounter + 8].pixelData,
                    spriteHappy = card.spriteData.sprites[spriteCounter + 9].pixelData,
                    spriteSleep = card.spriteData.sprites[spriteCounter + 10].pixelData,
                    spriteAttack = card.spriteData.sprites[spriteCounter + 11].pixelData,
                    spriteDodge = card.spriteData.sprites[spriteCounter + 12].pixelData
                )
            }

            val spriteId = database
                .spriteDao()
                .insertSprite(domainSprite)


            domainCharacters.add(
                CardCharacter(
                    cardId = cardId,
                    spriteId = spriteId,
                    charaIndex = index,
                    nameSprite = card.spriteData.sprites[spriteCounter].pixelData,
                    stage = characters[index].stage,
                    attribute = NfcCharacter.Attribute.entries[characters[index].attribute],
                    baseHp = characters[index].hp,
                    baseBp = characters[index].dp,
                    baseAp = characters[index].ap,
                    nameWidth = card.spriteData.sprites[spriteCounter].spriteDimensions.width,
                    nameHeight = card.spriteData.sprites[spriteCounter].spriteDimensions.height
                )
            )

            spriteCounter += if (card is BemCard) {
                14
            } else {
                when (index) {
                    0 -> 6
                    1 -> 7
                    else -> 14
                }
            }
        }

        database
            .characterDao()
            .insertCharacter(*domainCharacters.toTypedArray())
    }

    private suspend fun importAdventureMissions(
        cardId: Long,
        card: com.github.cfogrady.vb.dim.card.Card<*, *, *, *, *, *>
    ) {
        Log.d("importAdventureMissions", "Importing adventure missions")
        if (card is BemCard) {
            card.adventureLevels.levels.forEach {
                database
                    .cardAdventureDao()
                    .insertNewAdventure(
                        cardId = cardId,
                        characterId = it.bossCharacterIndex,
                        steps = it.steps,
                        bossAp = it.bossAp,
                        bossHp = it.bossHp,
                        bossDp = it.bossDp,
                        bossBp = it.bossBp
                    )
            }
        } else if (card is DimCard) {
            card.adventureLevels.levels.map {
                database
                    .cardAdventureDao()
                    .insertNewAdventure(
                        cardId = cardId,
                        characterId = it.bossCharacterIndex,
                        steps = it.steps,
                        bossAp = it.bossAp,
                        bossHp = it.bossHp,
                        bossDp = it.bossDp,
                        bossBp = null
                    )
            }
        }
    }

    private suspend fun importCardFusions(
        cardId: Long,
        card: com.github.cfogrady.vb.dim.card.Card<*, *, *, *, *, *>
    ) {
        Log.d("importCardFusions", "Importing card fusions")
        if (card is DimCard) {
            card
                .attributeFusions
                .entries
                .forEach {
                    Log.d("importCardFusions", "Importing fusion: ${it.attribute1Fusion}")
                    if (it.attribute1Fusion != 65535 && it.characterIndex != 65535) {
                        database
                            .cardFusionsDao()
                            .insertNewFusion(
                                cardId = cardId,
                                fromCharaId = it.characterIndex,
                                attribute = NfcCharacter.Attribute.Virus,
                                toCharaId = it.attribute1Fusion,
                            )
                    }

                    if (it.attribute2Fusion != 65535 && it.characterIndex != 65535) {
                        database
                            .cardFusionsDao()
                            .insertNewFusion(
                                cardId = cardId,
                                fromCharaId = it.characterIndex,
                                attribute = NfcCharacter.Attribute.Data,
                                toCharaId = it.attribute2Fusion,
                            )
                    }

                    if (it.attribute3Fusion != 65535 && it.characterIndex != 65535) {
                        database
                            .cardFusionsDao()
                            .insertNewFusion(
                                cardId = cardId,
                                fromCharaId = it.characterIndex,
                                attribute = NfcCharacter.Attribute.Vaccine,
                                toCharaId = it.attribute3Fusion,
                            )
                    }

                    if (it.attribute4Fusion != 65535 && it.characterIndex != 65535) {
                        database
                            .cardFusionsDao()
                            .insertNewFusion(
                                cardId = cardId,
                                fromCharaId = it.characterIndex,
                                attribute = NfcCharacter.Attribute.Free,
                                toCharaId = it.attribute4Fusion,
                            )
                    }
                }
        }
    }

    private suspend fun importEvoData(
        cardId: Long,
        card: com.github.cfogrady.vb.dim.card.Card<*, *, *, *, *, *>
    ) {
        for (index in 0 until card.transformationRequirements.transformationEntries.size) {
            val evo = card.transformationRequirements.transformationEntries[index]

            var transformationTimerHours: Int
            var unlockAdventureLevel: Int

            if (card is BemCard) {
                transformationTimerHours = card
                    .transformationRequirements
                    .transformationEntries[index]
                    .minutesUntilTransformation / 60
                unlockAdventureLevel = if (
                    card
                        .transformationRequirements
                        .transformationEntries[index]
                        .requiredCompletedAdventureLevel == 65535
                ) {
                    0
                } else {
                    card
                        .transformationRequirements
                        .transformationEntries[index]
                        .requiredCompletedAdventureLevel
                }
            } else {
                transformationTimerHours = (card as DimCard)
                    .transformationRequirements
                    .transformationEntries[index]
                    .hoursUntilEvolution
                unlockAdventureLevel = if (
                    card
                        .adventureLevels
                        .levels
                        .last()
                        .bossCharacterIndex == card.transformationRequirements.transformationEntries[index].toCharacterIndex
                ) {
                    14
                    /*
                    Magic number incoming!!

                    In the case of DiMCards, stage 15 is the one that unlocks the locked character.
                    We know it is a locked character if the last adventure level's boss character index
                    is the current index. If it is, we add stage 15 complete as a requirement for transformation.
                     */
                } else {
                    0
                    /*
                    Another magic number...

                    The rest of the characters are not locked.
                     */
                }
            }

            database
                .characterDao()
                .insertPossibleTransformation(
                    cardId = cardId,
                    fromChraraIndex = evo.fromCharacterIndex,
                    toChraraIndex = evo.toCharacterIndex,
                    requiredVitals = evo.requiredVitalValues,
                    requiredTrophies = evo.requiredTrophies,
                    requiredBattles = evo.requiredBattles,
                    requiredWinRate = evo.requiredWinRatio,
                    requiredAdventureLevelCompleted = unlockAdventureLevel,
                    changeTimerHours = transformationTimerHours
                )
        }
    }
}