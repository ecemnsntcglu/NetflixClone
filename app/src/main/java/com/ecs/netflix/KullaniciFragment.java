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

import com.ecs.netflix.databinding.FragmentKullaniciBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class KullaniciFragment extends Fragment {

    private static final String TAG = "KullaniciFragment";

    private FirebaseAuth auth;
    private FragmentKullaniciBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        Log.e("Deneme", "Selam aşkım buradayım!");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentKullaniciBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@androidx.annotation.NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser guncelKullanici = auth.getCurrentUser();
        if (guncelKullanici != null) {
            NavDirections action = KullaniciFragmentDirections.kullaniciToFeed();
            NavHostFragment.findNavController(this).navigate(action);
        }

        binding.signinTxt.setOnClickListener(v -> {
            Log.d(TAG, "Sign-in butonuna tıklandı.");
            signin();
        });

        binding.loginBtn.setOnClickListener(v -> {
            Log.d(TAG, "Login butonuna tıklandı.");
            login();
        });
    }

    public void login() {
        String email = binding.emailEdt.getText().toString().trim();
        String password = binding.passwordEdt.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Email veya parola boş olamaz!", Toast.LENGTH_LONG).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (isAdded()) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            Toast.makeText(requireContext(), "Giriş başarılı!", Toast.LENGTH_SHORT).show();
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                NavDirections action = KullaniciFragmentDirections.kullaniciToFeed();
                                NavHostFragment.findNavController(this).navigate(action);
                            }, 1000);
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(requireContext(), "Giriş başarısız: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void signin() {
        Toast.makeText(requireContext(), "Yönlendiriliyor!", Toast.LENGTH_SHORT).show();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            NavDirections action = KullaniciFragmentDirections.kullaniciToSign();
            NavHostFragment.findNavController(this).navigate(action);
        }, 1000);
    }
}