package com.njp.mobileloomo.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.njp.mobileloomo.R
import com.njp.mobileloomo.bean.Coor2D

class PointsAdapter : RecyclerView.Adapter<PointsAdapter.ViewHolder>() {

    private val data = ArrayList<Coor2D>()

    fun add(point: Coor2D) {
        data.add(point)
        notifyItemInserted(data.size - 1)
    }

    fun remove() {
        if (data.size > 0) {
            data.removeAt(data.size - 1)
            notifyItemRemoved(data.size)
        }
    }

    fun clear() {
        val size = data.size
        data.clear()
        notifyItemRangeRemoved(0, size)
    }

    fun getPonits() = data.map { it.id }.joinToString(",")

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val itemView = LayoutInflater.from(p0.context)
                .inflate(R.layout.item_point, p0, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.textView.text = data[p1].name
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView = itemView.findViewById<TextView>(R.id.tv_name)
    }
}