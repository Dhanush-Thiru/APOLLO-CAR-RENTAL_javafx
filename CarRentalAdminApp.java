import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.stream.Collectors;

public class CarRentalAdminApp extends Application {

    // --- Data Models -----------------------------------

    public static class Car {
        private final String company;
        private final String model;
        private final String type;
        private final double pricePerDay;
        private final int seats;

        public Car(String company, String model, String type, double pricePerDay, int seats) {
            this.company = company;
            this.model = model;
            this.type = type;
            this.pricePerDay = pricePerDay;
            this.seats = seats;
        }

        public String getCompany() { return company; }
        public String getModel() { return model; }
        public String getType() { return type; }
        public double getPricePerDay() { return pricePerDay; }
        public int getSeats() { return seats; }
    }

    public static class Customer {
        private final String customerId;
        private final String name;
        private final String email;
        private final String phone;

        public Customer(String customerId, String name, String email, String phone) {
            this.customerId = customerId;
            this.name = name;
            this.email = email;
            this.phone = phone;
        }

        public String getCustomerId() { return customerId; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
    }

    // --- Application Setup ---------------------------------------------------

    private BorderPane rootLayout;
    private Stage primaryStage;
    private Scene mainScene;
    private ObservableList<Car> carData;
    private ObservableList<Customer> customerData;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.primaryStage.setTitle("Car Rental Admin System");

        initializePlaceholderData();

        VBox loginLayout = createLoginScreen();
        Scene loginScene = new Scene(loginLayout, 400, 300);
        
        // FIX FOR NullPointerException 
        String styleUrl = null;
        if (getClass().getResource("styles.css") != null) {
            styleUrl = getClass().getResource("styles.css").toExternalForm();
        } else if (getClass().getResource("default.css") != null) {
            styleUrl = getClass().getResource("default.css").toExternalForm();
        }

        if (styleUrl != null) {
            loginScene.getStylesheets().add(styleUrl);
        }

        this.primaryStage.setScene(loginScene);
        this.primaryStage.show();
    }

    private void initializePlaceholderData() {
        carData = FXCollections.observableArrayList(
                new Car("Toyota", "Camry", "Sedan", 50.00, 5),
                new Car("Honda", "CR-V", "SUV", 70.00, 5),
                new Car("BMW", "X5", "SUV", 150.00, 5),
                new Car("Ford", "Mustang", "Coupe", 100.00, 4)
        );

        // Updated customer names and phone numbers to Indian style
        customerData = FXCollections.observableArrayList(
                new Customer("C001", "Priya Sharma", "priya.s@example.in", "+91-98765-43210"),
                new Customer("C002", "Amit Singh", "amit.s@example.in", "+91-99887-76655"),
                new Customer("C003", "Karthik Menon", "karthik.m@example.in", "+91-90000-11111")
        );
    }

    private VBox createLoginScreen() {
        VBox loginBox = new VBox(20);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setPadding(new Insets(30));
        
        loginBox.setStyle("-fx-background-color: #333333;");

        Label title = new Label("Employee Login");
        title.setFont(new Font("Arial", 24));
        title.setStyle("-fx-text-fill: #E0E0E0;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Unique Password");
        passwordField.setMaxWidth(250);

        Button loginBtn = new Button("Login & Verify");
        loginBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-padding: 10 20; -fx-font-size: 14px; -fx-cursor: hand;");

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: red;");

        loginBtn.setOnAction(e -> {
            // JDBC INTEGRATION POINT 1: Login verification here
            if (passwordField.getText().equals("admin123")) {
                statusLabel.setText("Login Successful!");
                statusLabel.setStyle("-fx-text-fill: #4CAF50;");
                showMainDashboard();
            } else {
                statusLabel.setText("Invalid Password.");
                statusLabel.setStyle("-fx-text-fill: #F44336;");
            }
        });

        loginBox.getChildren().addAll(title, passwordField, loginBtn, statusLabel);
        return loginBox;
    }

    private void showMainDashboard() {
        rootLayout = new BorderPane();
        VBox sidebar = createSidebar(rootLayout);
        rootLayout.setLeft(sidebar);

        rootLayout.setCenter(createCarManagementView());

        mainScene = new Scene(rootLayout, 1000, 700);
        primaryStage.setScene(mainScene);
        primaryStage.centerOnScreen();
    }

    // --- Dashboard Components ------------------------------------------------

    private VBox createSidebar(BorderPane mainLayout) {
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(200);
        sidebar.setStyle("-fx-background-color: #2c3e50;");

        Label title = new Label("Admin Functions");
        title.setFont(new Font("Arial", 18));
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Button carMgtBtn = createSidebarButton("Car Management");
        Button customerBtn = createSidebarButton("Customer Details");
        Button reportsBtn = createSidebarButton("Fetch Reports");
        
        carMgtBtn.setOnAction(e -> mainLayout.setCenter(createCarManagementView()));
        customerBtn.setOnAction(e -> mainLayout.setCenter(createCustomerDetailsView()));
        reportsBtn.setOnAction(e -> mainLayout.setCenter(createReportsView()));

        sidebar.getChildren().addAll(title, new Separator(), carMgtBtn, customerBtn, reportsBtn);
        return sidebar;
    }
    
