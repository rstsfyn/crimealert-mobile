package com.restusofyan.crimealert_mobile.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.restusofyan.crimealert_mobile.data.notification.NotificationItem

class NotificationRepository(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("notifications", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveNotification(notification: NotificationItem) {
        val notifications = getNotifications().toMutableList()
        notifications.add(0, notification) // Add to top


        if (notifications.size > 50) {
            notifications.removeAt(notifications.size - 1)
        }

        val json = gson.toJson(notifications)
        sharedPreferences.edit().putString("notification_list", json).apply()
    }

    fun getNotifications(): List<NotificationItem> {
        val json = sharedPreferences.getString("notification_list", null)
        return if (json != null) {
            val type = object : TypeToken<List<NotificationItem>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun markAsRead(notificationId: String) {
        val notifications = getNotifications().toMutableList()
        val index = notifications.indexOfFirst { it.id == notificationId }
        if (index != -1) {
            notifications[index] = notifications[index].copy(isRead = true)
            val json = gson.toJson(notifications)
            sharedPreferences.edit().putString("notification_list", json).apply()
        }
    }

    fun clearAll() {
        sharedPreferences.edit().remove("notification_list").apply()
    }
}