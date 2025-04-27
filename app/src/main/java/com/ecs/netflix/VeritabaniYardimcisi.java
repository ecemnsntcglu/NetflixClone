package com.ecs.netflix;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class VeritabaniYardimcisi extends SQLiteOpenHelper {

    private static final String VERITABANI_ADI = "diziDB";
    private static final int SURUM = 1;

    public VeritabaniYardimcisi(Context context) {
        super(context, VERITABANI_ADI, null, SURUM);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS kategoriler (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "kategori_adi TEXT)"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS diziler (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "dizi_adi TEXT, " +
                        "dizi_resim TEXT, " +
                        "kategori_id INTEGER, " +
                        "FOREIGN KEY(kategori_id) REFERENCES kategoriler(id))"
        );

        ornekVerileriEkle(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS diziler");
        db.execSQL("DROP TABLE IF EXISTS kategoriler");
        onCreate(db);
    }

    public static List<Kategori> getKategoriler(Context context) {
        VeritabaniYardimcisi dbHelper = new VeritabaniYardimcisi(context);
        return dbHelper.kategorileriGetir();
    }

    public List<Kategori> kategorileriGetir() {
        List<Kategori> kategoriler = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor kategoriCursor = db.rawQuery("SELECT * FROM kategoriler", null);
        while (kategoriCursor.moveToNext()) {
            int kategoriId = kategoriCursor.getInt(0);
            String kategoriAdi = kategoriCursor.getString(1);

            Cursor diziCursor = db.rawQuery(
                    "SELECT * FROM diziler WHERE kategori_id=?",
                    new String[]{String.valueOf(kategoriId)}
            );

            List<Dizi> diziListesi = new ArrayList<>();
            while (diziCursor.moveToNext()) {
                String diziAdi = diziCursor.getString(1);
                String diziResim = diziCursor.getString(2);
                diziListesi.add(new Dizi(diziAdi, diziResim));
            }
            diziCursor.close();

            kategoriler.add(new Kategori(kategoriAdi, diziListesi));
        }
        kategoriCursor.close();

        return kategoriler;
    }

    private void ornekVerileriEkle(SQLiteDatabase db) {
        // Eğer veriler zaten varsa tekrar ekleme!
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM kategoriler", null);
        cursor.moveToFirst();
        int kategoriSayisi = cursor.getInt(0);
        cursor.close();

        if (kategoriSayisi > 0) {
            return; // Zaten veri var, eklemeye gerek yok.
        }

        // --- Buradan sonrası sadece ilk kez çalışırsa yapılacak ---
        db.execSQL("INSERT INTO kategoriler (kategori_adi) VALUES ('Popüler Diziler')");
        db.execSQL("INSERT INTO kategoriler (kategori_adi) VALUES ('Komedi Dizileri')");
        db.execSQL("INSERT INTO kategoriler (kategori_adi) VALUES ('Bilim Kurgu Dizileri')");

        db.execSQL("INSERT INTO diziler (dizi_adi, dizi_resim, kategori_id) VALUES ('Breaking Bad', 'breakingbad', 1)");
        db.execSQL("INSERT INTO diziler (dizi_adi, dizi_resim, kategori_id) VALUES ('Money Heist', 'moneyheist', 1)");
        db.execSQL("INSERT INTO diziler (dizi_adi, dizi_resim, kategori_id) VALUES ('Friends', 'friends', 2)");
        db.execSQL("INSERT INTO diziler (dizi_adi, dizi_resim, kategori_id) VALUES ('Big Bang Theory', 'bigbangtheory', 2)");
        db.execSQL("INSERT INTO diziler (dizi_adi, dizi_resim, kategori_id) VALUES ('Stranger Things', 'strangerthings', 3)");
        db.execSQL("INSERT INTO diziler (dizi_adi, dizi_resim, kategori_id) VALUES ('Dark', 'dark', 3)");
    }

}
