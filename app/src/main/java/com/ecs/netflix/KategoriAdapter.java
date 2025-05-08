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
    private DiziAdapter.OnItemClickListener diziListener;
    private FilmAdapter.OnItemClickListener filmListener;

    public KategoriAdapter(Context context, List<Kategori> kategoriListesi,
                           DiziAdapter.OnItemClickListener diziListener,
                           FilmAdapter.OnItemClickListener filmListener) {
        this.context = context;
        this.kategoriListesi = kategoriListesi;
        this.diziListener = diziListener;
        this.filmListener = filmListener;
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
            // 🔥 Film Listesi (ID EKLENDİ)
            List<Film> filmListesi = new ArrayList<>();
            db.collection("movies").whereArrayContains("genres", kategori.getKategoriAdi()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Film film = document.toObject(Film.class);
                        film.setId(document.getId()); // Firestore'daki belge ID'sini ekle
                        filmListesi.add(film);
                    }

                    FilmAdapter filmAdapter = new FilmAdapter(context, filmListesi, selectedFilmId -> {
                        if (filmListener != null) {
                            Bundle bundle = new Bundle();
                            bundle.putString("contentId", selectedFilmId); // 🔥 Sadece ID taşınıyor

                            Navigation.findNavController(holder.itemView)
                                    .navigate(R.id.feedToDetay, bundle);
                        }
                    });

                    holder.diziRecyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
                    holder.diziRecyclerView.setAdapter(filmAdapter);
                    filmAdapter.notifyDataSetChanged(); // 🔥 RecyclerView güncellendi
                }
            });
        } else {
            // 🔥 Dizi Listesi (ID EKLENDİ)
            List<Dizi> diziListesi = new ArrayList<>();
            db.collection("series").whereArrayContains("genres", kategori.getKategoriAdi()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Dizi dizi = document.toObject(Dizi.class);
                        dizi.setId(document.getId()); // Firestore'daki belge ID'sini ekle
                        diziListesi.add(dizi);
                    }

                    DiziAdapter diziAdapter = new DiziAdapter(context, diziListesi, selectedDiziId -> {
                        Bundle bundle = new Bundle();
                        bundle.putString("contentId", selectedDiziId); // 🔥 Sadece ID taşınıyor

                        Navigation.findNavController(holder.itemView)
                                .navigate(R.id.feedToDetay, bundle);
                    });

                    holder.diziRecyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
                    holder.diziRecyclerView.setAdapter(diziAdapter);
                    diziAdapter.notifyDataSetChanged(); // 🔥 RecyclerView güncellendi
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