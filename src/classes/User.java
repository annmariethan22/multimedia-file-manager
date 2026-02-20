package classes;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String role; // "Admin", "Author", "Simple User"
    
    //βλεπει ο χρηστης
    private List<String> authorizedCategories;
    
    // για notifications σε αυτα που ακολουθει
    private List<String> followedDocuments;

    
    public User(String firstName, String lastName, String username, String password, String role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.role = role;
        this.authorizedCategories = new ArrayList<>();
        this.followedDocuments = new ArrayList<>();
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
    }

    
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public List<String> getAuthorizedCategories() { return authorizedCategories; }
    public List<String> getFollowedDocuments() { return followedDocuments; }

    // ελεγχος δικαιωματος σε κατηγορια
    public boolean hasAccessToCategory(String categoryName) {
        if (this.role.equals("Admin")) return true;
        return authorizedCategories.contains(categoryName);
    }
    public User() {} 
}
