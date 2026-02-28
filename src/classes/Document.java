package classes;

import java.util.ArrayList;
import java.util.List;

/**
 * Η κλάση Document αναπαριστά ένα έγγραφο (π.χ. θεατρικό έργο) στο σύστημα διαχείρισης.
 * Περιέχει τα μεταδεδομένα του εγγράφου (τίτλος, συγγραφέας, κατηγορία, ημερομηνία) 
 * και διαχειρίζεται το ιστορικό των εκδόσεών του (versioning).
 */
public class Document {
    private String title;
    private String authorName;
    private String category;
    private String creationDate;
    private List<String> versions;
    private int currentVersionNumber;

    /**
     * Προεπιλεγμένος κατασκευαστής (default constructor).
     * Δημιουργεί ένα κενό αντικείμενο Document. Απαραίτητο για τη σωστή φόρτωση από το Jackson (JSON).
     */
    public Document() {} 

    /**
     * Κατασκευαστής με παραμέτρους για την αρχικοποίηση ενός νέου εγγράφου.
     * Δημιουργεί την πρώτη έκδοση (version 1) με το αρχικό κείμενο.
     *
     * @param title        Ο τίτλος του νέου εγγράφου.
     * @param authorName   Το όνομα του συγγραφέα.
     * @param category     Η κατηγορία στην οποία ανήκει (π.χ. Κωμωδία).
     * @param creationDate Η ημερομηνία δημιουργίας ή έκδοσης του εγγράφου.
     * @param firstContent Το κείμενο της αρχικής έκδοσης του εγγράφου.
     */
    public Document(String title, String authorName, String category, String creationDate, String firstContent) {
        this.title = title;
        this.authorName = authorName;
        this.category = category;
        this.creationDate = creationDate;
        this.versions = new ArrayList<>();
        this.versions.add(firstContent);
        this.currentVersionNumber = 1; 
    }

    /**
     * Προσθέτει μια νέα έκδοση στο έγγραφο.
     * Αποθηκεύει το νέο κείμενο στο ιστορικό και αυξάνει αυτόματα τον αριθμό της τρέχουσας έκδοσης κατά 1.
     *
     * @param newContent Το κείμενο της νέας έκδοσης.
     */
    public void updateContent(String newContent) {
        this.versions.add(newContent); 
        this.currentVersionNumber++;  
    }

    /**
     * Επιστρέφει το κείμενο που δικαιούται να δει ένας απλός χρήστης (Simple User).
     * Βάσει προδιαγραφών, ο απλός χρήστης βλέπει μόνο την τελευταία (τρέχουσα) έκδοση.
     *
     * @return Το κείμενο της τελευταίας έκδοσης του εγγράφου.
     */
    public String getVisibleContentForSimpleUser() {
        return versions.get(versions.size() - 1);
    }

    /**
     * Επιστρέφει τις εκδόσεις που δικαιούται να δει ένας συγγραφέας (Author) ή διαχειριστής (Admin).
     * Βάσει προδιαγραφών, έχουν πρόσβαση στην τρέχουσα και το πολύ στις 2 προηγούμενες (μέγιστο 3).
     *
     * @return Μια λίστα με τα κείμενα των (έως) 3 τελευταίων εκδόσεων.
     */
    public List<String> getVisibleVersionsForAuthor() {
        int total = versions.size();
        if (total <= 3) {
            return new ArrayList<>(versions);
        } else {
            return new ArrayList<>(versions.subList(total - 3, total));
        }
    }
    
    /**
     * Επιστρέφει τον τίτλο του εγγράφου.
     * @return Ο τίτλος ως String.
     */
    public String getTitle() { return title; }

    /**
     * Επιστρέφει το όνομα του συγγραφέα.
     * @return Το όνομα του συγγραφέα ως String.
     */
    public String getAuthorName() { return authorName; }

    /**
     * Επιστρέφει την κατηγορία του εγγράφου.
     * @return Η κατηγορία ως String.
     */
    public String getCategory() { return category; }

    /**
     * Επιστρέφει την ημερομηνία δημιουργίας.
     * @return Η ημερομηνία ως String.
     */
    public String getCreationDate() { return creationDate; }

    /**
     * Επιστρέφει το πλήρες ιστορικό όλων των εκδόσεων του εγγράφου.
     * @return Λίστα (List) με όλα τα κείμενα.
     */
    public List<String> getVersions() { return versions; }

    /**
     * Επιστρέφει τον αριθμό της τρέχουσας (τελευταίας) έκδοσης.
     * @return Ο αριθμός έκδοσης (int).
     */
    public int getCurrentVersionNumber() { return currentVersionNumber; }

    /**
     * Ορίζει τον τίτλο του εγγράφου.
     * @param title Ο νέος τίτλος.
     */
    public void setTitle(String title) { this.title = title; }

    /**
     * Ορίζει το όνομα του συγγραφέα.
     * @param authorName Το νέο όνομα συγγραφέα.
     */
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    /**
     * Ορίζει την κατηγορία του εγγράφου.
     * @param category Η νέα κατηγορία.
     */
    public void setCategory(String category) { this.category = category; }

    /**
     * Ορίζει την ημερομηνία δημιουργίας.
     * @param creationDate Η νέα ημερομηνία.
     */
    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }

    /**
     * Ορίζει τη λίστα με το ιστορικό των εκδόσεων (συνήθως καλείται από το Jackson κατά τη φόρτωση JSON).
     * @param versions Η νέα λίστα εκδόσεων.
     */
    public void setVersions(List<String> versions) { this.versions = versions; }

    /**
     * Ορίζει τον τρέχοντα αριθμό έκδοσης (συνήθως καλείται από το Jackson κατά τη φόρτωση JSON).
     * @param currentVersionNumber Ο νέος αριθμός έκδοσης.
     */
    public void setCurrentVersionNumber(int currentVersionNumber) { this.currentVersionNumber = currentVersionNumber; }
}