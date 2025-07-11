package com.example.must_connect.posts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.must_connect.R
import com.example.must_connect.models.Post
import com.example.must_connect.utils.DateUtils
import com.example.must_connect.App

class PostAdapter(
    private val posts: List<Post>,
    private val isTeacher: Boolean,
    private val listener: OnItemClickListener? = null,
    private val authorMap: Map<String, String> = emptyMap(),
    private val showEditButton: Boolean = true,
    private val isAdmin: Boolean = false  // Added isAdmin flag
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(post: Post)
        fun onEditClick(post: Post)
        fun onDeleteClick(post: Post)
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        val ivMedia: ImageView = itemView.findViewById(R.id.ivMedia)
        val tvAuthor: TextView = itemView.findViewById(R.id.tvAuthor)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvPostType: TextView = itemView.findViewById(R.id.tvPostType)
        val btnEdit: Button? = itemView.findViewById(R.id.btnEdit)
        val btnDelete: Button? = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val layout = if (isTeacher) R.layout.item_post_teacher else R.layout.item_post_student
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        val currentUserId = App.currentUser?.objectId ?: ""
        val isOwnPost = post.authorId == currentUserId

        holder.tvTitle.text = post.title
        holder.tvContent.text = post.content
        holder.tvAuthor.text = "Posted by ${authorMap[post.authorId] ?: "Unknown"}"
        holder.tvTime.text = DateUtils.formatDateTime(post.createdAt)

        holder.tvPostType.text = when (post.type) {
            "notice" -> "Notice"
            "suggestion" -> "Suggestion"
            "poll" -> "Poll"
            else -> "Post"
        }

        if (!post.mediaUrl.isNullOrEmpty()) {
            holder.ivMedia.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(post.mediaUrl)
                .placeholder(R.drawable.placeholder)
                .into(holder.ivMedia)
        } else {
            holder.ivMedia.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { listener?.onItemClick(post) }

        if (isTeacher) {
            // ADMIN: Always show delete button, never show edit button
            if (isAdmin) {
                holder.btnEdit?.visibility = View.GONE
                holder.btnDelete?.visibility = View.VISIBLE
            }
            // TEACHER: Show buttons only for own posts
            else {
                holder.btnEdit?.visibility = if (isOwnPost && showEditButton) View.VISIBLE else View.GONE
                holder.btnDelete?.visibility = if (isOwnPost) View.VISIBLE else View.GONE
            }

            holder.btnEdit?.setOnClickListener {
                if (isOwnPost) listener?.onEditClick(post)
            }

            holder.btnDelete?.setOnClickListener {
                if (isOwnPost || isAdmin) listener?.onDeleteClick(post)
            }
        }
    }

    override fun getItemCount() = posts.size
}