package com.example.must_connect.posts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.must_connect.R
import com.example.must_connect.models.Post

class PostAdapter(
    private val posts: List<Post>,
    private val isTeacher: Boolean // Different UI for teachers
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        val ivMedia: ImageView = itemView.findViewById(R.id.ivMedia)
        val tvAuthor: TextView = itemView.findViewById(R.id.tvAuthor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val layout = if (isTeacher) R.layout.item_post_teacher else R.layout.item_post_student
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        holder.tvTitle.text = post.title
        holder.tvContent.text = post.content

        // Load media if available
        if (!post.mediaUrl.isNullOrEmpty()) {
            holder.ivMedia.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(post.mediaUrl)
                .placeholder(R.drawable.placeholder)
                .into(holder.ivMedia)
        } else {
            holder.ivMedia.visibility = View.GONE
        }

        // Load author name (simplified - in real app you'd fetch user data)
        holder.tvAuthor.text = "Posted by Teacher"
    }

    override fun getItemCount() = posts.size
}