package bankManagementSystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BankManagementSystem extends JFrame {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/bankdb";
    private static final String USER = "admin";
    private static final String PASS = "Pranavk@12345";

    private JTextField accountIdField, nameField, amountField, balanceField;
    private JTextArea resultArea;

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
        return conn;
    }

    public BankManagementSystem() {
        setTitle("Bank Management System");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());


        JPanel formPanel = new JPanel(new GridBagLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        

        JLabel accountIdLabel = new JLabel("Account ID: ");
        accountIdField = new JTextField(15);
        JLabel nameLabel = new JLabel("Name: ");
        nameField = new JTextField(15);
        JLabel amountLabel = new JLabel("Amount: ");
        amountField = new JTextField(15);
        JLabel balanceLabel = new JLabel("Balance: ");
        balanceField = new JTextField(15);
        balanceField.setEditable(false);


        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(accountIdLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(accountIdField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(nameLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(amountLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(amountField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(balanceLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(balanceField, gbc);


        JButton createButton = new JButton("Create Account");
        JButton depositButton = new JButton("Deposit");
        JButton withdrawButton = new JButton("Withdraw");
        JButton checkBalanceButton = new JButton("Check Balance");


        buttonPanel.add(createButton);
        buttonPanel.add(depositButton);
        buttonPanel.add(withdrawButton);
        buttonPanel.add(checkBalanceButton);


        resultArea = new JTextArea(10, 40);
        resultArea.setEditable(false);
        JScrollPane resultScrollPane = new JScrollPane(resultArea);


        add(formPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(resultScrollPane, BorderLayout.SOUTH);


        createButton.addActionListener(new CreateAccountAction());
        depositButton.addActionListener(new DepositAction());
        withdrawButton.addActionListener(new WithdrawAction());
        checkBalanceButton.addActionListener(new CheckBalanceAction());

        setVisible(true);
    }


    private class CreateAccountAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try (Connection conn = connect()) {
                String name = nameField.getText();
                double balance = Double.parseDouble(amountField.getText());

                String sql = "INSERT INTO accounts (name, balance) VALUES (?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
                stmt.setString(1, name);
                stmt.setDouble(2, balance);
                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int accountId = rs.getInt(1);
                    resultArea.setText("Account created successfully! Your account ID is: " + accountId);
                } else {
                    resultArea.setText("Account creation failed.");
                }

            } catch (SQLException ex) {
                resultArea.setText("Error creating account: " + ex.getMessage());
            }
        }
    }

    private class DepositAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try (Connection conn = connect()) {
                int accountId = Integer.parseInt(accountIdField.getText());
                double amount = Double.parseDouble(amountField.getText());

                String sql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setDouble(1, amount);
                stmt.setInt(2, accountId);
                int rowsUpdated = stmt.executeUpdate();

                if (rowsUpdated > 0) {
                    resultArea.setText("Deposit successful!");
                } else {
                    resultArea.setText("Account not found.");
                }

            } catch (SQLException ex) {
                resultArea.setText("Error depositing money: " + ex.getMessage());
            }
        }
    }

    private class WithdrawAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try (Connection conn = connect()) {
                int accountId = Integer.parseInt(accountIdField.getText());
                double amount = Double.parseDouble(amountField.getText());

                String sql = "SELECT balance FROM accounts WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, accountId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    double balance = rs.getDouble("balance");
                    if (balance >= amount) {
                        sql = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
                        stmt = conn.prepareStatement(sql);
                        stmt.setDouble(1, amount);
                        stmt.setInt(2, accountId);
                        stmt.executeUpdate();
                        resultArea.setText("Withdrawal successful!");
                    } else {
                        resultArea.setText("Insufficient funds.");
                    }
                } else {
                    resultArea.setText("Account not found.");
                }

            } catch (SQLException ex) {
                resultArea.setText("Error withdrawing money: " + ex.getMessage());
            }
        }
    }

    private class CheckBalanceAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try (Connection conn = connect()) {
                int accountId = Integer.parseInt(accountIdField.getText());

                String sql = "SELECT balance FROM accounts WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, accountId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    double balance = rs.getDouble("balance");
                    balanceField.setText(String.valueOf(balance));
                    resultArea.setText("Current balance: " + balance);
                } else {
                    resultArea.setText("Account not found.");
                }

            } catch (SQLException ex) {
                resultArea.setText("Error checking balance: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BankManagementSystem());
    }
}









