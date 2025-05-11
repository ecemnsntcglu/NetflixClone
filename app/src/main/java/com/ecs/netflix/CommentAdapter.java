package com.ecs.netflix;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.VH> {
    private List<Comment> list;
    private Context ctx;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    public CommentAdapter(Context ctx, List<Comment> list) {
        this.ctx = ctx;
        this.list = list;
    }

    public static class VH extends RecyclerView.ViewHolder {
        ImageView ivPhoto, ivStatus; // 🔥 Status ikonu eklendi
        TextView tvUserName, tvDate, tvCommentText;

        public VH(View v) {
            super(v);
            ivPhoto = v.findViewById(R.id.ivUserPhoto);
            ivStatus = v.findViewById(R.id.ivstatus); // 🔥 Status ikonu bağlandı
            tvUserName = v.findViewById(R.id.tvUserName);
            tvDate = v.findViewById(R.id.tvDate);
            tvCommentText = v.findViewById(R.id.tvCommentText);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int i) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_comment, p, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Comment c = list.get(pos);
        h.tvCommentText.setText(c.getCommentText());

        // 🔥 Tarih formatla:
        Date d = new Date(c.getTimestamp());
        String formatted = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(d);
        h.tvDate.setText(formatted);

        // 🔥 Kullanıcı adı + fotoğraf + status çek:
        db.collection("users").document(c.getUserId())
                .get().addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        String url  = doc.getString("profileImage");
                        h.tvUserName.setText(name != null ? name : "Anonim");
                        if (url != null && !url.isEmpty()) {
                            Glide.with(ctx).load(url).circleCrop().into(h.ivPhoto);
                        }
                    }
                });

        // 🔥 Status değerine göre ikon değiştir
        if ("like".equals(c.getStatus())) {
            h.ivStatus.setImageResource(R.drawable.ic_like);
        } else if ("love".equals(c.getStatus())) {
            h.ivStatus.setImageResource(R.drawable.ic_love);
        } else if ("dislike".equals(c.getStatus())) {
            h.ivStatus.setImageResource(R.drawable.ic_dislike);
        } else {
            h.ivStatus.setVisibility(View.GONE); // Eğer status yoksa ikon gizlenir
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}