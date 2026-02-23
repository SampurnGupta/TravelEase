package com.example.travelease.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.travelease.BudgetActivity;
import com.example.travelease.CurrencyConverterActivity;
import com.example.travelease.OffersActivity;
import com.example.travelease.PackingChecklistActivity;
import com.example.travelease.PlacesActivity;
import com.example.travelease.ProfileActivity;
import com.example.travelease.SOSActivity;
import com.example.travelease.WeatherActivity;
import com.example.travelease.databinding.FragmentHomeBinding;
import com.example.travelease.model.Destination;
import com.example.travelease.ui.adapters.DestinationAdapterold;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ramotion.circlemenu.CircleMenuView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private DestinationAdapterold destinationAdapterold;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupRecyclerViews();
        loadDataFromAssets();
        setupClickListeners();
        setupCircleMenu();

        return root;
    }

    private void setupRecyclerViews() {
        binding.popularDestinationsRecycler.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        destinationAdapterold = new DestinationAdapterold();
        binding.popularDestinationsRecycler.setAdapter(destinationAdapterold);
    }

    private void loadDataFromAssets() {
        Context context = getContext();
        if (context == null) return;

        List<Destination> allDestinations = loadDestinationsFromJson(context);
        List<Destination> randomDestinations = pickRandomItems(allDestinations, 5);
        destinationAdapterold.setDestinations(randomDestinations);
    }

    private List<Destination> loadDestinationsFromJson(Context context) {
        String json = readAssetFile(context, "destination.json");
        if (json == null) return new ArrayList<>();
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Destination>>(){}.getType();
        try {
            return gson.fromJson(json, listType);
        } catch (Exception e) {
            Log.e("HomeFragment", "Failed to parse destination.json", e);
            return new ArrayList<>();
        }
    }

    private String readAssetFile(Context context, String filename) {
        try {
            InputStream is = context.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private <T> List<T> pickRandomItems(List<T> list, int n) {
        if (list == null) return new ArrayList<>();
        List<T> copy = new ArrayList<>(list);
        Collections.shuffle(copy);
        return copy.subList(0, Math.min(n, copy.size()));
    }

    private void setupClickListeners() {
        binding.seeAllDestinations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Navigate to all destinations screen
                Toast.makeText(getContext(), "Destination", Toast.LENGTH_SHORT).show();
                Intent intent9 = new Intent(getContext(), PlacesActivity.class);
                startActivity(intent9);
            }
        });
    }

    private void setupCircleMenu() {
        CircleMenuView circleMenu = binding.circleMenu;
        circleMenu.setEventListener(new CircleMenuView.EventListener() {
            @Override
            public void onButtonClickAnimationEnd(@NonNull CircleMenuView view, int index) {
                if (getContext() == null) return;
                switch (index) {
                    case 0:
                        // Show toast
                        Toast.makeText(getContext(), "Weather", Toast.LENGTH_SHORT).show();
                        // Redirect to WeatherActivity
                        Intent intent = new Intent(getContext(), WeatherActivity.class);
                        startActivity(intent);
                        break;
                    case 1:
                        Toast.makeText(getContext(), "Checklist", Toast.LENGTH_SHORT).show();
                        Intent intent2 = new Intent(getContext(), PackingChecklistActivity.class);
                        startActivity(intent2);
                        break;
                    case 2:
                        Toast.makeText(getContext(), "Budget", Toast.LENGTH_SHORT).show();
                        Intent intent3 = new Intent(getContext(), BudgetActivity.class);
                        startActivity(intent3);
                        break;
                    case 3:
                        Toast.makeText(getContext(), "Destination", Toast.LENGTH_SHORT).show();
                        Intent intent4 = new Intent(getContext(), CurrencyConverterActivity.class);
                        startActivity(intent4);
                        break;
                    case 4:
                        Toast.makeText(getContext(), "SOS", Toast.LENGTH_SHORT).show();
                        Intent intent5 = new Intent(getContext(), SOSActivity.class);
                        startActivity(intent5);
                        break;
                    case 5:
                        Toast.makeText(getContext(), "Profile", Toast.LENGTH_SHORT).show();
                        Intent intent6 = new Intent(getContext(), ProfileActivity.class);
                        startActivity(intent6);
                        break;
                    case 6:
                        Toast.makeText(getContext(), "Destinations", Toast.LENGTH_SHORT).show();
                        Intent intent7 = new Intent(getContext(), PlacesActivity.class);
                        startActivity(intent7);
                        break;
                    case 7:
                        Toast.makeText(getContext(), "Offers", Toast.LENGTH_SHORT).show();
                        Intent intent8 = new Intent(getContext(), OffersActivity.class);
                        startActivity(intent8);
                        break;
                    default:
                        // Do nothing
                        break;
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
