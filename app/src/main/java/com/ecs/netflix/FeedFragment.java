package com.ecs.netflix;

import android.content.Context;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends Fragment {

    private FragmentFeedBinding binding;
    private KategoriAdapter kategoriAdapter;
    private List<Kategori> kategoriler;
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        // 🔥 Firestore başlat ve yeni önbellek ayarlarını uygula
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED) // 🔥 Önbellek sınırını kaldır
                .build();
        db.setFirestoreSettings(settings);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFeedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Tema yönetimi
        applyThemeSettings();

        // Kategori listesi oluştur
        kategoriler = new ArrayList<>();
        kategoriAdapter = new KategoriAdapter(requireContext(), kategoriler, new ContentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String contentId, String type) {
                SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                sharedPreferences.edit().putString("contentType", type).apply();

                NavDirections action = FeedFragmentDirections.feedToDetay(contentId);
                NavHostFragment.findNavController(FeedFragment.this).navigate(action);
            }
        });

        binding.parentRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.parentRecyclerView.setAdapter(kategoriAdapter);

        // Seçili içerik türünü al ve kategorileri yükle
        String selectedType = sharedPreferences.getString("contentType", "Dizi");
        kategorileriAl(selectedType);

        // Toggle butonları dinleyici ekle
        binding.toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                String contentType = (checkedId == R.id.btnDiziler) ? "Dizi" : "Film";
                sharedPreferences.edit().putString("contentType", contentType).apply();
                kategorileriAl(contentType);
            }
        });
    }

    private void applyThemeSettings() {
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
    }

    private void kategorileriAl(String contentType) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            System.out.println("Kullanıcı oturumu açık değil!");
            return;
        }

        String userId = user.getUid();
        String collectionName = contentType.equals("Film") ? "movies" : "series";

        db.collection(collectionName).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
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

                // 🔥 Kullanıcının tercih ettiği kategorileri al
                db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                    List<String> tercihEdilenTurler = new ArrayList<>();
                    if (documentSnapshot.exists()) {
                        tercihEdilenTurler = (List<String>) documentSnapshot.get("preferences");
                    }

                    List<Kategori> oncelikliKategoriler = new ArrayList<>();
                    List<Kategori> digerKategoriler = new ArrayList<>();

                    for (String tur : tumTurler) {
                        Kategori kategori = new Kategori(tur, new ArrayList<>());
                        if (tercihEdilenTurler != null && tercihEdilenTurler.contains(tur)) {
                            oncelikliKategoriler.add(kategori); // 🔥 Kullanıcının tercih ettiği kategorileri öne al
                        } else {
                            digerKategoriler.add(kategori);
                        }
                    }

                    // 🔥 Öncelikli kategorileri önce ekleyelim
                    kategoriler.addAll(oncelikliKategoriler);
                    kategoriler.addAll(digerKategoriler);

                    kategoriAdapter.notifyDataSetChanged();
                }).addOnFailureListener(e -> {
                    System.out.println("Firestore'dan kullanıcı tercihleri çekilemedi: " + e.getMessage());
                });

            } else {
                System.out.println("Firestore'dan veri çekme hatası: " + (task.getException() != null ? task.getException().getMessage() : "Bilinmeyen hata"));
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}