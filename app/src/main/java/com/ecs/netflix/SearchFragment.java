package com.ecs.netflix;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.SearchView;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private SearchView searchView;
    private RecyclerView recyclerViewMovies, recyclerViewSeries;
    private FirebaseFirestore db;
    private TextView textViewNoSeriesFound, textViewNoMoviesFound;
    private SharedPreferences sharedPreferences;
    private FilmAdapter filmAdapter;
    private DiziAdapter diziAdapter;
    private List<Film> tumFilmler = new ArrayList<>();
    private List<Dizi> tumDiziler = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        db = FirebaseFirestore.getInstance();
        sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        textViewNoSeriesFound = view.findViewById(R.id.textViewNoSeriesFound);
        textViewNoMoviesFound = view.findViewById(R.id.textViewNoMoviesFound);
        searchView = view.findViewById(R.id.searchView);
        recyclerViewMovies = view.findViewById(R.id.recyclerViewMovies);
        recyclerViewSeries = view.findViewById(R.id.recyclerViewSeries);

        filmAdapter = new FilmAdapter(getContext(), new ArrayList<>(), selectedFilmId -> {
            sharedPreferences.edit().putString("contentType", "Film").apply();
            NavDirections action = SearchFragmentDirections.searchToDetay(selectedFilmId);
            NavHostFragment.findNavController(SearchFragment.this).navigate(action);
        });

        diziAdapter = new DiziAdapter(getContext(), new ArrayList<>(), selectedDiziId -> {
            sharedPreferences.edit().putString("contentType", "Dizi").apply();
            NavDirections action = SearchFragmentDirections.searchToDetay(selectedDiziId);
            NavHostFragment.findNavController(SearchFragment.this).navigate(action);
        });

        // 🔥 GridLayoutManager ile öğeleri yatayda sıralıyoruz (2 sütun olacak)
        recyclerViewMovies.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerViewMovies.setAdapter(filmAdapter);

        recyclerViewSeries.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerViewSeries.setAdapter(diziAdapter);

        // Verileri yükle
        tumFilmleriGetir();
        tumDizileriGetir();

        // Arama işlemi
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filtrele(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filtrele(newText);
                return true;
            }
        });

        return view;
    }



    private void tumFilmleriGetir() {
        db.collection("movies")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("Firestore", "Film verileri alınamadı!", e);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        tumFilmler.clear();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Film film = doc.toObject(Film.class);
                            film.setId(doc.getId()); // Firestore'daki belge ID'sini ekle
                            tumFilmler.add(film);
                        }
                        filmAdapter.setFilmListesi(tumFilmler);
                    }
                });
    }

    private void tumDizileriGetir() {
        db.collection("series")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("Firestore", "Dizi verileri alınamadı!", e);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        tumDiziler.clear();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Dizi dizi = doc.toObject(Dizi.class);
                            dizi.setId(doc.getId()); // Firestore'daki belge ID'sini ekle
                            tumDiziler.add(dizi);
                        }
                        diziAdapter.setDiziListesi(tumDiziler);
                    }
                });
    }

    private void filtrele(String query) {
        List<Film> filtreliFilmler = new ArrayList<>();
        List<Dizi> filtreliDiziler = new ArrayList<>();

        for (Film film : tumFilmler) {
            if (film.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filtreliFilmler.add(film);
            }
        }

        for (Dizi dizi : tumDiziler) {
            if (dizi.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filtreliDiziler.add(dizi);
            }
        }

        filmAdapter.setFilmListesi(filtreliFilmler);
        diziAdapter.setDiziListesi(filtreliDiziler);

        textViewNoSeriesFound.setVisibility(filtreliDiziler.isEmpty() ? View.VISIBLE : View.GONE);
        textViewNoMoviesFound.setVisibility(filtreliFilmler.isEmpty() ? View.VISIBLE : View.GONE);
    }
}