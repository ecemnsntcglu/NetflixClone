package com.ecs.netflix;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ecs.netflix.databinding.FragmentAccountBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ContentAdapter contentAdapter;
    private List<Content> likedList;

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

        // Beğenilen içerikleri yükle
        loadLikedContent();

        // Çıkış yap butonu
        binding.btnCikis.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(requireContext(), "Çıkış yapıldı!", Toast.LENGTH_SHORT).show();

            // Kullanıcıyı giriş ekranına yönlendir
            NavDirections action = AccountFragmentDirections.accountToKullanici();
            NavHostFragment.findNavController(AccountFragment.this).navigate(action);
        });

        // 🌙 Tema değiştirme butonu
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

    private void loadLikedContent() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Kullanıcı oturumu açık değil!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        likedList = new ArrayList<>();
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        contentAdapter = new ContentAdapter(requireContext(), likedList, (contentId, type) -> {
            // 🔥 Seçilen içeriğe göre `SharedPreferences` güncelle
            sharedPreferences.edit().putString("contentType", type).apply();

            // 🔥 Detay sayfasına yönlendir
            NavDirections action = AccountFragmentDirections.accountToDetay(contentId);
            NavHostFragment.findNavController(AccountFragment.this).navigate(action);
        });

        binding.recyclerViewLiked.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewLiked.setAdapter(contentAdapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 🔥 Kullanıcının `likedlist` alanını çek
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> likedItems = (List<Map<String, Object>>) documentSnapshot.get("likedlist");
                        if (likedItems != null) {
                            for (Map<String, Object> item : likedItems) {
                                String contentId = (String) item.get("ID");
                                String type = (String) item.get("type");

                                if (contentId != null && type != null) {
                                    fetchContentDetails(contentId, type);
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Beğenilen içerikler yüklenemedi!", Toast.LENGTH_SHORT).show());
    }

    // 🔥 İçeriği `movies` veya `series` koleksiyonundan çek
    private void fetchContentDetails(String contentId, String type) {
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
                        likedList.add(content);
                        contentAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "İçerik bilgisi yüklenemedi!", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}