package services;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import classes.Category;
import classes.Document;
import classes.User;

public class DataManager {
    private List<User> users = new ArrayList<>();
    private List<Document> documents = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();
    
    private final String FOLDER_PATH = "medialab/";
    private final ObjectMapper mapper = new ObjectMapper();

    
    public void loadData() {
        try {
            mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            File folder = new File(FOLDER_PATH);
            if (!folder.exists()) folder.mkdir(); 

            File userFile = new File(FOLDER_PATH + "users.json");
            if (userFile.exists()) {
                users = mapper.readValue(userFile, new TypeReference<List<User>>(){});
            }
            
            File catFile = new File(FOLDER_PATH + "categories.json");
            if (catFile.exists()) {
                categories = mapper.readValue(catFile, new TypeReference<List<Category>>(){});
            }

            File docFile = new File(FOLDER_PATH + "documents.json");
            if (docFile.exists()) {
                documents = mapper.readValue(docFile, new TypeReference<List<Document>>(){});
            }
            
            // υπαρχει admin medialab
            checkAndCreateDefaultAdmin();
            
        } catch (IOException e) {
            System.err.println("Σφάλμα κατά τη φόρτωση: " + e.getMessage());
        }
    }

    
    public void saveData() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(FOLDER_PATH + "users.json"), users);
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(FOLDER_PATH + "categories.json"), categories);
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(FOLDER_PATH + "documents.json"), documents);
            System.out.println("Τα δεδομένα αποθηκεύτηκαν επιτυχώς στο " + FOLDER_PATH);
        } catch (IOException e) {
            System.err.println("Σφάλμα κατά την αποθήκευση: " + e.getMessage());
        }
    }

    private void checkAndCreateDefaultAdmin() {
        boolean exists = users.stream().anyMatch(u -> u.getUsername().equals("medialab"));
        if (!exists) {
            users.add(new User("Media", "Lab", "medialab", "medialab_2025", "Admin"));
        }
    }

    
    public List<User> getUsers() { return users; }
    public List<Document> getDocuments() { return documents; }
    public List<Category> getCategories() { return categories; }
}
