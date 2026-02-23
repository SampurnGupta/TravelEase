package com.example.travelease.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelease.R;
import com.example.travelease.model.Destination;

import java.util.ArrayList;
import java.util.List;

public class DestinationAdapterold extends RecyclerView.Adapter<DestinationAdapterold.DestinationViewHolder> {

    private List<Destination> destinations = new ArrayList<>();
    private OnDestinationClickListener listener;

    public interface OnDestinationClickListener {
        void onDestinationClick(Destination destination, int position);
    }

    public void setOnDestinationClickListener(OnDestinationClickListener listener) {
        this.listener = listener;
    }

    public void setDestinations(List<Destination> destinations) {
        this.destinations = destinations;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DestinationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_destinationg, parent, false);
        return new DestinationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DestinationViewHolder holder, int position) {
        Destination destination = destinations.get(position);
        holder.bind(destination);
    }

    @Override
    public int getItemCount() {
        return destinations.size();
    }

    class DestinationViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView nameTextView;
        private TextView countryTextView;

        public DestinationViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.destination_image);
            nameTextView = itemView.findViewById(R.id.destination_name);
            countryTextView = itemView.findViewById(R.id.destination_country);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onDestinationClick(destinations.get(position), position);
                    }
                }
            });
        }

        public void bind(Destination destination) {
            nameTextView.setText(destination.getName());
            countryTextView.setText(destination.getCountry());
            
            // Load image with Glide
            Glide.with(itemView.getContext())
                    .load(destination.getImageUrl())
                    .placeholder(R.drawable.ic_colosseum)
                    .error(R.drawable.ic_colosseum)
                    .centerCrop()
                    .into(imageView);
        }
    }
}
