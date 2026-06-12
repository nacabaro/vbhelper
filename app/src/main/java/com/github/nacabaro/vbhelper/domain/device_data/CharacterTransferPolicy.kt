package com.github.nacabaro.vbhelper.domain.device_data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.github.nacabaro.vbhelper.transfer.ExportFormat
import com.github.nacabaro.vbhelper.transfer.TransferTarget
import com.github.nacabaro.vbhelper.transfer.TransferTransport
import com.github.nacabaro.vbhelper.utils.DeviceType

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = UserCharacter::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class CharacterTransferPolicy(
    @PrimaryKey val characterId: Long,
    val nativeDeviceType: DeviceType,
    val preferredHceExportFormat: ExportFormat,
    val preferredNfcaExportFormat: ExportFormat,
    val lastObservedImportFormat: ExportFormat?,
    val lastTransferTransport: TransferTransport?,
    val lastTransferTarget: TransferTarget?,
    val preserveVbRoundTrip: Boolean,
    val preserveBeRoundTrip: Boolean,
)

