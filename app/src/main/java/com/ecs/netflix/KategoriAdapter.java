package com.ecs.netflix;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class KategoriAdapter extends RecyclerView.Adapter<KategoriAdapter.KategoriViewHolder> {

    private Context context;
    private List<Kategori> kategoriListesi;
    private DiziAdapter.OnItemClickListener listener;


    public KategoriAdapter(Context context, List<Kategori> kategoriListesi, DiziAdapter.OnItemClickListener listener) {
        this.context = context;
        this.kategoriListesi = kategoriListesi;
        this.listener = listener;
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

        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String contentType = sharedPreferences.getString("contentType", "Dizi");

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (contentType.equals("Film")) {
            // 🔥 Film Listesi (DEĞİŞMEDİ)
            List<Film> filmListesi = new ArrayList<>();
            db.collection("movies").whereArrayContains("genres", kategori.getKategoriAdi()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Film film = document.toObject(Film.class);
                        filmListesi.add(film);
                    }

                    FilmAdapter filmAdapter = new FilmAdapter(context, filmListesi, selectedFilm -> {
                        Bundle bundle = new Bundle();
                        bundle.putString("title", selectedFilm.getTitle());
                        bundle.putString("poster_url", selectedFilm.getPoster_url());

                        Navigation.findNavController(holder.itemView)
                                .navigate(R.id.feedToDetay, bundle);
                    });

                    holder.diziRecyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
                    holder.diziRecyclerView.setAdapter(filmAdapter);

                }
            });
        } else {
            // 🔥 Dizi Listesi (TIKLAMA EKLENDİ)
            List<Dizi> diziListesi = new ArrayList<>();
            db.collection("series").whereArrayContains("genres", kategori.getKategoriAdi()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Dizi dizi = document.toObject(Dizi.class);
                        diziListesi.add(dizi);
                    }

                    DiziAdapter diziAdapter = new DiziAdapter(context, diziListesi, selectedDizi -> {
                        Bundle bundle = new Bundle();
                        bundle.putString("title", selectedDizi.getTitle());
                        bundle.putString("poster_url", selectedDizi.getPoster_url());  // 🔄 düzeltildi
                        bundle.putString("trailer_url", selectedDizi.getTrailer_url()); // ✅ EKLENDİ

                        Navigation.findNavController(holder.itemView)
                                .navigate(R.id.feedToDetay, bundle);
                    });


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
