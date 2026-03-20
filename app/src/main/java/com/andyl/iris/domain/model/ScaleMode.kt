package com.andyl.iris.domain.model

import androidx.annotation.StringRes
import com.andyl.iris.R

enum class ScaleMode(@param:StringRes val labelRes: Int) {
    CROP(R.string.crop),
    STRETCH(R.string.stretch),
    FIT(R.string.fit)
}
