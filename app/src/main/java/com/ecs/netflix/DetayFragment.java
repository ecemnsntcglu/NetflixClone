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
import java.util.List;
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
        String contentType = sharedPreferences.getString("contentType", null); // İçeriğin türü (Film veya Dizi)

        if (contentId == null || contentType == null) {
            Toast.makeText(getContext(), "İçerik ID veya türü eksik!", Toast.LENGTH_SHORT).show();
            return;
        }
        binding.textViewCast.setOnClickListener(v -> {
            if (binding.textViewCast.getMaxLines() == 2) {
                binding.textViewCast.setMaxLines(10); // 🔥 Açıklamanın tamamını göster
            } else {
                binding.textViewCast.setMaxLines(2); // 🔥 Eski haline döndür
            }
        });
        binding.textViewDescription.setOnClickListener(v -> {
            if (binding.textViewDescription.getMaxLines() == 3) {
                binding.textViewDescription.setMaxLines(10); // 🔥 Açıklamanın tamamını göster
            } else {
                binding.textViewDescription.setMaxLines(3); // 🔥 Eski haline döndür
            }
        });

        // Firestore'dan içeriği çek
        fetchContent(contentId, contentType);

        // Puan verme işlemi
        setupRatingMenu(view);
    }

    private void fetchContent(String contentId, String contentType) {
        String collectionName = contentType.equals("Film") ? "movies" : "series";

        db.collection(collectionName).document(contentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String title = documentSnapshot.getString("title");
                        String trailerUrl = documentSnapshot.getString("trailer_url");
                        String description = documentSnapshot.getString("description");
                        String director = documentSnapshot.getString("director");
                        List<String> castList = (List<String>) documentSnapshot.get("cast");

                        binding.textViewTitle.setText(title != null ? title : "Başlık bulunamadı");
                        binding.textViewDescription.setText(description != null ? description : "Açıklama bulunamadı");

                        // 🔥 Yönetmen ve oyuncu bilgilerini ekrana yaz
                        String castText = "Yönetmen: " + (director != null ? director : "Bilinmiyor") + "\nOyuncular: ";
                        if (castList != null && !castList.isEmpty()) {
                            castText += String.join(", ", castList);
                        } else {
                            castText += "Bilinmiyor";
                        }
                        binding.textViewCast.setText(castText);

                        loadTrailer(trailerUrl);
                    } else {
                        Toast.makeText(getContext(), "İçerik bulunamadı!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Veri çekme hatası!", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadTrailer(String trailerUrl) {
        if (trailerUrl != null) {
            String videoId = extractYouTubeVideoId(trailerUrl);

            if (videoId != null) {
                YouTubePlayerView playerView = binding.youtubePlayerView;
                getLifecycle().addObserver(playerView);

                playerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                    @Override
                    public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                        youTubePlayer.loadVideo(videoId, 0);
                    }
                });
            } else {
                Log.e("DetayFragment", "Geçersiz YouTube URL: " + trailerUrl);
                Toast.makeText(getContext(), "Fragman oynatılamıyor, geçersiz bağlantı!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("DetayFragment", "Trailer bulunamadı: " + trailerUrl);
            Toast.makeText(getContext(), "Trailer bulunamadı", Toast.LENGTH_SHORT).show();
        }
    }

    private String extractYouTubeVideoId(String url) {
        String videoId = null;

        try {
            Uri uri = Uri.parse(url);
            if (url.contains("youtube.com/watch")) {
                videoId = uri.getQueryParameter("v"); // Normal YouTube linki
            } else if (url.contains("youtu.be/")) {
                videoId = uri.getLastPathSegment(); // Kısa YouTube linki
            }
        } catch (Exception e) {
            Log.e("DetayFragment", "YouTube Video ID çıkarılamadı!", e);
        }

        return videoId;
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