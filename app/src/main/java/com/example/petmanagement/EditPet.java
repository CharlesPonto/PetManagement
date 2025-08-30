package com.example.petmanagement;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
public class EditPet extends AppCompatActivity {
    EditText txtName, txtGender, txtAge, txtType, txtBreed;
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
        txtName = findViewById(R.id.txtName);
        txtGender = findViewById(R.id.txtGender);
        txtAge = findViewById(R.id.txtAge);
        txtType = findViewById(R.id.txtType);
        txtBreed = findViewById(R.id.txtBreed);
        ivPetImage = findViewById(R.id.ivPetImage);
        btnUpload = findViewById(R.id.btnUpload);
        btnSave = findViewById(R.id.btnSave);
        btnSave.setText("Update Pet"); // make it clear this is edit mode
        petsRef = FirebaseDatabase.getInstance().getReference("pets");
        storageRef = FirebaseStorage.getInstance().getReference("pet_images");
        petId = getIntent().getStringExtra("petId");
        if (petId == null) {
            Toast.makeText(this, "No Pet ID found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
// Load existing pet details

        petsRef.child(petId).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                currentPet = snapshot.getValue(Pet.class);
                if (currentPet != null) {
                    txtName.setText(currentPet.getName());
                    txtGender.setText(currentPet.getGender());
                    txtAge.setText(String.valueOf(currentPet.getAge()));
                    txtType.setText(currentPet.getType());
                    txtBreed.setText(currentPet.getBreed());
                    if (currentPet.getImageUrl() != null &&
                            !currentPet.getImageUrl().isEmpty()) {
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            ivPetImage.setImageURI(imageUri);
        }
    }
    private void updatePet() {
        String name = txtName.getText().toString();
        String gender = txtGender.getText().toString();
        String type = txtType.getText().toString();
        String breed = txtBreed.getText().toString();
        int age = 0;
        try {
            age = Integer.parseInt(txtAge.getText().toString());
        } catch (NumberFormatException e) {
            Log.e("EditPet", "Age not valid, defaulting to 0");
        }
        if (imageUri != null) {
            StorageReference imgRef = storageRef.child(petId + ".jpg");
            int finalAge = age;
            imgRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                    imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Pet pet = new Pet(petId, name, gender, finalAge, type, breed,
                                uri.toString());
                        petsRef.child(petId).setValue(pet);
                        Toast.makeText(this, "Pet Updated (new image)",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    })
            );
        } else {
            String existingUrl = (currentPet != null && currentPet.getImageUrl() != null) ?
                    currentPet.getImageUrl() : "";
            Pet pet = new Pet(petId, name, gender, age, type, breed, existingUrl);
            petsRef.child(petId).setValue(pet);
            Toast.makeText(this, "Pet Updated", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}