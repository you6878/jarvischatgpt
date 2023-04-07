package com.howlab.jarvischatgpt.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.howlab.jarvischatgpt.R
import com.howlab.jarvischatgpt.databinding.ListItemGroupChatAiBinding
import com.howlab.jarvischatgpt.databinding.ListItemGroupChatUserMeBinding
import com.howlab.jarvischatgpt.databinding.ListItemGroupChatUserOtherBinding
import java.time.LocalDateTime

data class ChatMessage(val message: String, val time: String, val role: String) {
    companion object {
        fun user(message: String): ChatMessage {
            val now = LocalDateTime.now()
            val minute = if (now.minute.toString().length == 2) {
                now.minute.toString()
            } else {
                "0${now.minute}"
            }

            val time = "${now.hour}:${minute}"

            return ChatMessage(message, time, role = "USER")
        }

        fun ai(message: String): ChatMessage {
            val now = LocalDateTime.now()
            val minute = if (now.minute.toString().length == 2) {
                now.minute.toString()
            } else {
                "0${now.minute}"
            }

            val time = "${now.hour}:${minute}"

            return ChatMessage(message, time, role = "AI")
        }

        fun gpt(message: String): ChatMessage {
            val now = LocalDateTime.now()
            val minute = if (now.minute.toString().length == 2) {
                now.minute.toString()
            } else {
                "0${now.minute}"
            }

            val time = "${now.hour}:${minute}"

            return ChatMessage(message, time, role = "")
        }
    }
}

class ChatAdapter(
    private val onClick: (ChatMessage) -> Unit
) : ListAdapter<ChatMessage, ChatViewHolder>(differ) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).role) {
            "USER" -> R.layout.list_item_group_chat_user_me
            "AI" -> R.layout.list_item_group_chat_user_other
            else -> R.layout.list_item_group_chat_ai
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return when (viewType) {
            R.layout.list_item_group_chat_user_me -> {
                ChatViewHolder.UserViewHolder(
                    ListItemGroupChatUserMeBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent, false
                    ), onClick
                )
            }
            R.layout.list_item_group_chat_user_other -> ChatViewHolder.AiViewHolder(
                ListItemGroupChatUserOtherBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false
                ), onClick
            )
            else -> ChatViewHolder.GptViewHolder(
                ListItemGroupChatAiBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val item = getItem(position)

        if (holder is ChatViewHolder.UserViewHolder) {
            holder.bind(item)
            return
        }

        if (holder is ChatViewHolder.AiViewHolder) {
            holder.bind(item)
            return
        }

        if (holder is ChatViewHolder.GptViewHolder) {
            holder.bind(item)
        }
    }

    companion object {
        val differ = object : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
                return oldItem.time == newItem.time
            }

            override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
                return oldItem == newItem
            }
        }
    }
}

sealed class ChatViewHolder(
    binding: ViewDataBinding
) : RecyclerView.ViewHolder(binding.root) {

    class UserViewHolder(
        private val binding: ListItemGroupChatUserMeBinding,
        val onClick: (ChatMessage) -> Unit
    ) :
        ChatViewHolder(binding) {

        fun bind(chat: ChatMessage) {
            binding.textGroupChatMessage.text = chat.message
            binding.textGroupChatTime.text = chat.time

            binding.root.setOnClickListener {
                onClick.invoke(chat)
            }
        }
    }

    class AiViewHolder(
        private val binding: ListItemGroupChatUserOtherBinding,
        val onClick: (ChatMessage) -> Unit
    ) : ChatViewHolder(binding) {

        fun bind(chat: ChatMessage) {
            binding.textGroupChatMessage.text = chat.message
            binding.textGroupChatTime.text = chat.time

            binding.root.setOnClickListener {
                onClick.invoke(chat)
            }
        }
    }

    class GptViewHolder(
        private val binding: ListItemGroupChatAiBinding
    ) : ChatViewHolder(binding) {

        fun bind(chat: ChatMessage) {
            binding.textGroupChatMessage.text = chat.message
            binding.textGroupChatTime.text = chat.time
        }
    }
}