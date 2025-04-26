package com.ecs.netflix;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ecs.netflix.databinding.FragmentFeedBinding;

import java.util.List;

public class FeedFragment extends Fragment {

    private FragmentFeedBinding binding;
    private List<Kategori> kategoriler;
    private KategoriAdapter kategoriAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Şu anda burada işimiz yok
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFeedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Veritabanı oluştur ve örnek verileri ekle
        VeritabaniYardimcisi dbHelper = new VeritabaniYardimcisi(requireContext());
        dbHelper.ornekVerileriEkle();  // ← Hatasız şekilde çağırdık!

        // Veritabanından kategorileri çek
        kategoriler = VeritabaniYardimcisi.getKategoriler(requireContext());

        // Adapter oluştur ve bağla
        kategoriAdapter = new KategoriAdapter(requireContext(), kategoriler);
        binding.parentRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.parentRecyclerView.setAdapter(kategoriAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
