package com.example.travelease;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class OffersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<Offer> offers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offers);

        recyclerView = findViewById(R.id.offersRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        offers = loadOffersFromAssets();
        OfferAdapter adapter = new OfferAdapter(offers, this);
        recyclerView.setAdapter(adapter);
    }

    private List<Offer> loadOffersFromAssets() {
        try {
            InputStream is = getAssets().open("offers.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            Type listType = new TypeToken<List<Offer>>(){}.getType();
            return new Gson().fromJson(json, listType);
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // --- Data Model ---
    static class Offer {
        @SerializedName("offer source")
        String offerSource;
        @SerializedName("offer heading")
        String offerHeading;
        @SerializedName("offer subheading/description")
        String offerSubheading;
        @SerializedName("cover photo for the offer link")
        String coverPhotoUrl;
        @SerializedName("offer link")
        String offerLink;
    }

    // --- RecyclerView Adapter ---
    static class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.OfferViewHolder> {
        private final List<Offer> items;
        private final Context context;

        OfferAdapter(List<Offer> items, Context context) {
            this.items = items;
            this.context = context;
        }

        @NonNull
        @Override
        public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_offer_card, parent, false);
            return new OfferViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull OfferViewHolder holder, int position) {
            Offer o = items.get(position);
            holder.offerSource.setText(o.offerSource);
            holder.offerHeading.setText(o.offerHeading);
            holder.offerSubheading.setText(o.offerSubheading);
            Glide.with(holder.coverPhoto.getContext()).load(o.coverPhotoUrl).into(holder.coverPhoto);

            holder.card.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(o.offerLink));
                context.startActivity(browserIntent);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class OfferViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView card;
            ImageView coverPhoto;
            TextView offerSource, offerHeading, offerSubheading;
            OfferViewHolder(@NonNull View itemView) {
                super(itemView);
                card = (MaterialCardView) itemView;
                coverPhoto = itemView.findViewById(R.id.offerCoverPhoto);
                offerSource = itemView.findViewById(R.id.offerSource);
                offerHeading = itemView.findViewById(R.id.offerHeading);
                offerSubheading = itemView.findViewById(R.id.offerSubheading);
            }
        }
    }
}
