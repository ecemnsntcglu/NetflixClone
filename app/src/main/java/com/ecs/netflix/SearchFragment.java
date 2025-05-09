package com.ecs.netflix;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
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
    private RecyclerView recyclerViewContent;
    private FirebaseFirestore db;
    private TextView textViewNoContentFound;
    private SharedPreferences sharedPreferences;
    private ContentAdapter contentAdapter;
    private List<Content> tumIcerikler = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        db = FirebaseFirestore.getInstance();
        sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        textViewNoContentFound = view.findViewById(R.id.textViewNoMoviesFound);
        searchView = view.findViewById(R.id.searchView);
        recyclerViewContent = view.findViewById(R.id.recyclerViewMovies);

        contentAdapter = new ContentAdapter(getContext(), new ArrayList<>(), (selectedContentId, type) -> {
            sharedPreferences.edit().putString("contentType", type).apply();
            NavDirections action = SearchFragmentDirections.searchToDetay(selectedContentId);
            NavHostFragment.findNavController(SearchFragment.this).navigate(action);
        });

        // 🔥 GridLayoutManager ile öğeleri yatayda sıralıyoruz (2 sütun olacak)
        recyclerViewContent.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerViewContent.setAdapter(contentAdapter);

        // Verileri yükle
        tumIcerikleriGetir();

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

    private void tumIcerikleriGetir() {
        tumIcerikler.clear();

        fetchContent("movies", "Film");
        fetchContent("series", "Dizi");
    }

    private void fetchContent(String collectionName, String type) {
        db.collection(collectionName)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("Firestore", type + " verileri alınamadı!", e);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Content content = new Content(
                                    doc.getId(),
                                    doc.getString("title"),
                                    doc.getString("poster_url"),
                                    type
                            );
                            tumIcerikler.add(content);
                        }
                        contentAdapter.setContentList(tumIcerikler);
                    }
                });
    }

    private void filtrele(String query) {
        List<Content> filtreliIcerikler = new ArrayList<>();

        for (Content content : tumIcerikler) {
            if (content.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filtreliIcerikler.add(content);
            }
        }

        contentAdapter.setContentList(filtreliIcerikler);
        textViewNoContentFound.setVisibility(filtreliIcerikler.isEmpty() ? View.VISIBLE : View.GONE);
    }
}