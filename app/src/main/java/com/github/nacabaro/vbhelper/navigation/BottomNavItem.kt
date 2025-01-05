package com.github.nacabaro.vbhelper.navigation

import com.github.nacabaro.vbhelper.R

sealed class BottomNavItem (
    var route: String,
    var icon: Int,
    var label: String
) {
    object Scan : BottomNavItem("Scan", R.drawable.baseline_nfc_24, "Scan")
    object Battles : BottomNavItem("Battle", R.drawable.baseline_swords_24, "Battle")
    object Home : BottomNavItem("Home", R.drawable.baseline_cottage_24, "Home")
    object Dex : BottomNavItem("Dex", R.drawable.baseline_menu_book_24, "Dex")
    object Storage : BottomNavItem("Storage", R.drawable.baseline_catching_pokemon_24, "Storage")
    object Settings : BottomNavItem("Settings", R.drawable.baseline_settings_24, "Settings")
}