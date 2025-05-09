package com.ecs.netflix;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ecs.netflix.databinding.ItemChildBinding;

import java.util.List;

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ContentViewHolder> {

    private Context context;
    private List<Content> contentList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String id, String type); // 🔥 ID ve tür bilgisi gönderilecek
    }

    public ContentAdapter(Context context, List<Content> contentList, OnItemClickListener listener) {
        this.context = context;
        this.contentList = contentList;
        this.listener = listener;
    }

    public void setContentList(List<Content> yeniContentListesi) {
        this.contentList = yeniContentListesi;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChildBinding binding = ItemChildBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ContentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ContentViewHolder holder, int position) {
        Content content = contentList.get(position);
        holder.binding.textViewDiziAdi.setText(content.getTitle());

        Glide.with(holder.itemView.getContext())
                .load(content.getPosterUrl())
                .placeholder(R.drawable.placeholderpic)
                .error(R.drawable.placeholderpic)
                .into(holder.binding.imageViewDizi);

        // 🔥 Tıklama işlemi (ID ve tür bilgisi gönderiliyor)
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(content.getId(), content.getType()); // 🔥 ID ve tür bilgisi gönderiliyor
            }
        });
    }

    @Override
    public int getItemCount() {
        return (contentList != null) ? contentList.size() : 0;
    }

    public static class ContentViewHolder extends RecyclerView.ViewHolder {
        ItemChildBinding binding;

        public ContentViewHolder(@NonNull ItemChildBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}