package classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String role; // "Admin", "Author", "Simple User"

    //βλεπει ο χρηστης
    private List<String> authorizedCategories;

    private List<String> followedDocuments;

    /**
     * Κρατάει την τελευταία έκδοση που έχει δει ο χρήστης για κάθε έγγραφο που παρακολουθεί.
     * Key: τίτλος εγγράφου, Value: αριθμός έκδοσης που έχει ήδη δει ο χρήστης.
     */
    private Map<String, Integer> lastSeenVersionByDocument;

    public User(String firstName, String lastName, String username, String password, String role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.role = role;
        this.authorizedCategories = new ArrayList<>();
        this.followedDocuments = new ArrayList<>();
        this.lastSeenVersionByDocument = new HashMap<>();
    }

    public void addAuthorizedCategory(String categoryName) {
        if (!this.authorizedCategories.contains(categoryName)) {
            this.authorizedCategories.add(categoryName);
        }
    }

    public void followDocument(String documentTitle) {
        if (!this.followedDocuments.contains(documentTitle)) {
            this.followedDocuments.add(documentTitle);
        }
        if (this.lastSeenVersionByDocument == null) {
            this.lastSeenVersionByDocument = new HashMap<>();
        }
        this.lastSeenVersionByDocument.putIfAbsent(documentTitle, 0);
    }

    public void unfollowDocument(String documentTitle) {
        if (this.followedDocuments != null) {
            this.followedDocuments.remove(documentTitle);
        }
        if (this.lastSeenVersionByDocument != null) {
            this.lastSeenVersionByDocument.remove(documentTitle);
        }
    }

    public int getLastSeenVersionForDocument(String documentTitle) {
        if (this.lastSeenVersionByDocument == null) {
            this.lastSeenVersionByDocument = new HashMap<>();
        }
        return this.lastSeenVersionByDocument.getOrDefault(documentTitle, 0);
    }

    public void markDocumentVersionAsSeen(String documentTitle, int versionNumber) {
        if (this.lastSeenVersionByDocument == null) {
            this.lastSeenVersionByDocument = new HashMap<>();
        }
        this.lastSeenVersionByDocument.put(documentTitle, versionNumber);
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public List<String> getAuthorizedCategories() { return authorizedCategories; }
    public List<String> getFollowedDocuments() { return followedDocuments; }
    public Map<String, Integer> getLastSeenVersionByDocument() { return lastSeenVersionByDocument; }

    // ελεγχος δικαιωματος σε κατηγορια
    public boolean hasAccessToCategory(String categoryName) {
        if (this.role.equals("Admin")) return true;
        return authorizedCategories.contains(categoryName);
    }

    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }
    public void setAuthorizedCategories(List<String> authorizedCategories) { this.authorizedCategories = authorizedCategories; }
    public void setFollowedDocuments(List<String> followedDocuments) { this.followedDocuments = followedDocuments; }
    public void setLastSeenVersionByDocument(Map<String, Integer> lastSeenVersionByDocument) { this.lastSeenVersionByDocument = lastSeenVersionByDocument; }

    public User() {
        this.authorizedCategories = new ArrayList<>();
        this.followedDocuments = new ArrayList<>();
        this.lastSeenVersionByDocument = new HashMap<>();
    }
}
