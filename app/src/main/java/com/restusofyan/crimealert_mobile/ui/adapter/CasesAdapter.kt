package com.restusofyan.crimealert_mobile.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.restusofyan.crimealert_mobile.R
import com.restusofyan.crimealert_mobile.data.model.CasesModel
import com.restusofyan.crimealert_mobile.data.response.casesreports.ListReportsItem

class CasesAdapter(
    private var casesList: List<ListReportsItem>,
    private val onItemClick: (ListReportsItem) -> Unit
) : RecyclerView.Adapter<CasesAdapter.NewsViewHolder>() {

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val newsImage: ImageView = itemView.findViewById(R.id.iv_news_image)
        val newsTitle: TextView = itemView.findViewById(R.id.tv_news_title)
        val newsDescription: TextView = itemView.findViewById(R.id.tv_news_description)
        val newsTimestamp: TextView = itemView.findViewById(R.id.tv_news_timestamp)
        val newsDate: TextView = itemView.findViewById(R.id.tv_news_date)
        val newsStatus: TextView = itemView.findViewById(R.id.tv_cases_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cases, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val currentItem = casesList[position]

        holder.newsTitle.text = currentItem.title ?: "No Title"
        holder.newsDescription.text = currentItem.description ?: "No Description"
        val time = currentItem.createdAt?.let {
            val tIndex = it.indexOf('T')
            if (tIndex != -1 && it.length >= tIndex + 6) {
                it.substring(tIndex + 1, tIndex + 6)
            } else {
                "--:--"
            }
        } ?: "--:--"
        holder.newsTimestamp.text = time
        holder.newsDate.text = currentItem.createdAt?.substringBefore("T") ?: "----/--/--"
        holder.newsStatus.text = currentItem.statusKasus ?: "Status Kasus"

        Glide.with(holder.itemView.context)
            .load(currentItem.picture?.replace("localhost", "10.0.2.2"))
            .centerCrop()
            .placeholder(R.drawable.profilephoto)
            .into(holder.newsImage)

        holder.itemView.setOnClickListener {
            onItemClick(currentItem)
        }
    }

    override fun getItemCount(): Int = casesList.size

    fun updateData(newData: List<ListReportsItem>) {
        casesList = newData
        notifyDataSetChanged()
    }
}
