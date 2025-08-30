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
import com.squareup.picasso.Picasso;

import java.util.Calendar;

public class EditPet extends AppCompatActivity {

    EditText txtName, txtAge, txtType, txtPrice, txtDob;
    Spinner spinnerGender, spinnerBreed;
    ImageView ivPetImage;
    Button btnUpload, btnSave;
    Uri imageUri;
    DatabaseReference petsRef;
    StorageReference storageRef;
    String petId;
    Pet currentPet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pet); // reuse same layout

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

        btnSave.setText("Update Pet"); // edit mode

        // Firebase setup
        petsRef = FirebaseDatabase.getInstance().getReference("pets");
        storageRef = FirebaseStorage.getInstance().getReference("pet_images");

        petId = getIntent().getStringExtra("petId");
        if (petId == null) {
            Toast.makeText(this, "No Pet ID found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup spinners
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
            DatePickerDialog dpd = new DatePickerDialog(EditPet.this,
                    (view, year, month, day) -> {
                        String dob = year + "-" + (month + 1) + "-" + day;
                        txtDob.setText(dob);
                    },
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            dpd.show();
        });

        // Load existing pet details
        petsRef.child(petId).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                currentPet = snapshot.getValue(Pet.class);
                if (currentPet != null) {
                    txtName.setText(currentPet.getName());
                    txtAge.setText(String.valueOf(currentPet.getAge()));
                    txtType.setText(currentPet.getType());
                    txtPrice.setText(String.valueOf(currentPet.getPrice()));
                    txtDob.setText(currentPet.getDateOfBirth());

                    // Set spinner values
                    if (currentPet.getGender() != null) {
                        int pos = genderAdapter.getPosition(currentPet.getGender());
                        if (pos >= 0) spinnerGender.setSelection(pos);
                    }
                    if (currentPet.getBreed() != null) {
                        int pos = breedAdapter.getPosition(currentPet.getBreed());
                        if (pos >= 0) spinnerBreed.setSelection(pos);
                    }

                    // Load image
                    if (currentPet.getImageUrl() != null && !currentPet.getImageUrl().isEmpty()) {
                        Picasso.get().load(currentPet.getImageUrl()).into(ivPetImage);
                    }
                }
            }
        });

        btnUpload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 101);
        });

        btnSave.setOnClickListener(v -> updatePet());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            ivPetImage.setImageURI(imageUri);
        }
    }

    private void updatePet() {
        String name = txtName.getText().toString();
        String gender = spinnerGender.getSelectedItem().toString();
        String type = txtType.getText().toString();
        String breed = spinnerBreed.getSelectedItem().toString();

        double price = 0;
        try { price = Double.parseDouble(txtPrice.getText().toString()); }
        catch (NumberFormatException ignored) {}

        String dob = txtDob.getText().toString();

        int age = 0;
        try { age = Integer.parseInt(txtAge.getText().toString()); }
        catch (NumberFormatException e) { Log.e("EditPet", "Invalid age, default 0"); }

        if (imageUri != null) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Updating...");
            progressDialog.show();

            StorageReference imgRef = storageRef.child(petId + ".jpg");
            int finalAge = age;
            double finalPrice = price;

            imgRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                    imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Pet pet = new Pet(petId, name, gender, finalAge, type, breed,
                                finalPrice, dob, uri.toString());
                        petsRef.child(petId).setValue(pet);
                        progressDialog.dismiss();
                        Toast.makeText(this, "Pet Updated (new image)", Toast.LENGTH_SHORT).show();
                        finish();
                    })
            ).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        } else {
            String existingUrl = (currentPet != null && currentPet.getImageUrl() != null) ? currentPet.getImageUrl() : "";
            Pet pet = new Pet(petId, name, gender, age, type, breed, price, dob, existingUrl);
            petsRef.child(petId).setValue(pet);
            Toast.makeText(this, "Pet Updated", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
