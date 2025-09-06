package com.example.petmanagement;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class FileStorageHelper {

    // Save list to file
    public static void saveList(Context context, String filename, ArrayList<String> list) {
        try {
            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            BufferedWriter writer = new BufferedWriter(new FileWriter(fos.getFD()));
            for (String item : list) {
                writer.write(item);
                writer.newLine();
            }
            writer.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load list from file
    public static ArrayList<String> loadList(Context context, String filename) {
        ArrayList<String> list = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(context.openFileInput(filename))
            );
            String line;
            while ((line = reader.readLine()) != null) {
                list.add(line);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            // File doesn't exist yet → return empty
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
