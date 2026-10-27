package com.github.nacabaro.vbhelper.domain.items

import com.github.nacabaro.vbhelper.screens.itemsScreen.ItemsScreenControllerImpl.ItemTypes

/**
 * Items that raise the stats a character needs to evolve (trophies, wins, battles).
 *
 * items.db is only copied on a fresh install, so these are inserted every time the database
 * is opened (INSERT OR IGNORE keeps the quantity of rows that already exist).
 * For these items itemIcon is the ItemTypes id and itemLength is the amount.
 */
object EvolutionItems {
    const val MAX_STAT = 9999

    private data class Seed(
        val id: Int,
        val name: String,
        val description: String,
        val type: ItemTypes,
        val amount: Int,
        val price: Int
    )

    private val seeds = listOf(
        Seed(38, "Trophies +1", "Adds 1 trophy", ItemTypes.Trophies, 1, 250),
        Seed(39, "Trophies +5", "Adds 5 trophies", ItemTypes.Trophies, 5, 800),
        Seed(40, "Trophies +10", "Adds 10 trophies", ItemTypes.Trophies, 10, 1500),
        Seed(41, "Wins +1", "Adds 1 won battle", ItemTypes.Wins, 1, 250),
        Seed(42, "Wins +5", "Adds 5 won battles", ItemTypes.Wins, 5, 800),
        Seed(43, "Wins +10", "Adds 10 won battles", ItemTypes.Wins, 10, 1500),
        Seed(44, "Battles +5", "Adds 5 battles (3 won, 2 lost)", ItemTypes.Battles, 5, 250),
        Seed(45, "Battles +10", "Adds 10 battles (5 won, 5 lost)", ItemTypes.Battles, 10, 450),
        Seed(46, "Reset losses", "Sets the lost battles of the current phase to 0", ItemTypes.ResetLosses, 0, 500),
    )

    val insertStatements: List<String> = seeds.map {
        "INSERT OR IGNORE INTO `Items` (`id`, `name`, `description`, `itemIcon`, `itemLength`, `price`, `quantity`, `itemType`) " +
            "VALUES (${it.id}, '${it.name}', '${it.description}', ${it.type.id}, ${it.amount}, ${it.price}, 0, '${ItemType.UNIVERSAL.name}')"
    }
}
