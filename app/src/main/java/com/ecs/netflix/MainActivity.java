package com.ecs.netflix;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.ecs.netflix.databinding.ActivityMainBinding;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        Log.d("FIREBASE", "Before init");
        FirebaseApp.initializeApp(this);
        Log.d("FIREBASE", "After init");

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.navHostFragment);
        if (navHostFragment != null) {
            NavigationUI.setupWithNavController(binding.bottomNav,
                    navHostFragment.getNavController());

            navHostFragment.getNavController().addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (destination.getId() == R.id.kullaniciFragment) {
                    binding.bottomNav.setVisibility(View.GONE);
                } else {
                    binding.bottomNav.setVisibility(View.VISIBLE);
                }
            });

        }
        }
}
