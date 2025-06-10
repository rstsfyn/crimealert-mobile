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
import java.text.SimpleDateFormat
import java.util.*

class CasesAdapter(
    private var casesList: List<ListReportsItem>,
    private val onItemClick: (ListReportsItem) -> Unit
) : RecyclerView.Adapter<CasesAdapter.NewsViewHolder>() {

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val casesImage: ImageView = itemView.findViewById(R.id.iv_news_image)
        val casesTitle: TextView = itemView.findViewById(R.id.tv_news_title)
        val casesDescription: TextView = itemView.findViewById(R.id.tv_news_description)
        val casesTimestamp: TextView = itemView.findViewById(R.id.tv_news_timestamp)
        val casesDate: TextView = itemView.findViewById(R.id.tv_news_date)
        val casesStatus: TextView = itemView.findViewById(R.id.tv_cases_status)
        val casesReporterAvatar: ImageView = itemView.findViewById(R.id.iv_reporter_avatar)
        val casesReporterName: TextView = itemView.findViewById(R.id.tv_reporter_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cases, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val currentItem = casesList[position]

        holder.casesTitle.text = currentItem.title ?: "No Title"
        holder.casesDescription.text = currentItem.description ?: "No Description"
        val timeAndDate = convertToGMT7(currentItem.createdAt)
        holder.casesTimestamp.text = timeAndDate.first
        holder.casesDate.text = timeAndDate.second

        val rawStatus = currentItem.statusKasus ?: "STATUS_TIDAK_DISET"
        val formattedStatus = rawStatus.lowercase()
            .split("_")
            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercaseChar() } }

        holder.casesStatus.text = formattedStatus

        when (rawStatus.uppercase()) {
            "BELUM_DITANGANI" -> {
                holder.casesStatus.setTextColor(holder.itemView.context.getColor(R.color.txt_belumditangani))
                holder.casesStatus.setBackgroundColor(holder.itemView.context.getColor(R.color.bg_belumditangani))
            }
            "SEDANG_DITANGANI" -> {
                holder.casesStatus.setTextColor(holder.itemView.context.getColor(R.color.txt_sedangditangani))
                holder.casesStatus.setBackgroundColor(holder.itemView.context.getColor(R.color.bg_sedangditangani))
            }
            "SUDAH_DITANGANI" -> {
                holder.casesStatus.setTextColor(holder.itemView.context.getColor(R.color.txt_sudahditangani))
                holder.casesStatus.setBackgroundColor(holder.itemView.context.getColor(R.color.bg_sudahditangani))
            }
            else -> {
                holder.casesStatus.setTextColor(holder.itemView.context.getColor(android.R.color.black))
                holder.casesStatus.setBackgroundColor(holder.itemView.context.getColor(android.R.color.darker_gray))
            }
        }

        Glide.with(holder.itemView.context)
            .load(currentItem.picture)
            .centerCrop()
            .placeholder(R.drawable.profilephoto)
            .into(holder.casesImage)

        Glide.with(holder.itemView.context)
            .load(currentItem.user?.avatar)
            .centerCrop()
            .placeholder(R.drawable.profilephoto)
            .into(holder.casesReporterAvatar)

        holder.casesReporterName.text = currentItem.user?.name ?: "Name"

        holder.itemView.setOnClickListener {
            onItemClick(currentItem)
        }
    }

    override fun getItemCount(): Int = casesList.size

    fun updateData(newData: List<ListReportsItem>) {
        casesList = newData
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