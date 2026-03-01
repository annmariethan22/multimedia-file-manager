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
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        File folder = new File(FOLDER_PATH);
        if (!folder.exists()) folder.mkdir(); 

        
        try {
            File userFile = new File(FOLDER_PATH + "users.json");
            // Διαβάζει μόνο αν το αρχείο υπάρχει ΚΑΙ δεν είναι εντελώς άδειο (0 bytes)
            if (userFile.exists() && userFile.length() > 0) {
                users = mapper.readValue(userFile, new TypeReference<List<User>>(){});
            }
        } catch (Exception e) { 
            System.err.println("Σφάλμα κατά τη φόρτωση των Users: " + e.getMessage()); 
        }
        
        
        try {
            File catFile = new File(FOLDER_PATH + "categories.json");
            if (catFile.exists() && catFile.length() > 0) {
                categories = mapper.readValue(catFile, new TypeReference<List<Category>>(){});
            }
        } catch (Exception e) { 
            System.err.println("Σφάλμα κατά τη φόρτωση των Categories: " + e.getMessage()); 
        }

        
        try {
            File docFile = new File(FOLDER_PATH + "documents.json");
            if (docFile.exists() && docFile.length() > 0) {
                documents = mapper.readValue(docFile, new TypeReference<List<Document>>(){});
            }
        } catch (Exception e) { 
            System.err.println("Σφάλμα κατά τη φόρτωση των Documents: " + e.getMessage()); 
        }
        
        
        checkAndCreateDefaultAdmin();
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
            
            users.add(new classes.Admin("Media", "Lab", "medialab", "medialab_2025"));
        }
    }

    
    public List<User> getUsers() { return users; }
    public List<Document> getDocuments() { return documents; }
    public List<Category> getCategories() { return categories; }
}
