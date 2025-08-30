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
public class AddPet extends AppCompatActivity {
    EditText txtName, txtGender, txtAge, txtType, txtBreed;
    ImageView ivPetImage;
    Button btnUpload, btnSave;
    Uri imageUri;
    DatabaseReference petsRef;
    StorageReference storageRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pet);
        txtName = findViewById(R.id.txtName);
        txtGender = findViewById(R.id.txtGender);
        txtAge = findViewById(R.id.txtAge);
        txtType = findViewById(R.id.txtType);
        txtBreed = findViewById(R.id.txtBreed);
        ivPetImage = findViewById(R.id.ivPetImage);
        btnUpload = findViewById(R.id.btnUpload);
        btnSave = findViewById(R.id.btnSave);
        petsRef = FirebaseDatabase.getInstance().getReference("pets");
        storageRef = FirebaseStorage.getInstance().getReference("pet_images");
        btnUpload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 101);
        });
        btnSave.setOnClickListener(v -> savePet());
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
    private void savePet() {
        String id = petsRef.push().getKey();
        String name = txtName.getText().toString();
        String gender = txtGender.getText().toString();
        String type = txtType.getText().toString();
        String breed = txtBreed.getText().toString();
        int age = 0;
        try {
            age = Integer.parseInt(txtAge.getText().toString());
        } catch (NumberFormatException e) {
            Log.e("AddPet", "⚠ Age not valid, defaulting to 0");
        }
        if (imageUri != null) {
            StorageReference imgRef = storageRef.child(id + ".jpg");
            int finalAge = age;
            imgRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->

                    imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Pet pet = new Pet(id, name, gender, finalAge, type, breed,
                                uri.toString());
                        petsRef.child(id).setValue(pet);
                        Toast.makeText(this, "✅ Pet Added", Toast.LENGTH_SHORT).show();
                        finish();
                    })
            );
        } else {
            Pet pet = new Pet(id, name, gender, age, type, breed, "");
            petsRef.child(id).setValue(pet);
            Toast.makeText(this, "✅ Pet Added (no image)", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}