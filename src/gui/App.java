package gui;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.DataManager;
import classes.User;
import classes.Document;
import javafx.collections.transformation.FilteredList;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import classes.Category;


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
    LoginController controller = new LoginController(dataManager);
    loggedInUser = controller.handleLogin(userField.getText(), passField.getText());

    if (loggedInUser != null) {
        openDashboard(primaryStage);
    }
    
});

        VBox layout = new VBox(10, label, userField, passField, loginBtn);
        layout.setAlignment(Pos.CENTER);
        Scene scene = new Scene(layout, 350, 250);
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void openDashboard(Stage stage) {
    VBox layout = new VBox(15);
    layout.setStyle("-fx-padding: 20;");
    layout.setAlignment(Pos.TOP_CENTER);

    Label welcomeLabel = new Label("Σύστημα Θεατρικών Έργων | Χρήστης: " + loggedInUser.getFirstName());
    welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

    
    TableView<Document> table = new TableView<>();
    
    TableColumn<Document, String> titleCol = new TableColumn<>("Τίτλος Έργου");
    titleCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("title"));
    
    TableColumn<Document, String> authorCol = new TableColumn<>("Συγγραφέας");
    authorCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("authorName"));

    table.getColumns().addAll(titleCol, authorCol);
    
    
    javafx.collections.ObservableList<Document> masterData = 
        javafx.collections.FXCollections.observableArrayList(dataManager.getDocuments());
    table.setItems(masterData);

    
    TextField searchField = new TextField();
    searchField.setPromptText("Αναζήτηση ανά τίτλο ή συγγραφέα...");
    searchField.textProperty().addListener((observable, oldValue, newValue) -> {
        javafx.collections.transformation.FilteredList<Document> filteredData = new javafx.collections.transformation.FilteredList<>(masterData, p -> true);
        filteredData.setPredicate(doc -> {
            if (newValue == null || newValue.isEmpty()) return true;
            String lowerCaseFilter = newValue.toLowerCase();
            return doc.getTitle().toLowerCase().contains(lowerCaseFilter) || 
                   doc.getAuthorName().toLowerCase().contains(lowerCaseFilter);
        });
        table.setItems(filteredData);
    });

    layout.getChildren().addAll(welcomeLabel, new Label("Αναζήτηση:"), searchField, table);

    
    if (loggedInUser.getRole().equals("Admin")) {
        Button adminBtn = new Button("Διαχείριση Χρηστών & Κατηγοριών");
        layout.getChildren().add(adminBtn);
    }

    Scene scene = new Scene(layout, 800, 500);
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
