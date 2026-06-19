package com.imhungry.looiai.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.imhungry.looiai.R
import com.imhungry.looiai.data.ChatMessage
import com.imhungry.looiai.databinding.ItemMessageBinding
import io.noties.markwon.Markwon

class ChatAdapter(private val markwon: Markwon) :
    RecyclerView.Adapter<ChatAdapter.MessageVH>() {

    private val items = mutableListOf<ChatMessage>()

    fun add(msg: ChatMessage) {
        items.add(msg)
        notifyItemInserted(items.size - 1)
    }

    fun updateLast(content: String) {
        if (items.isEmpty()) return
        val idx = items.size - 1
        items[idx] = items[idx].copy(content = content)
        notifyItemChanged(idx)
    }

    inner class MessageVH(val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageVH {
        val binding = ItemMessageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MessageVH(binding)
    }

    override fun onBindViewHolder(holder: MessageVH, position: Int) {
        val msg = items[position]
        val anim = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.slide_up)
        holder.itemView.startAnimation(anim)

        if (msg.role == "user") {
            holder.binding.tvUser.visibility = View.VISIBLE
            holder.binding.tvAi.visibility = View.GONE
            holder.binding.tvUser.text = msg.content
        } else {
            holder.binding.tvAi.visibility = View.VISIBLE
            holder.binding.tvUser.visibility = View.GONE
            markwon.setMarkdown(holder.binding.tvAi, msg.content)
        }
    }

    override fun getItemCount() = items.size
}
