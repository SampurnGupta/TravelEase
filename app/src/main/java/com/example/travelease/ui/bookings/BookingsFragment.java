package com.example.travelease.ui.bookings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.travelease.databinding.FragmentBookingsBinding;

public class BookingsFragment extends Fragment {

    private FragmentBookingsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBookingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textBookings;
        textView.setText("Your bookings will appear here");
        
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
