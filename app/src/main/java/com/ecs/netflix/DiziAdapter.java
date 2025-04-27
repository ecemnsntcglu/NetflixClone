package com.ecs.netflix;

import android.content.Context;
import android.util.Log;
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

    public DiziAdapter(Context context, List<Dizi> diziListesi) {
        this.context = context;
        this.diziListesi = diziListesi;
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

        // Glide ile görsel yükleme
        Glide.with(context)
                .load(dizi.getPoster_url()) // Firestore'dan gelen URL
                .placeholder(R.drawable.placeholderpic) // Yüklenirken gösterilecek görsel
                .error(R.drawable.placeholderpic) // Hata durumunda gösterilecek görsel
                .into(holder.binding.imageViewDizi);
        Log.d("Firestore", "Poster URL: " + dizi.getPoster_url());
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