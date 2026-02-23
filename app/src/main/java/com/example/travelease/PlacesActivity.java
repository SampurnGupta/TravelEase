package com.example.travelease;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class PlacesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<Destination> destinations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);

        recyclerView = findViewById(R.id.placesRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        destinations = loadDestinationsFromAssets();

        PlaceAdapter adapter = new PlaceAdapter(destinations, this::showPlaceDialog);
        recyclerView.setAdapter(adapter);
    }

    private List<Destination> loadDestinationsFromAssets() {
        try {
            InputStream is = getAssets().open("destination.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            Type listType = new TypeToken<List<Destination>>(){}.getType();
            return new Gson().fromJson(json, listType);
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private void showPlaceDialog(Destination destination) {
        final Dialog dialog = new Dialog(this, android.R.style.Theme_Material_Light_Dialog_NoActionBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_place_details);

        // Set dialog size to 60% of screen
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout((int)(metrics.widthPixels * 0.9), (int)(metrics.heightPixels * 0.6));
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.CENTER);
        }

        ImageButton closeBtn = dialog.findViewById(R.id.closeButton);
        closeBtn.setOnClickListener(v -> dialog.dismiss());

        ImageView coverPhoto = dialog.findViewById(R.id.detailCoverPhoto);
        Glide.with(this).load(destination.coverPhotoUrl).into(coverPhoto);

        TextView name = dialog.findViewById(R.id.detailName);
        name.setText(destination.name);

        TextView country = dialog.findViewById(R.id.detailCountry);
        country.setText(destination.country);

        ImageView image = dialog.findViewById(R.id.detailImage);
        Glide.with(this).load(destination.imageUrl).into(image);

        TextView desc = dialog.findViewById(R.id.detailDescription);
        desc.setText(destination.description);

        LinearLayout factsContainer = dialog.findViewById(R.id.factsContainer);
        factsContainer.removeAllViews();
        for (String fact : destination.facts) {
            TextView tv = new TextView(this);
            tv.setText("• " + fact);
            tv.setTextSize(15f);
            tv.setPadding(0, 0, 0, 6);
            factsContainer.addView(tv);
        }

        // Gallery slider (each image loaded twice)
        List<String> gallery = new ArrayList<>();
        for (String url : destination.photoGalleryUrls) {
            gallery.add(url);
            gallery.add(url);
        }
        ViewPager2 gallerySlider = dialog.findViewById(R.id.gallerySlider);
        gallerySlider.setAdapter(new GalleryAdapter(gallery));

        LinearLayout reviewsContainer = dialog.findViewById(R.id.reviewsContainer);
        reviewsContainer.removeAllViews();
        for (String review : destination.reviews) {
            TextView tv = new TextView(this);
            tv.setText("“" + review + "”");
            tv.setTextSize(15f);
            tv.setPadding(0, 0, 0, 12);
            reviewsContainer.addView(tv);
        }

        dialog.show();
    }

    // --- Data Model ---
    static class Destination {
        String name;
        String country;
        String imageUrl;
        String description;
        String coverPhotoUrl;
        List<String> facts;
        List<String> reviews;
        List<String> photoGalleryUrls;
    }

    // --- RecyclerView Adapter for Grid ---
    static class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {
        private final List<Destination> items;
        private final OnPlaceClickListener listener;

        interface OnPlaceClickListener {
            void onPlaceClick(Destination destination);
        }

        PlaceAdapter(List<Destination> items, OnPlaceClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place_card, parent, false);
            return new PlaceViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
            Destination d = items.get(position);
            holder.name.setText(d.name);
            holder.country.setText(d.country);
            Glide.with(holder.itemView.getContext()).load(d.coverPhotoUrl).into(holder.coverPhoto);
            holder.card.setOnClickListener(v -> listener.onPlaceClick(d));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class PlaceViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView card;
            ImageView coverPhoto;
            TextView name, country;
            PlaceViewHolder(@NonNull View itemView) {
                super(itemView);
                card = (MaterialCardView) itemView;
                coverPhoto = itemView.findViewById(R.id.coverPhoto);
                name = itemView.findViewById(R.id.nameText);
                country = itemView.findViewById(R.id.countryText);
            }
        }
    }

    // --- Gallery Slider Adapter ---
    static class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {
        private final List<String> imageUrls;

        GalleryAdapter(List<String> imageUrls) {
            this.imageUrls = imageUrls;
        }

        @NonNull
        @Override
        public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_image, parent, false);
            return new GalleryViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
            Glide.with(holder.image.getContext()).load(imageUrls.get(position)).into(holder.image);
        }

        @Override
        public int getItemCount() {
            return imageUrls.size();
        }

        static class GalleryViewHolder extends RecyclerView.ViewHolder {
            ImageView image;
            GalleryViewHolder(@NonNull View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.galleryImage);
            }
        }
    }
}