package services;

import classes.Document;
import classes.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DocumentService {

    public Document createDocument(String title, String authorName, String category, LocalDate creationDate, String initialContent) {
        Document doc = new Document();
        doc.setTitle(title);
        doc.setAuthorName(authorName);
        doc.setCategory(category);
        doc.setCurrentVersionNumber(1);
        doc.setCreationDate(creationDate != null ? creationDate.toString() : LocalDate.now().toString());
        doc.setVersions(new ArrayList<>());
        doc.getVersions().add(initialContent);
        return doc;
    }

    public void addVersion(Document doc, String newContent) {
        doc.updateContent(newContent);
    }

    public void deleteDocumentAndCleanupFollows(Document target, List<Document> documents, List<User> users) {
        documents.remove(target);
        for (User u : users) {
            u.unfollowDocument(target.getTitle());
        }
    }

    public List<Integer> getVisibleVersionNumbersForUser(Document doc, User user) {
        int total = doc.getVersions() == null ? 0 : doc.getVersions().size();
        if (total == 0) return List.of();

        if ("Simple User".equals(user.getRole())) {
            return List.of(doc.getCurrentVersionNumber());
        }

        int start = Math.max(1, total - 2);
        List<Integer> visible = new ArrayList<>();
        for (int v = start; v <= total; v++) {
            visible.add(v);
        }
        visible.sort(Comparator.reverseOrder());
        return visible;
    }

    public String getContentByVersion(Document doc, int versionNumber) {
        if (doc.getVersions() == null || versionNumber < 1 || versionNumber > doc.getVersions().size()) {
            return "";
        }
        return doc.getVersions().get(versionNumber - 1);
    }
}
