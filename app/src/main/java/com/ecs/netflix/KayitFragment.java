package com.ecs.netflix;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.content.DialogInterface;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import com.ecs.netflix.databinding.FragmentKayitBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KayitFragment extends Fragment {

    private static final String TAG = "KayitFragment";
    private FragmentKayitBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentKayitBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@androidx.annotation.NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        binding.btnKayit.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String ad = binding.etAd.getText().toString().trim();
        String soyad = binding.etSoyad.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String telefon = binding.etTelefon.getText().toString().trim();
        String password = binding.etSifre.getText().toString().trim(); // Şifreyi kullanıcıdan al

        if (ad.isEmpty() || soyad.isEmpty() || email.isEmpty() || telefon.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Tüm alanları doldurun!", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();

                            Map<String, Object> userData = new HashMap<>();
                            userData.put("userID", userId);
                            userData.put("name", ad);
                            userData.put("surname", soyad);
                            userData.put("email", email);
                            userData.put("phone", telefon);
                            userData.put("preferences", new ArrayList<>()); // Başlangıçta boş

                            db.collection("users").document(userId).set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Kullanıcı Firestore'a başarıyla kaydedildi.");
                                        Toast.makeText(requireContext(), "Kayıt başarılı!", Toast.LENGTH_SHORT).show();

                                        // Tercih seçimi ekranını aç
                                        selectFavoriteGenres(userId);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w(TAG, "Kullanıcı bilgileri Firestore'a kaydedilemedi.", e);
                                        Toast.makeText(requireContext(), "Veritabanına kayıt başarısız: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Log.w(TAG, "Authentication başarısız!", task.getException());
                        Toast.makeText(requireContext(), "Kayıt başarısız: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void selectFavoriteGenres(String userId) {
        String[] diziTurleri = {"Aksiyon", "Bilim Kurgu", "Komedi", "Dram", "Korku", "Gerilim"};
        boolean[] secilenTurler = new boolean[diziTurleri.length];
        List<String> seciliTurlerListesi = new ArrayList<>();

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Favori Türlerini Seç");
        builder.setMultiChoiceItems(diziTurleri, secilenTurler, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    seciliTurlerListesi.add(diziTurleri[which]);
                } else {
                    seciliTurlerListesi.remove(diziTurleri[which]);
                }
            }
        });

        builder.setPositiveButton("Kaydet", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Firestore'a kullanıcı tercihlerini kaydetme
                Map<String, Object> preferencesUpdate = new HashMap<>();
                preferencesUpdate.put("preferences", seciliTurlerListesi);

                db.collection("users").document(userId).update(preferencesUpdate)
                        .addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), "Tercihler kaydedildi!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(requireContext(), "Hata oluştu!", Toast.LENGTH_SHORT).show());

                // Kullanıcı tercihleri kaydettikten sonra ana sayfaya yönlendirme
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    NavDirections action = KayitFragmentDirections.kayitToFeed();
                    NavHostFragment.findNavController(KayitFragment.this).navigate(action);
                }, 1000);
            }
        });

        builder.setNegativeButton("İptal", null);
        builder.create().show();
    }
}