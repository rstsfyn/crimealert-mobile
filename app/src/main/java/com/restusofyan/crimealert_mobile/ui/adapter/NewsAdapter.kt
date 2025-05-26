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

class NewsAdapter(
    private val newsList: List<CasesModel>,
    private val onItemClick: (CasesModel) -> Unit
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val newsImage: ImageView = itemView.findViewById(R.id.iv_news_image)
        val newsTitle: TextView = itemView.findViewById(R.id.tv_news_title)
        val newsDescription: TextView = itemView.findViewById(R.id.tv_news_description)
        val newsTimestamp: TextView = itemView.findViewById(R.id.tv_news_timestamp)
        val newsDate: TextView = itemView.findViewById(R.id.tv_news_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val currentItem = newsList[position]

        holder.newsTitle.text = currentItem.title
        holder.newsDescription.text = currentItem.description
        holder.newsTimestamp.text = currentItem.timestamp
        holder.newsDate.text = currentItem.date

        Glide.with(holder.itemView.context)
            .load(currentItem.imageUrl)
            .centerCrop()
            .placeholder(R.drawable.profilephoto)
            .into(holder.newsImage)

        // Item click listener
        holder.itemView.setOnClickListener {
            onItemClick(currentItem)
        }
    }

    override fun getItemCount(): Int = newsList.size
}
