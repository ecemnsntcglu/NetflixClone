package com.ecs.netflix;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ecs.netflix.databinding.FragmentDetayBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetayFragment extends Fragment {

    private FragmentDetayBinding binding;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private CommentAdapter commentAdapter;

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
        if (args != null) {
            String contentId = args.getString("contentId");
            if (contentId != null) {
                fetchComments(contentId); // contentId'yi buraya geçiriyoruz
            } else {
                Toast.makeText(getContext(), "İçerik ID'si alınamadı!", Toast.LENGTH_SHORT).show();
            }
        }

        // Yorum ekleme işlemi
        binding.buttonPostComment.setOnClickListener(v -> {
            String commentText = binding.editTextComment.getText().toString();
            if (!commentText.isEmpty()) {
                String contentId = args.getString("contentId");  // contentId'yi burada alıyoruz
                addComment(contentId, commentText);
                binding.editTextComment.setText(""); // Yorum ekledikten sonra inputu temizle
            } else {
                Toast.makeText(getContext(), "Yorum boş olamaz!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchComments(String contentId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<Comment> commentList = new ArrayList<>();
        CommentAdapter commentAdapter = new CommentAdapter(getContext(), commentList);

        // RecyclerView'e bağla
        binding.recyclerViewComments.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewComments.setAdapter(commentAdapter);

        db.collection("movies").document(contentId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<Map<String, Object>> comments = (List<Map<String, Object>>) documentSnapshot.get("comments");
                if (comments != null) {
                    for (Map<String, Object> commentData : comments) {
                        String userId = (String) commentData.get("userID");
                        String commentText = (String) commentData.get("comment");
                        String status = (String) commentData.get("status");

                        fetchUserName(userId, commentText, status, commentList, commentAdapter);
                    }
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Yorumlar yüklenemedi!", Toast.LENGTH_SHORT).show();
        });
    }

    // Kullanıcının adını çekme fonksiyonu
    private void fetchUserName(String userId, String commentText, String status, List<Comment> commentList, CommentAdapter commentAdapter) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            String userName = documentSnapshot.exists() ? documentSnapshot.getString("name") : "Bilinmeyen Kullanıcı";

            commentList.add(new Comment(userName, commentText, status));
            commentAdapter.notifyDataSetChanged(); // RecyclerView'i güncelle
        });
    }

    private void addComment(String contentId, String commentText) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Kullanıcı oturumu açık değil!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        String userName = user.getDisplayName(); // Kullanıcı adı
        String status = "onaylı"; // Yorumun durumu (örneğin onaylı)

        Map<String, Object> comment = new HashMap<>();
        comment.put("userID", userId);
        comment.put("comment", commentText);
        comment.put("status", status);
        comment.put("timestamp", FieldValue.serverTimestamp());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("movies") // Eğer contentType "Film" ise "movies" koleksiyonunu kullan
                .document(contentId)
                .collection("comments") // Yorumları içeren alt koleksiyon
                .add(comment)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Yorum başarıyla eklendi!", Toast.LENGTH_SHORT).show();
                    fetchComments(contentId);  // Yorumları yeniden yükle
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Yorum ekleme başarısız!", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}