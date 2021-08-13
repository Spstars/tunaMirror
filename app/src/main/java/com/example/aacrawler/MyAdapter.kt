package com.example.aacrawler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(var items:ArrayList<TunaData>,val textSize:Float):RecyclerView.Adapter<MyAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var nameView: TextView
        var aaView:TextView
        init{
            aaView=itemView.findViewById(R.id.AAview)
            nameView = itemView.findViewById(R.id.nameTuna)
            aaView.setTextSize(textSize)
            nameView.setTextSize(textSize*1.5F)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v=LayoutInflater.from(parent.context)
            .inflate(R.layout.aaview,parent,false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.aaView.text=items[position].aa
        holder.nameView.text=items[position].nickname

    }
    override fun getItemCount(): Int {
        return items.size
    }

}