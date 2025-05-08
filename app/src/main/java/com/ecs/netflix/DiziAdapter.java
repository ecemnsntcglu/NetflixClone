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
        void onItemClick(String id); // 🔥 Artık sadece ID alıyor
    }

    public DiziAdapter(Context context, List<Dizi> diziListesi, OnItemClickListener listener) {
        this.context = context;
        this.diziListesi = diziListesi;
        this.listener = listener;
    }

    public void setDiziListesi(List<Dizi> yeniDiziListesi) {
        this.diziListesi = yeniDiziListesi;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DiziViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChildBinding binding = ItemChildBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new DiziViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DiziViewHolder holder, int position) {
        Dizi dizi = diziListesi.get(position);
        holder.binding.textViewDiziAdi.setText(dizi.getTitle());

        Glide.with(holder.itemView.getContext())
                .load(dizi.getPoster_url())
                .placeholder(R.drawable.placeholderpic)
                .error(R.drawable.placeholderpic)
                .into(holder.binding.imageViewDizi);

        // Tıklama işlemi (Sadece ID taşınıyor)
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(dizi.getId()); // 🔥 Artık hata vermeyecek
            }
        });
    }

    @Override
    public int getItemCount() {
        return (diziListesi != null) ? diziListesi.size() : 0;
    }

    public static class DiziViewHolder extends RecyclerView.ViewHolder {
        ItemChildBinding binding;

        public DiziViewHolder(@NonNull ItemChildBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}