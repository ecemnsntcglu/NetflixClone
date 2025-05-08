package com.ecs.netflix;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ecs.netflix.databinding.FragmentDetayBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.HashMap;
import java.util.Map;

public class DetayFragment extends Fragment {

    private FragmentDetayBinding binding;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDetayBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // SharedPreferences başlat
        sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        // Firebase başlat
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // İçerik ID ve türünü al
        Bundle args = getArguments();
        if (args == null) {
            Toast.makeText(getContext(), "İçerik bilgisi bulunamadı!", Toast.LENGTH_SHORT).show();
            return;
        }

        String contentId = args.getString("contentId"); // İçeriğin ID'si
        String contentType = sharedPreferences.getString("contentType", null); // İçeriğin türü (movie veya series)

        if (contentId == null || contentType == null) {
            Toast.makeText(getContext(), "İçerik ID veya türü eksik!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firestore'dan içeriği çek
        db.collection(contentType.equals("Film") ? "movies" : "series")
                .document(contentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // İçeriği nesneye dönüştür
                        if (contentType.equals("Film")) {
                            Film film = documentSnapshot.toObject(Film.class);
                            if (film != null) {
                                film.setId(documentSnapshot.getId());
                                updateUIWithFilm(film);
                            }
                        } else {
                            Dizi dizi = documentSnapshot.toObject(Dizi.class);
                            if (dizi != null) {
                                dizi.setId(documentSnapshot.getId());
                                updateUIWithDizi(dizi);
                            }
                        }
                    } else {
                        Toast.makeText(getContext(), "İçerik bulunamadı!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Veri çekme hatası!", Toast.LENGTH_SHORT).show();
                });

        // Puan verme işlemi
        setupRatingMenu(view);
    }

    private void updateUIWithFilm(Film film) {
        binding.textViewTitle.setText(film.getTitle());
        loadTrailer(film.getTrailer_url());
    }

    private void updateUIWithDizi(Dizi dizi) {
        binding.textViewTitle.setText(dizi.getTitle());
        loadTrailer(dizi.getTrailer_url());
    }

    private void loadTrailer(String trailerUrl) {
        if (trailerUrl != null && trailerUrl.contains("v=")) {
            Uri uri = Uri.parse(trailerUrl);
            String videoId = uri.getQueryParameter("v");

            YouTubePlayerView playerView = binding.youtubePlayerView;
            getLifecycle().addObserver(playerView);

            playerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                @Override
                public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                    youTubePlayer.loadVideo(videoId, 0);
                }
            });
        } else {
            Log.e("DetayFragment", "Trailer bulunamadı: " + trailerUrl);
            Toast.makeText(getContext(), "Trailer bulunamadı", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRatingMenu(View view) {
        ImageView imageRate = view.findViewById(R.id.imageRate);

        imageRate.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), imageRate);
            popup.getMenuInflater().inflate(R.menu.menu_puan_ver, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if (id == R.id.action_begenmedim) {
                    Toast.makeText(getContext(), "Beğenmedim seçildi", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.action_begendim || id == R.id.action_cok_begendim) {
                    addToLikedList();
                }

                return false;
            });

            popup.show();
        });
    }

    private void addToLikedList() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Kullanıcı oturumu açık değil!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        Bundle args = getArguments();
        if (args == null) {
            Toast.makeText(getContext(), "İçerik bilgisi bulunamadı!", Toast.LENGTH_SHORT).show();
            return;
        }

        String contentId = args.getString("contentId");
        String contentType = sharedPreferences.getString("contentType", null);

        if (contentId == null || contentType == null) {
            Toast.makeText(getContext(), "İçerik ID veya türü eksik!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> likedListEntry = new HashMap<>();
        likedListEntry.put("ID", contentId);
        likedListEntry.put("type", contentType);

        db.collection("users").document(userId)
                .update("likedlist", FieldValue.arrayUnion(likedListEntry))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Beğenildi ve listeye eklendi 💖", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Listeye ekleme başarısız oldu 😢", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}