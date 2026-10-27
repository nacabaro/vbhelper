package com.github.nacabaro.vbhelper.screens.itemsScreen

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.nacabaro.vbhelper.R
import com.github.nacabaro.vbhelper.domain.items.ItemType
import com.github.nacabaro.vbhelper.utils.DeviceType

// Special missions only exist on the original Vital Bracelet, so they count as VB
fun ItemType.isCompatibleWith(deviceType: DeviceType): Boolean = when (this) {
    ItemType.UNIVERSAL -> true
    ItemType.BEITEM -> deviceType == DeviceType.BEDevice
    ItemType.VBITEM, ItemType.SPECIALMISSION -> deviceType == DeviceType.VBDevice
}

private fun badgeRes(itemType: ItemType): Int = when (itemType) {
    ItemType.BEITEM -> R.string.item_compat_badge_be
    ItemType.VBITEM, ItemType.SPECIALMISSION -> R.string.item_compat_badge_vb
    ItemType.UNIVERSAL -> R.string.item_compat_badge_all
}

private fun descriptionRes(itemType: ItemType): Int = when (itemType) {
    ItemType.BEITEM -> R.string.item_compat_be
    ItemType.VBITEM, ItemType.SPECIALMISSION -> R.string.item_compat_vb
    ItemType.UNIVERSAL -> R.string.item_compat_all
}

@Composable
fun ItemCompatibilityBadge(
    itemType: ItemType,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(badgeRes(itemType)),
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
fun itemCompatibilityText(itemType: ItemType): String {
    return stringResource(
        R.string.item_compat_label,
        stringResource(descriptionRes(itemType))
    )
}
