package com.ecs.netflix;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        holder.binding.textViewDiziAdi.setText(dizi.getDiziAdi());
        holder.binding.imageViewDizi.setImageResource(
                context.getResources().getIdentifier(dizi.getDiziResimUrl(), "drawable", context.getPackageName())
        );
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
