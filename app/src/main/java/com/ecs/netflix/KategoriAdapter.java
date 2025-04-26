package com.ecs.netflix;

import com.ecs.netflix.Kategori;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecs.netflix.R;
import com.ecs.netflix.Dizi;
import com.ecs.netflix.Kategori;

import java.util.List;

public class KategoriAdapter extends RecyclerView.Adapter<KategoriAdapter.KategoriViewHolder> {

    private Context context;
    private List<Kategori> kategoriListesi;

    public KategoriAdapter(Context context, List<Kategori> kategoriListesi) {
        this.context = context;
        this.kategoriListesi = kategoriListesi;
    }

    @NonNull
    @Override
    public KategoriViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_parent, parent, false);
        return new KategoriViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KategoriViewHolder holder, int position) {
        Kategori kategori = kategoriListesi.get(position);
        holder.kategoriText.setText(kategori.getKategoriAdi());

        DiziAdapter diziAdapter = new DiziAdapter(context, kategori.getDiziListesi());
        holder.diziRecyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        holder.diziRecyclerView.setAdapter(diziAdapter);
    }

    @Override
    public int getItemCount() {
        return kategoriListesi.size();
    }

    public static class KategoriViewHolder extends RecyclerView.ViewHolder {
        TextView kategoriText;
        RecyclerView diziRecyclerView;

        public KategoriViewHolder(@NonNull View itemView) {
            super(itemView);
            kategoriText = itemView.findViewById(R.id.txtKategori);
            diziRecyclerView = itemView.findViewById(R.id.recyclerChild);
        }
    }
}


