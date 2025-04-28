package com.ecs.netflix;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ecs.netflix.databinding.FragmentDetayBinding;

public class DetayFragment extends Fragment {

    private FragmentDetayBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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

                if (id == R.id.action_begenmedim) {
                    Toast.makeText(getContext(), "Beğenmedim seçildi", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.action_begendim) {
                    Toast.makeText(getContext(), "Beğendim seçildi", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.action_cok_begendim) {
                    Toast.makeText(getContext(), "Çok Beğendim seçildi", Toast.LENGTH_SHORT).show();
                    return true;
                }

                return false;
            });
            popup.show();
        });

        ImageView imageShare = view.findViewById(R.id.imageShare);

        imageShare.setOnClickListener(v -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Şu diziyi izlemelisin! 😍");
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
        });

        Bundle bundle = getArguments();
        if (bundle != null) {
            String title = bundle.getString("title");
            String posterUrl = bundle.getString("poster_url");

            // Başlığı güncelle
            binding.textViewTitle.setText(title);

            // Poster görselini yükle
            Glide.with(requireContext())
                    .load(posterUrl)
                    .placeholder(R.drawable.placeholderpic) // Yüklenirken gösterilecek görsel
                    .error(R.drawable.placeholderpic)       // Hata olursa gösterilecek
                    .into(binding.imageViewTrailer);
        }

    }
}
