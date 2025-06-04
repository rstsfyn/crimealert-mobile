package com.restusofyan.crimealert_mobile.data.notification

data class NotificationItem(
    val id: String,
    val type: NotificationType,
    val title: String,
    val description: String,
    val timestamp: String,
    val isRead: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val imageUrl: String? = null,
    val statusKasus: String? = null
)

enum class NotificationType {
    INCIDENT,
    REPORT
}