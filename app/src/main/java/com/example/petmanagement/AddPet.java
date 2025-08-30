package com.example.petmanagement;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;

public class AddPet extends AppCompatActivity {

    EditText txtName, txtAge, txtType, txtPrice, txtDob;
    Spinner spinnerGender, spinnerBreed;
    ImageView ivPetImage;
    Button btnUpload, btnSave;
    Uri imageUri;
    DatabaseReference petsRef;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pet);

        // Find views
        txtName = findViewById(R.id.txtName);
        txtAge = findViewById(R.id.txtAge);
        txtType = findViewById(R.id.txtType);
        txtPrice = findViewById(R.id.txtPrice);
        txtDob = findViewById(R.id.txtDob);
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerBreed = findViewById(R.id.spinnerBreed);
        ivPetImage = findViewById(R.id.ivPetImage);
        btnUpload = findViewById(R.id.btnUpload);
        btnSave = findViewById(R.id.btnSave);

        petsRef = FirebaseDatabase.getInstance().getReference("pets");
        storageRef = FirebaseStorage.getInstance().getReference("pet_images");

        // Spinner setup
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"Male", "Female"});
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        ArrayAdapter<String> breedAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"Persian", "Siamese", "Bulldog", "Golden Retriever", "Parrot"});
        breedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBreed.setAdapter(breedAdapter);

        // Date picker for DOB
        txtDob.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(AddPet.this,
                    (view, year, month, day) -> {
                        String dob = year + "-" + (month + 1) + "-" + day;
                        txtDob.setText(dob);
                    },
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            dpd.show();
        });

        btnUpload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 101);
        });

        btnSave.setOnClickListener(v -> savePet());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            ivPetImage.setImageURI(imageUri);
        }
    }

    private void savePet() {
        String id = petsRef.push().getKey();
        if (id == null) {
            Toast.makeText(this, "❌ Failed to generate ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = txtName.getText().toString();

        // ✅ Get values from Spinner
        String gender = spinnerGender.getSelectedItem().toString();
        String breed = spinnerBreed.getSelectedItem().toString();
        String type = txtType.getText().toString();

        // ✅ Price & DOB
        double price = 0;
        try {
            price = Double.parseDouble(txtPrice.getText().toString());
        } catch (NumberFormatException ignored) {}

        String dob = txtDob.getText().toString();

        int age = 0;
        try {
            age = Integer.parseInt(txtAge.getText().toString());
        } catch (NumberFormatException e) {
            Log.e("AddPet", "⚠ Age not valid, defaulting to 0");
        }

        if (imageUri != null) {
            // ✅ Show progress while uploading
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Uploading...");
            progressDialog.show();

            StorageReference imgRef = storageRef.child(id + ".jpg");
            int finalAge = age;
            double finalPrice = price;

            imgRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot ->
                            imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                Pet pet = new Pet(id, name, gender, finalAge, type, breed, finalPrice, dob, uri.toString());
                                petsRef.child(id).setValue(pet);
                                progressDialog.dismiss();
                                Toast.makeText(this, "✅ Pet Added", Toast.LENGTH_SHORT).show();
                                finish();
                            }))
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Log.e("AddPet", "Upload failed", e); // 👈 Add this
                        Toast.makeText(this, "❌ Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            Pet pet = new Pet(id, name, gender, age, type, breed, price, dob, "");
            petsRef.child(id).setValue(pet);
            Toast.makeText(this, "✅ Pet Added (no image)", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
