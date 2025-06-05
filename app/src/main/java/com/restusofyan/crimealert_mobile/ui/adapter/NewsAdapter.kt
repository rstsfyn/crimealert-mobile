package com.restusofyan.crimealert_mobile.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.restusofyan.crimealert_mobile.R
import com.restusofyan.crimealert_mobile.data.response.casesreports.ListReportsItem

class NewsAdapter(
    private var newsList: List<ListReportsItem>,
    private val onItemClick: (ListReportsItem) -> Unit
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val newsImage: ImageView = itemView.findViewById(R.id.iv_news_image)
        val newsTitle: TextView = itemView.findViewById(R.id.tv_news_title)
        val newsDescription: TextView = itemView.findViewById(R.id.tv_news_description)
        val newsTimestamp: TextView = itemView.findViewById(R.id.tv_news_timestamp)
        val newsDate: TextView = itemView.findViewById(R.id.tv_news_date)
        val newsReporterAvatar: ImageView = itemView.findViewById(R.id.iv_reporter_avatar)
        val newsReporterName: TextView = itemView.findViewById(R.id.tv_reporter_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val currentItem = newsList[position]

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

        Glide.with(holder.itemView.context)
            .load(currentItem.picture)
            .centerCrop()
            .placeholder(R.drawable.profilephoto)
            .into(holder.newsImage)

        Glide.with(holder.itemView.context)
            .load(currentItem.user?.avatar)
            .centerCrop()
            .placeholder(R.drawable.profilephoto)
            .into(holder.newsReporterAvatar)

        holder.newsReporterName.text = currentItem.user?.name ?: "Name"

        holder.itemView.setOnClickListener {
            onItemClick(currentItem)
        }
    }

    override fun getItemCount(): Int = newsList.size

    fun updateData(newData: List<ListReportsItem>) {
        newsList = newData
        notifyDataSetChanged()
    }
}
