package com.example.must_connect.posts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.must_connect.R
import com.example.must_connect.models.Comment
import com.example.must_connect.utils.DateUtils

class CommentAdapter(
    private val comments: List<Comment>,
    private val showDeleteButton: Boolean = false,
    private val onDeleteClickListener: (Comment) -> Unit
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAuthor: TextView = itemView.findViewById(R.id.tvAuthor)
        val tvText: TextView = itemView.findViewById(R.id.tvText)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.tvAuthor.text = comment.authorName
        holder.tvText.text = comment.text
        holder.tvTime.text = comment.createdAt?.let { DateUtils.formatDateTime(it) } ?: ""

        holder.btnDelete.visibility = if (showDeleteButton) View.VISIBLE else View.GONE
        holder.btnDelete.setOnClickListener {
            onDeleteClickListener(comment)
        }
    }

    override fun getItemCount() = comments.size
}