package com.common.baseui.extension

import android.content.Context
import android.content.res.Resources
import androidx.viewbinding.ViewBinding

inline val ViewBinding.context: Context get() = root.context
inline val ViewBinding.res: Resources get() = root.context.resources