//package bankManagementSystem;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.InputMismatchException;
//import java.util.Scanner;
//
//public class BankManagementSystem {
//
//    
//    private static final String DB_URL = "jdbc:mysql://localhost:3306/bankdb";
//    private static final String USER = "admin";
//    private static final String PASS = "Pranavk@12345";
//
//    public static Connection connect() {
//        Connection conn = null;
//        try {
//            conn = DriverManager.getConnection(DB_URL, USER, PASS);
//        } catch (SQLException e) {
//            System.out.println("Connection failed: " + e.getMessage());
//        }
//        return conn;
//    }
//
//	
//	public static void main(String[] args) {
//	    Scanner scanner = new Scanner(System.in);
//	    String choice = "";
//
//	    while (!choice.equals("5") && !choice.equals("exit")) {
//	        System.out.println("==== Bank Management System ====");
//	        System.out.println("1. Create Account");
//	        System.out.println("2. Deposit Money");
//	        System.out.println("3. Withdraw Money");
//	        System.out.println("4. Check Balance");
//	        System.out.println("5. Exit");
//	        System.out.println("Choose an option (or type 'exit' to exit): ");
//
//	        choice = scanner.nextLine().trim(); 
//
//	        try {
//	            switch (choice) {
//	                case "1":
//	                    try (Connection conn = connect()) {
//	                        System.out.print("Enter your name: ");
//	                        String name = scanner.nextLine();
//
//	                        System.out.print("Enter initial deposit: ");
//	                        double balance = scanner.nextDouble();
//	                        scanner.nextLine(); 
//
//	                        String sql = "INSERT INTO accounts (name, balance) VALUES (?, ?)";
//	                        PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
//	                        stmt.setString(1, name);
//	                        stmt.setDouble(2, balance);
//	                        stmt.executeUpdate();
//
//	                        
//	                        ResultSet rs = stmt.getGeneratedKeys();
//	                        if (rs.next()) {
//	                            int generatedId = rs.getInt(1); 
//	                            System.out.println("Account created successfully! Your account ID is: " + generatedId);
//	                        } else {
//	                            System.out.println("Account creation failed. No ID returned.");
//	                        }
//
//	                    } catch (SQLException e) {
//	                        System.out.println("Error creating account: " + e.getMessage());
//	                    } catch (InputMismatchException e) {
//	                        System.out.println("Invalid input. Please enter a valid number.");
//	                    }
//	                    break;
//
//	                case "2":
//	                    try (Connection conn = connect()) {
//	                        System.out.print("Enter account ID: ");
//	                        int accountId = scanner.nextInt();
//
//	                        System.out.print("Enter deposit amount: ");
//	                        double amount = scanner.nextDouble();
//	                        scanner.nextLine(); 
//
//	                        String sql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
//	                        PreparedStatement stmt = conn.prepareStatement(sql);
//	                        stmt.setDouble(1, amount);
//	                        stmt.setInt(2, accountId);
//	                        int rowsUpdated = stmt.executeUpdate();
//
//	                        if (rowsUpdated > 0) {
//	                            System.out.println("Deposit successful!");
//	                        } else {
//	                            System.out.println("Account not found.");
//	                        }
//
//	                    } catch (SQLException e) {
//	                        System.out.println("Error depositing money: " + e.getMessage());
//	                    } catch (InputMismatchException e) {
//	                        System.out.println("Invalid input. Please enter a valid number.");
//	                    }
//	                    break;
//
//	                case "3":
//	                    try (Connection conn = connect()) {
//	                        System.out.print("Enter account ID: ");
//	                        int accountId = scanner.nextInt();
//
//	                        System.out.print("Enter withdrawal amount: ");
//	                        double amount = scanner.nextDouble();
//	                        scanner.nextLine(); 
//
//	                        String sql = "SELECT balance FROM accounts WHERE id = ?";
//	                        PreparedStatement stmt = conn.prepareStatement(sql);
//	                        stmt.setInt(1, accountId);
//	                        ResultSet rs = stmt.executeQuery();
//
//	                        if (rs.next()) {
//	                            double currentBalance = rs.getDouble("balance");
//
//	                            if (currentBalance >= amount) {
//	                                sql = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
//	                                stmt = conn.prepareStatement(sql);
//	                                stmt.setDouble(1, amount);
//	                                stmt.setInt(2, accountId);
//	                                stmt.executeUpdate();
//	                                System.out.println("Withdrawal successful!");
//	                            } else {
//	                                System.out.println("Insufficient funds.");
//	                            }
//	                        } else {
//	                            System.out.println("Account not found.");
//	                        }
//
//	                    } catch (SQLException e) {
//	                        System.out.println("Error withdrawing money: " + e.getMessage());
//	                    } catch (InputMismatchException e) {
//	                        System.out.println("Invalid input. Please enter a valid number.");
//	                    }
//	                    break;
//
//	                case "4":
//	                    try (Connection conn = connect()) {
//	                        System.out.print("Enter account ID: ");
//	                        int accountId = scanner.nextInt();
//	                        scanner.nextLine(); 
//
//	                        String sql = "SELECT balance FROM accounts WHERE id = ?";
//	                        PreparedStatement stmt = conn.prepareStatement(sql);
//	                        stmt.setInt(1, accountId);
//	                        ResultSet rs = stmt.executeQuery();
//
//	                        if (rs.next()) {
//	                            double balance = rs.getDouble("balance");
//	                            System.out.println("Current balance: " + balance);
//	                        } else {
//	                            System.out.println("Account not found.");
//	                        }
//
//	                    } catch (SQLException e) {
//	                        System.out.println("Error checking balance: " + e.getMessage());
//	                    } catch (InputMismatchException e) {
//	                        System.out.println("Invalid input. Please enter a valid number.");
//	                    }
//	                    break;
//
//	                case "5":
//	                case "exit":
//	                    System.out.println("Thank you for using the Bank Management System!");
//	                    break;
//
//	                default:
//	                    System.out.println("Invalid choice, please try again.");
//	            }
//	        } catch (InputMismatchException e) {
//	            System.out.println("Invalid input. Please enter a valid number.");
//	            scanner.nextLine(); 
//	        } catch (Exception e) {
//	            System.out.println("Error processing input: " + e.getMessage());
//	        }
//
//	    }
//
//	    scanner.close(); 
//	}
//
//
//}


