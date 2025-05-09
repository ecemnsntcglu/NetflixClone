package com.ecs.netflix;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class KategoriAdapter extends RecyclerView.Adapter<KategoriAdapter.KategoriViewHolder> {

    private Context context;
    private List<Kategori> kategoriListesi;
    private ContentAdapter.OnItemClickListener contentListener;

    public KategoriAdapter(Context context, List<Kategori> kategoriListesi, ContentAdapter.OnItemClickListener contentListener) {
        this.context = context;
        this.kategoriListesi = kategoriListesi;
        this.contentListener = contentListener;
    }

    @NonNull
    @Override
    public KategoriViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_parent, parent, false);
        return new KategoriViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KategoriViewHolder holder, int position) {
        Kategori kategori = kategoriListesi.get(position);
        holder.kategoriText.setText(kategori.getKategoriAdi());

        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String contentType = sharedPreferences.getString("contentType", "Dizi");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<Content> contentList = new ArrayList<>();

        // 🔥 Hem `movies` hem `series` koleksiyonlarını çek
        fetchContent(db, "movies", kategori.getKategoriAdi(), "Film", contentList, holder);
        fetchContent(db, "series", kategori.getKategoriAdi(), "Dizi", contentList, holder);
    }

    private void fetchContent(FirebaseFirestore db, String collectionName, String genre, String type, List<Content> contentList, KategoriViewHolder holder) {
        db.collection(collectionName).whereArrayContains("genres", genre).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Content content = new Content(
                            document.getId(),
                            document.getString("title"),
                            document.getString("poster_url"),
                            type
                    );
                    contentList.add(content);
                }

                ContentAdapter contentAdapter = new ContentAdapter(context, contentList, (contentId, contentType) -> {
                    SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                    sharedPreferences.edit().putString("contentType", contentType).apply();

                    Bundle bundle = new Bundle();
                    bundle.putString("contentId", contentId);

                    Navigation.findNavController(holder.itemView)
                            .navigate(R.id.feedToDetay, bundle);
                });

                holder.contentRecyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
                holder.contentRecyclerView.setAdapter(contentAdapter);
                contentAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return kategoriListesi.size();
    }

    public static class KategoriViewHolder extends RecyclerView.ViewHolder {
        TextView kategoriText;
        RecyclerView contentRecyclerView;

        public KategoriViewHolder(@NonNull View itemView) {
            super(itemView);
            kategoriText = itemView.findViewById(R.id.txtKategori);
            contentRecyclerView = itemView.findViewById(R.id.recyclerChild);
        }
    }
}