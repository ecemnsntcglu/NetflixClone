package com.ecs.netflix;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.ecs.netflix.databinding.FragmentAccountBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.bumptech.glide.Glide;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Kullanıcı bilgilerini yükle
        loadUserInfo();

        // Profil fotoğrafı güncelleme işlemi
        binding.imageViewProfile.setOnClickListener(v -> selectImageFromGallery());

        // Çıkış yap butonu
        binding.btnCikis.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(requireContext(), "Çıkış yapıldı!", Toast.LENGTH_SHORT).show();

            // Kullanıcıyı giriş ekranına yönlendir
            NavDirections action = AccountFragmentDirections.accountToKullanici();
            NavHostFragment.findNavController(AccountFragment.this).navigate(action);
        });

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

    // Kullanıcı bilgilerini Firestore'dan çekme
    private void loadUserInfo() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            binding.tvUserName.setText("Merhaba " + name.toUpperCase());
                            loadProfileImage();
                        } else {
                            Toast.makeText(requireContext(), "Kullanıcı bilgileri bulunamadı!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), "Bilgileri yükleme hatası!", Toast.LENGTH_SHORT).show());
        }
    }

    // Profil fotoğrafını Firestore'dan çekme
    private void loadProfileImage() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Kullanıcı oturumu açık değil!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String profileImageUrl = documentSnapshot.getString("profileImage");
                        if (profileImageUrl != null) {
                            Glide.with(getContext())
                                    .load(profileImageUrl)
                                    .into(binding.imageViewProfile);  // Profil resmi ImageView'a yükle
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Profil resmi yüklenemedi!", Toast.LENGTH_SHORT).show();
                });
    }

    // Resim seçme işlemi
    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*"); // Yalnızca resimleri seçtirecek
        startActivityForResult(intent, 101);
    }

    // Resmi Firebase Storage'a yükleyip Firestore'a kaydetme
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            binding.imageViewProfile.setImageURI(imageUri);  // Profil resmini güncelle
            uploadProfileImage(imageUri);  // Resmi Firebase'e yükle
        }
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
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String profileImageUrl = uri.toString();
                        // Firestore'a profil resmini kaydediyoruz
                        saveProfileImageToFirestore(profileImageUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Resim yükleme başarısız oldu", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveProfileImageToFirestore(String profileImageUrl) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Kullanıcı oturumu açık değil!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("profileImage", profileImageUrl);

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
