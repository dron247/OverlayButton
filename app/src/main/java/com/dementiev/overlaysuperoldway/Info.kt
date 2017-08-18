package com.dementiev.overlaysuperoldway

/**
 * A data class, like POJO
 * Created by dron on 01.04.17.
 */

data class Info(
        val packageName: String,
        val takeTime: Long,
        val appLabel: String,
        val appVersion: String
) {
    override fun toString(): String = "$appLabel($packageName:$appVersion)"
}
