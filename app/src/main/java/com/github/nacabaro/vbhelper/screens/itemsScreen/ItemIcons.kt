package com.github.nacabaro.vbhelper.screens.itemsScreen

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.github.nacabaro.vbhelper.R
import com.github.nacabaro.vbhelper.dtos.ItemDtos

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
        ItemsScreenControllerImpl.ItemTypes.Trophies.id -> R.drawable.baseline_trophy_24
        ItemsScreenControllerImpl.ItemTypes.Wins.id -> R.drawable.baseline_wins_24
        ItemsScreenControllerImpl.ItemTypes.Battles.id -> R.drawable.baseline_battles_24
        ItemsScreenControllerImpl.ItemTypes.ResetLosses.id -> R.drawable.baseline_reset_losses_24
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

// Evolution items show their amount ("+5") instead of a timer icon
@Composable
fun ItemLengthIndicator(
    item: ItemDtos.ItemsWithQuantities,
    tint: Color,
    size: Dp,
    modifier: Modifier = Modifier
) {
    when {
        item.itemIcon == ItemsScreenControllerImpl.ItemTypes.ResetLosses.id -> Icon(
            painter = painterResource(id = R.drawable.baseline_reset_24),
            contentDescription = null,
            tint = tint,
            modifier = modifier.size(size)
        )
        item.itemIcon in ItemsScreenControllerImpl.ItemTypes.Trophies.id..ItemsScreenControllerImpl.ItemTypes.Battles.id -> Text(
            text = "+${item.itemLength}",
            color = tint,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value / 3).sp,
            modifier = modifier
        )
        else -> Icon(
            painter = painterResource(id = getLengthResource(item.itemLength)),
            contentDescription = null,
            tint = tint,
            modifier = modifier.size(size)
        )
    }
}
