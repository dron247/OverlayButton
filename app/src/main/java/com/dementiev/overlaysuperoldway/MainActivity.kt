package com.dementiev.overlaysuperoldway

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import android.app.AppOpsManager
import android.content.Context
import android.content.Context.APP_OPS_SERVICE


class MainActivity : AppCompatActivity() {
    private val OVERLAY_PERMISSION_REQUEST_CODE = 42
    private val USAGESTATS_PERMISSION_REQUEST_CODE = 228

    private var mHaveOverlayPermission = true
    private var mHaveStatsPermission = true
    private lateinit var mButtonToggleService: Button
    private lateinit var mCollectionList: RecyclerView
    private lateinit var mServiceIntent: Intent
    private lateinit var mApplication: App

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mServiceIntent = Intent(this, MyService::class.java)
        mApplication = this.application as App

        setContentView(R.layout.activity_main)
        mButtonToggleService = findViewById(R.id.button_toggle_service) as Button
        mCollectionList = findViewById(R.id.apps_collection) as RecyclerView
        mButtonToggleService.setOnClickListener { toggleService() }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            // nope
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + packageName))
            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
        } else {
            // We good from the start
            mHaveOverlayPermission = true
            showToast("Yay overlay!")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !checkUsageStatsPermission()) {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivityForResult(intent, USAGESTATS_PERMISSION_REQUEST_CODE)
            // request usage stats
        } else {
            mHaveStatsPermission = true
            showToast("Yay overlay stats!")
        }
    }

    override fun onResume() {
        super.onResume()
        stopMyService()
        mCollectionList.layoutManager = LinearLayoutManager(this)
        mCollectionList.adapter = AppsListAdapter(this, mApplication.collection) {
            showToast(it.toString())
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            OVERLAY_PERMISSION_REQUEST_CODE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mHaveOverlayPermission = Settings.canDrawOverlays(this)
                    showToast(if (mHaveOverlayPermission) "Yay overlay!" else "No overlay permission :(")
                }
            }
            USAGESTATS_PERMISSION_REQUEST_CODE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mHaveStatsPermission = checkUsageStatsPermission()
                    showToast(if (mHaveStatsPermission) "Yay stats!" else "No stats permission :(")
                }
            }
            else -> {
            }
        }
    }

    private fun checkUsageStatsPermission(): Boolean {
        val mgr = applicationContext
                .getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        return mgr.checkOpNoThrow("android:get_usage_stats",
                android.os.Process.myUid(), this.packageName) == AppOpsManager.MODE_ALLOWED
    }

    private fun toggleService() {
        if (!stopMyService()) {
            startMyService()
        }
    }

    private fun stopMyService(): Boolean = stopService(mServiceIntent)

    private fun startMyService() {
        startService(mServiceIntent)
    }

    private fun showToast(value: String) {
        Toast.makeText(this, value, Toast.LENGTH_LONG).show()
    }

}
