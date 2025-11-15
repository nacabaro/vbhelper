package com.github.nacabaro.vbhelper.screens.itemsScreen

import com.github.nacabaro.vbhelper.R

fun getIconResource(index: Int): Int {
    return when (index) {
        ItemsScreenControllerImpl.ItemTypes.PPTraining.id -> R.drawable.baseline_agility_24
        ItemsScreenControllerImpl.ItemTypes.APTraining.id -> R.drawable.baseline_attack_24
        ItemsScreenControllerImpl.ItemTypes.HPTraining.id -> R.drawable.baseline_shield_24
        ItemsScreenControllerImpl.ItemTypes.BPTraining.id -> R.drawable.baseline_trophy_24
        ItemsScreenControllerImpl.ItemTypes.AllTraining.id -> R.drawable.baseline_arrow_up_24
        6 -> R.drawable.baseline_timer_24
        7 -> R.drawable.baseline_rank_24
        8 -> R.drawable.baseline_vitals_24
        else -> R.drawable.baseline_question_mark_24
    }
}

fun getLengthResource(index: Int): Int {
    return when (index) {
        15 -> R.drawable.baseline_15_min_timer
        30 -> R.drawable.baseline_30_min_timer
        60 -> R.drawable.baseline_60_min_timer
        -60 -> R.drawable.baseline_60_min_timer
        300 -> R.drawable.baseline_5_hour_timer
        600 -> R.drawable.baseline_10_hour_timer
        -720 -> R.drawable.baseline_12_hour_timer
        -1440 -> R.drawable.baseline_24_hour_timer
        6000 -> R.drawable.baseline_reset_24
        1000 -> R.drawable.baseline_single_arrow_up
        2500 -> R.drawable.baseline_double_arrow_up
        5000 -> R.drawable.baseline_triple_arrow_up
        9999 -> R.drawable.baseline_health_24
        -500 -> R.drawable.baseline_single_arrow_down
        -1000 -> R.drawable.baseline_double_arrow_down
        -2500 -> R.drawable.baseline_triple_arrow_down
        -9999 -> R.drawable.baseline_reset_24
        else -> R.drawable.baseline_question_mark_24
    }
}