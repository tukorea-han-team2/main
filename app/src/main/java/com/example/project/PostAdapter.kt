package com.example.project

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PostAdapter(private var posts: List<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        private val categoryTextView: TextView = itemView.findViewById(R.id.categoryTextView)
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)

        fun bind(post: Post) {
            descriptionTextView.text = post.description
            categoryTextView.text = post.category

            // 이미지 로딩 라이브러리를 사용하여 imageUrl을 imageView에 표시할 수 있음 (예: Glide, Picasso 등)
            if (!post.imageUrl.isNullOrEmpty()) {
                imageView.visibility = View.VISIBLE
                // 이미지 로딩 처리
                // Glide.with(itemView.context).load(post.imageUrl).into(imageView)
            } else {
                imageView.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updatePosts(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }
}