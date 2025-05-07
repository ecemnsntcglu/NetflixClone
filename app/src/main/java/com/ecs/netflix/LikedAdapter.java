package com.ecs.netflix;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ecs.netflix.databinding.ItemChildBinding;


import java.util.List;

public class LikedAdapter extends RecyclerView.Adapter<LikedAdapter.LikedViewHolder> {

    private Context context;
    private List<Dizi> likedList;

    public LikedAdapter(Context context, List<Dizi> likedList) {
        this.context = context;
        this.likedList = likedList;
    }

    @NonNull
    @Override
    public LikedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChildBinding binding = ItemChildBinding.inflate(LayoutInflater.from(context), parent, false);
        return new LikedViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LikedViewHolder holder, int position) {
        Dizi dizi = likedList.get(position);
        holder.binding.textViewDiziAdi.setText(dizi.getTitle());

        Glide.with(context)
                .load(dizi.getPoster_url())
                .placeholder(R.drawable.placeholderpic)
                .into(holder.binding.imageViewDizi);
    }

    @Override
    public int getItemCount() {
        return likedList.size();
    }

    public static class LikedViewHolder extends RecyclerView.ViewHolder {
        ItemChildBinding binding;

        public LikedViewHolder(@NonNull ItemChildBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
