
package classes;

import java.util.ArrayList;
import java.util.List;

public class Document {
    private String title;
    private String authorName;
    private String category;
    private String creationDate;
    private List<String> versions;
    private int currentVersionNumber;

    public Document(String title, String authorName, String category, String creationDate, String firstContent) {
        this.title = title;
        this.authorName = authorName;
        this.category = category;
        this.creationDate = creationDate;
        this.versions = new ArrayList<>();
        this.versions.add(firstContent);
        this.currentVersionNumber = 1; 
    }

    public void updateContent(String newContent) {
    this.versions.add(newContent); 
    this.currentVersionNumber++;  
}

    

    // απλοι χρήστες 
    public String getVisibleContentForSimpleUser() {
        return versions.get(versions.size() - 1);
    }

    // αντιμινς 
    public List<String> getVisibleVersionsForAuthor() {
        int total = versions.size();
        if (total <= 3) {
            return new ArrayList<>(versions);
        } else {
            // 3 τελευταίες εκδόσεις
            return new ArrayList<>(versions.subList(total - 3, total));
        }
    }
    public Document() {} 
    
    public String getTitle() { return title; }
    public String getAuthorName() { return authorName; }
    public String getCategory() { return category; }
    public int getCurrentVersionNumber() { return currentVersionNumber; }
}

public void setTitle(String title) { this.title = title; }
public void setAuthorName(String authorName) { this.authorName = authorName; }
public void setCategory(String category) { this.category = category; }
public void setCreationDate(String creationDate) { this.creationDate = creationDate; }
public void setVersions(List<String> versions) { this.versions = versions; }
public void setCurrentVersionNumber(int currentVersionNumber) { this.currentVersionNumber = currentVersionNumber; }