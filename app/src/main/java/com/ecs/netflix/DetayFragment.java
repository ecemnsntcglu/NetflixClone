package com.ecs.netflix;

import android.content.Context;
import android.content.Intent;
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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ecs.netflix.databinding.FragmentDetayBinding;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class DetayFragment extends Fragment {

    private FragmentDetayBinding binding;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    String status;

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

        // 1. RecyclerView + Adapter
        List<Comment> comments = new ArrayList<>();
        CommentAdapter adapter = new CommentAdapter(getContext(), comments);
        binding.recyclerViewComments.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewComments.setAdapter(adapter);
        String selectedType = sharedPreferences.getString("contentType", "Dizi");
// 2. Yorumları dinle
        String contentId = getArguments().getString("contentId");
        db.collection("comments")
                .whereEqualTo("contentId", contentId)
                .orderBy("timestamp")
                .addSnapshotListener((snap, err) -> {
                    if (err != null) return;

                    comments.clear();

                    // 🔥 Beğeni sayıları (AtomicInteger kullanarak değiştirilebilir hale getirdik)
                    AtomicInteger likeCount = new AtomicInteger(0);
                    AtomicInteger loveCount = new AtomicInteger(0);
                    AtomicInteger dislikeCount = new AtomicInteger(0);

                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Comment comment = d.toObject(Comment.class);

                        if (comment != null) {
                            // 🔥 Beğeni sayısını artır
                            if ("like".equals(comment.getStatus())) {
                                likeCount.incrementAndGet();
                            } else if ("love".equals(comment.getStatus())) {
                                loveCount.incrementAndGet();
                            } else if ("dislike".equals(comment.getStatus())) {
                                dislikeCount.incrementAndGet();
                            }

                            // 🔥 Yorum içeriği null değilse listeye ekle
                            if (comment.getCommentText() != null && !comment.getCommentText().trim().isEmpty()) {
                                comments.add(comment);
                            }

                        }
                    }

                    adapter.notifyDataSetChanged();

                    // 🔥 Beğeni sayısını güncelle (UI Thread içinde çalıştır)
                    requireActivity().runOnUiThread(() -> {
                        binding.textLikeCount.setText(String.valueOf(likeCount.get()));
                        binding.textLoveCount.setText(String.valueOf(loveCount.get()));
                        binding.textDislikeCount.setText(String.valueOf(dislikeCount.get()));
                    });
                });

// 3. Yeni yorum ekleme
        binding.buttonPostComment.setOnClickListener(v -> {
            String txt = binding.editTextComment.getText().toString().trim();
            FirebaseUser u = auth.getCurrentUser();
            if (u != null && !txt.isEmpty()) {
                long currentTime = System.currentTimeMillis();

                Comment newComment = new Comment(u.getUid(), txt, selectedType,status,currentTime);

                // 1. Firestore'a gönder
                Map<String, Object> m = new HashMap<>();
                m.put("userId", u.getUid());
                m.put("commentText", txt);
                m.put("timestamp", currentTime);
                m.put("contentId", contentId);
                m.put("type",selectedType);
                m.put("status",status);


                db.collection("comments").add(m)
                        .addOnSuccessListener(doc -> {
                            binding.editTextComment.setText("");

                            // 2. Anında RecyclerView'a ekle
                            comments.add(newComment);
                            adapter.notifyItemInserted(comments.size() - 1);
                            binding.recyclerViewComments.scrollToPosition(comments.size() - 1);
                        });
                binding.editTextComment.setVisibility(View.GONE);
                binding.buttonPostComment.setVisibility(View.GONE);
            }
        });


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
        binding.imageFav.setOnClickListener(v -> {
           addToList("favorites");
        });

        // Firestore'dan içeriği çek
        fetchContent(contentId, contentType);

        // Puan verme işlemi
        setupRatingMenu(view);
        alreadyExistsInListAsync("favorites", contentId, exists -> {
            if (exists) {
                binding.imageFav.setImageResource(R.drawable.fav_btn);
            } else {
                binding.imageFav.setImageResource(R.drawable.non_fav_btn);
            }
        });

        setupShareButton();



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
                String contentId = getArguments().getString("contentId");
                String contentType = sharedPreferences.getString("contentType", null);
                binding.editTextComment.setVisibility(View.VISIBLE);
                binding.buttonPostComment.setVisibility(View.VISIBLE);

                // 🔥 Kullanıcıyı yorum yazma alanına odakla
                binding.editTextComment.requestFocus();

                if (id == R.id.action_begenmedim) {
                    Toast.makeText(getContext(), "Beğenmedim seçildi", Toast.LENGTH_SHORT).show();
                    removeFromList("likedlist",contentId,contentType);
                    status= "dislike";
                } else if (id == R.id.action_begendim ) {
                    Toast.makeText(getContext(), "Beğendim seçildi", Toast.LENGTH_SHORT).show();
                    addToList("likedlist");
                    status= "like";
                }else if ( id == R.id.action_cok_begendim) {
                    Toast.makeText(getContext(), "Çok Beğendim seçildi", Toast.LENGTH_SHORT).show();
                    addToList("likedlist");
                    status= "love";
                }

                return false;
            });

            popup.show();
        });
    }
    private void addToList(String listType) {
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

        Map<String, Object> entry = new HashMap<>();
        entry.put("ID", contentId);
        entry.put("type", contentType);

        alreadyExistsInListAsync(listType, contentId, exists -> {
            if (exists) {
                if (listType.equals("favorites")) {
                    removeFromList(listType, contentId, contentType);
                    binding.imageFav.setImageResource(R.drawable.non_fav_btn);
                }
            } else {
                db.collection("users").document(userId)
                        .update(listType, FieldValue.arrayUnion(entry))
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Listeye eklendi ✅", Toast.LENGTH_SHORT).show();
                            if (listType.equals("favorites")) {
                                binding.imageFav.setImageResource(R.drawable.fav_btn);
                            }
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Listeye ekleme başarısız oldu", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void alreadyExistsInListAsync(String listType, String contentId, Consumer<Boolean> callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.accept(false);
            return;
        }

        String userId = user.getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    boolean exists = false;
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> itemList = (List<Map<String, Object>>) documentSnapshot.get(listType);
                        if (itemList != null) {
                            for (Map<String, Object> item : itemList) {
                                String existingId = (String) item.get("ID");
                                if (existingId != null && existingId.equals(contentId)) {
                                    exists = true;
                                    break;
                                }
                            }
                        }
                    }
                    callback.accept(exists);
                })
                .addOnFailureListener(e -> callback.accept(false));
    }

    private void removeFromList(String listType, String contentId, String contentType) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();

        Map<String, Object> entry = new HashMap<>();
        entry.put("ID", contentId);
        entry.put("type", contentType);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId)
                .update(listType, FieldValue.arrayRemove(entry))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Listeden çıkarıldı ❌", Toast.LENGTH_SHORT).show();
                    if (listType.equals("favorites")) {
                        binding.imageFav.setImageResource(R.drawable.non_fav_btn);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Listeden çıkarma başarısız oldu", Toast.LENGTH_SHORT).show());
    }


    private void setupShareButton() {
        binding.imageShare.setOnClickListener(v -> {

                    String contentText = "İzlemek için mükemmel bir içerik! Başlık: " + binding.textViewTitle.getText().toString();
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Film/Dizi Paylaşımı");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, contentText);
                    startActivity(Intent.createChooser(shareIntent, "Paylaşmak için seçin"));


        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}