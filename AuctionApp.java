package manjunath;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class AuctionApp {
    private Connection conn;
    private Statement stmt;
    private JFrame adminFrame;
    private JTable itemsTable;
    private DefaultTableModel tableModel;
    private JLabel timerLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AuctionApp auctionApp = new AuctionApp();
            auctionApp.createAdminFrame();
        });
    }

    public AuctionApp() {
        // Database setup
        try {
            String url = "jdbc:mysql://localhost:3306/manjunath";
            String user = "root";
            String password = "pcmb2003";

            conn = DriverManager.getConnection(url, user, password);
            stmt = conn.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS items (id INT AUTO_INCREMENT PRIMARY KEY, item_name VARCHAR(255), item_price DOUBLE)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public Statement getStatement() {
        return stmt;
    }

    public void createAdminFrame() {
        adminFrame = new JFrame("Admin Frame");
        adminFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Admin Panel Label
        JLabel adminPanelLabel = new JLabel("ADMIN PANEL");
        adminPanelLabel.setFont(new Font("Arial", Font.BOLD, 24));
        adminPanelLabel.setHorizontalAlignment(JLabel.CENTER);

        // Timer Label
        timerLabel = new JLabel("TIMER: 1:00");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        timerLabel.setHorizontalAlignment(JLabel.RIGHT);

        // Item details input
        JLabel itemNameLabel = new JLabel("Item Name:");
        JTextField itemNameField = new JTextField();
        JLabel itemPriceLabel = new JLabel("Item Price:");
        JTextField itemPriceField = new JTextField();
        JButton addItemButton = new JButton("Add Item");
        addItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addItem(itemNameField.getText(), Double.parseDouble(itemPriceField.getText()));
                displayItems();
            }
        });

        // Items table
        tableModel = new DefaultTableModel(new Object[]{"Item Name", "Item Price"}, 0);
        itemsTable = new JTable(tableModel);

        // Start Auction button
        JButton startAuctionButton = new JButton("Start Auction");
        startAuctionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               startAuction();
            }
        });
        // Layout
        GroupLayout layout = new GroupLayout(adminFrame.getContentPane());
        adminFrame.getContentPane().setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(adminPanelLabel, GroupLayout.Alignment.CENTER)
                .addComponent(timerLabel, GroupLayout.Alignment.TRAILING)
                .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup()
                                .addComponent(itemNameLabel)
                                .addComponent(itemPriceLabel)
                                .addComponent(addItemButton))
                        .addGroup(layout.createParallelGroup()
                                .addComponent(itemNameField)
                                .addComponent(itemPriceField)))
                .addComponent(itemsTable)
                .addComponent(startAuctionButton)
        );
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(adminPanelLabel)
                .addComponent(timerLabel)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(itemNameLabel)
                        .addComponent(itemNameField))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(itemPriceLabel)
                        .addComponent(itemPriceField))
                .addComponent(addItemButton)
                .addComponent(itemsTable)
                .addComponent(startAuctionButton)
        );
        adminFrame.pack();
        adminFrame.setLocationRelativeTo(null);
        adminFrame.setVisible(true);
    }
    private void addItem(String itemName, double itemPrice) {
        try {
            String sql = "INSERT INTO items (item_name, item_price) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, itemName);
                pstmt.setDouble(2, itemPrice);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void displayItems() {
        tableModel.setRowCount(0); // Clear existing items in the table
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM items");
            while (rs.next()) {
                String itemName = rs.getString("item_name");
                double itemPrice = rs.getDouble("item_price");
                tableModel.addRow(new Object[]{itemName, itemPrice});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public Connection getConnection() {
        return conn;
    }
    private void startAuction() {
String timerValue = timerLabel.getText().substring(7);
        adminFrame.dispose();
        CustomerFrame customerFrame = new CustomerFrame(AuctionApp.this, timerValue);
        customerFrame.startTimer();
        customerFrame.display();
    }
}
