package com.ecs.netflix;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ecs.netflix.databinding.ItemBolumBinding;
import java.util.List;

public class BolumAdapter extends RecyclerView.Adapter<BolumAdapter.BolumViewHolder> {

    private Context context;
    private List<Bolum> bolumListesi;

    public BolumAdapter(Context context, List<Bolum> bolumListesi) {
        this.context = context;
        this.bolumListesi = bolumListesi;
    }

    @NonNull
    @Override
    public BolumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBolumBinding binding = ItemBolumBinding.inflate(LayoutInflater.from(context), parent, false);
        return new BolumViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BolumViewHolder holder, int position) {
        Bolum bolum = bolumListesi.get(position);
        holder.binding.textViewBolumAdi.setText(bolum.getBolumNo() + ". Bölüm: " + bolum.getAd());
        holder.binding.textViewSure.setText(bolum.getSure() + " dk");
    }

    @Override
    public int getItemCount() {
        return bolumListesi.size();
    }

    public static class BolumViewHolder extends RecyclerView.ViewHolder {
        ItemBolumBinding binding;

        public BolumViewHolder(@NonNull ItemBolumBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
