package com.ecs.netflix;

import com.ecs.netflix.Kategori;


import android.content.Context;
import android.content.SharedPreferences;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
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

        // Kullanıcının seçtiği içerik türünü SharedPreferences'tan al
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String contentType = sharedPreferences.getString("contentType", "Dizi"); // Varsayılan "Dizi"

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (contentType.equals("Film")) {
            // **Film Listesi Oluştur**
            List<Film> filmListesi = new ArrayList<>();
            db.collection("movies").whereArrayContains("genres", kategori.getKategoriAdi()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Film film = document.toObject(Film.class);
                        filmListesi.add(film);
                    }

                    FilmAdapter filmAdapter = new FilmAdapter(context, filmListesi);
                    holder.diziRecyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
                    holder.diziRecyclerView.setAdapter(filmAdapter);
                }
            });
        } else {
            // **Dizi Listesi Oluştur**
            List<Dizi> diziListesi = new ArrayList<>();
            db.collection("series").whereArrayContains("genres", kategori.getKategoriAdi()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Dizi dizi = document.toObject(Dizi.class);
                        diziListesi.add(dizi);
                    }

                    DiziAdapter diziAdapter = new DiziAdapter(context, diziListesi);
                    holder.diziRecyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
                    holder.diziRecyclerView.setAdapter(diziAdapter);
                }
            });
        }
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