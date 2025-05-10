package com.ecs.netflix;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private Context context;
    private List<Comment> commentList;

    public CommentAdapter(Context context, List<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.userName.setText(comment.getUserName());
        holder.commentText.setText(comment.getCommentText());

        // 🔥 Status değerine göre ikon belirleme
        if (comment.getStatus().equals("Beğendim")) {
            holder.statusIcon.setImageResource(R.drawable.ic_like);
        } else if (comment.getStatus().equals("Çok Beğendim")) {
            holder.statusIcon.setImageResource(R.drawable.ic_love);
        } else {
            holder.statusIcon.setImageResource(R.drawable.ic_dislike);
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView userName, commentText;
        ImageView statusIcon;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.txtUserName);
            commentText = itemView.findViewById(R.id.txtComment);
            statusIcon = itemView.findViewById(R.id.imgStatus);
        }
    }
}
