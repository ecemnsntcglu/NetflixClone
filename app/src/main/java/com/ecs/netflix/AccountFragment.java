package com.ecs.netflix;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.ecs.netflix.databinding.FragmentAccountBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class AccountFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private FragmentAccountBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ContentAdapter likedAdapter;
    private ContentAdapter favoritesAdapter;
    private List<Content> likedList = new ArrayList<>();
    private List<Content> favoritesList = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@androidx.annotation.NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        // Kullanıcı bilgilerini yükle
        loadUserInfo();
        loadUserAchievements();

        // Beğenilen içerikleri yükle
        loadContent("likedlist");
        loadContent("favorites");
        loadProfileImage();

        // Profil fotoğrafını güncelleme işlemi
        binding.imageViewProfile.setOnClickListener(v -> openImageChooser());

        // Çıkış yap butonu
        binding.btnCikis.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(requireContext(), "Çıkış yapıldı!", Toast.LENGTH_SHORT).show();
            NavDirections action = AccountFragmentDirections.accountToKullanici();
            NavHostFragment.findNavController(AccountFragment.this).navigate(action);
        });

        binding.btnBilgileriGuncelle.setOnClickListener(v -> showEditUserDialog());

        // Tema değiştirme butonu
        ThemePrefManager themePrefManager = new ThemePrefManager(requireContext());
        binding.switchTema.setOnClickListener(v -> {
            if (themePrefManager.isDarkMode()) {
                themePrefManager.setDarkMode(false);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                themePrefManager.setDarkMode(true);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        });

    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*"); // Yalnızca resim dosyalarını seç
        resultLauncher.launch(intent);  // Yeni API ile çalıştır
    }

    private final ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        // Glide ile resmi yükleyelim
                        Glide.with(getContext())
                                .load(imageUri)
                                .circleCrop().into(binding.imageViewProfile);
                        // Profil resmi ImageView'a yükle
                        saveProfileImageToFirestore(imageUri.toString());  // Firestore'a URL kaydet
                    }
                }
            });






    private void saveProfileImageToFirestore(String imageUrl) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Kullanıcı oturumu açık değil!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Profil resminin URL'sini Firestore'a kaydediyoruz
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("profileImage", imageUrl);

        db.collection("users").document(userId)
                .update(profileData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Profil resmi başarıyla güncellendi!", Toast.LENGTH_SHORT).show();
                    loadProfileImage();  // Profil resmini yükle
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Profil resmi güncellenemedi!", Toast.LENGTH_SHORT).show();
                });
    }



    private void loadProfileImage() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Kullanıcı oturumu açık değil!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String profileImageUrl = documentSnapshot.getString("profileImage");
                        Log.d("FirestoreURL", "Stored Profile Image URL: " + profileImageUrl);
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(getContext())
                                    .load(profileImageUrl)
                                    .circleCrop().into(binding.imageViewProfile);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Profil resmi yüklenemedi!", Toast.LENGTH_SHORT).show());
    }
    private void uploadProfileImage(Uri imageUri) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Kullanıcı oturumu açık değil!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("profile_pictures/" + userId + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String profileImageUrl = uri.toString();
                    saveProfileImageToFirestore(profileImageUrl);  // **Gerçek Storage URL'yi kaydet**
                }))
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Resim yükleme başarısız oldu", Toast.LENGTH_SHORT).show());
    }

    // Kullanıcı bilgilerini yükleme
    private void loadUserInfo() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            binding.tvUserName.setText("Merhaba " + name.toUpperCase());
                        } else {
                            Toast.makeText(requireContext(), "Kullanıcı bilgileri bulunamadı!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), "Bilgileri yükleme hatası!", Toast.LENGTH_SHORT).show());
        }
    }

    private void loadContent(String listType) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Kullanıcı oturumu açık değil!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        // Uygun listeyi ve adapter'ı seç
        List<Content> targetList;
        ContentAdapter targetAdapter;

        if (listType.equals("favorites")) {
            favoritesList.clear();
            targetList = favoritesList;

            favoritesAdapter = new ContentAdapter(requireContext(), targetList, (contentId, type) -> {
                sharedPreferences.edit().putString("contentType", type).apply();
                NavDirections action = AccountFragmentDirections.accountToDetay(contentId);
                NavHostFragment.findNavController(AccountFragment.this).navigate(action);
            });

            binding.recyclerViewFav.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
            binding.recyclerViewFav.setAdapter(favoritesAdapter);

            targetAdapter = favoritesAdapter;
        } else {
            likedList.clear();
            targetList = likedList;

            likedAdapter = new ContentAdapter(requireContext(), targetList, (contentId, type) -> {
                sharedPreferences.edit().putString("contentType", type).apply();
                NavDirections action = AccountFragmentDirections.accountToDetay(contentId);
                NavHostFragment.findNavController(AccountFragment.this).navigate(action);
            });

            binding.recyclerViewLiked.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
            binding.recyclerViewLiked.setAdapter(likedAdapter);

            targetAdapter = likedAdapter;
        }

        // Firestore'dan kullanıcı verisini çek
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> items = (List<Map<String, Object>>) documentSnapshot.get(listType);
                        if (items != null) {
                            for (Map<String, Object> item : items) {
                                String contentId = (String) item.get("ID");
                                String type = (String) item.get("type");

                                if (contentId != null && type != null) {
                                    fetchContentDetails(contentId, type, targetList, targetAdapter);
                                } else {
                                    Toast.makeText(getContext(), "TYPE ya da ID eksik!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), listType.equals("favorites") ? "Favoriler yüklenemedi!" : "Beğenilenler yüklenemedi!", Toast.LENGTH_SHORT).show());
    }

    // 🔥 İçeriği movies veya series koleksiyonundan çek
    private void fetchContentDetails(String contentId, String type, List<Content> contentList, ContentAdapter adapter) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String collectionName = type.equals("Film") ? "movies" : "series";

        db.collection(collectionName).document(contentId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Content content = new Content(
                                doc.getId(),
                                doc.getString("title"),
                                doc.getString("poster_url"),
                                type
                        );
                        contentList.add(content);
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "İçerik bilgisi yüklenemedi!", Toast.LENGTH_SHORT).show());
    }


    private void showEditUserDialog() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        EditText etName = new EditText(getContext());
        EditText etSurname = new EditText(getContext());
        EditText etEmail = new EditText(getContext());
        EditText etPhone = new EditText(getContext());

        layout.addView(etName);
        layout.addView(etSurname);
        layout.addView(etEmail);
        layout.addView(etPhone);

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        etName.setText(documentSnapshot.getString("name"));
                        etSurname.setText(documentSnapshot.getString("surname"));
                        etEmail.setText(documentSnapshot.getString("email"));
                        etPhone.setText(documentSnapshot.getString("phone"));
                    }
                });

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Bilgileri Güncelle");
        builder.setView(layout);
        builder.setPositiveButton("Kaydet", (dialog, which) -> {
            String newName = etName.getText().toString();
            String newSurname = etSurname.getText().toString();
            String newEmail = etEmail.getText().toString();
            String newPhone = etPhone.getText().toString();

            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("name", newName);
            updatedData.put("surname", newSurname);
            updatedData.put("email", newEmail);
            updatedData.put("phone", newPhone);

            db.collection("users").document(userId)
                    .update(updatedData)
                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Bilgiler Güncellendi!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Güncelleme Başarısız!", Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("İptal", null);
        builder.show();
    }
    private void loadUserAchievements() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(document -> {
                    List<Map<String, Object>> favs = (List<Map<String, Object>>) document.get("favorites");
                    int favCount = favs != null ? favs.size() : 0;

                    db.collection("comments").whereEqualTo("userId", userId).get()
                            .addOnSuccessListener(commentSnap -> {
                                int commentCount = commentSnap.size();

                                List<Map<String, Object>> watched = (List<Map<String, Object>>) document.get("recentlyWatched");
                                int watchCount = watched != null ? watched.size() : 0;

                                showBadges(favCount, commentCount, watchCount);
                            });
                });
    }

    private void showBadges(int favCount, int commentCount, int watchCount) {
        StringBuilder badges = new StringBuilder();

        if (favCount >= 10) badges.append("🏅 Beğeni Canavarı\n");
        if (commentCount >= 1) badges.append("💬 Sosyal Kullanıcı\n");
        if (watchCount >= 1) badges.append("🎬 Dizi Uzmanı\n");

        if (badges.length() == 0) {
            badges.append("Henüz rozet kazanmadın 💔");
        }

        binding.tvBadges.setText(badges.toString());
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
