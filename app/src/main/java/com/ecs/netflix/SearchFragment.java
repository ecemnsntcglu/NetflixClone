package com.ecs.netflix;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private SearchView searchView;
    private RecyclerView recyclerViewMovies, recyclerViewSeries;
    private FirebaseFirestore db;
    private TextView textViewNoSeriesFound,textViewNoMoviesFound;

    private FilmAdapter filmAdapter;
    private DiziAdapter diziAdapter;
    private List<Film> tumFilmler = new ArrayList<>();
    private List<Dizi> tumDiziler = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        db = FirebaseFirestore.getInstance();
        textViewNoSeriesFound = view.findViewById(R.id.textViewNoSeriesFound);
        textViewNoMoviesFound = view.findViewById(R.id.textViewNoMoviesFound);
        searchView = view.findViewById(R.id.searchView);
        recyclerViewMovies = view.findViewById(R.id.recyclerViewMovies);
        recyclerViewSeries = view.findViewById(R.id.recyclerViewSeries);

        filmAdapter = new FilmAdapter(getContext(), new ArrayList<>(), film ->
                Toast.makeText(getContext(), "Film seçildi: " + film.getTitle(), Toast.LENGTH_SHORT).show());

        diziAdapter = new DiziAdapter(getContext(), new ArrayList<>(), dizi ->
                Toast.makeText(getContext(), "Dizi seçildi: " + dizi.getTitle(), Toast.LENGTH_SHORT).show());

        recyclerViewMovies.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewMovies.setAdapter(filmAdapter);

        recyclerViewSeries.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewSeries.setAdapter(diziAdapter);

        // Verileri yükle
        tumFilmleriGetir();
        tumDizileriGetir();

        // Arama
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
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tumFilmler.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Film film = doc.toObject(Film.class);
                        tumFilmler.add(film);
                    }
                    filmAdapter.setFilmListesi(tumFilmler); // Tümü yüklensin
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Film verileri alınamadı!", e));
    }

    private void tumDizileriGetir() {
        db.collection("series")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tumDiziler.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Dizi dizi = doc.toObject(Dizi.class);
                        tumDiziler.add(dizi);
                    }
                    diziAdapter.setDiziListesi(tumDiziler); // Tümü yüklensin
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Dizi verileri alınamadı!", e));
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
