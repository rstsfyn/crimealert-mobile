package com.restusofyan.crimealert_mobile.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.restusofyan.crimealert_mobile.data.response.casesreports.ListReportsItem
import com.restusofyan.crimealert_mobile.databinding.ItemNewsBinding

class NewsAdapter(
    private var items: List<ListReportsItem>,
    private val onItemClick: (ListReportsItem) -> Unit
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    fun updateData(newItems: List<ListReportsItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class NewsViewHolder(private val binding: ItemNewsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ListReportsItem) {
            binding.tvNewsTitle.text = item.title ?: "-"
            binding.tvNewsDescription.text = item.description ?: "-"
            binding.tvNewsDate.text = item.createdAt?.substringBefore("T") ?: "-"
            val pictureUrl = item.picture?.replace("localhost", "10.0.2.2")
            Glide.with(binding.root.context)
                .load(pictureUrl)
                .into(binding.ivNewsImage)

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = ItemNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
