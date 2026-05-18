package com.github.nacabaro.vbhelper.companion.validation

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ValidatedCardManager(
    private val validatedCardDao: ValidatedCardDao,
) {
    private val mutex = Mutex()
    private var validatedIds: Set<Int>? = null

    private suspend fun ensureLoaded(): Set<Int> {
        validatedIds?.let { return it }
        return mutex.withLock {
            validatedIds ?: validatedCardDao.getIds().toSet().also { validatedIds = it }
        }
    }

    suspend fun addValidatedCard(cardId: Int) {
        mutex.withLock {
            val current = validatedIds ?: validatedCardDao.getIds().toSet()
            if (!current.contains(cardId)) {
                validatedCardDao.insert(ValidatedCardEntity(cardId))
                validatedIds = current + cardId
            } else {
                validatedIds = current
            }
        }
    }

    suspend fun isValidatedCard(cardId: Int): Boolean {
        return ensureLoaded().contains(cardId)
    }
}

