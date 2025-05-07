package com.ecs.netflix;

import android.net.Uri;
import android.os.Bundle;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.HashMap;
import java.util.Map;

public class DetayFragment extends Fragment {

    private FragmentDetayBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDetayBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView imageRate = view.findViewById(R.id.imageRate);

        imageRate.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), imageRate);
            popup.getMenuInflater().inflate(R.menu.menu_puan_ver, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                // Firebase'e ekleme işlemi
                String title = getArguments().getString("title");
                String trailerUrl = getArguments().getString("trailer_url");
                String posterUrl = getArguments().getString("poster_url");

                // Firebase'ye kaydetme işlemi
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                if (id == R.id.action_begenmedim) {
                    Toast.makeText(getContext(), "Beğenmedim seçildi", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.action_begendim || id == R.id.action_cok_begendim) {
                    // Beğenilen içerik
                    Map<String, Object> likedData = new HashMap<>();
                    likedData.put("title", title);
                    likedData.put("poster_url", posterUrl);
                    likedData.put("trailer_url", trailerUrl);
                    likedData.put("timestamp", FieldValue.serverTimestamp()); // zaman sıralaması için

                    db.collection("likedContent")
                            .add(likedData)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(getContext(), "Beğenildi ve kaydedildi 💖", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Ekleme başarısız oldu 😢", Toast.LENGTH_SHORT).show();
                            });
                }

                return false;
            });

            popup.show();
        });

        Bundle args = getArguments();

        String title = args.getString("title");
        String trailerUrl = args.getString("trailer_url");

        binding.textViewTitle.setText(title);

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
            Toast.makeText(getContext(), "Trailer bulunamadı", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
