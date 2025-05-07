package com.ecs.netflix;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ecs.netflix.databinding.FragmentFeedBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends Fragment {

    private FragmentFeedBinding binding;
    private KategoriAdapter kategoriAdapter;
    private List<Kategori> kategoriler;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireContext().getSharedPreferences("UserPrefs", getContext().MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFeedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ThemePrefManager themePrefManager = new ThemePrefManager(requireContext());
        int toggleGroupColor = themePrefManager.isDarkMode() ? R.color.toogle_dark : R.color.toogle_light;
        int buttonColor = themePrefManager.isDarkMode() ? R.color.toogle_dark : R.color.toogle_light;

        binding.toggleGroup.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), toggleGroupColor));
        binding.btnDiziler.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), buttonColor));
        binding.btnFilmler.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), buttonColor));

        int textColorId = themePrefManager.isDarkMode() ? R.color.buton_dark : R.color.buton_light;
        int borderDrawableId = themePrefManager.isDarkMode() ? R.drawable.buton_border : R.drawable.buton_border;

        binding.btnOynat.setTextColor(ContextCompat.getColor(requireContext(), textColorId));
        binding.btnListeyeEkle.setTextColor(ContextCompat.getColor(requireContext(), textColorId));
        binding.btnOynat.setBackgroundResource(borderDrawableId);
        binding.btnListeyeEkle.setBackgroundResource(borderDrawableId);

        kategoriler = new ArrayList<>();

        kategoriAdapter = new KategoriAdapter(requireContext(), kategoriler, new DiziAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Dizi dizi) {
                NavDirections action = FeedFragmentDirections.feedToDetay(
                        dizi.getTitle(),
                        dizi.getTrailer_url(),
                        dizi.getPoster_url()

                );
                NavHostFragment.findNavController(FeedFragment.this).navigate(action);
            }
        });

        binding.parentRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.parentRecyclerView.setAdapter(kategoriAdapter);

        String selectedType = sharedPreferences.getString("contentType", "Dizi");
        kategorileriAl(selectedType);

        binding.toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                String contentType = (checkedId == R.id.btnDiziler) ? "Dizi" : "Film";
                sharedPreferences.edit().putString("contentType", contentType).apply();
                kategorileriAl(contentType);
            }
        });
    }

    private void kategorileriAl(String contentType) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String collectionName = contentType.equals("Film") ? "movies" : "series";

        db.collection(collectionName).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                kategoriler.clear();
                List<String> tumTurler = new ArrayList<>();

                for (QueryDocumentSnapshot document : task.getResult()) {
                    List<String> genres = (List<String>) document.get("genres");
                    if (genres != null) {
                        for (String genre : genres) {
                            if (!tumTurler.contains(genre)) {
                                tumTurler.add(genre);
                            }
                        }
                    }
                }

                for (String tur : tumTurler) {
                    kategoriler.add(new Kategori(tur, new ArrayList<>()));
                }

                kategoriAdapter.notifyDataSetChanged();
            } else {
                System.out.println("Firestore'dan veri çekme hatası: " + task.getException());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
