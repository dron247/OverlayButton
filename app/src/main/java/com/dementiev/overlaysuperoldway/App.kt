package com.dementiev.overlaysuperoldway

import android.app.Application
import java.util.*

/**
 * Just some singleton to collect data
 * Created by dron247 on 01.04.17.
 */

class App : Application() {
    // should be explicitly specified with mutable type
    private val mCollected: MutableList<Info> = ArrayList()

    fun appendInfo(info: Info) {
        if (info.packageName == packageName) return
        mCollected.add(info)
    }

    val collection: List<Info>
        get() = mCollected
}
