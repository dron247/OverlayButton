package com.dementiev.overlaysuperoldway

import android.app.Application

import java.util.ArrayList

/**
 * Created by dron on 01.04.17.
 */

class App : Application() {
    private val mCollected: MutableList<Info> = ArrayList()

    fun appendInfo(info: Info) {
        if (info.packageName == packageName) return
        mCollected.add(info)
    }

    val collection: List<Info>
        get() = mCollected
}
