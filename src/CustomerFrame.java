package manjunath;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerFrame {
    private AuctionApp auctionApp;
    private JFrame frame;
    private String timerValue;
    private JLabel timerLabel;
    private JLabel itemNameLabel;
    private JLabel itemPriceLabel;
    private Timer auctionTimer;
    private boolean auctionStarted = false;
    

    public CustomerFrame(AuctionApp auctionApp, String timerValue) {
        this.auctionApp = auctionApp;
        this.timerValue = timerValue;

        frame = new JFrame("Customer Frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Customer Panel Label
        JLabel customerPanelLabel = new JLabel("CUSTOMER PANEL");
        customerPanelLabel.setFont(new Font("Arial", Font.BOLD, 24));
        customerPanelLabel.setHorizontalAlignment(JLabel.CENTER);

        // Timer Label
        timerLabel = new JLabel("TIMER: " + timerValue);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        timerLabel.setHorizontalAlignment(JLabel.RIGHT);

        // Bidder details
        JLabel bidderNameLabel = new JLabel("Bidder Name:");
        JLabel bidPriceLabel = new JLabel("Bid Price:");

        // Text fields for bidder name and bid price
        JTextField bidderNameField = new JTextField();
        JTextField bidPriceField = new JTextField();
        // Bid button
        JButton bidButton = new JButton("Place Bid");
        bidButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placeBid(bidderNameField.getText(), Double.parseDouble(bidPriceField.getText()));
            }
        });
        itemNameLabel = new JLabel("ITEM NAME: ");
        itemPriceLabel = new JLabel("ITEM PRICE: ");
        setItemDetailsFromDatabase();
        auctionTimer = new Timer(1000, new ActionListener() {
            int minutes = Integer.parseInt(timerValue.split(":")[0]);
            int seconds = Integer.parseInt(timerValue.split(":")[1]);
            @Override
            public void actionPerformed(ActionEvent e) {
                if (auctionStarted) {
                    if (minutes >= 0 && seconds >= 0) {
                        updateTimerLabel(String.format("%02d:%02d", minutes, seconds));
                        if (seconds == 0) {
                            minutes--;
                            seconds = 59;
                        } else {
                            seconds--;
                        }
                    } else {
                        auctionTimer.stop(); 
                        JOptionPane.showMessageDialog(frame, "Auction Completed!");
                        // Close the customer frame
                        frame.dispose();
                        // Display the highest bid after auction ends
                        displayMaxBid();
                    }
                }
            }
        });
        // Layout
        GroupLayout layout = new GroupLayout(frame.getContentPane());
        frame.getContentPane().setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(customerPanelLabel, GroupLayout.Alignment.CENTER)
                .addComponent(timerLabel, GroupLayout.Alignment.TRAILING)
                .addComponent(itemNameLabel)
                .addComponent(itemPriceLabel)
                .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup()
                                .addComponent(bidderNameLabel)
                                .addComponent(bidPriceLabel))
                        .addGroup(layout.createParallelGroup()
                                .addComponent(bidderNameField)
                                .addComponent(bidPriceField)))
                .addComponent(bidButton)
        );
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(customerPanelLabel)
                .addComponent(timerLabel)
                .addComponent(itemNameLabel)
                .addComponent(itemPriceLabel)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(bidderNameLabel)
                        .addComponent(bidderNameField))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(bidPriceLabel)
                        .addComponent(bidPriceField))
                .addComponent(bidButton)
        );
        frame.pack();
        frame.setLocationRelativeTo(null);
    }
    public void startTimer() {
        if (!auctionStarted) {
            auctionStarted = true;
            auctionTimer.start();
        }
    }
    public void display() {
        frame.setVisible(true);
    }
    public void setItemDetails(String itemName, double itemPrice) {
        itemNameLabel.setText("ITEM NAME: " + itemName);
        itemPriceLabel.setText("ITEM PRICE: $" + itemPrice);
    }
    public void setItemDetailsFromDatabase() {
        try {
            String sql = "SELECT * FROM items LIMIT 1";
            try (ResultSet rs = auctionApp.getStatement().executeQuery(sql)) {
                if (rs.next()) {
                    String itemName = rs.getString("item_name");
                    double itemPrice = rs.getDouble("item_price");
                    setItemDetails(itemName, itemPrice);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void placeBid(String bidderName, double bidPrice) {
        try {
            // Update bids table with bidder information
            String insertBidSQL = "INSERT INTO bids (bidder_name, bid_price, item_name) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = auctionApp.getConnection().prepareStatement(insertBidSQL)) {
                pstmt.setString(1, bidderName);
                pstmt.setDouble(2, bidPrice);
                pstmt.setString(3, itemNameLabel.getText().substring(11)); // Extract item name from label
                pstmt.executeUpdate();
            }
            // Update items table with bidder information
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Display a confirmation message
        JOptionPane.showMessageDialog(frame, "Bid placed by " + bidderName + " for $" + bidPrice);
        // Update the highest bid details after placing a bid
        displayMaxBid();
    }

    // existing code...


    private void displayMaxBid() {
        try {
            String maxBidSQL = "SELECT * FROM bids WHERE item_name = ? ORDER BY bid_price DESC LIMIT 1";
            try (PreparedStatement pstmt = auctionApp.getConnection().prepareStatement(maxBidSQL)) {
                pstmt.setString(1, itemNameLabel.getText().substring(11)); // Extract item name from label
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String bidderName = rs.getString("bidder_name");
                        double bidPrice = rs.getDouble("bid_price");

                        // Check if the timer is still running
                        if (auctionTimer.isRunning()) {
                            // Wait until the timer completes
                            return;
                        }

                        // Display the highest bid after auction ends
                        JOptionPane.showMessageDialog(frame, "Auction Completed!\nHighest Bid:\nBidder: " + bidderName +
                                "\nBid Price: $" + bidPrice + "\nItem Name: " + itemNameLabel.getText().substring(11));

                        // Delete entries from items and bids tables
                        deleteEntries();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void deleteEntries() {
        try {
            // Delete from items table
            String deleteItemsSQL = "DELETE FROM items WHERE item_name = ?";
            try (PreparedStatement pstmt = auctionApp.getConnection().prepareStatement(deleteItemsSQL)) {
                pstmt.setString(1, itemNameLabel.getText().substring(11)); // Extract item name from label
                pstmt.executeUpdate();
            }

            // Delete from bids table
            String deleteBidsSQL = "DELETE FROM bids WHERE item_name = ?";
            try (PreparedStatement pstmt = auctionApp.getConnection().prepareStatement(deleteBidsSQL)) {
                pstmt.setString(1, itemNameLabel.getText().substring(11)); // Extract item name from label
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void updateTimerLabel(String time) {
        SwingUtilities.invokeLater(() -> {
            // Update the timer label
            timerLabel.setText("TIMER: " + time);
        });
    }
}
