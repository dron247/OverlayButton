package com.dementiev.overlaysuperoldway

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

/**
 * RecyclerView adapter for collected data
 * Created by dron247 on 01.04.17.
 */
class AppsListAdapter(
        private val context: Context,
        private val items: List<Info>,
        private val clickListener: (Info) -> Unit) :
        RecyclerView.Adapter<AppsListAdapter.AppViewHolder>() {

    //optimization of resource usage
    private val DP16: Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, context.resources.displayMetrics).toInt()
    private val dateFormat = SimpleDateFormat("hh:mm.ss    dd.MM.yyyy")

    // do not load the animation here, it wont be beauty, because it is instance of a tweener
    //private val animation: Animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)


    override fun onBindViewHolder(holder: AppViewHolder?, position: Int) {
        val item = getItem(position)
        holder?.apply { // just like builder huh
            packageLabel?.text = item.packageName
            timeLabel?.text = dateFormat.format(Date(item.takeTime))
            nameLabel?.text = item.appLabel
            versionLabel?.text = item.appVersion
            container?.setOnClickListener { clickListener(item) }
            container?.let { animate(it) } // animate new view
        }
    }

    private fun animate(root: View) {
        AnimationUtils
                .loadAnimation(context, android.R.anim.slide_in_left)
                .let(root::startAnimation)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): AppViewHolder {
        // let's just create a layout from code
        val layout = LinearLayout(parent?.context)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layout.setPadding(0, DP16, 0, DP16)
        layout.layoutParams = lp
        layout.orientation = LinearLayout.VERTICAL

        val textViewLayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val nameLabel = TextView(parent?.context)
        nameLabel.layoutParams = textViewLayoutParams
        layout.addView(nameLabel)

        val packageLabel = TextView(parent?.context)
        packageLabel.layoutParams = textViewLayoutParams
        layout.addView(packageLabel)

        val versionLabel = TextView(parent?.context)
        versionLabel.layoutParams = textViewLayoutParams
        layout.addView(versionLabel)

        val timeLabel = TextView(parent?.context)
        timeLabel.layoutParams = textViewLayoutParams
        layout.addView(timeLabel)

        return AppViewHolder(layout, packageLabel, timeLabel, nameLabel, versionLabel)
    }

    /**
     *  Returns count of items in source collection
     */
    override fun getItemCount(): Int = items.size

    /**
     * Returns concrete item by it's position
     */
    private fun getItem(position: Int): Info = items[position]

    // yes, the whole class by 1 string
    class AppViewHolder(
            val container: View?,
            val packageLabel: TextView?,
            val timeLabel: TextView?,
            val nameLabel: TextView?,
            val versionLabel: TextView?
    ) : RecyclerView.ViewHolder(container)
}