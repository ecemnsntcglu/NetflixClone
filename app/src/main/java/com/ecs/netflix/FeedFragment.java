package com.ecs.netflix;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.ecs.netflix.Kategori;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ecs.netflix.databinding.FragmentFeedBinding;
import com.ecs.netflix.KategoriAdapter; // <-- BUNU EKLE!
import com.ecs.netflix.Kategori; // <-- BUNU DA EKLE!
import com.ecs.netflix.VeritabaniYardimcisi; // <-- BUNU DA EKLE!

import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends Fragment {

    private FragmentFeedBinding binding;
    private List<Kategori> kategoriler;

    private KategoriAdapter kategoriAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Şu anda burada işimiz yok, boş bırakıyoruz
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
        new VeritabaniYardimcisi(requireContext()).ornekVerileriEkle();


        // 1. Veritabanından kategorileri çekiyoruz
        kategoriler = VeritabaniYardimcisi.getKategoriler(requireContext());

        // 2. Adapter oluşturuyoruz
        kategoriAdapter = new KategoriAdapter(requireContext(), kategoriler);


        // 3. RecyclerView'a adapterı ve LayoutManager'ı bağlıyoruz
        binding.parentRecyclerView.setAdapter(kategoriAdapter);
        binding.parentRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
