package com.example.travelease;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SOSActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST = 101;
    private static final String PREFS = "sos_contacts";
    private static final String CONTACTS_KEY = "contacts";

    private ArrayList<Contact> contactList = new ArrayList<>();
    private ContactAdapter adapter;
    private FusedLocationProviderClient fusedLocationClient;

    private ActivityResultLauncher<Intent> contactPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);

        Button pickContactButton = findViewById(R.id.pickContactButton);
        RecyclerView recyclerView = findViewById(R.id.contactRecyclerView);
        Button sosButton = findViewById(R.id.sosButton);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Load contacts from SharedPreferences
        loadContacts();

        adapter = new ContactAdapter(contactList, this::removeContact);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Modern contact picker
        contactPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri contactUri = result.getData().getData();
                        String[] projection = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                ContactsContract.CommonDataKinds.Phone.NUMBER};
                        ContentResolver cr = getContentResolver();
                        Cursor cursor = cr.query(contactUri, projection, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            String name = cursor.getString(0);
                            String number = cursor.getString(1);
                            Contact contact = new Contact(name, number);
                            if (!contactList.contains(contact)) {
                                contactList.add(contact);
                                adapter.notifyItemInserted(contactList.size() - 1);
                                saveContacts();
                            } else {
                                Toast.makeText(this, "Contact already added", Toast.LENGTH_SHORT).show();
                            }
                            cursor.close();
                        }
                    }
                });

        pickContactButton.setOnClickListener(v -> pickContact());
        sosButton.setOnClickListener(v -> sendSOS());

        // Request permissions if not granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS, Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST);
        }
    }

    private void pickContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        contactPickerLauncher.launch(intent);
    }

    private void removeContact(int position) {
        contactList.remove(position);
        adapter.notifyItemRemoved(position);
        saveContacts();
    }

    private void loadContacts() {
        contactList.clear();
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String json = prefs.getString(CONTACTS_KEY, null);
        if (json != null) {
            try {
                JSONArray arr = new JSONArray(json);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    contactList.add(new Contact(obj.getString("name"), obj.getString("number")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveContacts() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        JSONArray arr = new JSONArray();
        for (Contact c : contactList) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("name", c.name);
                obj.put("number", c.number);
                arr.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        prefs.edit().putString(CONTACTS_KEY, arr.toString()).apply();
    }

    private void sendSOS() {
        if (contactList.isEmpty()) {
            Toast.makeText(this, "No emergency contacts added.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Get location and send SMS
        getLocationAndSendSOS();
    }

    private void getLocationAndSendSOS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                sendSMSToAll(formatLocation(location));
            } else {
                // Request a single update if last location is null
                LocationRequest locationRequest = LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setInterval(2000)
                        .setFastestInterval(1000)
                        .setNumUpdates(1);
                fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        Location loc = locationResult.getLastLocation();
                        if (loc != null) {
                            sendSMSToAll(formatLocation(loc));
                        } else {
                            sendSMSToAll("Unavailable");
                        }
                        fusedLocationClient.removeLocationUpdates(this);
                    }
                }, Looper.getMainLooper());
            }
        }).addOnFailureListener(e -> sendSMSToAll("Unavailable"));
    }

    private String formatLocation(Location loc) {
        return loc.getLatitude() + "," + loc.getLongitude();
    }

    private void sendSMSToAll(String location) {
        String message = "HELP; Location: " + location;
        SmsManager smsManager = SmsManager.getDefault();
        for (Contact c : contactList) {
            smsManager.sendTextMessage(c.number, null, message, null, null);
        }
        Toast.makeText(this, "SOS sent to all contacts", Toast.LENGTH_SHORT).show();
    }

    // Contact data class
    static class Contact {
        String name, number;
        Contact(String name, String number) {
            this.name = name;
            this.number = number;
        }
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Contact)) return false;
            Contact other = (Contact) obj;
            return name.equals(other.name) && number.equals(other.number);
        }
    }

    // RecyclerView Adapter for contacts
    static class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
        private final ArrayList<Contact> contacts;
        private final RemoveListener removeListener;

        interface RemoveListener {
            void onRemove(int position);
        }

        ContactAdapter(ArrayList<Contact> contacts, RemoveListener listener) {
            this.contacts = contacts;
            this.removeListener = listener;
        }

        @NonNull
        @Override
        public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
            return new ContactViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
            Contact c = contacts.get(position);
            holder.contactName.setText(c.name);
            holder.contactNumber.setText(c.number);
            holder.removeBtn.setOnClickListener(v -> removeListener.onRemove(holder.getAdapterPosition()));
        }

        @Override
        public int getItemCount() {
            return contacts.size();
        }

        static class ContactViewHolder extends RecyclerView.ViewHolder {
            TextView contactName, contactNumber;
            ImageButton removeBtn;
            ContactViewHolder(@NonNull View itemView) {
                super(itemView);
                contactName = itemView.findViewById(R.id.contactName);
                contactNumber = itemView.findViewById(R.id.contactNumber);
                removeBtn = itemView.findViewById(R.id.removeBtn);
            }
        }
    }
}
