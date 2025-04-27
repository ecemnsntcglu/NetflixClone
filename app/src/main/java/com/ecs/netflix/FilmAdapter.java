package com.ecs.netflix;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ecs.netflix.databinding.ItemChildBinding;

import java.util.List;
public class FilmAdapter extends RecyclerView.Adapter<com.ecs.netflix.DiziAdapter.DiziViewHolder>  {

     private Context context;
        private List<Film> filmListesi;

        public FilmAdapter(Context context, List<Film> filmListesi) {
            this.context = context;
            this.filmListesi = filmListesi;
        }

        @NonNull
        @Override
        public com.ecs.netflix.DiziAdapter.DiziViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemChildBinding binding = ItemChildBinding.inflate(LayoutInflater.from(context), parent, false);
            return new com.ecs.netflix.DiziAdapter.DiziViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull com.ecs.netflix.DiziAdapter.DiziViewHolder holder, int position) {
            Film film = filmListesi.get(position);
            holder.binding.textViewDiziAdi.setText(film.getTitle());

            // Glide ile görsel yükleme
            Glide.with(context)
                    .load(film.getPoster_url()) // Firestore'dan gelen URL
                    .placeholder(R.drawable.placeholderpic) // Yüklenirken gösterilecek görsel
                    .error(R.drawable.placeholderpic) // Hata durumunda gösterilecek görsel
                    .into(holder.binding.imageViewDizi);
            Log.d("Firestore", "Poster URL: " + film.getPoster_url());
        }

        @Override
        public int getItemCount() {
            return filmListesi.size();
        }

        public static class DiziViewHolder extends RecyclerView.ViewHolder {
            ItemChildBinding binding;

            public DiziViewHolder(@NonNull ItemChildBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }

