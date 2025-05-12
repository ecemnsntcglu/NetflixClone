package com.ecs.netflix;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.ecs.netflix.databinding.FragmentFeedBinding;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedFragment extends Fragment {

    private FragmentFeedBinding binding;
    private KategoriAdapter kategoriAdapter;
    private List<Kategori> kategoriler;
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;
    String suggestedCont;


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
        sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        kategoriAdapter = new KategoriAdapter(requireContext(), kategoriler, new ContentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String contentId, String type) {

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
        setPopularCnt(selectedType);
        // Toggle butonları dinleyici ekle
        binding.toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                String contentType = (checkedId == R.id.btnDiziler) ? "Dizi" : "Film";
                sharedPreferences.edit().putString("contentType", contentType).apply();
                kategorileriAl(contentType);
                setPopularCnt(contentType);
            }
        });
        binding.btnOynat.setOnClickListener(v -> {
            if (suggestedCont != null) {
                NavDirections action = FeedFragmentDirections.feedToDetay(suggestedCont);
                NavHostFragment.findNavController(requireParentFragment()).navigate(action);
            } else {
                Log.e("NavigationError", "suggestedCont değeri null, geçiş yapılamıyor.");
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

        // Önce CACHE sonra SERVER
        db.collection(collectionName)
                .get(com.google.firebase.firestore.Source.CACHE)
                .addOnSuccessListener(cachedResult -> {
                    if (!cachedResult.isEmpty()) {
                        kullanicininTercihlerineGoreKategorileriHazirla(cachedResult, userId);
                    } else {
                        // Cache boşsa sunucudan al
                        db.collection(collectionName)
                                .get(com.google.firebase.firestore.Source.SERVER)
                                .addOnSuccessListener(serverResult -> {
                                    kullanicininTercihlerineGoreKategorileriHazirla(serverResult, userId);
                                })
                                .addOnFailureListener(e -> {
                                    System.out.println("Sunucudan veri alınamadı: " + e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    System.out.println("CACHE erişimi başarısız: " + e.getMessage());
                });
    }
    private void setPopularCnt(String selectedType) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) return; // Kullanıcı oturum açmamışsa çık

        String currentUserId = currentUser.getUid();

        // 🔥 En çok beğeni alan içerikleri saklamak için HashMap
        Map<String, Integer> contentLikeCount = new HashMap<>();

        // 🔥 Seçilen türe göre en çok beğeni alan içerikleri çek
        db.collection("comments")
                .whereEqualTo("type", selectedType) // 🔥 Seçilen türe göre filtreleme
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String contentId = doc.getString("contentId");
                        String status = doc.getString("status");

                        if (contentId != null && ("love".equals(status) || "like".equals(status))) {
                            contentLikeCount.put(contentId, contentLikeCount.getOrDefault(contentId, 0) + 1);
                        }
                    }

                    // 🔥 Beğeni sayısına göre sıralama
                    List<Map.Entry<String, Integer>> sortedContent = new ArrayList<>(contentLikeCount.entrySet());
                    sortedContent.sort((a, b) -> Integer.compare(b.getValue(), a.getValue())); // Çok beğenilenler önce

                    // 🔥 Kullanıcının tercihlerine uygun ilk içeriği bul
                    db.collection("users").document(currentUserId)
                            .get()
                            .addOnSuccessListener(userDoc -> {
                                if (userDoc.exists()) {
                                    List<String> preferences = (List<String>) userDoc.get("preferences");

                                    if (preferences != null) {
                                        for (Map.Entry<String, Integer> entry : sortedContent) { // 🔥 Döngü içinde `break;` kullanılabilir
                                            String contentId = entry.getKey();
                                            String collectionName = "Dizi".equals(selectedType) ? "series" : "movies";

                                            db.collection(collectionName).document(contentId)
                                                    .get()
                                                    .addOnSuccessListener(documentSnapshot -> {
                                                        if (documentSnapshot.exists()) {
                                                            List<String> contentGenres = (List<String>) documentSnapshot.get("genres"); // 🔥 İçeriğin türleri

                                                            if (contentGenres != null && preferences != null) {
                                                                for (String genre : contentGenres) {
                                                                    if (preferences.contains(genre)) {
                                                                        Log.d("FirebaseDebug", "İçerik uygun bulundu: " + contentId);

                                                                        // 🔥 Kullanıcı bu içeriğe yorum yapmış mı?
                                                                        db.collection("comments")
                                                                                .whereEqualTo("contentId", contentId)
                                                                                .whereEqualTo("userId", currentUserId)
                                                                                .get()
                                                                                .addOnSuccessListener(commentSnapshot -> {
                                                                                    if (commentSnapshot.isEmpty()) {
                                                                                        // 🔥 Kullanıcı bu içeriğe yorum yapmamış, içeriği öner
                                                                                        suggestContent(contentId);
                                                                                    }
                                                                                });

                                                                        return; // 🔥 İlk eşleşen içeriği bulunca fonksiyondan çık
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                }
                            });
                });
    }

    private void suggestContent(String contentId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
suggestedCont=contentId;
        // 🔥 Fragment bağlanmış mı kontrol et
        if (!isAdded()) {
            Log.e("FirebaseDebug", "Fragment henüz eklenmemiş, içerik önerilemiyor.");
            return;
        }

        View rootView = getView();
        if (rootView == null) {
            Log.e("FirebaseDebug", "Fragment'in View'i henüz oluşturulmadı.");
            return;
        }

        ShapeableImageView imageViewPoster = rootView.findViewById(R.id.imageView); // 🔥 ImageView bağlandı

        // 🔥 SharedPreferences'den `contentType` değerini al
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String selectedType = sharedPreferences.getString("contentType", "Dizi"); // Varsayılan "Dizi"

        // 🔥 Seçilen türe göre koleksiyon belirle
        String collectionName = "Dizi".equals(selectedType) ? "series" : "movies";
        Log.d("FirebaseDebug", "İçerik uygun bulundu: " + contentId + " - Koleksiyon: " + collectionName);

        // 🔥 Firestore'da ilgili koleksiyonda içeriği ara
        db.collection(collectionName).document(contentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String posterUrl = documentSnapshot.getString("poster_url");
                        if (posterUrl != null && !posterUrl.isEmpty()) {
                            loadPosterIntoImageView(posterUrl, imageViewPoster);

                        } else {
                            Log.e("FirebaseDebug", "Poster URL boş veya null!");
                        }
                    } else {
                        Log.e("FirebaseDebug", "Belirtilen içerik Firestore'da bulunamadı!");
                    }
                })
                .addOnFailureListener(e -> Log.e("FirebaseDebug", "Firestore hatası: " + e.getMessage()));
    }

    private void loadPosterIntoImageView(String posterUrl, ShapeableImageView imageView) {
        if (!isAdded()) {
            Log.e("FirebaseDebug", "Fragment henüz eklenmemiş, görsel yüklenemiyor.");
            return;
        }

        Context context = getContext();
        if (context != null) {
            Glide.with(context)
                    .load(posterUrl)
                    .placeholder(R.drawable.placeholderpic) // 🔥 Yükleme sırasında geçici görsel
                    .error(R.drawable.placeholderpic) // 🔥 Hata olursa gösterilecek görsel
                    .into(imageView);
        } else {
            Log.e("FirebaseDebug", "Context null, Glide yüklenemiyor.");
        }
    }

    private void kullanicininTercihlerineGoreKategorileriHazirla(Iterable<QueryDocumentSnapshot> documents, String userId) {
        kategoriler.clear();
        List<String> tumTurler = new ArrayList<>();

        for (QueryDocumentSnapshot document : documents) {
            List<String> genres = (List<String>) document.get("genres");
            if (genres != null) {
                for (String genre : genres) {
                    if (!tumTurler.contains(genre)) {
                        tumTurler.add(genre);
                    }
                }
            }
        }

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
                    oncelikliKategoriler.add(kategori);
                } else {
                    digerKategoriler.add(kategori);
                }
            }

            kategoriler.addAll(oncelikliKategoriler);
            kategoriler.addAll(digerKategoriler);
            kategoriAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            System.out.println("Kullanıcı tercihleri alınamadı: " + e.getMessage());
        });
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}