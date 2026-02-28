package gui;

import classes.Document;
import classes.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import services.DataManager;
import services.DocumentService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class App extends javafx.application.Application {
    private final DataManager dataManager = new DataManager();
    private final DocumentService documentService = new DocumentService();
    private User loggedInUser;

    @Override
    public void start(Stage primaryStage) {
        dataManager.loadData();

        primaryStage.setTitle("Θεατρικό Σύστημα - Είσοδος");

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
                showUpdateNotificationsIfNeeded();
                openDashboard(primaryStage);
            }
        });

        VBox layout = new VBox(10, label, userField, passField, loginBtn);
        layout.setAlignment(Pos.CENTER);
        Scene scene = new Scene(layout, 350, 250);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showUpdateNotificationsIfNeeded() {
        StringBuilder notifications = new StringBuilder();

        for (String followedTitle : loggedInUser.getFollowedDocuments()) {
            Document doc = dataManager.getDocuments().stream()
                    .filter(d -> d.getTitle().equals(followedTitle))
                    .findFirst()
                    .orElse(null);

            if (doc == null) {
                continue;
            }

            int lastSeen = loggedInUser.getLastSeenVersionForDocument(followedTitle);
            if (doc.getCurrentVersionNumber() > lastSeen) {
                notifications.append("• ").append(followedTitle)
                        .append(" (Τελευταία: v").append(doc.getCurrentVersionNumber())
                        .append(", έχετε δει: v").append(lastSeen).append(")\n");
            }
        }

        if (notifications.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Ειδοποιήσεις Συστήματος");
            alert.setHeaderText("Έγγραφα που παρακολουθείτε ενημερώθηκαν!");
            alert.setContentText(notifications.toString());
            alert.showAndWait();
        }
    }

    private void openDashboard(Stage stage) {
        stage.setTitle("MediaLab Documents");

        VBox statsBox = new VBox(10);
        statsBox.setStyle("-fx-padding: 15; -fx-background-color: #e8e8e8; -fx-border-color: #cccccc; -fx-border-width: 0 0 2 0;");

        Label statsTitle = new Label("Συγκεντρωτικές Πληροφορίες");
        statsTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label totalCatsLabel = new Label();
        Label totalDocsLabel = new Label();
        Label followedDocsLabel = new Label();

        Runnable refreshStats = () -> {
            totalCatsLabel.setText("• Συνολικές Κατηγορίες Συστήματος: " + dataManager.getCategories().size());
            totalDocsLabel.setText("• Συνολικά Έγγραφα Συστήματος: " + dataManager.getDocuments().size());
            followedDocsLabel.setText("• Έγγραφα που παρακολουθείτε: " + loggedInUser.getFollowedDocuments().size());
        };
        refreshStats.run();

        statsBox.getChildren().addAll(statsTitle, totalCatsLabel, totalDocsLabel, followedDocsLabel);

        VBox mainContentBox = new VBox(15);
        mainContentBox.setStyle("-fx-padding: 20;");

        Label welcomeLabel = new Label("Χρήστης: " + loggedInUser.getFirstName() + " (" + loggedInUser.getRole() + ")");
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TextField searchField = new TextField();
        searchField.setPromptText("Αναζήτηση ανά τίτλο ή συγγραφέα ή κατηγορία...");

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

        table.getColumns().addAll(titleCol, authorCol, categoryCol, dateCol, versionCol);

        ObservableList<Document> masterData = FXCollections.observableArrayList();
        Runnable refreshDocumentTable = () -> {
            masterData.clear();
            for (Document doc : dataManager.getDocuments()) {
                if ("Admin".equals(loggedInUser.getRole())) {
                    masterData.add(doc);
                } else if (loggedInUser.getAuthorizedCategories() != null &&
                        loggedInUser.getAuthorizedCategories().contains(doc.getCategory())) {
                    masterData.add(doc);
                }
            }
        };
        refreshDocumentTable.run();

        FilteredList<Document> filteredData = new FilteredList<>(masterData, p -> true);
        table.setItems(filteredData);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            String filter = newValue == null ? "" : newValue.toLowerCase();
            filteredData.setPredicate(doc -> {
                if (filter.isEmpty()) return true;
                return doc.getTitle().toLowerCase().contains(filter)
                        || doc.getAuthorName().toLowerCase().contains(filter)
                        || doc.getCategory().toLowerCase().contains(filter);
            });
        });

        HBox buttonBox = new HBox(10);

        Button readBtn = new Button("Ανάγνωση (Προβολή)");
        readBtn.setOnAction(e -> {
            Document selectedDoc = table.getSelectionModel().getSelectedItem();
            if (selectedDoc == null) {
                new Alert(Alert.AlertType.WARNING, "Παρακαλώ επιλέξτε ένα έργο από τον πίνακα για ανάγνωση!").showAndWait();
                return;
            }

            showDocumentReadDialog(selectedDoc);
            loggedInUser.markDocumentVersionAsSeen(selectedDoc.getTitle(), selectedDoc.getCurrentVersionNumber());
        });
        buttonBox.getChildren().add(readBtn);

        Button followBtn = new Button("Παρακολούθηση/Κατάργηση");
        buttonBox.getChildren().add(followBtn);

        ListView<String> watchListView = new ListView<>();
        watchListView.setPrefHeight(160);
        watchListView.setPlaceholder(new Label("Δεν παρακολουθείτε κάποιο έγγραφο."));

        Runnable refreshWatchList = () -> {
            watchListView.getItems().clear();
            for (String title : loggedInUser.getFollowedDocuments()) {
                Document d = dataManager.getDocuments().stream().filter(doc -> doc.getTitle().equals(title)).findFirst().orElse(null);
                if (d != null) {
                    watchListView.getItems().add(title + " (τρέχουσα v" + d.getCurrentVersionNumber() + ")");
                }
            }
        };
        refreshWatchList.run();

        followBtn.setOnAction(e -> {
            Document selectedDoc = table.getSelectionModel().getSelectedItem();
            if (selectedDoc == null) {
                new Alert(Alert.AlertType.WARNING, "Επιλέξτε ένα έργο από τον πίνακα!").showAndWait();
                return;
            }

            String docTitle = selectedDoc.getTitle();
            List<String> userFollows = loggedInUser.getFollowedDocuments();

            if (userFollows.contains(docTitle)) {
                loggedInUser.unfollowDocument(docTitle);
                new Alert(Alert.AlertType.INFORMATION, "Σταματήσατε να παρακολουθείτε το: " + docTitle).showAndWait();
            } else {
                loggedInUser.followDocument(docTitle);
                loggedInUser.markDocumentVersionAsSeen(docTitle, selectedDoc.getCurrentVersionNumber());
                new Alert(Alert.AlertType.INFORMATION, "Ξεκινήσατε να παρακολουθείτε το: " + docTitle).showAndWait();
            }

            refreshStats.run();
            refreshWatchList.run();
        });

        if ("Admin".equals(loggedInUser.getRole()) || "Author".equals(loggedInUser.getRole())) {
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
                dialog.setHeaderText("Γράψτε το κείμενο για τη ΝΕΑ έκδοση (τρέχουσα v" + selectedDoc.getCurrentVersionNumber() + "):");

                ButtonType saveButtonType = new ButtonType("Αποθήκευση", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

                TextArea textArea = new TextArea();
                textArea.setWrapText(true);
                textArea.setPrefHeight(300);
                textArea.setPrefWidth(400);

                List<String> versions = selectedDoc.getVersions();
                if (versions != null && !versions.isEmpty()) {
                    textArea.setText(versions.get(versions.size() - 1));
                }

                dialog.getDialogPane().setContent(textArea);
                dialog.setResultConverter(dialogButton -> dialogButton == saveButtonType ? textArea.getText() : null);

                dialog.showAndWait().ifPresent(newContent -> {
                    if (newContent.trim().isEmpty()) {
                        new Alert(Alert.AlertType.ERROR, "Το κείμενο δεν μπορεί να είναι κενό!").showAndWait();
                        return;
                    }
                    documentService.addVersion(selectedDoc, newContent);
                    table.refresh();
                    refreshWatchList.run();
                    new Alert(Alert.AlertType.INFORMATION, "Επιτυχία! Δημιουργήθηκε η έκδοση v" + selectedDoc.getCurrentVersionNumber()).showAndWait();
                });
            });

            Button newDocBtn = new Button("Νέο Έργο");
            buttonBox.getChildren().add(newDocBtn);

            newDocBtn.setOnAction(e -> {
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Νέο Έργο");
                dialog.setHeaderText("Δημιουργία Νέου Έργου");
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(20, 50, 10, 10));

                TextField titleField = new TextField();
                TextField authorField = new TextField(loggedInUser.getFirstName() + " " + loggedInUser.getLastName());

                ComboBox<String> categoryBox = new ComboBox<>();
                if ("Admin".equals(loggedInUser.getRole())) {
                    for (classes.Category c : dataManager.getCategories()) categoryBox.getItems().add(c.getName());
                } else {
                    categoryBox.getItems().addAll(loggedInUser.getAuthorizedCategories());
                }
                if (!categoryBox.getItems().isEmpty()) categoryBox.getSelectionModel().selectFirst();

                DatePicker datePicker = new DatePicker(LocalDate.now());
                TextArea initialTextArea = new TextArea();
                initialTextArea.setPromptText("Γράψτε το αρχικό κείμενο του έργου εδώ...");
                initialTextArea.setPrefRowCount(6);
                initialTextArea.setWrapText(true);

                grid.add(new Label("Τίτλος Έργου:"), 0, 0);
                grid.add(titleField, 1, 0);
                grid.add(new Label("Συγγραφέας:"), 0, 1);
                grid.add(authorField, 1, 1);
                grid.add(new Label("Κατηγορία:"), 0, 2);
                grid.add(categoryBox, 1, 2);
                grid.add(new Label("Ημερομηνία:"), 0, 3);
                grid.add(datePicker, 1, 3);
                grid.add(new Label("Κείμενο:"), 0, 4);
                grid.add(initialTextArea, 1, 4);

                dialog.getDialogPane().setContent(grid);

                dialog.showAndWait().ifPresent(result -> {
                    if (result == ButtonType.OK) {
                        if (titleField.getText().trim().isEmpty() || initialTextArea.getText().trim().isEmpty() || categoryBox.getValue() == null) {
                            new Alert(Alert.AlertType.ERROR, "Ο Τίτλος, η Κατηγορία και το Κείμενο είναι υποχρεωτικά!").showAndWait();
                            return;
                        }

                        Document doc = documentService.createDocument(
                                titleField.getText(),
                                authorField.getText(),
                                categoryBox.getValue(),
                                datePicker.getValue(),
                                initialTextArea.getText()
                        );

                        dataManager.getDocuments().add(doc);
                        refreshDocumentTable.run();
                        refreshStats.run();
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
                        documentService.deleteDocumentAndCleanupFollows(selectedDoc, dataManager.getDocuments(), dataManager.getUsers());
                        refreshDocumentTable.run();
                        refreshStats.run();
                        refreshWatchList.run();
                        new Alert(Alert.AlertType.INFORMATION, "Το έργο διαγράφηκε επιτυχώς!").showAndWait();
                    }
                });
            });
        }

        if ("Admin".equals(loggedInUser.getRole())) {
            Button adminBtn = new Button("Διαχείριση Συστήματος (Admin)");
            buttonBox.getChildren().add(adminBtn);
            adminBtn.setOnAction(e -> openAdminPanel());
        }

        VBox documentsTabContent = new VBox(10,
                new Label("Αναζήτηση Εγγράφων:"),
                searchField,
                table,
                buttonBox
        );

        Button removeFollowBtn = new Button("Κατάργηση επιλεγμένης παρακολούθησης");
        removeFollowBtn.setOnAction(e -> {
            String selected = watchListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                new Alert(Alert.AlertType.WARNING, "Επιλέξτε μία παρακολούθηση από τη λίστα.").showAndWait();
                return;
            }
            String docTitle = selected.replaceAll("\\s*\\(τρέχουσα v.*$", "");
            loggedInUser.unfollowDocument(docTitle);
            refreshWatchList.run();
            refreshStats.run();
        });

        VBox watchTabContent = new VBox(10,
                new Label("Ενεργές παρακολουθήσεις:"),
                watchListView,
                removeFollowBtn
        );
        watchTabContent.setPadding(new Insets(5, 0, 0, 0));

        TabPane userTabs = new TabPane();
        Tab docsTab = new Tab("Έγγραφα", documentsTabContent);
        docsTab.setClosable(false);
        Tab watchTab = new Tab("Παρακολουθήσεις", watchTabContent);
        watchTab.setClosable(false);
        userTabs.getTabs().addAll(docsTab, watchTab);

        mainContentBox.getChildren().addAll(welcomeLabel, userTabs);

        BorderPane root = new BorderPane();
        root.setTop(statsBox);
        root.setCenter(mainContentBox);

        Scene scene = new Scene(root, 900, 650);
        stage.setScene(scene);
    }

    private void showDocumentReadDialog(Document selectedDoc) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Ανάγνωση: " + selectedDoc.getTitle());
        dialog.setHeaderText("Συγγραφέας: " + selectedDoc.getAuthorName() + " | Κατηγορία: " + selectedDoc.getCategory() +
                "\nΗμερομηνία Δημιουργίας: " + selectedDoc.getCreationDate() + " | Τρέχουσα Έκδοση: v" + selectedDoc.getCurrentVersionNumber());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(10));

        TextArea contentArea = new TextArea();
        contentArea.setEditable(false);
        contentArea.setWrapText(true);
        contentArea.setPrefWidth(520);
        contentArea.setPrefHeight(340);

        List<Integer> visibleVersions = documentService.getVisibleVersionNumbersForUser(selectedDoc, loggedInUser);

        if (visibleVersions.isEmpty()) {
            contentArea.setText("Δεν υπάρχει κείμενο για αυτό το έργο.");
        } else if ("Simple User".equals(loggedInUser.getRole())) {
            int latest = visibleVersions.get(0);
            contentArea.setText(documentService.getContentByVersion(selectedDoc, latest));
            contentBox.getChildren().add(new Label("Διαθέσιμη έκδοση: v" + latest));
        } else {
            ComboBox<Integer> versionBox = new ComboBox<>();
            versionBox.getItems().addAll(visibleVersions);
            versionBox.getSelectionModel().selectFirst();

            Runnable refreshVersionContent = () -> {
                Integer selectedVersion = versionBox.getValue();
                if (selectedVersion == null) return;
                contentArea.setText(documentService.getContentByVersion(selectedDoc, selectedVersion));
            };
            refreshVersionContent.run();
            versionBox.setOnAction(ev -> refreshVersionContent.run());

            HBox selector = new HBox(8, new Label("Επιλέξτε έκδοση:"), versionBox);
            selector.setAlignment(Pos.CENTER_LEFT);
            contentBox.getChildren().add(selector);
        }

        contentBox.getChildren().add(contentArea);
        dialog.getDialogPane().setContent(contentBox);
        dialog.showAndWait();
    }

    private void openAdminPanel() {
        Stage adminStage = new Stage();
        adminStage.setTitle("Πάνελ Διαχειριστή - MediaLab");

        TabPane tabPane = new TabPane();

        Tab usersTab = new Tab("Χρήστες");
        usersTab.setClosable(false);
        VBox usersBox = new VBox(10);
        usersBox.setPadding(new Insets(15));

        ListView<String> usersList = new ListView<>();
        Runnable refreshUsers = () -> {
            usersList.getItems().clear();
            for (User u : dataManager.getUsers()) {
                usersList.getItems().add(u.getUsername() + " (" + u.getRole() + ") - " + u.getFirstName() + " " + u.getLastName());
            }
        };
        refreshUsers.run();

        HBox userBtns = new HBox(10);
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

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);

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

            grid.add(new Label("Όνομα:"), 0, 0);
            grid.add(fnField, 1, 0);
            grid.add(new Label("Επώνυμο:"), 0, 1);
            grid.add(lnField, 1, 1);
            grid.add(new Label("Password:"), 0, 2);
            grid.add(pwField, 1, 2);
            grid.add(new Label("Ρόλος:"), 0, 3);
            grid.add(roleBox, 1, 3);
            grid.add(new Label("Κατηγορίες\n(Ctrl+Click):"), 0, 4);
            grid.add(catSelection, 1, 4);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    if (pwField.getText().isEmpty() || fnField.getText().isEmpty() || lnField.getText().isEmpty()) {
                        new Alert(Alert.AlertType.ERROR, "Συμπληρώστε όλα τα πεδία!").showAndWait();
                        return null;
                    }
                    if (catSelection.getSelectionModel().getSelectedItems().isEmpty() && !"Admin".equals(roleBox.getValue())) {
                        new Alert(Alert.AlertType.ERROR, "Πρέπει να αναθέσετε τουλάχιστον 1 κατηγορία!").showAndWait();
                        return null;
                    }

                    u.setPassword(pwField.getText());
                    u.setFirstName(fnField.getText());
                    u.setLastName(lnField.getText());
                    u.setRole(roleBox.getValue());
                    u.setAuthorizedCategories(new ArrayList<>(catSelection.getSelectionModel().getSelectedItems()));
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

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);

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

            grid.add(new Label("Όνομα:"), 0, 0);
            grid.add(fnField, 1, 0);
            grid.add(new Label("Επώνυμο:"), 0, 1);
            grid.add(lnField, 1, 1);
            grid.add(new Label("Username:"), 0, 2);
            grid.add(unField, 1, 2);
            grid.add(new Label("Password:"), 0, 3);
            grid.add(pwField, 1, 3);
            grid.add(new Label("Ρόλος:"), 0, 4);
            grid.add(roleBox, 1, 4);
            grid.add(new Label("Κατηγορίες\n(Ctrl+Click):"), 0, 5);
            grid.add(catSelection, 1, 5);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    if (unField.getText().isEmpty() || pwField.getText().isEmpty() || fnField.getText().isEmpty() || lnField.getText().isEmpty()) {
                        new Alert(Alert.AlertType.ERROR, "Συμπληρώστε όλα τα πεδία!").showAndWait();
                        return null;
                    }
                    if (catSelection.getSelectionModel().getSelectedItems().isEmpty() && !"Admin".equals(roleBox.getValue())) {
                        new Alert(Alert.AlertType.ERROR, "Πρέπει να αναθέσετε τουλάχιστον 1 κατηγορία!").showAndWait();
                        return null;
                    }
                    User u = new User();
                    u.setUsername(unField.getText());
                    u.setPassword(pwField.getText());
                    u.setFirstName(fnField.getText());
                    u.setLastName(lnField.getText());
                    u.setRole(roleBox.getValue());
                    u.setAuthorizedCategories(new ArrayList<>(catSelection.getSelectionModel().getSelectedItems()));
                    u.setFollowedDocuments(new ArrayList<>());
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
        catBox.setPadding(new Insets(15));

        ListView<String> catList = new ListView<>();
        Runnable refreshCats = () -> {
            catList.getItems().clear();
            for (classes.Category c : dataManager.getCategories()) {
                catList.getItems().add(c.getName());
            }
        };
        refreshCats.run();

        HBox catBtns = new HBox(10);
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

                        List<Document> toDelete = new ArrayList<>();
                        for (Document doc : dataManager.getDocuments()) {
                            if (doc.getCategory().equals(catName)) toDelete.add(doc);
                        }
                        dataManager.getDocuments().removeAll(toDelete);

                        for (Document delDoc : toDelete) {
                            for (User u : dataManager.getUsers()) {
                                u.unfollowDocument(delDoc.getTitle());
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
