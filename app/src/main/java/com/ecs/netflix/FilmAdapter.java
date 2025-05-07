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

    public void setFilmListesi(List<Film> filtreliFilmler) {
        this.filmListesi=filtreliFilmler;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(Film film);
    }

    public FilmAdapter(Context context, List<Film> filmListesi, OnItemClickListener listener) {
        this.context = context;
        this.filmListesi = filmListesi;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FilmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChildBinding binding = ItemChildBinding.inflate(LayoutInflater.from(context), parent, false);
        return new FilmViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FilmViewHolder holder, int position) {
        Film film = filmListesi.get(position);
        holder.binding.textViewDiziAdi.setText(film.getTitle());

        Glide.with(context)
                .load(film.getPoster_url()) // ✅ camelCase!
                .placeholder(R.drawable.placeholderpic)
                .error(R.drawable.placeholderpic)
                .into(holder.binding.imageViewDizi);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(film);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filmListesi.size();
    }

    public static class FilmViewHolder extends RecyclerView.ViewHolder {
        ItemChildBinding binding;

        public FilmViewHolder(@NonNull ItemChildBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
