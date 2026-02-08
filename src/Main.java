import classes.Category;
import classes.Document;
import services.DataManager;

public class Main { 
    public static void main(String[] args) {
        DataManager manager = new DataManager();
        
        // ανακτηση απο τζσον
        manager.loadData(); 

        // Προσθηκη κατηγοριας
        if (manager.getCategories().isEmpty()) {
            manager.getCategories().add(new Category("Αρχαία Τραγωδία")); 
        }

        // Δημιουργία εγγράφου με τα υποχρεωτικά πεδία 
        if (manager.getDocuments().isEmpty()) {
            
            Document d = new Document("Αντιγόνη", "Σοφοκλής", "Αρχαία Τραγωδία", "2024-02-08", "Πρώτη Σκηνή..."); 
            manager.getDocuments().add(d);
        }

        
        manager.saveData(); 
        
        System.out.println("Το έργο αποθηκεύτηκε στο medialab/documents.json");
    }
}