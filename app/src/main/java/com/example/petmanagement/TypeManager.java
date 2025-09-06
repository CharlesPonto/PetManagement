package com.example.petmanagement;

import android.content.Context;
import java.util.ArrayList;

public class TypeManager {
    private static ArrayList<String> typeList;

    public static ArrayList<String> getTypes(Context context) {
        if (typeList == null) {
            typeList = FileStorageHelper.loadList(context, "types.txt");
            if (typeList.isEmpty()) {
                typeList.add("Dog");
                typeList.add("Cat");
                typeList.add("Bird");
            }
            if (!typeList.contains("Add New Type...")) {
                typeList.add("Add New Type...");
            }
        }
        return typeList;
    }

    public static void addType(Context context, String newType) {
        if (!typeList.contains(newType)) {
            typeList.add(typeList.size() - 1, newType); // add before "Add New..."
            FileStorageHelper.saveList(context, "types.txt", typeList);
        }
    }
}
