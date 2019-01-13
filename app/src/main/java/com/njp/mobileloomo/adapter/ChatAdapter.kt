package com.njp.mobileloomo.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.njp.mobileloomo.R

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    private val data = ArrayList<String>()

    fun add(item: String) {
        val position = data.size
        data.add(item)
        notifyItemInserted(position)
    }

    override fun getItemViewType(position: Int): Int {
        return if (data[position].split(":")[0] == "man") 0 else 1
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val itemView = LayoutInflater.from(p0.context)
                .inflate(
                        if (p1 == 0) R.layout.item_man else R.layout.item_robot,
                        p0,
                        false
                )
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.tvContent.text = data[p1].split(":")[1]
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvContent = itemView.findViewById<TextView>(R.id.tv_content)
    }

}