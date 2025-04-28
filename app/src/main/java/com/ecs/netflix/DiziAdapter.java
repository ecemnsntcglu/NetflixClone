package com.ecs.netflix;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ecs.netflix.databinding.ItemChildBinding;

import java.util.List;

public class DiziAdapter extends RecyclerView.Adapter<DiziAdapter.DiziViewHolder> {

    private Context context;
    private List<Dizi> diziListesi;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Dizi dizi);
    }

    public DiziAdapter(Context context, List<Dizi> diziListesi, OnItemClickListener listener) {
        this.context = context;
        this.diziListesi = diziListesi;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DiziViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChildBinding binding = ItemChildBinding.inflate(LayoutInflater.from(context), parent, false);
        return new DiziViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DiziViewHolder holder, int position) {
        Dizi dizi = diziListesi.get(position);
        holder.binding.textViewDiziAdi.setText(dizi.getTitle());

        Glide.with(context)
                .load(dizi.getPoster_url())
                .placeholder(R.drawable.placeholderpic)
                .error(R.drawable.placeholderpic)
                .into(holder.binding.imageViewDizi);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(dizi);
            }
        });
    }

    @Override
    public int getItemCount() {
        return diziListesi.size();
    }

    public static class DiziViewHolder extends RecyclerView.ViewHolder {
        ItemChildBinding binding;

        public DiziViewHolder(@NonNull ItemChildBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
