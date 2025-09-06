package com.example.petmanagement;

import android.content.Context;
import java.util.ArrayList;

public class BreedManager {
    private static ArrayList<String> breedList;

    public static ArrayList<String> getBreeds(Context context) {
        if (breedList == null) {
            breedList = FileStorageHelper.loadList(context, "breeds.txt");
            if (breedList.isEmpty()) {
                breedList.add("Askal");
                breedList.add("Persian");
            }
            if (!breedList.contains("Add New Breed...")) {
                breedList.add("Add New Breed...");
            }
        }
        return breedList;
    }

    public static void addBreed(Context context, String newBreed) {
        if (!breedList.contains(newBreed)) {
            breedList.add(breedList.size() - 1, newBreed);
            FileStorageHelper.saveList(context, "breeds.txt", breedList);
        }
    }
}
