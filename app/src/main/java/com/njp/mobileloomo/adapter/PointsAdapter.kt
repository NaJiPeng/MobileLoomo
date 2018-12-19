package com.njp.mobileloomo.adapter

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.njp.mobileloomo.R
import com.njp.mobileloomo.bean.Point
import com.njp.mobileloomo.databinding.ItemPointBinding

class PointsAdapter(val listener: (Int) -> Unit) : RecyclerView.Adapter<PointsAdapter.ViewHolder>() {

    private val mList = ArrayList<Point>()

    fun setData(data: String) {
        if (data.isEmpty()) {
            return
        }
        mList.addAll(
                data.split(",").map {
                    val strings = it.split("-")
                    Point(strings[0].toInt(), strings[1])
                })
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val itemView = LayoutInflater.from(p0.context)
                .inflate(R.layout.item_point, p0, false)

        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.mBinding?.tvName?.text = mList[p1].name
        p0.itemView.setOnClickListener {
            listener.invoke(mList[p1].id)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mBinding = DataBindingUtil.bind<ItemPointBinding>(itemView)
    }
}