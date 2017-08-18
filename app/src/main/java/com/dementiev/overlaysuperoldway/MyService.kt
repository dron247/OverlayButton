package com.dementiev.overlaysuperoldway

import android.app.ActivityManager
import android.app.Service
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.Toast
import java.util.*

//private String _;


class MyService : Service() {
    private val CLICK_COOLDOWN: Long = 1000

    private var mLastClickTime: Long = 0

    private var mRootView: View? = null
    private lateinit var mParentApp: App
    private lateinit var mPackageManager: PackageManager

    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        mRootView = LayoutInflater.from(this).inflate(R.layout.overlay_button, null)

        mParentApp = application as App

        mPackageManager = mParentApp.packageManager

        mRootView?.let {
            createOverlay(it) {
                if (System.currentTimeMillis() - mLastClickTime > CLICK_COOLDOWN) {
                    mParentApp.appendInfo(foregroundTask)
                    mLastClickTime = System.currentTimeMillis()

                    Toast.makeText(this, "Data acquired", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mRootView != null) {
            windowManager.removeView(mRootView)
        }
    }


    private fun createOverlay(root: View, clickListener: (View) -> Unit?) {
        val windowLayoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT)
        windowLayoutParams.apply {
            gravity = Gravity.CENTER or Gravity.LEFT
            x = 0
            y = 10
        }

        val windowManager = windowManager
        windowManager.addView(root, windowLayoutParams)

        val container = root.findViewById(R.id.overlay_image_container)
        container.setOnTouchListener(object : View.OnTouchListener {
            private var initialX: Int = 0
            private var initialY: Int = 0
            private var initialTouchX: Float = 0.toFloat()
            private var initialTouchY: Float = 0.toFloat()
            private var isClick = false

            // catch a click on overlay button - not something trivial
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = windowLayoutParams.x
                        initialY = windowLayoutParams.y

                        //get the touch location
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                    }
                    MotionEvent.ACTION_UP -> {
                        val dX = (event.rawX - initialTouchX).toInt()
                        val dY = (event.rawY - initialTouchY).toInt()

                        //The check for dX <10 && YDiff< 10 because sometime elements moves a little while clicking.
                        //So that is click event.
                        isClick = dX < 10 && dY < 10
                    }
                    MotionEvent.ACTION_MOVE -> {
                        //Calculate the X and Y coordinates of the view.
                        windowLayoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                        windowLayoutParams.y = initialY + (event.rawY - initialTouchY).toInt()

                        //Update the layout with new X & Y coordinate
                        windowManager.updateViewLayout(root, windowLayoutParams)
                    }
                    else -> {
                    }
                }


                if (isClick) {
                    clickListener(container)
                }
                return true
            }
        })
    }


    private val windowManager: WindowManager
        get() = getSystemService(Context.WINDOW_SERVICE) as WindowManager


    private val foregroundTask: Info
        get() { // find out which app under our overlay
            var currentAppPackage = "error"
            var currentAppName = "error"
            var currentAppVersion = "error"

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                val usm = this.getSystemService(Service.USAGE_STATS_SERVICE) as UsageStatsManager
                val time = System.currentTimeMillis()
                val appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time)
                if (appList != null && appList.size > 0) {
                    val mySortedMap = TreeMap<Long, UsageStats>()
                    for (usageStats in appList) {
                        mySortedMap.put(usageStats.lastTimeUsed, usageStats)
                    }
                    if (!mySortedMap.isEmpty()) {
                        currentAppPackage = mySortedMap[mySortedMap.lastKey()]!!.packageName
                    }

                    val appInfo = mPackageManager.getApplicationInfo(currentAppPackage, 0)

                    try {
                        currentAppName = mPackageManager.getApplicationLabel(appInfo).toString()
                    } catch (notFound: PackageManager.NameNotFoundException) {
                        currentAppName = "unknown"
                    }

                    val pkgInfo = mPackageManager.getPackageInfo(currentAppPackage, 0)
                    currentAppVersion = pkgInfo.versionCode.toString() + "/" + pkgInfo.versionName
                }
            } else {
                val am = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val tasks = am.runningAppProcesses
                currentAppPackage = tasks[0].processName

                val appInfo = mPackageManager.getApplicationInfo(currentAppPackage, 0)

                try {
                    currentAppName = mPackageManager.getApplicationLabel(appInfo).toString()
                } catch (notFound: PackageManager.NameNotFoundException) {
                    currentAppName = "unknown"
                }

                val pkgInfo = mPackageManager.getPackageInfo(currentAppPackage, 0)
                currentAppVersion = pkgInfo.versionCode.toString() + "/" + pkgInfo.versionName
            }

            Log.e("EL_SERVIDOR", currentAppPackage)


            return Info(currentAppPackage, System.currentTimeMillis(), currentAppName, currentAppVersion)
        }

}
