package com.restusofyan.crimealert_mobile.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.restusofyan.crimealert_mobile.R
import com.restusofyan.crimealert_mobile.data.response.casesreports.StatusHistoryItem
import com.restusofyan.crimealert_mobile.databinding.ItemStatusHistoryBinding
import java.text.SimpleDateFormat
import java.util.*

class StatusHistoryAdapter(
    private val statusHistoryList: List<StatusHistoryItem>
) : RecyclerView.Adapter<StatusHistoryAdapter.StatusHistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusHistoryViewHolder {
        val binding = ItemStatusHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StatusHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StatusHistoryViewHolder, position: Int) {
        holder.bind(statusHistoryList[position])
    }

    override fun getItemCount(): Int = statusHistoryList.size

    class StatusHistoryViewHolder(
        private val binding: ItemStatusHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StatusHistoryItem) {
            // Set updater info
            binding.tvUpdaterName.text = item.updater?.name ?: "Unknown"
            binding.tvUpdaterRole.text = when(item.updater?.role) {
                "polisi" -> "Polisi"
                "masyarakat" -> "Masyarakat"
                else -> item.updater?.role ?: "Unknown"
            }

            // Set date
            binding.tvHistoryDate.text = formatDate(item.createdAt)

            // Set status
            binding.tvStatusValue.text = formatStatus(item.statusKasus)

            // Set notes
            binding.tvNotesValue.text = item.notes ?: "Tidak ada catatan"

            // Set evidence photo if available
            if (!item.evidencePhoto.isNullOrEmpty()) {
                binding.ivEvidencePhoto.visibility = View.VISIBLE
                Glide.with(binding.root.context)
                    .load(item.evidencePhoto)
                    .placeholder(R.drawable.bg_photoreport)
                    .error(R.drawable.bg_photoreport)
                    .into(binding.ivEvidencePhoto)
            } else {
                binding.ivEvidencePhoto.visibility = View.GONE
            }
        }

        private fun formatDate(dateString: String?): String {
            return try {
                if (dateString.isNullOrEmpty()) return "--/--/----"
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")

                val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                outputFormat.timeZone = TimeZone.getTimeZone("GMT+7")

                val cleanDateString = dateString.replace("Z", "")
                val date = inputFormat.parse(cleanDateString)
                date?.let { outputFormat.format(it) } ?: "--/--/----"
            } catch (e: Exception) {
                dateString?.substring(0, minOf(10, dateString.length)) ?: "--/--/----"
            }
        }

        private fun formatStatus(status: String?): String {
            return when(status) {
                "belum_ditangani" -> "Belum Ditangani"
                "sedang_ditangani" -> "Sedang Ditangani"
                "sudah_ditangani" -> "Sudah Ditangani"
                "tidak_dapat_ditangani" -> "Tidak Dapat Ditangani"
                else -> status ?: "Unknown"
            }
        }
    }
}
