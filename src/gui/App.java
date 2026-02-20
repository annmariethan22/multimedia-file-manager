package gui;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.DataManager;
import classes.User;

public class App extends Application {
    private DataManager dataManager = new DataManager();
    private User loggedInUser;

    @Override
    public void start(Stage primaryStage) {
        dataManager.loadData(); 

        primaryStage.setTitle("Θεατρικό Σύστημα - Είσοδος");

        // login
        Label label = new Label("Σύνδεση Χρήστη");
        TextField userField = new TextField();
        userField.setPromptText("Όνομα χρήστη");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Κωδικός πρόσβασης");
        Button loginBtn = new Button("Είσοδος");

        loginBtn.setOnAction(e -> {
            String username = userField.getText();
            String password = passField.getText();

            // isuser
            loggedInUser = dataManager.getUsers().stream()
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);

            if (loggedInUser != null) {
                System.out.println("Επιτυχής είσοδος: " + loggedInUser.getRole());
                openDashboard(primaryStage); // Μετάβαση στο Dashboard
            } else {
                new Alert(Alert.AlertType.ERROR, "Λάθος στοιχεία!").show();
            }
        });

        VBox layout = new VBox(10, label, userField, passField, loginBtn);
        layout.setAlignment(Pos.CENTER);
        Scene scene = new Scene(layout, 350, 250);
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void openDashboard(Stage stage) {
        
        Label welcomeLabel = new Label("Καλώς ήρθατε, " + loggedInUser.getFirstName());
        VBox layout = new VBox(20, welcomeLabel);
        
        // Αν είναι Admin, πρόσθεσε κουμπί διαχείρισης χρηστών
        if (loggedInUser.getRole().equals("Admin")) {
            layout.getChildren().add(new Button("Διαχείριση Χρηστών"));
        }

        Scene scene = new Scene(layout, 600, 400);
        stage.setScene(scene);
    }

    @Override
    public void stop() {
        dataManager.saveData(); 
    }

    public static void main(String[] args) {
        launch(args);
    }
}
