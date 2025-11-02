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

import java.sql.*;
import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Collectors;

public class CarRentalAdminApp extends Application {

    // --- Data Models (Note: Customer ID is treated as a String in Java but Int in DB) -----------------------------------

    public static class Car {
        private int carId; 
        private final String company;
        private final String model;
        private final String type;
        private final double pricePerDay;
        private final int seats;

        // Constructor used for fetching existing cars
        public Car(int carId, String company, String model, String type, double pricePerDay, int seats) {
            this.carId = carId;
            this.company = company;
            this.model = model;
            this.type = type;
            this.pricePerDay = pricePerDay;
            this.seats = seats;
        }

        // Getters for TableView/UI
        public int getCarId() { return carId; }
        public String getCompany() { return company; }
        public String getModel() { return model; }
        public String getType() { return type; }
        public double getPricePerDay() { return pricePerDay; }
        public int getSeats() { return seats; }
        
        @Override
        public String toString() {
            return String.format("%s %s (%s, $%.2f/day)", company, model, type, pricePerDay);
        }
    }

    public static class Customer {
        private final String customerId; // Matches the VARCHAR field in DB (C001, C002, etc.)
        private final String name;
        private final String email;
        private final String phone;
        private final String dateOfBirth;
        private final String password; // Placeholder for simplicity

        public Customer(String customerId, String name, String email, String phone, String dateOfBirth, String password) {
            this.customerId = customerId;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.dateOfBirth = dateOfBirth;
            this.password = password;
        }

        // Getters for TableView/UI
        public String getCustomerId() { return customerId; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getDateOfBirth() { return dateOfBirth; }
        public String getPassword() { return password; }
        
        @Override
        public String toString() {
            return String.format("%s (%s)", name, customerId);
        }
    }
    
    // --- Mock Rental History Class (Assuming a 'rental' table exists in MySQL) ---
    public static class RentalHistory {
        private final int rentalId;
        private final String customerName;
        private final String carDetails;
        private final String startDate;
        private final String endDate;
        private final String status;

        public RentalHistory(int rentalId, String customerName, String carDetails, String startDate, String endDate, String status) {
            this.rentalId = rentalId;
            this.customerName = customerName;
            this.carDetails = carDetails;
            this.startDate = startDate;
            this.endDate = endDate;
            this.status = status;
        }

        public int getRentalId() { return rentalId; }
        public String getCustomerName() { return customerName; }
        public String getCarDetails() { return carDetails; }
        public String getStartDate() { return startDate; }
        public String getEndDate() { return endDate; }
        public String getStatus() { return status; }
    }


    // --- Application Setup ---------------------------------------------------

    private BorderPane rootLayout;
    private Stage primaryStage;
    private ObservableList<Car> carData;
    private ObservableList<Customer> customerData;
    private ObservableList<RentalHistory> rentalHistoryData;
    private Label statusLabel = new Label(); // Global label for database messages

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.primaryStage.setTitle("Car Rental Admin System (JDBC MySQL)");

        carData = FXCollections.observableArrayList();
        customerData = FXCollections.observableArrayList();
        rentalHistoryData = FXCollections.observableArrayList();
        
        // Try to load initial data from DB before showing login
        loadAllCars();
        loadAllCustomers(); 
        loadRentalHistoryMock(); // Load mock rental history for the History View
        
        VBox loginLayout = createLoginScreen();
        Scene loginScene = new Scene(loginLayout, 400, 300);
        
        // Fallback for CSS loading if resource is not found
        try {
            loginScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
        } catch (NullPointerException e) {
            System.err.println("Warning: styles.css not found. Running without styles.");
        }


        this.primaryStage.setScene(loginScene);
        this.primaryStage.show();
    }

    // --- Data Loading Methods (JDBC READ) ---

