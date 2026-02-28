package gui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.DataManager;
import classes.User;
import classes.Document;


public class App extends javafx.application.Application {
    private DataManager dataManager = new DataManager();
    private User loggedInUser;

    @Override
    public void start(Stage primaryStage) throws Exception {
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
                StringBuilder notifications = new StringBuilder();
                for (String followedTitle : loggedInUser.getFollowedDocuments()) {
                    for (Document doc : dataManager.getDocuments()) {
                        if (doc.getTitle().equals(followedTitle) && doc.getCurrentVersionNumber() > 1) {
                            notifications.append("• ").append(followedTitle)
                                         .append(" (Νέα Έκδοση: v").append(doc.getCurrentVersionNumber()).append(")\n");
                        }
                    }
                }

                if (notifications.length() > 0) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Ειδοποιήσεις Συστήματος");
                    alert.setHeaderText("Έγγραφα που παρακολουθείτε ενημερώθηκαν!");
                    alert.setContentText(notifications.toString());
                    alert.showAndWait();
                }
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
        
        stage.setTitle("MediaLab Documents");

        
        VBox statsBox = new VBox(10);
        statsBox.setStyle("-fx-padding: 15; -fx-background-color: #e8e8e8; -fx-border-color: #cccccc; -fx-border-width: 0 0 2 0;");
        
