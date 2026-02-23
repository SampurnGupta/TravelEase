package com.example.travelease;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    private static final String PREFS = "profile_prefs";
    private static final String KEY_NAME = "name";
    private static final String KEY_NUMBER = "number";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_PHOTO = "photo";

    private ShapeableImageView profileImage;
    private TextInputEditText nameInput, numberInput, emailInput, addressInput;
    private ImageButton editButton;

    private boolean isEditMode = false;
    private ActivityResultLauncher<Intent> photoPickerLauncher;
    private Uri profileImageUri = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImage = findViewById(R.id.profileImage);
        nameInput = findViewById(R.id.nameInput);
        numberInput = findViewById(R.id.numberInput);
        emailInput = findViewById(R.id.emailInput);
        addressInput = findViewById(R.id.addressInput);
        editButton = findViewById(R.id.editButton);

        loadProfile();

        // Photo picker
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            profileImageUri = uri;
                            setProfileImage(uri);
                        }
                    }
                });

        profileImage.setOnClickListener(v -> {
            if (isEditMode) pickImage();
        });

        editButton.setOnClickListener(v -> {
            if (isEditMode) {
                if (saveProfile()) {
                    setEditMode(false);
                    Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show();
                }
            } else {
                setEditMode(true);
            }
        });

        setEditMode(false);
    }

    private void setEditMode(boolean enable) {
        isEditMode = enable;
        nameInput.setEnabled(enable);
        numberInput.setEnabled(enable);
        emailInput.setEnabled(enable);
        addressInput.setEnabled(enable);
        profileImage.setClickable(enable);
        editButton.setImageResource(enable ? android.R.drawable.ic_menu_save : android.R.drawable.ic_menu_edit);
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photoPickerLauncher.launch(intent);
    }

    private void setProfileImage(Uri uri) {
        try {
            Bitmap bitmap;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), uri));
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            }
            profileImage.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadProfile() {
        SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        nameInput.setText(prefs.getString(KEY_NAME, ""));
        numberInput.setText(prefs.getString(KEY_NUMBER, ""));
        emailInput.setText(prefs.getString(KEY_EMAIL, ""));
        addressInput.setText(prefs.getString(KEY_ADDRESS, ""));
        String photoBase64 = prefs.getString(KEY_PHOTO, null);
        if (!TextUtils.isEmpty(photoBase64)) {
            try {
                byte[] bytes = android.util.Base64.decode(photoBase64, android.util.Base64.DEFAULT);
                Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                profileImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                profileImage.setImageResource(R.drawable.ic_lock); // fallback
            }
        } else {
            profileImage.setImageResource(R.drawable.ic_lock);
        }
    }

    private boolean saveProfile() {
        String name = nameInput.getText().toString().trim();
        String number = numberInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();

        if (name.isEmpty() || number.isEmpty() || email.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return false;
        }

        SharedPreferences.Editor editor = getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_NUMBER, number);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_ADDRESS, address);

        Bitmap bitmap = null;
        if (profileImageUri != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), profileImageUri));
                } else {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), profileImageUri);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            profileImage.setDrawingCacheEnabled(true);
            bitmap = profileImage.getDrawingCache();
        }
        if (bitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            String photoBase64 = android.util.Base64.encodeToString(baos.toByteArray(), android.util.Base64.DEFAULT);
            editor.putString(KEY_PHOTO, photoBase64);
        }
        editor.apply();
        return true;
    }
}