    private void loadAllCars() {
        carData.clear();
        String query = "SELECT car_id, company, model, type, price_per_day, seats FROM car";
        
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
                
            while (rs.next()) {
                Car car = new Car(
                    rs.getInt("car_id"),
                    rs.getString("company"),
                    rs.getString("model"),
                    rs.getString("type"),
                    rs.getDouble("price_per_day"),
                    rs.getInt("seats")
                );
                carData.add(car);
            }
            setStatus("Data loaded successfully. Cars: " + carData.size(), "#4CAF50");

        } catch (SQLException e) {
            setStatus("Database Error: Cannot load cars. Check password/connection. " + e.getMessage(), "#F44336");
            e.printStackTrace();
        }
    }

    private void loadAllCustomers() {
        customerData.clear();
        String query = "SELECT customer_id, name, email, phone, date_of_birth, password FROM customer";
        
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Customer customer = new Customer(
                    rs.getString("customer_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("date_of_birth"),
                    rs.getString("password")
                );
                customerData.add(customer);
            }
            setStatus("Customers loaded successfully. Total: " + customerData.size(), "#4CAF50");
            
        } catch (SQLException e) {
            setStatus("Database Error: Cannot load customers. " + e.getMessage(), "#F44336");
            e.printStackTrace();
        }
    }
    
    /**
     * Helper to load mock rental data as the actual 'rental' table is not guaranteed to be created by the user.
     */
    private void loadRentalHistoryMock() {
        // This is mock data, replace with JDBC query if a 'rental' table is implemented.
        rentalHistoryData.addAll(
            new RentalHistory(101, "Priya Sharma", "Toyota Camry", "2025-10-01", "2025-10-05", "Returned"),
            new RentalHistory(102, "Amit Singh", "BMW X5", "2025-10-25", "2025-11-01", "Returned"),
            new RentalHistory(103, "Karthik Menon", "Honda CR-V", "2025-11-10", "2025-11-17", "Active")
        );
    }

    private void setStatus(String message, String color) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.setStyle("-fx-text-fill: " + color + ";");
        }
        System.out.println(message);
    }
    
    // --- Login Screen (JDBC Verification) ---

    private VBox createLoginScreen() {
        VBox loginBox = new VBox(20);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setPadding(new Insets(30));
        loginBox.setStyle("-fx-background-color: #333333;");

        Label title = new Label("Employee Login");
        title.setFont(new Font("Arial", 24));
        title.setStyle("-fx-text-fill: #E0E0E0;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Employee Password (Admin)");
        passwordField.setMaxWidth(250);
        passwordField.setText("admin123"); // Demo password

        Button loginBtn = new Button("Login & Verify");
        loginBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-padding: 10 20; -fx-font-size: 14px; -fx-cursor: hand;");

        statusLabel.setStyle("-fx-text-fill: red;");
        statusLabel.setText("Awaiting Login...");


        loginBtn.setOnAction(e -> {
            if (passwordField.getText().equals("admin123")) {
                setStatus("Login Successful! Connecting to DB...", "#4CAF50");
                loadAllCars();
                loadAllCustomers();
                showMainDashboard();
            } else {
                setStatus("Invalid Password.", "#F44336");
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

        Scene mainScene = new Scene(rootLayout, 1000, 700);
        primaryStage.setScene(mainScene);
        primaryStage.centerOnScreen();
    }

    private VBox createSidebar(BorderPane mainLayout) {
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(200);
        sidebar.setStyle("-fx-background-color: #2c3e50;");

        Label title = new Label("Admin Functions");
        title.setFont(new Font("Arial", 18));
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        // CAR Section
        Label carSection = new Label("CAR");
        carSection.setFont(new Font("Arial", 14));
        carSection.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");

        Button existingCustomerBtn = createSidebarButton("Existing Customer");
        Button newCustomerBtn = createSidebarButton("New Customer");

        // Sub-buttons under Existing Customer (Rent, Return, History)
        Button rentCarBtn = createSidebarButton("  - Rent Car");
        Button returnCarBtn = createSidebarButton("  - Return Car");
        Button historyBtn = createSidebarButton("  - History");

        // CUSTOMER Section
        Label customerSection = new Label("CUSTOMER");
        customerSection.setFont(new Font("Arial", 14));
        customerSection.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");

        Button existingCustDetailsBtn = createSidebarButton("Existing Customer");
        Button newCustBtn = createSidebarButton("New Customer");
        Button updateCustomerBtn = createSidebarButton("Update Customer");
        Button deleteCustomerBtn = createSidebarButton("Delete Customer");

        // RECEIPT Section
        Label receiptSection = new Label("RECEIPT");
        receiptSection.setFont(new Font("Arial", 14));
        receiptSection.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");

        Button changeUsernameBtn = createSidebarButton("Change Username");
        Button changePhoneBtn = createSidebarButton("Change Phone Number");
        Button changeDobBtn = createSidebarButton("Change Date of Birth");
        Button changePasswordBtn = createSidebarButton("Change Password");

        // Existing buttons
        Button carMgtBtn = createSidebarButton("Car Management");
        Button customerDetailsBtn = createSidebarButton("Customer Details");
        Button reportsBtn = createSidebarButton("Fetch Reports");
       
        // Event handlers for new buttons
        existingCustomerBtn.setOnAction(e -> mainLayout.setCenter(createExistingCustomerView()));
        
        // FIX: Replaced the faulty reference with the correct method name
        newCustomerBtn.setOnAction(e -> mainLayout.setCenter(createNewCustomerRegistrationView())); 
        
        rentCarBtn.setOnAction(e -> mainLayout.setCenter(createRentCarView()));
        returnCarBtn.setOnAction(e -> mainLayout.setCenter(createReturnCarView()));
        historyBtn.setOnAction(e -> mainLayout.setCenter(createHistoryView()));
        existingCustDetailsBtn.setOnAction(e -> mainLayout.setCenter(createCustomerDetailsView()));
        newCustBtn.setOnAction(e -> mainLayout.setCenter(createNewCustomerRegistrationView()));
        updateCustomerBtn.setOnAction(e -> mainLayout.setCenter(createUpdateCustomerView()));
        deleteCustomerBtn.setOnAction(e -> mainLayout.setCenter(createDeleteCustomerView()));
        changeUsernameBtn.setOnAction(e -> mainLayout.setCenter(createChangeUsernameView()));
        changePhoneBtn.setOnAction(e -> mainLayout.setCenter(createChangePhoneView()));
        changeDobBtn.setOnAction(e -> mainLayout.setCenter(createChangeDobView()));
        changePasswordBtn.setOnAction(e -> mainLayout.setCenter(createChangePasswordView()));

        carMgtBtn.setOnAction(e -> mainLayout.setCenter(createCarManagementView()));
        customerDetailsBtn.setOnAction(e -> mainLayout.setCenter(createCustomerDetailsView()));
        reportsBtn.setOnAction(e -> mainLayout.setCenter(createReportsView()));

        sidebar.getChildren().addAll(title, new Separator(), carSection, existingCustomerBtn, rentCarBtn, returnCarBtn, historyBtn, newCustomerBtn,
                                     customerSection, existingCustDetailsBtn, newCustBtn, updateCustomerBtn, deleteCustomerBtn,
                                     receiptSection, changeUsernameBtn, changePhoneBtn, changeDobBtn, changePasswordBtn,
                                     new Separator(), carMgtBtn, customerDetailsBtn, reportsBtn, statusLabel); // Added status label
        return sidebar;
    }
   
    private Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-padding: 8; -fx-font-size: 12px; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #4a637a; -fx-text-fill: white; -fx-padding: 8; -fx-font-size: 12px; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-padding: 8; -fx-font-size: 12px; -fx-cursor: hand;"));
        return btn;
    }

    private VBox createExistingCustomerView() {
        VBox box = new VBox(20);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #ecf0f1;");
        Label header = new Label("Existing Customer - Actions");
        header.setFont(new Font("Arial", 20));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Label info = new Label("Please use the sub-options (Rent Car, Return Car, History) in the sidebar.");
        info.setStyle("-fx-text-fill: #95a5a6;");
        box.getChildren().addAll(header, info);
        return box;
    }
    
    /**
     * Generates a new Customer ID based on current list size (Mock ID generation).
     * NOTE: This is NOT safe for real-world concurrent applications but works for this demo.
     */
    private String generateNewCustomerId() {
        int maxId = 0;
        for (Customer c : customerData) {
            try {
                // Assumes IDs are "C001", "C002", etc.
                int idNum = Integer.parseInt(c.getCustomerId().substring(1));
                if (idNum > maxId) {
                    maxId = idNum;
                }
            } catch (NumberFormatException ignored) {
                // Ignore non-standard IDs
            }
        }
        return String.format("C%03d", maxId + 1);
    }
    
    // --- New Customer Registration View (JDBC INSERT) ---
    private VBox createNewCustomerRegistrationView() {
        VBox box = new VBox(20);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #ecf0f1;");
        Label header = new Label("New Customer Registration");
        header.setFont(new Font("Arial", 20));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Registration form
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10, 0, 10, 0));
       
        TextField nameInput = new TextField(); nameInput.setPromptText("Full Name");
        TextField emailInput = new TextField(); emailInput.setPromptText("Email (Unique)");
        TextField phoneInput = new TextField(); phoneInput.setPromptText("Phone Number");
        DatePicker dobPicker = new DatePicker(); dobPicker.setPromptText("Date of Birth");
        PasswordField passwordInput = new PasswordField(); passwordInput.setPromptText("Password");

        formGrid.add(new Label("Name:"), 0, 0); formGrid.add(nameInput, 1, 0);
        formGrid.add(new Label("Email:"), 0, 1); formGrid.add(emailInput, 1, 1);
        formGrid.add(new Label("Phone:"), 2, 0); formGrid.add(phoneInput, 3, 0);
        formGrid.add(new Label("DOB:"), 2, 1); formGrid.add(dobPicker, 3, 1);
        formGrid.add(new Label("Password:"), 0, 2); formGrid.add(passwordInput, 1, 2);

        Button registerBtn = new Button("Register Customer");
        registerBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 8 15; -fx-cursor: hand;");
        registerBtn.setOnAction(e -> {
            String name = nameInput.getText();
            String email = emailInput.getText();
            String phone = phoneInput.getText();
            String dob = dobPicker.getValue() != null ? dobPicker.getValue().toString() : "";
            String password = passwordInput.getText();
            String customerId = generateNewCustomerId();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                setStatus("Error: Name, Email, and Password are required.", "#F44336");
                return;
            }

            // JDBC INSERT new customer
            String sql = "INSERT INTO customer (customer_id, name, email, phone, date_of_birth, password) VALUES (?, ?, ?, ?, ?, ?)";
            
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                     
                pstmt.setString(1, customerId);
                pstmt.setString(2, name);
                pstmt.setString(3, email);
                pstmt.setString(4, phone);
                pstmt.setString(5, dob);
                pstmt.setString(6, password);
                
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    setStatus("Customer " + customerId + " registered successfully!", "#27ae60");
                    loadAllCustomers(); // Refresh the list
                    // Clear form
                    nameInput.clear(); emailInput.clear(); phoneInput.clear(); dobPicker.setValue(null); passwordInput.clear();
                }

            } catch (SQLException ex) {
                setStatus("Error registering customer (Email might be duplicate): " + ex.getMessage(), "#F44336");
                ex.printStackTrace();
            }
        });

        Button clearBtn = new Button("Clear Form");
        clearBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 8 15; -fx-cursor: hand;");
        clearBtn.setOnAction(e -> {
            nameInput.clear(); emailInput.clear(); phoneInput.clear(); dobPicker.setValue(null); passwordInput.clear();
        });

        HBox buttonBox = new HBox(10, registerBtn, clearBtn);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(header, new Separator(), formGrid, buttonBox);
        return box;
    }
    
    // --- Rent Car View (JDBC INSERT into rental table - Assumed) ---
    private VBox createRentCarView() {
        VBox box = new VBox(20);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #ecf0f1;");
        Label header = new Label("Rent Car to Existing Customer");
        header.setFont(new Font("Arial", 20));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10, 0, 10, 0));
       
        ComboBox<Customer> customerSelection = new ComboBox<>(customerData);
        customerSelection.setPromptText("Select Customer");
        customerSelection.setPrefWidth(200);
        
        ComboBox<Car> carSelection = new ComboBox<>(carData);
        carSelection.setPromptText("Select Available Car");
        carSelection.setPrefWidth(200);
        
        DatePicker startDate = new DatePicker(LocalDate.now()); 
        startDate.setPromptText("Start Date");
        DatePicker endDate = new DatePicker(LocalDate.now().plusDays(3)); 
        endDate.setPromptText("End Date");

        formGrid.add(new Label("Customer:"), 0, 0); formGrid.add(customerSelection, 1, 0);
        formGrid.add(new Label("Car:"), 2, 0); formGrid.add(carSelection, 3, 0);
        formGrid.add(new Label("Start Date:"), 0, 1); formGrid.add(startDate, 1, 1);
        formGrid.add(new Label("End Date:"), 2, 1); formGrid.add(endDate, 3, 1);

        Button rentBtn = new Button("Process Rental");
        rentBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 10 20; -fx-cursor: hand;");
        rentBtn.setOnAction(e -> {
            if (customerSelection.getValue() == null || carSelection.getValue() == null || startDate.getValue() == null || endDate.getValue() == null) {
                setStatus("Error: All fields must be selected.", "#F44336");
                return;
            }
            
            // NOTE: This requires a 'rental' table in your MySQL DB with customer_id and car_id
            String sql = "INSERT INTO rental (customer_id, car_id, start_date, end_date, status) VALUES (?, ?, ?, ?, 'Active')";
            
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                     
                pstmt.setString(1, customerSelection.getValue().getCustomerId());
                pstmt.setInt(2, carSelection.getValue().getCarId());
                pstmt.setString(3, startDate.getValue().toString());
                pstmt.setString(4, endDate.getValue().toString());
                
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    setStatus("Rental processed for " + customerSelection.getValue().getName() + ".", "#27ae60");
                }
            } catch (SQLException ex) {
                setStatus("Rental Error (Missing 'rental' table?): " + ex.getMessage(), "#F44336");
                ex.printStackTrace();
            }
        });

        box.getChildren().addAll(header, new Separator(), formGrid, rentBtn);
        return box;
    }

    // --- Return Car View (JDBC UPDATE rental table - Assumed) ---
    private VBox createReturnCarView() {
        VBox box = new VBox(20);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #ecf0f1;");
        Label header = new Label("Return Car & Finalize");
        header.setFont(new Font("Arial", 20));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // In a real application, this table would show ACTIVE rentals loaded from the 'rental' table.
        // Here we use a TextField to mock searching by Rental ID.
        TextField rentalIdInput = new TextField(); 
        rentalIdInput.setPromptText("Enter Rental ID to finalize");
        rentalIdInput.setMaxWidth(200);

        Label fineLabel = new Label("Fine/Overdue Cost: ₹0.00");
        fineLabel.setFont(new Font("Arial", 14));

        Button returnBtn = new Button("Process Return");
        returnBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 10 20; -fx-cursor: hand;");
        returnBtn.setOnAction(e -> {
            String rentalId = rentalIdInput.getText().trim();
            if (rentalId.isEmpty()) {
                setStatus("Error: Enter a Rental ID.", "#F44336");
                return;
            }
            
            // NOTE: This requires a 'rental' table in your MySQL DB
            String sql = "UPDATE rental SET status = 'Returned', actual_return_date = CURDATE() WHERE rental_id = ?";
            
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                     
                pstmt.setString(1, rentalId);
                
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    setStatus("Rental ID " + rentalId + " returned successfully. Total Cost Finalized.", "#27ae60");
                    rentalIdInput.clear();
                    fineLabel.setText("Fine/Overdue Cost: ₹0.00");
                } else {
                    setStatus("Error: Rental ID " + rentalId + " not found or already returned.", "#F44336");
                }
            } catch (SQLException ex) {
                setStatus("Return Error (Missing 'rental' table?): " + ex.getMessage(), "#F44336");
                ex.printStackTrace();
            }
        });

        VBox form = new VBox(10, 
            new Label("Rental ID:"), 
            rentalIdInput,
            fineLabel, 
            returnBtn
        );
        form.setPrefWidth(250);

        box.getChildren().addAll(header, new Separator(), form);
        return box;
    }

    // --- Rental History View (Mocked Data) ---
    private VBox createHistoryView() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #ecf0f1;");
        Label header = new Label("Rental History");
        header.setFont(new Font("Arial", 20));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        TableView<RentalHistory> historyTable = new TableView<>(rentalHistoryData);
        historyTable.setPrefHeight(500);

        TableColumn<RentalHistory, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("rentalId"));

        TableColumn<RentalHistory, String> custCol = new TableColumn<>("Customer");
        custCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        custCol.setPrefWidth(150);

        TableColumn<RentalHistory, String> carCol = new TableColumn<>("Car");
        carCol.setCellValueFactory(new PropertyValueFactory<>("carDetails"));
        carCol.setPrefWidth(200);
        
        TableColumn<RentalHistory, String> startCol = new TableColumn<>("Start Date");
        startCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        
        TableColumn<RentalHistory, String> endCol = new TableColumn<>("End Date");
        endCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        
        TableColumn<RentalHistory, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        historyTable.getColumns().addAll(idCol, custCol, carCol, startCol, endCol, statusCol);

        box.getChildren().addAll(header, new Label("All Rental Transactions (Mock/Live):"), historyTable);
        return box;
    }
    
    // --- Reusable Update Method for Customer Fields ---
    private boolean updateCustomerFieldInDB(String customerId, String fieldName, String newValue) {
        String sql = "UPDATE customer SET " + fieldName + " = ? WHERE customer_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
                 
            pstmt.setString(1, newValue);
            pstmt.setString(2, customerId);
            
            return pstmt.executeUpdate() > 0;

        } catch (SQLException ex) {
            setStatus("Error updating " + fieldName + ": " + ex.getMessage(), "#F44336");
            ex.printStackTrace();
            return false;
        }
    }

    // --- Full Update Customer View (JDBC UPDATE) ---
    private VBox createUpdateCustomerView() {
        VBox box = new VBox(20);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #ecf0f1;");
        Label header = new Label("Update Customer Details");
        header.setFont(new Font("Arial", 20));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Selection
        ComboBox<Customer> customerSelection = new ComboBox<>(customerData);
        customerSelection.setPromptText("Select Customer to Update");
        customerSelection.setPrefWidth(300);

        // Form fields
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10); formGrid.setVgap(10);
       
        TextField nameInput = new TextField(); 
        TextField emailInput = new TextField(); 
        TextField phoneInput = new TextField(); 
        DatePicker dobPicker = new DatePicker(); 
        PasswordField passwordInput = new PasswordField(); 

        formGrid.add(new Label("Name:"), 0, 0); formGrid.add(nameInput, 1, 0);
        formGrid.add(new Label("Email:"), 0, 1); formGrid.add(emailInput, 1, 1);
        formGrid.add(new Label("Phone:"), 2, 0); formGrid.add(phoneInput, 3, 0);
        formGrid.add(new Label("DOB:"), 2, 1); formGrid.add(dobPicker, 3, 1);
        formGrid.add(new Label("Password:"), 0, 2); formGrid.add(passwordInput, 1, 2);

        // Listener to populate form
        customerSelection.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                nameInput.setText(newVal.getName());
                emailInput.setText(newVal.getEmail());
                phoneInput.setText(newVal.getPhone());
                // DOB parse and set is complex; keeping as text/password placeholders for simplicity.
                // In a real app, you'd parse newVal.getDateOfBirth() into LocalDate.
                passwordInput.setText(newVal.getPassword()); 
            } else {
                 nameInput.clear(); emailInput.clear(); phoneInput.clear(); dobPicker.setValue(null); passwordInput.clear();
            }
        });

        Button updateBtn = new Button("Apply All Changes");
        updateBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 8 15; -fx-cursor: hand;");
        updateBtn.setOnAction(e -> {
            Customer selected = customerSelection.getValue();
            if (selected == null) { setStatus("Error: Select a customer first.", "#F44336"); return; }
            
            String sql = "UPDATE customer SET name=?, email=?, phone=?, date_of_birth=?, password=? WHERE customer_id=?";
            
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                     
                pstmt.setString(1, nameInput.getText());
                pstmt.setString(2, emailInput.getText());
                pstmt.setString(3, phoneInput.getText());
                pstmt.setString(4, dobPicker.getValue() != null ? dobPicker.getValue().toString() : selected.getDateOfBirth());
                pstmt.setString(5, passwordInput.getText());
                pstmt.setString(6, selected.getCustomerId());
                
                if (pstmt.executeUpdate() > 0) {
                    setStatus("Customer " + selected.getCustomerId() + " updated successfully.", "#f39c12");
                    loadAllCustomers();
                } else {
                    setStatus("No changes made or required fields empty.", "#95a5a6");
                }

            } catch (SQLException ex) {
                setStatus("Error updating customer: " + ex.getMessage(), "#F44336");
                ex.printStackTrace();
            }
        });

        box.getChildren().addAll(header, customerSelection, formGrid, updateBtn);
        return box;
    }

    // --- Delete Customer View (JDBC DELETE) ---
    private VBox createDeleteCustomerView() {
        VBox box = new VBox(20);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #ecf0f1;");
        Label header = new Label("Delete Customer");
        header.setFont(new Font("Arial", 20));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Table for selection
        TableView<Customer> customerTable = createCustomerTable();
        customerTable.setPrefHeight(300);

        Label warning = new Label("WARNING: Deleting a customer is permanent and may fail if they have active rentals!");
        warning.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

        Button deleteBtn = new Button("Permanently Delete Selected Customer");
        deleteBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-padding: 8 15; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> {
            Customer selected = customerTable.getSelectionModel().getSelectedItem();
            if (selected == null) { setStatus("Error: Select a customer to delete.", "#F44336"); return; }
            
            String sql = "DELETE FROM customer WHERE customer_id=?";
            
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                     
                pstmt.setString(1, selected.getCustomerId());
                
                if (pstmt.executeUpdate() > 0) {
                    setStatus("Customer " + selected.getCustomerId() + " DELETED.", "#c0392b");
                    loadAllCustomers(); // Refresh the list
                } else {
                    setStatus("Deletion failed or customer not found.", "#95a5a6");
                }

            } catch (SQLException ex) {
                setStatus("Deletion Error: Customer likely has foreign key constraints (active rentals).", "#F44336");
                ex.printStackTrace();
            }
        });

        box.getChildren().addAll(header, new Separator(), warning, new Label("Select Customer:"), customerTable, deleteBtn);
        return box;
    }

    // --- Specific Account Update Views (Simplified using updateCustomerFieldInDB helper) ---

    private VBox createChangeUsernameView() {
        return createSimpleUpdateView("Change Email (Username)", "email", "New Email Address");
    }

    private VBox createChangePhoneView() {
        return createSimpleUpdateView("Change Phone Number", "phone", "New Phone Number");
    }

    private VBox createChangeDobView() {
        // DOB input remains a TextField for simplicity (as it's stored as VARCHAR)
        return createSimpleUpdateView("Change Date of Birth", "date_of_birth", "New DOB (YYYY-MM-DD)");
    }

    private VBox createChangePasswordView() {
        return createSimpleUpdateView("Change Password", "password", "New Password");
    }

    /**
     * Reusable VBox layout for single field updates (Email, Phone, Password).
     */
    private VBox createSimpleUpdateView(String title, String dbField, String promptText) {
        VBox box = new VBox(20);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #ecf0f1;");
        Label header = new Label(title);
        header.setFont(new Font("Arial", 20));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        ComboBox<Customer> customerSelection = new ComboBox<>(customerData);
        customerSelection.setPromptText("Select Customer");
        
        TextField newInput = new TextField(); 
        newInput.setPromptText(promptText);

        VBox form = new VBox(10, 
            new Label("Customer:"), customerSelection,
            new Label(promptText + ":"), newInput
        );

        Button updateBtn = new Button("Update " + title.split(" ")[1]);
        updateBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 8 15; -fx-cursor: hand;");
        
        updateBtn.setOnAction(e -> {
            Customer selected = customerSelection.getValue();
            String newValue = newInput.getText().trim();
            if (selected == null || newValue.isEmpty()) { 
                setStatus("Error: Select customer and enter new value.", "#F44336");
                return; 
            }
            
            if (updateCustomerFieldInDB(selected.getCustomerId(), dbField, newValue)) {
                setStatus(title.split(" ")[1] + " updated for " + selected.getName(), "#f39c12");
                loadAllCustomers();
                newInput.clear();
                customerSelection.getSelectionModel().clearSelection();
            }
        });

        box.getChildren().addAll(header, new Separator(), form, updateBtn);
        return box;
    }

    // --- Car Management View (JDBC CREATE/UPDATE/DELETE) ---
    
    // (createCarManagementView and its helpers remain the same as they were already functional)
    // ... [Code for createCarManagementView, createCarTable, createCarSearchPanel] ...

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
       
        TextField carIdInput = new TextField(); carIdInput.setPromptText("ID (Auto-Assigned)"); carIdInput.setDisable(true);
        TextField companyInput = new TextField(); companyInput.setPromptText("Company (e.g., Toyota)");
        TextField modelInput = new TextField(); modelInput.setPromptText("Model (e.g., Camry)");
        TextField typeInput = new TextField(); typeInput.setPromptText("Type (e.g., Sedan)");
        TextField priceInput = new TextField(); priceInput.setPromptText("Price Per Day (e.g., 50.00)");
        TextField seatsInput = new TextField(); seatsInput.setPromptText("Number of Seats (e.g., 5)");

        formGrid.add(new Label("ID:"), 0, 0); formGrid.add(carIdInput, 1, 0);
        formGrid.add(new Label("Company:"), 0, 1); formGrid.add(companyInput, 1, 1);
        formGrid.add(new Label("Model:"), 0, 2); formGrid.add(modelInput, 1, 2);
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

        // Listener to populate form on selection
        carTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                carIdInput.setText(String.valueOf(newVal.getCarId()));
                companyInput.setText(newVal.getCompany());
                modelInput.setText(newVal.getModel());
                typeInput.setText(newVal.getType());
                priceInput.setText(String.valueOf(newVal.getPricePerDay()));
                seatsInput.setText(String.valueOf(newVal.getSeats()));
            } else {
                carIdInput.clear();
            }
        });

        // --- Event Handlers (JDBC Implementation) ---

        clearBtn.setOnAction(e -> {
            carIdInput.clear(); companyInput.clear(); modelInput.clear(); typeInput.clear(); priceInput.clear(); seatsInput.clear();
            carTable.getSelectionModel().clearSelection();
        });

        addBtn.setOnAction(e -> {
            String sql = "INSERT INTO car (company, model, type, price_per_day, seats) VALUES (?, ?, ?, ?, ?)";
            
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                     
                pstmt.setString(1, companyInput.getText());
                pstmt.setString(2, modelInput.getText());
                pstmt.setString(3, typeInput.getText());
                pstmt.setDouble(4, Double.parseDouble(priceInput.getText()));
                pstmt.setInt(5, Integer.parseInt(seatsInput.getText()));
                
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    setStatus("Car added successfully!", "#27ae60");
                    loadAllCars(); // Refresh data from DB
                }

            } catch (SQLException | NumberFormatException ex) {
                setStatus("Error adding car. Check data types and connection. " + ex.getMessage(), "#F44336");
                ex.printStackTrace();
            }
        });

        updateBtn.setOnAction(e -> {
            Car selectedCar = carTable.getSelectionModel().getSelectedItem();
            if (selectedCar == null) {
                setStatus("Please select a car to update.", "#F44336");
                return;
            }
            String sql = "UPDATE car SET company=?, model=?, type=?, price_per_day=?, seats=? WHERE car_id=?";
            
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                     
                pstmt.setString(1, companyInput.getText());
                pstmt.setString(2, modelInput.getText());
                pstmt.setString(3, typeInput.getText());
                pstmt.setDouble(4, Double.parseDouble(priceInput.getText()));
                pstmt.setInt(5, Integer.parseInt(seatsInput.getText()));
                pstmt.setInt(6, selectedCar.getCarId());
                
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    setStatus("Car ID " + selectedCar.getCarId() + " updated successfully!", "#f39c12");
                    loadAllCars(); // Refresh data from DB
                } else {
                    setStatus("No changes made or car not found.", "#95a5a6");
                }

            } catch (SQLException | NumberFormatException ex) {
                setStatus("Error updating car: " + ex.getMessage(), "#F44336");
                ex.printStackTrace();
            }
        });

        deleteBtn.setOnAction(e -> {
            Car selectedCar = carTable.getSelectionModel().getSelectedItem();
            if (selectedCar == null) {
                setStatus("Please select a car to delete.", "#F44336");
                return;
            }
            String sql = "DELETE FROM car WHERE car_id=?";
            
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                     
                pstmt.setInt(1, selectedCar.getCarId());
                
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    setStatus("Car " + selectedCar.getModel() + " deleted successfully!", "#c0392b");
                    loadAllCars(); // Refresh data from DB
                    clearBtn.fire(); // Clear the form
                }

            } catch (SQLException ex) {
                setStatus("Error deleting car: " + ex.getMessage(), "#F44336");
                ex.printStackTrace();
            }
        });


        // Search/Filter Panel
        VBox searchPanel = createCarSearchPanel(carTable);

        carMgtBox.getChildren().addAll(header, formGrid, buttonBox, searchPanel, new Label("All Car Details:"), carTable);
        return carMgtBox;
    }
   
    private TableView<Car> createCarTable() {
        TableView<Car> table = new TableView<>();
        table.setItems(carData);

        TableColumn<Car, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("carId"));

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
       
        table.getColumns().addAll(idCol, companyCol, modelCol, typeCol, priceCol, seatsCol);
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
        applyFilterBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 8 15; -fx-cursor: hand;");
        clearFilterBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 8 15; -fx-cursor: hand;");

        // Simple filtering logic (in-memory filtering of current loaded data)
        applyFilterBtn.setOnAction(e -> {
            String field = groupByCombo.getValue();
            String filterValue = filterInput.getText().trim().toLowerCase();
           
            if (field != null && !filterValue.isEmpty()) {
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

    // --- Customer Details View (JDBC READ) -----------------------------------------------

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

        // --- Event Handler (In-memory filter for demonstration) ---
        searchBtn.setOnAction(e -> {
            String query = searchInput.getText().trim().toLowerCase();
           
            // In-memory filter placeholder
            ObservableList<Customer> filteredList = customerData.stream()
                .filter(cust -> cust.getName().toLowerCase().contains(query) ||
                                 cust.getCustomerId().toLowerCase().contains(query) ||
                                 cust.getEmail().toLowerCase().contains(query))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
            customerTable.setItems(filteredList);
            
            // To implement true JDBC search, you would call a new loadCustomer() method with a WHERE clause here.
        });

        customerBox.getChildren().addAll(header, searchHBox, new Label("All Customer Details:"), customerTable);
        return customerBox;
    }
   
    private TableView<Customer> createCustomerTable() {
        TableView<Customer> table = new TableView<>();
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
       
        TableColumn<Customer, String> dobCol = new TableColumn<>("Date of Birth");
        dobCol.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));

        TableColumn<Customer, String> passwordCol = new TableColumn<>("Password");
        passwordCol.setCellValueFactory(new PropertyValueFactory<>("password"));
       
        table.getColumns().addAll(idCol, nameCol, emailCol, phoneCol, dobCol, passwordCol);
        return table;
    }

    private VBox createReportsView() {
        VBox reportBox = new VBox(20);
        reportBox.setPadding(new Insets(20));
        reportBox.setAlignment(Pos.CENTER);
        reportBox.setStyle("-fx-background-color: #ecf0f1;");

        Label header = new Label("Fetch Reports for Analysis (Mock)");
        header.setFont(new Font("Arial", 20));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
       
        Label info = new Label("This view would typically run complex JOIN and GROUP BY queries on the database.");
        info.setStyle("-fx-text-fill: #95a5a6;");
       
        // Mock data visualization
        TextArea reportOutput = new TextArea();
        reportOutput.setEditable(false);
        reportOutput.setPrefHeight(300);
        reportOutput.setText("--- Monthly Revenue Summary (Mock Data) ---\n" +
                             "Total Rentals: 35\n" +
                             "Active Rentals: 3\n" +
                             "Total Revenue: ₹1,55,000\n" +
                             "Top Car Model: Honda CR-V\n" +
                             "-----------------------------------------\n");

        Button generateBtn = new Button("Generate Standard Report (Monthly)");
        generateBtn.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-padding: 10 20; -fx-cursor: hand;");
       
        generateBtn.setOnAction(e -> {
            setStatus("Running simulated report query...", "#e67e22");
            // In a real app, this runs a complex JDBC query.
        });

        reportBox.getChildren().addAll(header, info, reportOutput, generateBtn);
        return reportBox;
    }

    public static void main(String[] args) {
        launch();
    }
}
