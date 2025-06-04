package com.restusofyan.crimealert_mobile.ui.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.restusofyan.crimealert_mobile.R
import com.restusofyan.crimealert_mobile.data.notification.NotificationItem
import com.restusofyan.crimealert_mobile.data.notification.NotificationType
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    private val context: Context,
    private var notifications: List<NotificationItem>,
    private val onNotificationClick: (NotificationItem) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    companion object {
        private const val TYPE_INCIDENT = 0
        private const val TYPE_REPORT = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (notifications[position].type) {
            NotificationType.INCIDENT -> TYPE_INCIDENT
            NotificationType.REPORT -> TYPE_REPORT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val layout = when (viewType) {
            TYPE_INCIDENT -> R.layout.item_notification_incident
            TYPE_REPORT -> R.layout.item_notification_report
            else -> R.layout.item_notification_incident
        }

        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification)
    }

    override fun getItemCount(): Int = notifications.size

    fun updateNotifications(newNotifications: List<NotificationItem>) {
        notifications = newNotifications
        notifyDataSetChanged()
    }

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardNotification)
        private val iconNotification: ImageView = itemView.findViewById(R.id.iconNotification)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvNotificationTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvNotificationDescription)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvNotificationTimestamp)
        private val ivNotificationImage: ImageView? = itemView.findViewById(R.id.ivNotificationImage)
        private val tvStatus: TextView? = itemView.findViewById(R.id.tvNotificationStatus)
        private val unreadIndicator: View = itemView.findViewById(R.id.unreadIndicator)

        fun bind(notification: NotificationItem) {
            tvTitle.text = notification.title
            tvDescription.text = notification.description

            tvTimestamp.text = formatTimestamp(notification.timestamp)

            when (notification.type) {
                NotificationType.INCIDENT -> {
                    iconNotification.setImageResource(R.drawable.ic_incident)
                    iconNotification.setColorFilter(ContextCompat.getColor(context, R.color.red))
                }
                NotificationType.REPORT -> {
                    iconNotification.setImageResource(R.drawable.ic_report)
                    iconNotification.setColorFilter(ContextCompat.getColor(context, R.color.blue))
                }
            }

            unreadIndicator.visibility = if (notification.isRead) View.GONE else View.VISIBLE

            if (notification.isRead) {
                cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
            } else {
                cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.grey))
            }

            ivNotificationImage?.let { imageView ->
                if (!notification.imageUrl.isNullOrEmpty()) {
                    imageView.visibility = View.VISIBLE
                    Glide.with(context)
                        .load(notification.imageUrl)
                        .placeholder(R.drawable.placeholder_photoreport)
                        .error(R.drawable.placeholder_photoreport)
                        .into(imageView)
                } else {
                    imageView.visibility = View.GONE
                }
            }

            tvStatus?.let { statusView ->
                if (notification.type == NotificationType.REPORT && !notification.statusKasus.isNullOrEmpty()) {
                    statusView.visibility = View.VISIBLE
                    statusView.text = "Status: ${notification.statusKasus}"
                } else {
                    statusView.visibility = View.GONE
                }
            }

            cardView.setOnClickListener {
                onNotificationClick(notification)
            }
        }

        private fun formatTimestamp(timestamp: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                val date = inputFormat.parse(timestamp)
                outputFormat.format(date ?: Date())
            } catch (e: Exception) {
                timestamp
            }
        }
    }
}