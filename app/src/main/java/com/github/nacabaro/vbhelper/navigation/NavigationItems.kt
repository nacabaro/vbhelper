package com.github.nacabaro.vbhelper.navigation

import com.github.nacabaro.vbhelper.R
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

sealed class NavigationItems(
    val route: String,
    @DrawableRes val icon: Int,
    @StringRes val label: Int
) {
    object Scan : NavigationItems(
        "Scan/{characterId}",
        R.drawable.baseline_nfc_24,
        R.string.nav_scan
    )

    object Battles : NavigationItems(
        "Battle",
        R.drawable.baseline_swords_24,
        R.string.nav_battle
    )

    object Home : NavigationItems(
        "Home",
        R.drawable.baseline_cottage_24,
        R.string.nav_home
    )

    object Dex : NavigationItems(
        "Dex",
        R.drawable.baseline_menu_book_24,
        R.string.nav_dex
    )

    object CardAdventure : NavigationItems(
        "CardAdventure/{cardId}",
        R.drawable.baseline_fort_24,
        R.string.nav_card_adventure
    )

    object Storage : NavigationItems(
        "Storage",
        R.drawable.baseline_catching_pokemon_24,
        R.string.nav_storage
    )

    object Settings : NavigationItems(
        "Settings",
        R.drawable.baseline_settings_24,
        R.string.nav_settings
    )

    object Viewer : NavigationItems(
        "Viewer",
        R.drawable.baseline_image_24,
        R.string.nav_viewer
    )

    object CardView : NavigationItems(
        "Card/{cardId}",
        R.drawable.baseline_image_24,
        R.string.nav_card
    )

    object Items : NavigationItems(
        "Items",
        R.drawable.baseline_data_24,
        R.string.nav_items
    )

    object MyItems : NavigationItems(
        "MyItems",
        R.drawable.baseline_data_24,
        R.string.nav_my_items
    )

    object ItemsStore : NavigationItems(
        "ItemsStore",
        R.drawable.baseline_data_24,
        R.string.nav_items_store
    )

    object ApplyItem : NavigationItems(
        "ApplyItem/{itemId}",
        R.drawable.baseline_data_24,
        R.string.nav_apply_item
    )

    object Adventure : NavigationItems(
        "Adventure",
        R.drawable.baseline_fort_24,
        R.string.nav_adventure
    )

    object Credits : NavigationItems(
        "Credits",
        R.drawable.baseline_data_24,
        R.string.nav_credits
    )
}