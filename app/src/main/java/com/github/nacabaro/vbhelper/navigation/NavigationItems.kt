package com.github.nacabaro.vbhelper.navigation

import com.github.nacabaro.vbhelper.R

sealed class NavigationItems (
    var route: String,
    var icon: Int,
    var label: String
) {
    object Scan : NavigationItems("Scan/{characterId}", R.drawable.baseline_nfc_24, "Scan")
    object Battles : NavigationItems("Battle", R.drawable.baseline_swords_24, "Battle")
    object Home : NavigationItems("Home", R.drawable.baseline_cottage_24, "Home")
    object Dex : NavigationItems("Dex", R.drawable.baseline_menu_book_24, "Dex")
    object Storage : NavigationItems("Storage", R.drawable.baseline_catching_pokemon_24, "Storage")
    object Settings : NavigationItems("Settings", R.drawable.baseline_settings_24, "Settings")
    object Viewer : NavigationItems("Viewer", R.drawable.baseline_image_24, "Viewer")
    object CardView : NavigationItems("Card/{cardId}", R.drawable.baseline_image_24, "Card")
    object Items : NavigationItems("Items", R.drawable.baseline_data_24, "Items")
    object MyItems : NavigationItems("MyItems", R.drawable.baseline_data_24, "My items")
    object ItemsStore : NavigationItems("ItemsStore", R.drawable.baseline_data_24, "Items store")
}