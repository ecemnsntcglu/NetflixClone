package com.ecs.netflix;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import com.ecs.netflix.databinding.FragmentKayitBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class KayitFragment extends Fragment {

    private static final String TAG = "KayitFragment";
    private FragmentKayitBinding binding;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentKayitBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@androidx.annotation.NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Kullanicilar");

        binding.btnKayit.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String ad = binding.etAd.getText().toString().trim();
        String soyad = binding.etSoyad.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String telefon = binding.etTelefon.getText().toString().trim();
        String password = "defaultPassword"; // Şifreyi nasıl alacağını kullanıcıdan seçebilirsin

        if (ad.isEmpty() || soyad.isEmpty() || email.isEmpty() || telefon.isEmpty()) {
            Toast.makeText(requireContext(), "Tüm alanları doldurun!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase Authentication ile kullanıcı oluştur
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    String userId = user.getUid();
                    Kullanici kullanici = new Kullanici(ad, soyad, email, telefon);

                    // Firebase Realtime Database'e kullanıcı kaydı
                    databaseReference.child(userId).setValue(kullanici).addOnCompleteListener(dbTask -> {
                        if (dbTask.isSuccessful()) {
                            Log.d(TAG, "Kullanıcı bilgileri başarıyla kaydedildi.");
                            Toast.makeText(requireContext(), "Kayıt başarılı!", Toast.LENGTH_SHORT).show();

                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                NavDirections action = KayitFragmentDirections.kayitToFeed();
                                NavHostFragment.findNavController(this).navigate(action);
                            }, 1000);
                        } else {
                            Log.w(TAG, "Kullanıcı bilgileri kaydedilemedi!", dbTask.getException());
                            Toast.makeText(requireContext(), "Veritabanına kayıt başarısız: " + dbTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                Log.w(TAG, "Kullanıcı oluşturulamadı!", task.getException());
                Toast.makeText(requireContext(), "Kayıt başarısız: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}