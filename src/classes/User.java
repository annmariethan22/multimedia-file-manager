package classes;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "role", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = SimpleUser.class, name = "Simple User"),
    @JsonSubTypes.Type(value = Author.class, name = "Author"),
    @JsonSubTypes.Type(value = Admin.class, name = "Admin")
})
public abstract class User {
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String role; 
    
    private List<String> authorizedCategories;
    private List<String> followedDocuments;
    private Map<String, Integer> lastSeenDocumentVersions;

    
    public User() {
        this.authorizedCategories = new ArrayList<>();
        this.followedDocuments = new ArrayList<>();
        this.lastSeenDocumentVersions = new HashMap<>();
    }

    public User(String firstName, String lastName, String username, String password, String role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.role = role;
        this.authorizedCategories = new ArrayList<>();
        this.followedDocuments = new ArrayList<>();
        this.lastSeenDocumentVersions = new HashMap<>();
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

    // Βασικός έλεγχος πρόσβασης (Τον κάνουμε overridable)
    public boolean hasAccessToCategory(String categoryName) {
        return authorizedCategories.contains(categoryName);
    }

    
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public List<String> getAuthorizedCategories() { return authorizedCategories; }
    public List<String> getFollowedDocuments() { return followedDocuments; }
    
    public Map<String, Integer> getLastSeenDocumentVersions() {
        if (lastSeenDocumentVersions == null) {
            lastSeenDocumentVersions = new HashMap<>();
        }
        return lastSeenDocumentVersions;
    }

    public int getSeenVersionForDocument(String documentTitle) {
        Integer version = getLastSeenDocumentVersions().get(documentTitle);
        return version == null ? 0 : version;
    }

    public void updateSeenVersionForDocument(String documentTitle, int version) {
        getLastSeenDocumentVersions().put(documentTitle, version);
    }

    public void removeSeenVersionForDocument(String documentTitle) {
        getLastSeenDocumentVersions().remove(documentTitle);
    }

    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }
    public void setAuthorizedCategories(List<String> authorizedCategories) { this.authorizedCategories = authorizedCategories; }
    public void setFollowedDocuments(List<String> followedDocuments) { this.followedDocuments = followedDocuments; }
    public void setLastSeenDocumentVersions(Map<String, Integer> lastSeenDocumentVersions) {
        this.lastSeenDocumentVersions = (lastSeenDocumentVersions == null) ? new HashMap<>() : lastSeenDocumentVersions;
    }
}