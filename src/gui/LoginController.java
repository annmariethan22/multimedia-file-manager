package gui;

import classes.User;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import services.DataManager;

public class LoginController {
    private DataManager dataManager;

    public LoginController(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    
    public User handleLogin(String username, String password) {
        
        User user = dataManager.getUsers().stream()
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);

        if (user != null) {
            System.out.println("Επιτυχής σύνδεση: " + user.getRole());
            return user;
        } else {
            showError("Σφάλμα Σύνδεσης", "Λάθος όνομα χρήστη ή κωδικός πρόσβασης.");
            return null;
        }
        
}
public User handleLogin(String username, String password) {
    
    return dataManager.getUsers().stream()
            .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
            .findFirst()
            .orElse(null); 
}
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}