    private Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-padding: 10; -fx-font-size: 14px; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #4a637a; -fx-text-fill: white; -fx-padding: 10; -fx-font-size: 14px; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-padding: 10; -fx-font-size: 14px; -fx-cursor: hand;"));
        return btn;
    }

    // --- Car Management View -------------------------------------------------

    private VBox createCarManagementView() {
        VBox carMgtBox = new VBox(20);
        carMgtBox.setPadding(new Insets(20));
        carMgtBox.setStyle("-fx-background-color: #ecf0f1;");

        Label header = new Label("Car Management (Add, Update, Delete)");
        header.setFont(new Font("Arial", 20));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Input Form
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10, 0, 10, 0));
        
        TextField companyInput = new TextField(); companyInput.setPromptText("Company (e.g., Toyota)");
        TextField modelInput = new TextField(); modelInput.setPromptText("Model (e.g., Camry)");
        TextField typeInput = new TextField(); typeInput.setPromptText("Type (e.g., Sedan)");
        TextField priceInput = new TextField(); priceInput.setPromptText("Price Per Day (e.g., 50.00)");
        TextField seatsInput = new TextField(); seatsInput.setPromptText("Number of Seats (e.g., 5)");

        formGrid.add(new Label("Company:"), 0, 0); formGrid.add(companyInput, 1, 0);
        formGrid.add(new Label("Model:"), 0, 1); formGrid.add(modelInput, 1, 1);
        formGrid.add(new Label("Type:"), 2, 0); formGrid.add(typeInput, 3, 0);
        formGrid.add(new Label("Price/Day:"), 2, 1); formGrid.add(priceInput, 3, 1);
        formGrid.add(new Label("Seats:"), 2, 2); formGrid.add(seatsInput, 3, 2);

        // Action Buttons
        Button addBtn = new Button("Add Car");
        Button updateBtn = new Button("Update Car Info");
        Button deleteBtn = new Button("Delete Car");
        Button clearBtn = new Button("Clear Form");
        
        HBox buttonBox = new HBox(10, addBtn, updateBtn, deleteBtn, clearBtn);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        
        // Apply consistent button styling
        String btnStyle = "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 8 15; -fx-cursor: hand;";
        String deleteStyle = "-fx-background-color: #c0392b; -fx-text-fill: white; -fx-padding: 8 15; -fx-cursor: hand;";
        String updateStyle = "-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 8 15; -fx-cursor: hand;";
        String clearStyle = "-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 8 15; -fx-cursor: hand;";

        addBtn.setStyle(btnStyle);
        updateBtn.setStyle(updateStyle);
        deleteBtn.setStyle(deleteStyle);
        clearBtn.setStyle(clearStyle);

        // Car TableView 
        TableView<Car> carTable = createCarTable();
        carTable.setPrefHeight(400);

        // --- Event Handlers (JDBC Placeholder) ---

        clearBtn.setOnAction(e -> {
            companyInput.clear(); modelInput.clear(); typeInput.clear(); priceInput.clear(); seatsInput.clear();
        });

        addBtn.setOnAction(e -> {
            // JDBC INSERT statement here
            System.out.println("JDBC Add Car function here: " + companyInput.getText());
        });

        updateBtn.setOnAction(e -> {
            // JDBC UPDATE statement here
            System.out.println("JDBC Update Car function here: " + modelInput.getText());
        });

        deleteBtn.setOnAction(e -> {
            // JDBC DELETE statement here
            Car selectedCar = carTable.getSelectionModel().getSelectedItem();
            if (selectedCar != null) {
                System.out.println("JDBC Delete Car function here: " + selectedCar.getModel());
            } else {
                System.out.println("Please select a car to delete.");
            }
        });


        // Search/Filter Panel 
        VBox searchPanel = createCarSearchPanel(carTable);

        carMgtBox.getChildren().addAll(header, formGrid, buttonBox, searchPanel, new Label("All Car Details:"), carTable);
        return carMgtBox;
    }
    
    private TableView<Car> createCarTable() {
        TableView<Car> table = new TableView<>();
        // JDBC Initial Data Load here
        table.setItems(carData); 

        TableColumn<Car, String> companyCol = new TableColumn<>("Company");
        companyCol.setCellValueFactory(new PropertyValueFactory<>("company"));
        companyCol.setPrefWidth(120);

        TableColumn<Car, String> modelCol = new TableColumn<>("Model");
        modelCol.setCellValueFactory(new PropertyValueFactory<>("model"));
        modelCol.setPrefWidth(150);

        TableColumn<Car, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<Car, Double> priceCol = new TableColumn<>("Price Per Day");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("pricePerDay"));

        TableColumn<Car, Integer> seatsCol = new TableColumn<>("Seats");
        seatsCol.setCellValueFactory(new PropertyValueFactory<>("seats"));
        
        table.getColumns().addAll(companyCol, modelCol, typeCol, priceCol, seatsCol);
        return table;
    }

    private VBox createCarSearchPanel(TableView<Car> carTable) {
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        
        Label groupLabel = new Label("Group By:");
        groupLabel.setStyle("-fx-font-weight: bold;");
        
        ComboBox<String> groupByCombo = new ComboBox<>();
        groupByCombo.setPromptText("Select Field");
        groupByCombo.getItems().addAll("Company", "Model", "Type", "Seats");

        TextField filterInput = new TextField();
        filterInput.setPromptText("Enter Value to Filter");
        
        Button applyFilterBtn = new Button("Apply Filter");
        Button clearFilterBtn = new Button("Clear Filter");
        applyFilterBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
        clearFilterBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-cursor: hand;");

        // Simple filtering logic (in-memory for now)
        applyFilterBtn.setOnAction(e -> {
            String field = groupByCombo.getValue();
            String filterValue = filterInput.getText().trim().toLowerCase();
            
            if (field != null && !filterValue.isEmpty()) {
                // JDBC Search/Filter query here
                ObservableList<Car> filteredList = carData.stream()
                        .filter(car -> {
                            return switch (field) {
                                case "Company" -> car.getCompany().toLowerCase().contains(filterValue);
                                case "Model" -> car.getModel().toLowerCase().contains(filterValue);
                                case "Type" -> car.getType().toLowerCase().contains(filterValue);
                                case "Seats" -> String.valueOf(car.getSeats()).contains(filterValue);
                                default -> true;
                            };
                        })
                        .collect(Collectors.toCollection(FXCollections::observableArrayList));
                carTable.setItems(filteredList);
            }
        });
        
        clearFilterBtn.setOnAction(e -> {
            carTable.setItems(carData);
            groupByCombo.getSelectionModel().clearSelection();
            filterInput.clear();
        });

        searchBox.getChildren().addAll(groupLabel, groupByCombo, filterInput, applyFilterBtn, clearFilterBtn);
        
        VBox panel = new VBox(5, new Label("Search for cars (Employee Search Criteria):"), searchBox);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1;");
        return panel;
    }

    // --- Customer Details View -----------------------------------------------

    private VBox createCustomerDetailsView() {
        VBox customerBox = new VBox(20);
        customerBox.setPadding(new Insets(20));
        customerBox.setStyle("-fx-background-color: #ecf0f1;"); 

        Label header = new Label("Customer Details (View & Search)");
        header.setFont(new Font("Arial", 20));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Search Panel
        TextField searchInput = new TextField();
        searchInput.setPromptText("Search for a Customer (by Name, ID, or Email)");
        searchInput.setMaxWidth(400);
        
        Button searchBtn = new Button("Search Customer");
        searchBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 8 15; -fx-cursor: hand;");

        HBox searchHBox = new HBox(10, searchInput, searchBtn);
        searchHBox.setAlignment(Pos.CENTER_LEFT);

        // Customer TableView
        TableView<Customer> customerTable = createCustomerTable();
        customerTable.setPrefHeight(500);

        // --- Event Handler (JDBC Placeholder) ---
        searchBtn.setOnAction(e -> {
            String query = searchInput.getText().trim().toLowerCase();
            // JDBC Search Customer query here
            System.out.println("JDBC Search Customer query here: " + query);
            
            // In-memory filter placeholder
            ObservableList<Customer> filteredList = customerData.stream()
                .filter(cust -> cust.getName().toLowerCase().contains(query) || 
                                 cust.getCustomerId().toLowerCase().contains(query) ||
                                 cust.getEmail().toLowerCase().contains(query))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
            customerTable.setItems(filteredList);
        });

        customerBox.getChildren().addAll(header, searchHBox, new Label("All Customer Details:"), customerTable);
        return customerBox;
    }
    
    private TableView<Customer> createCustomerTable() {
        TableView<Customer> table = new TableView<>();
        // JDBC Initial Customer Data Load here
        table.setItems(customerData);

        TableColumn<Customer, String> idCol = new TableColumn<>("Customer ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));

        TableColumn<Customer, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(180);

        TableColumn<Customer, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(250);

        TableColumn<Customer, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        
        table.getColumns().addAll(idCol, nameCol, emailCol, phoneCol);
        return table;
    }

    // --- Reports View --------------------------------------------------------

    private VBox createReportsView() {
        VBox reportBox = new VBox(20);
        reportBox.setPadding(new Insets(20));
        reportBox.setAlignment(Pos.CENTER);
        reportBox.setStyle("-fx-background-color: #ecf0f1;"); 

        Label header = new Label("Fetch Reports for Analysis");
        header.setFont(new Font("Arial", 20));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label info = new Label("Report generation will be implemented here.");
        info.setStyle("-fx-text-fill: #95a5a6;");
        
        Button generateBtn = new Button("Generate Standard Report (Monthly)");
        generateBtn.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-padding: 10 20; -fx-cursor: hand;");
        
        generateBtn.setOnAction(e -> {
            // JDBC Report Generation query here
            System.out.println("JDBC Report Generation query here...");
        });

        reportBox.getChildren().addAll(header, info, generateBtn);
        return reportBox;
    }


    public static void main(String[] args) {
        launch();
    }
}
