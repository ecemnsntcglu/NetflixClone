package com.ecs.netflix;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import com.ecs.netflix.databinding.FragmentAccountBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

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
    public void onViewCreated(@androidx.annotation.NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Kullanıcı bilgilerini yükle
        loadUserInfo();

        // Çıkış yap butonu
        binding.btnCikis.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(requireContext(), "Çıkış yapıldı!", Toast.LENGTH_SHORT).show();

            // Kullanıcıyı giriş ekranına yönlendir
            NavDirections action = AccountFragmentDirections.accountToKullanici();
            NavHostFragment.findNavController(AccountFragment.this).navigate(action);
        });

        // Kullanıcı bilgilerini güncelleme butonu
        binding.btnBilgileriGuncelle.setOnClickListener(v -> showEditUserDialog());

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
