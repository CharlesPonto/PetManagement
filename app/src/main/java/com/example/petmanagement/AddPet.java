package com.example.petmanagement;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.*;

public class AddPet extends AppCompatActivity {

    EditText txtName, txtAge, txtPrice, txtDob;
    Spinner spinnerGender, spinnerType, spinnerBreed;
    Button btnSave;
    DatabaseReference petsRef;

    ArrayAdapter<String> typeAdapter, breedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pet);

        // Find views
        txtName = findViewById(R.id.txtName);
        txtAge = findViewById(R.id.txtAge);
        txtPrice = findViewById(R.id.txtPrice);
        txtDob = findViewById(R.id.txtDob);
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerType = findViewById(R.id.spinnerType);
        spinnerBreed = findViewById(R.id.spinnerBreed);
        btnSave = findViewById(R.id.btnSave);

        petsRef = FirebaseDatabase.getInstance().getReference("pets");

        // Gender Spinner
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"Male", "Female"});
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        // Type Spinner from TypeManager
        typeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                TypeManager.getTypes(this));
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        // Breed Spinner from BreedManager
        breedAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                BreedManager.getBreeds(this));
        breedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBreed.setAdapter(breedAdapter);

        // Handle Type Spinner selection
        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                String selected = spinnerType.getSelectedItem().toString();
                if (selected.equals("Add New Type...")) {
                    showAddDialog("Type");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Handle Breed Spinner selection
        spinnerBreed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                String selected = spinnerBreed.getSelectedItem().toString();
                if (selected.equals("Add New Breed...")) {
                    showAddDialog("Breed");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

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

        btnSave.setOnClickListener(v -> savePet());
    }

    private void showAddDialog(String type) {
        EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Add New " + type)
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String newItem = input.getText().toString().trim();
                    if (!newItem.isEmpty()) {
                        if (type.equals("Type")) {
                            TypeManager.addType(this, newItem);
                            typeAdapter.notifyDataSetChanged();
                            spinnerType.setSelection(TypeManager.getTypes(this).indexOf(newItem));
                        } else {
                            BreedManager.addBreed(this, newItem);
                            breedAdapter.notifyDataSetChanged();
                            spinnerBreed.setSelection(BreedManager.getBreeds(this).indexOf(newItem));
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void savePet() {
        String id = petsRef.push().getKey();
        if (id == null) {
            Toast.makeText(this, "❌ Failed to generate ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = txtName.getText().toString();
        String gender = spinnerGender.getSelectedItem().toString();
        String type = spinnerType.getSelectedItem().toString();
        String breed = spinnerBreed.getSelectedItem().toString();

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

        Pet pet = new Pet(id, name, gender, age, type, breed, price, dob, "");
        petsRef.child(id).setValue(pet);

        Toast.makeText(this, "✅ Pet Added", Toast.LENGTH_SHORT).show();
        finish();
    }
}
