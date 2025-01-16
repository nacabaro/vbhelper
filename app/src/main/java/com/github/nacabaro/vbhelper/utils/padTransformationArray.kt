package com.github.nacabaro.vbhelper.utils

import com.github.cfogrady.vbnfc.data.NfcCharacter

fun padTransformationArray(
    transformationArray: Array<NfcCharacter.Transformation>
): Array<NfcCharacter.Transformation> {
    if (transformationArray.size >= 8) {
        return transformationArray
    }

    val paddedArray = Array(8) {
        NfcCharacter.Transformation(
            toCharIndex = 255u,
            year = 65535u,
            month = 255u,
            day = 255u
        )
    }

    System.arraycopy(transformationArray, 0, paddedArray, 0, transformationArray.size)
    return paddedArray
}