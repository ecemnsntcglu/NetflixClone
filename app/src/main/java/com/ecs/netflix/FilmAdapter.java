package com.ecs.netflix;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ecs.netflix.databinding.ItemChildBinding;

import java.util.List;

public class FilmAdapter extends RecyclerView.Adapter<FilmAdapter.FilmViewHolder> {

    private Context context;
    private List<Film> filmListesi;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String id); // 🔥 Artık sadece ID alıyor
    }

    public FilmAdapter(Context context, List<Film> filmListesi, OnItemClickListener listener) {
        this.context = context;
        this.filmListesi = filmListesi;
        this.listener = listener;
    }

    public void setFilmListesi(List<Film> yeniFilmListesi) {
        this.filmListesi = yeniFilmListesi;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FilmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChildBinding binding = ItemChildBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new FilmViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FilmViewHolder holder, int position) {
        Film film = filmListesi.get(position);
        holder.binding.textViewDiziAdi.setText(film.getTitle());

        Glide.with(holder.itemView.getContext())
                .load(film.getPoster_url())
                .placeholder(R.drawable.placeholderpic)
                .error(R.drawable.placeholderpic)
                .into(holder.binding.imageViewDizi);

        // Tıklama işlemi (Sadece ID taşınıyor)
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(film.getId()); // 🔥 Artık hata vermeyecek
            }
        });
    }

    @Override
    public int getItemCount() {
        return (filmListesi != null) ? filmListesi.size() : 0;
    }

    public static class FilmViewHolder extends RecyclerView.ViewHolder {
        ItemChildBinding binding;

        public FilmViewHolder(@NonNull ItemChildBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}