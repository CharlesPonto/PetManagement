package com.example.petmanagement;

public class Pet {
    private String id;
    private String name;
    private String gender;
    private int age;
    private String type;
    private String breed;
    private String imageUrl;
    private double price;
    private String dateOfBirth;

    public Pet() {} // required by Firebase

    public Pet(String id, String name, String gender, int age, String type, String breed,
               double price, String dateOfBirth, String imageUrl) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.type = type;
        this.breed = breed;
        this.price = price;
        this.dateOfBirth = dateOfBirth;
        this.imageUrl = imageUrl;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getGender() { return gender; }
    public int getAge() { return age; }
    public String getType() { return type; }
    public String getBreed() { return breed; }
    public String getImageUrl() { return imageUrl; }
    public double getPrice() { return price; }
    public String getDateOfBirth() { return dateOfBirth; }
}