        Label statsTitle = new Label("Συγκεντρωτικές Πληροφορίες");
        statsTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        
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
        categoryCol.setPrefWidth(130);

        
        TableColumn<Document, String> dateCol = new TableColumn<>("Ημερομηνία");
        dateCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("creationDate"));
        dateCol.setPrefWidth(100);

        TableColumn<Document, Integer> versionCol = new TableColumn<>("Έκδοση");
        versionCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("currentVersionNumber"));
        versionCol.setPrefWidth(70);

        @SuppressWarnings("unchecked")
        TableColumn<Document, ?>[] columns = new TableColumn[] { titleCol, authorCol, categoryCol, dateCol, versionCol };
        table.getColumns().addAll(columns);
        
        
        
        javafx.collections.ObservableList<Document> masterData = javafx.collections.FXCollections.observableArrayList();
        
        for (Document doc : dataManager.getDocuments()) {
            if (loggedInUser.getRole().equals("Admin")) {
                
                masterData.add(doc);
            } else {
                
                if (loggedInUser.getAuthorizedCategories() != null && loggedInUser.getAuthorizedCategories().contains(doc.getCategory())) {
                    masterData.add(doc);
                }
            }
        }
        
        table.setItems(masterData);
        

        
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            javafx.collections.transformation.FilteredList<Document> filteredData = new javafx.collections.transformation.FilteredList<>(masterData, p -> true);
            filteredData.setPredicate(doc -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return doc.getTitle().toLowerCase().contains(lowerCaseFilter) || 
                       doc.getAuthorName().toLowerCase().contains(lowerCaseFilter) ||
                       doc.getCategory().toLowerCase().contains(lowerCaseFilter);
            });
            table.setItems(filteredData);
        });

        mainContentBox.getChildren().addAll(welcomeLabel, new Label("Αναζήτηση Εγγράφων:"), searchField, table);

        
        javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox(10);
        
        Button readBtn = new Button("Ανάγνωση (Προβολή)");
        readBtn.setOnAction(e -> {
            
            Document selectedDoc = table.getSelectionModel().getSelectedItem();
            
            if (selectedDoc == null) {
                
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Προσοχή");
                alert.setHeaderText(null);
                alert.setContentText("Παρακαλώ επιλέξτε ένα έργο από τον πίνακα για ανάγνωση!");
                alert.showAndWait();
                return;
            }

            
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Ανάγνωση: " + selectedDoc.getTitle());
            info.setHeaderText("Συγγραφέας: " + selectedDoc.getAuthorName() + " | Κατηγορία: " + selectedDoc.getCategory() + "\nΗμερομηνία Δημιουργίας: " + selectedDoc.getCreationDate() + " | Τρέχουσα Έκδοση: v" + selectedDoc.getCurrentVersionNumber());
            
            StringBuilder contentToDisplay = new StringBuilder();
            java.util.List<String> allVersions = selectedDoc.getVersions();
            
            if (allVersions == null || allVersions.isEmpty()) {
                contentToDisplay.append("Δεν υπάρχει κείμενο για αυτό το έργο.");
            } else {
               
                if (loggedInUser.getRole().equals("Simple User")) {
                    contentToDisplay.append("--- Τρέχουσα Έκδοση (v").append(selectedDoc.getCurrentVersionNumber()).append(") ---\n");
                    contentToDisplay.append(allVersions.get(allVersions.size() - 1));
                } else {
                    
                    int start = Math.max(0, allVersions.size() - 3);
                    for (int i = allVersions.size() - 1; i >= start; i--) {
                        contentToDisplay.append("=== Έκδοση v").append(i + 1).append(" ===\n");
                        contentToDisplay.append(allVersions.get(i)).append("\n\n");
                    }
                }
            }
            
            
            info.setContentText(contentToDisplay.toString());
            info.getDialogPane().setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
            info.getDialogPane().setMinWidth(500);
            
            info.showAndWait();
        });
        buttonBox.getChildren().add(readBtn);
        
        Button followBtn = new Button(" Παρακολούθηση");
        buttonBox.getChildren().add(followBtn);
        
        followBtn.setOnAction(e -> {
            Document selectedDoc = table.getSelectionModel().getSelectedItem();
            if (selectedDoc == null) {
                new Alert(Alert.AlertType.WARNING, "Επιλέξτε ένα έργο από τον πίνακα!").showAndWait();
                return;
            }

            String docTitle = selectedDoc.getTitle();
            java.util.List<String> userFollows = loggedInUser.getFollowedDocuments();

            if (userFollows.contains(docTitle)) {
                userFollows.remove(docTitle);
                new Alert(Alert.AlertType.INFORMATION, "Σταματήσατε να παρακολουθείτε το: " + docTitle).showAndWait();
            } else {
                userFollows.add(docTitle);
                new Alert(Alert.AlertType.INFORMATION, "Ξεκινήσατε να παρακολουθείτε το: " + docTitle).showAndWait();
            }
            
            followedDocsLabel.setText("• Έγγραφα που παρακολουθείτε: " + userFollows.size());
        });

        if (loggedInUser.getRole().equals("Admin") || loggedInUser.getRole().equals("Author")) {
            
            Button editBtn = new Button("Επεξεργασία (Νέα Έκδοση)");
            buttonBox.getChildren().add(editBtn);
            
            editBtn.setOnAction(e -> {
                Document selectedDoc = table.getSelectionModel().getSelectedItem();
                if (selectedDoc == null) {
                    new Alert(Alert.AlertType.WARNING, "Παρακαλώ επιλέξτε ένα έργο από τον πίνακα για επεξεργασία!").showAndWait();
                    return;
                }

                Dialog<String> dialog = new Dialog<>();
                dialog.setTitle("Νέα Έκδοση: " + selectedDoc.getTitle());
                dialog.setHeaderText("Επεξεργασία Έργου. Η τρέχουσα έκδοση είναι η v" + selectedDoc.getCurrentVersionNumber() + ".\nΓράψτε το κείμενο για τη ΝΕΑ έκδοση:");

                ButtonType saveButtonType = new ButtonType("Αποθήκευση", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

                TextArea textArea = new TextArea();
                textArea.setWrapText(true);
                textArea.setPrefHeight(300);
                textArea.setPrefWidth(400);
                
                java.util.List<String> versions = selectedDoc.getVersions();
                if (versions != null && !versions.isEmpty()) {
                    textArea.setText(versions.get(versions.size() - 1));
                }

                dialog.getDialogPane().setContent(textArea);

                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == saveButtonType) return textArea.getText();
                    return null;
                });

                dialog.showAndWait().ifPresent(newContent -> {
                    if (newContent.trim().isEmpty()) {
                        new Alert(Alert.AlertType.ERROR, "Το κείμενο δεν μπορεί να είναι κενό!").showAndWait();
                        return;
                    }
                    selectedDoc.getVersions().add(newContent);
                    selectedDoc.setCurrentVersionNumber(selectedDoc.getCurrentVersionNumber() + 1);
                    table.refresh();
                    new Alert(Alert.AlertType.INFORMATION, "Επιτυχία! Δημιουργήθηκε η έκδοση v" + selectedDoc.getCurrentVersionNumber()).showAndWait();
                });
            }); 

            
            Button newDocBtn = new Button("Νέο Έργο");
            buttonBox.getChildren().add(newDocBtn);
            
            newDocBtn.setOnAction(e -> {
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Νέο Έργο");
                dialog.setHeaderText("Δημιουργία Νέου Έργου\nΣυμπληρώστε τα υποχρεωτικά πεδία:");
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
                grid.setHgap(10); grid.setVgap(10);
                grid.setPadding(new javafx.geometry.Insets(20, 50, 10, 10));

                TextField titleField = new TextField();
                titleField.setPromptText("Τίτλος");

                
                TextField authorField = new TextField(loggedInUser.getFirstName() + " " + loggedInUser.getLastName());

                ComboBox<String> categoryBox = new ComboBox<>();
                
                if (loggedInUser.getRole().equals("Admin")) {
                    for (classes.Category c : dataManager.getCategories()) categoryBox.getItems().add(c.getName());
                } else {
                    categoryBox.getItems().addAll(loggedInUser.getAuthorizedCategories());
                }
                if (!categoryBox.getItems().isEmpty()) categoryBox.getSelectionModel().selectFirst();

                DatePicker datePicker = new DatePicker(java.time.LocalDate.now());

                
                TextArea initialTextArea = new TextArea();
                initialTextArea.setPromptText("Γράψτε το αρχικό κείμενο του έργου εδώ...");
                initialTextArea.setPrefRowCount(6);
                initialTextArea.setWrapText(true);

                grid.add(new Label("Τίτλος Έργου:"), 0, 0); grid.add(titleField, 1, 0);
                grid.add(new Label("Συγγραφέας:"), 0, 1); grid.add(authorField, 1, 1);
                grid.add(new Label("Κατηγορία:"), 0, 2); grid.add(categoryBox, 1, 2);
                grid.add(new Label("Ημερομηνία:"), 0, 3); grid.add(datePicker, 1, 3);
                grid.add(new Label("Κείμενο:"), 0, 4); grid.add(initialTextArea, 1, 4);

                dialog.getDialogPane().setContent(grid);

                dialog.showAndWait().ifPresent(result -> {
                    if (result == ButtonType.OK) {
                        if (titleField.getText().trim().isEmpty() || initialTextArea.getText().trim().isEmpty() || categoryBox.getValue() == null) {
                            new Alert(Alert.AlertType.ERROR, "Ο Τίτλος, η Κατηγορία και το Κείμενο είναι υποχρεωτικά!").showAndWait();
                            return;
                        }
                        
                        Document doc = new Document();
                        doc.setTitle(titleField.getText());
                        doc.setAuthorName(authorField.getText());
                        doc.setCategory(categoryBox.getValue());
                        doc.setCurrentVersionNumber(1);
                        doc.setCreationDate(datePicker.getValue() != null ? datePicker.getValue().toString() : java.time.LocalDate.now().toString());
                        doc.setVersions(new java.util.ArrayList<>());
                        doc.getVersions().add(initialTextArea.getText()); 
                        
                        dataManager.getDocuments().add(doc);
                        masterData.add(doc); 
                        totalDocsLabel.setText("• Συνολικά Έγγραφα Συστήματος: " + dataManager.getDocuments().size());
                    }
                });
            }); 
            
           
            Button deleteDocBtn = new Button("Διαγραφή");
            buttonBox.getChildren().add(deleteDocBtn);
            
            deleteDocBtn.setOnAction(e -> {
                Document selectedDoc = table.getSelectionModel().getSelectedItem();
                if (selectedDoc == null) {
                    new Alert(Alert.AlertType.WARNING, "Επιλέξτε ένα έργο για διαγραφή!").showAndWait();
                    return;
                }

                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Σίγουρα θέλετε να διαγράψετε το έργο '" + selectedDoc.getTitle() + "';\n(Η διαγραφή είναι οριστική)");
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        
                        dataManager.getDocuments().remove(selectedDoc);
                        masterData.remove(selectedDoc);
                        
                        
                        for (classes.User u : dataManager.getUsers()) {
                            if (u.getFollowedDocuments() != null) {
                                u.getFollowedDocuments().remove(selectedDoc.getTitle());
                            }
                        }
                        
                      
                        totalDocsLabel.setText("• Συνολικά Έγγραφα Συστήματος: " + dataManager.getDocuments().size());
                        followedDocsLabel.setText("• Έγγραφα που παρακολουθείτε: " + loggedInUser.getFollowedDocuments().size());
                        new Alert(Alert.AlertType.INFORMATION, "Το έργο διαγράφηκε επιτυχώς!").showAndWait();
                    }
                });
            });
            
        } 

        
        
        if (loggedInUser.getRole().equals("Admin")) {
            Button adminBtn = new Button("Διαχείριση Συστήματος (Admin)");
            buttonBox.getChildren().add(adminBtn);
            
            adminBtn.setOnAction(e -> openAdminPanel());
        }
        
        mainContentBox.getChildren().add(buttonBox);

        javafx.scene.layout.BorderPane root = new javafx.scene.layout.BorderPane();
        root.setTop(statsBox);       
        root.setCenter(mainContentBox); 

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
    } 

    
    private void openAdminPanel() {
        Stage adminStage = new Stage();
        adminStage.setTitle("Πάνελ Διαχειριστή - MediaLab");

        TabPane tabPane = new TabPane();

        
        Tab usersTab = new Tab("Χρήστες");
        usersTab.setClosable(false);
        VBox usersBox = new VBox(10);
        usersBox.setPadding(new javafx.geometry.Insets(15));

        ListView<String> usersList = new ListView<>();
        Runnable refreshUsers = () -> {
            usersList.getItems().clear();
            for (User u : dataManager.getUsers()) {
                usersList.getItems().add(u.getUsername() + " (" + u.getRole() + ") - " + u.getFirstName() + " " + u.getLastName());
            }
        };
        refreshUsers.run();

       
        javafx.scene.layout.HBox userBtns = new javafx.scene.layout.HBox(10);
        Button addUserBtn = new Button("Προσθήκη");
        Button editUserBtn = new Button("Επεξεργασία");
        Button delUserBtn = new Button("Διαγραφή");
        userBtns.getChildren().addAll(addUserBtn, editUserBtn, delUserBtn);

        
        editUserBtn.setOnAction(e -> {
            int idx = usersList.getSelectionModel().getSelectedIndex();
            if (idx < 0) {
                new Alert(Alert.AlertType.WARNING, "Επιλέξτε έναν χρήστη από τη λίστα πρώτα!").showAndWait();
                return;
            }
            
            User u = dataManager.getUsers().get(idx);
            if (u.getUsername().equals("medialab")) {
                new Alert(Alert.AlertType.ERROR, "Δεν μπορείτε να επεξεργαστείτε τον κεντρικό διαχειριστή!").showAndWait();
                return;
            }

            Dialog<User> dialog = new Dialog<>();
            dialog.setTitle("Επεξεργασία Χρήστη");
            dialog.setHeaderText("Τροποποίηση στοιχείων και δικαιωμάτων: " + u.getUsername());
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
            grid.setHgap(10); grid.setVgap(10);

            TextField fnField = new TextField(u.getFirstName());
            TextField lnField = new TextField(u.getLastName());
            PasswordField pwField = new PasswordField();
            pwField.setText(u.getPassword());
            
            ComboBox<String> roleBox = new ComboBox<>();
            roleBox.getItems().addAll("Simple User", "Author", "Admin");
            roleBox.setValue(u.getRole());

            
            ListView<String> catSelection = new ListView<>();
            catSelection.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            for (classes.Category c : dataManager.getCategories()) {
                catSelection.getItems().add(c.getName());
            }
            catSelection.setPrefHeight(100);
            
            if (u.getAuthorizedCategories() != null) {
                for (String cat : u.getAuthorizedCategories()) {
                    catSelection.getSelectionModel().select(cat);
                }
            }

            grid.add(new Label("Όνομα:"), 0, 0); grid.add(fnField, 1, 0);
            grid.add(new Label("Επώνυμο:"), 0, 1); grid.add(lnField, 1, 1);
            grid.add(new Label("Password:"), 0, 2); grid.add(pwField, 1, 2);
            grid.add(new Label("Ρόλος:"), 0, 3); grid.add(roleBox, 1, 3);
            grid.add(new Label("Κατηγορίες\n(Ctrl+Click):"), 0, 4); grid.add(catSelection, 1, 4);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    if (pwField.getText().isEmpty() || fnField.getText().isEmpty() || lnField.getText().isEmpty()) {
                        new Alert(Alert.AlertType.ERROR, "Συμπληρώστε όλα τα πεδία!").showAndWait();
                        return null;
                    }
                    if (catSelection.getSelectionModel().getSelectedItems().isEmpty() && !roleBox.getValue().equals("Admin")) {
                        new Alert(Alert.AlertType.ERROR, "Πρέπει να αναθέσετε τουλάχιστον 1 κατηγορία!").showAndWait();
                        return null;
                    }
                    // Ενημέρωση του χρήστη με τα νέα στοιχεία
                    u.setPassword(pwField.getText());
                    u.setFirstName(fnField.getText());
                    u.setLastName(lnField.getText());
                    u.setRole(roleBox.getValue());
                    u.setAuthorizedCategories(new java.util.ArrayList<>(catSelection.getSelectionModel().getSelectedItems()));
                    return u;
                }
                return null;
            });

            dialog.showAndWait().ifPresent(editedUser -> {
                refreshUsers.run();
                new Alert(Alert.AlertType.INFORMATION, "Τα στοιχεία του χρήστη ενημερώθηκαν!").showAndWait();
            });
        });

        addUserBtn.setOnAction(e -> {
            Dialog<User> dialog = new Dialog<>();
            dialog.setTitle("Νέος Χρήστης");
            dialog.setHeaderText("Εισάγετε τα στοιχεία του νέου χρήστη:");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
            grid.setHgap(10); grid.setVgap(10);

            TextField unField = new TextField();
            PasswordField pwField = new PasswordField();
            TextField fnField = new TextField();
            TextField lnField = new TextField();
            
            ComboBox<String> roleBox = new ComboBox<>();
            roleBox.getItems().addAll("Simple User", "Author", "Admin");
            roleBox.getSelectionModel().selectFirst();

            
            ListView<String> catSelection = new ListView<>();
            catSelection.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            for (classes.Category c : dataManager.getCategories()) {
                catSelection.getItems().add(c.getName());
            }
            catSelection.setPrefHeight(100);

            grid.add(new Label("Όνομα:"), 0, 0); grid.add(fnField, 1, 0);
            grid.add(new Label("Επώνυμο:"), 0, 1); grid.add(lnField, 1, 1);
            grid.add(new Label("Username:"), 0, 2); grid.add(unField, 1, 2);
            grid.add(new Label("Password:"), 0, 3); grid.add(pwField, 1, 3);
            grid.add(new Label("Ρόλος:"), 0, 4); grid.add(roleBox, 1, 4);
            grid.add(new Label("Κατηγορίες\n(Ctrl+Click):"), 0, 5); grid.add(catSelection, 1, 5);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    if (unField.getText().isEmpty() || pwField.getText().isEmpty() || fnField.getText().isEmpty() || lnField.getText().isEmpty()) {
                        new Alert(Alert.AlertType.ERROR, "Συμπληρώστε όλα τα πεδία!").showAndWait();
                        return null;
                    }
                    if (catSelection.getSelectionModel().getSelectedItems().isEmpty() && !roleBox.getValue().equals("Admin")) {
                        new Alert(Alert.AlertType.ERROR, "Πρέπει να αναθέσετε τουλάχιστον 1 κατηγορία!").showAndWait();
                        return null;
                    }
                    User u = new User();
                    u.setUsername(unField.getText());
                    u.setPassword(pwField.getText());
                    u.setFirstName(fnField.getText());
                    u.setLastName(lnField.getText());
                    u.setRole(roleBox.getValue());
                    u.setAuthorizedCategories(new java.util.ArrayList<>(catSelection.getSelectionModel().getSelectedItems()));
                    u.setFollowedDocuments(new java.util.ArrayList<>());
                    return u;
                }
                return null;
            });

            dialog.showAndWait().ifPresent(newUser -> {
                dataManager.getUsers().add(newUser);
                refreshUsers.run();
                new Alert(Alert.AlertType.INFORMATION, "Ο χρήστης δημιουργήθηκε επιτυχώς!").showAndWait();
            });
        });

        delUserBtn.setOnAction(e -> {
            int idx = usersList.getSelectionModel().getSelectedIndex();
            if (idx >= 0) {
                User u = dataManager.getUsers().get(idx);
                if (u.getUsername().equals("medialab")) {
                    new Alert(Alert.AlertType.ERROR, "Δεν μπορείτε να διαγράψετε τον κεντρικό διαχειριστή!").showAndWait();
                    return;
                }
                dataManager.getUsers().remove(u);
                refreshUsers.run();
            }
        });

        usersBox.getChildren().addAll(new Label("Λίστα Χρηστών Συστήματος:"), usersList, userBtns);
        usersTab.setContent(usersBox);

        
        Tab catTab = new Tab("Κατηγορίες");
        catTab.setClosable(false);
        VBox catBox = new VBox(10);
        catBox.setPadding(new javafx.geometry.Insets(15));

        ListView<String> catList = new ListView<>();
        Runnable refreshCats = () -> {
            catList.getItems().clear();
            for (classes.Category c : dataManager.getCategories()) {
                catList.getItems().add(c.getName());
            }
        };
        refreshCats.run();

        javafx.scene.layout.HBox catBtns = new javafx.scene.layout.HBox(10);
        Button addCatBtn = new Button("Νέα");
        Button editCatBtn = new Button("Μετονομασία");
        Button delCatBtn = new Button("Διαγραφή");
        catBtns.getChildren().addAll(addCatBtn, editCatBtn, delCatBtn);

        addCatBtn.setOnAction(e -> {
            TextInputDialog d = new TextInputDialog();
            d.setTitle("Νέα Κατηγορία");
            d.setHeaderText("Δώστε το όνομα της νέας κατηγορίας:");
            d.showAndWait().ifPresent(name -> {
                if (!name.trim().isEmpty()) {
                    classes.Category c = new classes.Category();
                    c.setName(name);
                    dataManager.getCategories().add(c);
                    refreshCats.run();
                }
            });
        });

        editCatBtn.setOnAction(e -> {
            int idx = catList.getSelectionModel().getSelectedIndex();
            if (idx >= 0) {
                classes.Category oldCat = dataManager.getCategories().get(idx);
                TextInputDialog d = new TextInputDialog(oldCat.getName());
                d.setTitle("Μετονομασία Κατηγορίας");
                d.setHeaderText("Νέο όνομα για την κατηγορία: " + oldCat.getName());
                d.showAndWait().ifPresent(newName -> {
                    if (!newName.trim().isEmpty()) {
                        String oldName = oldCat.getName();
                        oldCat.setName(newName);
                        
                        
                        for (Document doc : dataManager.getDocuments()) {
                            if (doc.getCategory().equals(oldName)) doc.setCategory(newName);
                        }
                        
                        for (User u : dataManager.getUsers()) {
                            if (u.getAuthorizedCategories() != null && u.getAuthorizedCategories().contains(oldName)) {
                                u.getAuthorizedCategories().remove(oldName);
                                u.getAuthorizedCategories().add(newName);
                            }
                        }
                        refreshCats.run();
                    }
                });
            }
        });

        delCatBtn.setOnAction(e -> {
            int idx = catList.getSelectionModel().getSelectedIndex();
            if (idx >= 0) {
                classes.Category c = dataManager.getCategories().get(idx);
                String catName = c.getName();
                
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Διαγραφή της κατηγορίας '" + catName + "';\nΠΡΟΣΟΧΗ: Θα διαγραφούν ΟΛΑ τα έγγραφά της!");
                confirm.showAndWait().ifPresent(res -> {
                    if (res == ButtonType.OK) {
                        dataManager.getCategories().remove(c);
                        
                        java.util.List<Document> toDelete = new java.util.ArrayList<>();
                        for (Document doc : dataManager.getDocuments()) {
                            if (doc.getCategory().equals(catName)) toDelete.add(doc);
                        }
                        dataManager.getDocuments().removeAll(toDelete);
                        
                        
                        for (Document delDoc : toDelete) {
                            for (User u : dataManager.getUsers()) {
                                if (u.getFollowedDocuments() != null) u.getFollowedDocuments().remove(delDoc.getTitle());
                            }
                        }
                       
                        for (User u : dataManager.getUsers()) {
                            if (u.getAuthorizedCategories() != null) u.getAuthorizedCategories().remove(catName);
                        }
                        
                        refreshCats.run();
                        new Alert(Alert.AlertType.INFORMATION, "Η κατηγορία και τα έγγραφά της διαγράφηκαν!").showAndWait();
                    }
                });
            }
        });

        catBox.getChildren().addAll(new Label("Λίστα Κατηγοριών Συστήματος:"), catList, catBtns);
        catTab.setContent(catBox);

        tabPane.getTabs().addAll(usersTab, catTab);

        Scene adminScene = new Scene(tabPane, 450, 450);
        adminStage.setScene(adminScene);
        adminStage.show();
    }

    @Override
    public void stop() {
        dataManager.saveData(); 
    }

    public static void main(String[] args) {
        launch(args);
    }
} 