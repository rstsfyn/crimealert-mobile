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
import java.text.SimpleDateFormat
import java.util.*

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

        val timeAndDate = convertToGMT7(currentItem.createdAt)
        holder.newsTimestamp.text = timeAndDate.first
        holder.newsDate.text = timeAndDate.second

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

    private fun convertToGMT7(dateTimeString: String?): Pair<String, String> {
        if (dateTimeString.isNullOrEmpty()) {
            return Pair("--:--", "----/--/--")
        }

        return try {
            val inputFormats = listOf(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                },
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                },
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
            )

            var date: Date? = null
            for (format in inputFormats) {
                try {
                    date = format.parse(dateTimeString)
                    break
                } catch (e: Exception) {
                    continue
                }
            }

            if (date == null) {
                return Pair("--:--", "----/--/--")
            }

            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("GMT+7")
            }
            val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("GMT+7")
            }

            val time = timeFormat.format(date)
            val dateFormatted = dateFormat.format(date)

            Pair(time, dateFormatted)

        } catch (e: Exception) {
            e.printStackTrace()
            Pair("--:--", "----/--/--")
        }
    }
}