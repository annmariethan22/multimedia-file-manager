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
        // Τίτλος παραθύρου σύμφωνα με την εκφώνηση
        stage.setTitle("MediaLab Documents");

        // ==========================================
        // ΜΕΡΟΣ 1: Συγκεντρωτικές Πληροφορίες (Πάνω)
        // ==========================================
        VBox statsBox = new VBox(10);
        statsBox.setStyle("-fx-padding: 15; -fx-background-color: #e8e8e8; -fx-border-color: #cccccc; -fx-border-width: 0 0 2 0;");
        
        Label statsTitle = new Label("Συγκεντρωτικές Πληροφορίες");
        statsTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        // Υπολογισμός των 3 στατιστικών που ζητάει η εκφώνηση
        int totalCats = dataManager.getCategories().size();
        int totalDocs = dataManager.getDocuments().size();
        int followedDocs = loggedInUser.getFollowedDocuments().size();

        Label totalCatsLabel = new Label("• Συνολικές Κατηγορίες Συστήματος: " + totalCats);
        Label totalDocsLabel = new Label("• Συνολικά Έγγραφα Συστήματος: " + totalDocs);
        Label followedDocsLabel = new Label("• Έγγραφα που παρακολουθείτε: " + followedDocs);
        
        statsBox.getChildren().addAll(statsTitle, totalCatsLabel, totalDocsLabel, followedDocsLabel);

      
        VBox mainContentBox = new VBox(15);
        mainContentBox.setStyle("-fx-padding: 20;");
        
        Label welcomeLabel = new Label("Χρήστης: " + loggedInUser.getFirstName() + " (" + loggedInUser.getRole() + ")");
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        
        TextField searchField = new TextField();
        searchField.setPromptText("Αναζήτηση ανά τίτλο ή συγγραφέα...");

        
        TableView<Document> table = new TableView<>();
        
        TableColumn<Document, String> titleCol = new TableColumn<>("Τίτλος Έργου");
        titleCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(200);
        
        TableColumn<Document, String> authorCol = new TableColumn<>("Συγγραφέας");
        authorCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("authorName"));
        authorCol.setPrefWidth(150);

        TableColumn<Document, String> categoryCol = new TableColumn<>("Κατηγορία");
        categoryCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(150);

        table.getColumns().addAll(titleCol, authorCol, categoryCol);
        
        
        javafx.collections.ObservableList<Document> masterData = 
            javafx.collections.FXCollections.observableArrayList(dataManager.getDocuments());
        table.setItems(masterData);

        
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

        mainContentBox.getChildren().addAll(welcomeLabel, new Label("Αναζήτηση Εγγράφων:"), searchField, table);

        
        javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox(10);
        
        Button readBtn = new Button("Ανάγνωση (Προβολή)");
        buttonBox.getChildren().add(readBtn);

        if (loggedInUser.getRole().equals("Admin") || loggedInUser.getRole().equals("Author")) {
            Button editBtn = new Button("Επεξεργασία (Νέα Έκδοση)");
            buttonBox.getChildren().add(editBtn);
        }
        
        if (loggedInUser.getRole().equals("Admin")) {
            Button adminBtn = new Button("Διαχείριση Συστήματος (Admin)");
            buttonBox.getChildren().add(adminBtn);
        }
        
        mainContentBox.getChildren().add(buttonBox);

       
        javafx.scene.layout.BorderPane root = new javafx.scene.layout.BorderPane();
        root.setTop(statsBox);       
        root.setCenter(mainContentBox); 

        Scene scene = new Scene(root, 800, 600);
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
