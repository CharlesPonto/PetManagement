package com.example.petmanagement;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;

public class EditPet extends AppCompatActivity {

    EditText txtName, txtAge, txtPrice, txtDob;
    Spinner spinnerGender, spinnerType, spinnerBreed;
    ImageView ivPetImage;
    Button btnUpload, btnSave;
    Uri imageUri;
    DatabaseReference petsRef;
    StorageReference storageRef;
    String petId;
    Pet currentPet;

    ArrayList<String> typeList;
    ArrayList<String> breedList;
    ArrayAdapter<String> typeAdapter;
    ArrayAdapter<String> breedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pet); // reuse same layout

        // Views
        txtName = findViewById(R.id.txtName);
        txtAge = findViewById(R.id.txtAge);
        txtPrice = findViewById(R.id.txtPrice);
        txtDob = findViewById(R.id.txtDob);
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerType = findViewById(R.id.spinnerType);
        spinnerBreed = findViewById(R.id.spinnerBreed);
        ivPetImage = findViewById(R.id.ivPetImage);
        btnUpload = findViewById(R.id.btnUpload);
        btnSave = findViewById(R.id.btnSave);

        btnSave.setText("Update Pet");

        // Firebase
        petsRef = FirebaseDatabase.getInstance().getReference("pets");
        storageRef = FirebaseStorage.getInstance().getReference("pet_images");

        petId = getIntent().getStringExtra("petId");
        if (petId == null) {
            Toast.makeText(this, "No Pet ID found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Setup gender spinner
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"Male", "Female"});
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        // --- Setup Type Spinner ---
        typeList = TypeManager.getTypes(this);
        typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, typeList);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                if (typeList.get(position).equals("Add New Type...")) {
                    showAddDialog("Type", newValue -> {
                        TypeManager.addType(EditPet.this, newValue);
                        typeAdapter.notifyDataSetChanged();
                        spinnerType.setSelection(typeList.indexOf(newValue));
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // --- Setup Breed Spinner ---
        breedList = BreedManager.getBreeds(this);
        breedAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, breedList);
        breedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBreed.setAdapter(breedAdapter);

        spinnerBreed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                if (breedList.get(position).equals("Add New Breed...")) {
                    showAddDialog("Breed", newValue -> {
                        BreedManager.addBreed(EditPet.this, newValue);
                        breedAdapter.notifyDataSetChanged();
                        spinnerBreed.setSelection(breedList.indexOf(newValue));
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Date picker
        txtDob.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(EditPet.this,
                    (view, year, month, day) -> txtDob.setText(year + "-" + (month + 1) + "-" + day),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            dpd.show();
        });

        // Load existing pet
        petsRef.child(petId).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                currentPet = snapshot.getValue(Pet.class);
                if (currentPet != null) {
                    txtName.setText(currentPet.getName());
                    txtAge.setText(String.valueOf(currentPet.getAge()));
                    txtPrice.setText(String.valueOf(currentPet.getPrice()));
                    txtDob.setText(currentPet.getDateOfBirth());

                    if (currentPet.getGender() != null) {
                        int pos = genderAdapter.getPosition(currentPet.getGender());
                        if (pos >= 0) spinnerGender.setSelection(pos);
                    }
                    if (currentPet.getType() != null) {
                        if (!typeList.contains(currentPet.getType())) {
                            TypeManager.addType(this, currentPet.getType());
                            typeAdapter.notifyDataSetChanged();
                        }
                        int pos = typeAdapter.getPosition(currentPet.getType());
                        if (pos >= 0) spinnerType.setSelection(pos);
                    }
                    if (currentPet.getBreed() != null) {
                        if (!breedList.contains(currentPet.getBreed())) {
                            BreedManager.addBreed(this, currentPet.getBreed());
                            breedAdapter.notifyDataSetChanged();
                        }
                        int pos = breedAdapter.getPosition(currentPet.getBreed());
                        if (pos >= 0) spinnerBreed.setSelection(pos);
                    }

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

    // --- Dialog helper with callback ---
    private void showAddDialog(String title, OnAddListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New " + title);

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String newValue = input.getText().toString().trim();
            if (!newValue.isEmpty()) {
                listener.onAdded(newValue);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    interface OnAddListener {
        void onAdded(String value);
    }

    private void updatePet() {
        String name = txtName.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();
        String type = spinnerType.getSelectedItem().toString();
        String breed = spinnerBreed.getSelectedItem().toString();
        double price = 0;
        int age = 0;

        try {
            price = Double.parseDouble(txtPrice.getText().toString());
        } catch (NumberFormatException ignored) {
        }
        try {
            age = Integer.parseInt(txtAge.getText().toString());
        } catch (NumberFormatException ignored) {
        }

        String dob = txtDob.getText().toString();

        if (name.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri != null) {
            ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("Updating...");
            pd.show();

            StorageReference imgRef = storageRef.child(petId + ".jpg");
            int finalAge = age;
            double finalPrice = price;

            imgRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                    imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Pet pet = new Pet(petId, name, gender, finalAge, type, breed, finalPrice, dob, uri.toString());
                        petsRef.child(petId).setValue(pet).addOnSuccessListener(aVoid -> {
                            pd.dismiss();
                            Toast.makeText(EditPet.this, "✅ Pet Updated", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK, new Intent()); // notify parent
                            finish(); // go back to list
                        }).addOnFailureListener(e -> {
                            pd.dismiss();
                            Toast.makeText(EditPet.this, "❌ Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                    })
            ).addOnFailureListener(e -> {
                pd.dismiss();
                Toast.makeText(this, "❌ Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        } else {
            String existingUrl = (currentPet != null && currentPet.getImageUrl() != null) ? currentPet.getImageUrl() : "";
            Pet pet = new Pet(petId, name, gender, age, type, breed, price, dob, existingUrl);

            petsRef.child(petId).setValue(pet).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "✅ Pet Updated", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK, new Intent()); // notify parent
                finish(); // close activity
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "❌ Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        }
    }
}


