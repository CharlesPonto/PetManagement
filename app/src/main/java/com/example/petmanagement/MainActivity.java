package com.example.petmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    FirebaseRecyclerAdapter<Pet, PetViewHolder> adapter;
    DatabaseReference petsRef;
    Button btnAddPet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        petsRef = FirebaseDatabase.getInstance().getReference("pets");
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnAddPet = findViewById(R.id.btnAddPet);
        btnAddPet.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddPet.class)));

        FirebaseRecyclerOptions<Pet> options =
                new FirebaseRecyclerOptions.Builder<Pet>()
                        .setQuery(petsRef, Pet.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<Pet, PetViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PetViewHolder holder, int position, @NonNull Pet pet) {
                holder.tvName.setText(pet.getName());
                holder.tvType.setText(pet.getType());

                if (pet.getImageUrl() != null && !pet.getImageUrl().isEmpty()) {
                    Picasso.get().load(pet.getImageUrl()).into(holder.ivPet);
                }

                holder.btnEdit.setOnClickListener(v -> {
                    Intent i = new Intent(MainActivity.this, EditPet.class);
                    i.putExtra("petId", pet.getId());
                    startActivity(i);
                });

                holder.btnDelete.setOnClickListener(v -> petsRef.child(pet.getId()).removeValue());
            }

            @NonNull
            @Override
            public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_pet, parent, false);
                return new PetViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
    }

    @Override protected void onStart() { super.onStart(); adapter.startListening(); }
    @Override protected void onStop() { super.onStop(); adapter.stopListening(); }
}

class PetViewHolder extends RecyclerView.ViewHolder {
    TextView tvName, tvType;
    ImageView ivPet;
    Button btnEdit, btnDelete;

    public PetViewHolder(@NonNull View itemView) {
        super(itemView);
        tvName = itemView.findViewById(R.id.tvName);
        tvType = itemView.findViewById(R.id.tvType);
        ivPet = itemView.findViewById(R.id.ivPet);
        btnEdit = itemView.findViewById(R.id.btnEdit);
        btnDelete = itemView.findViewById(R.id.btnDelete);
    }
}