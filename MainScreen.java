package manjunath;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainScreen {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Main Screen");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            JPanel mainPanel = createMainPanel(frame);
            frame.getContentPane().add(mainPanel);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static JPanel createMainPanel(JFrame frame) {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // AUCTION PLATFORM panel
        JPanel auctionPlatformPanel = new JPanel();
        auctionPlatformPanel.setBorder(BorderFactory.createTitledBorder("AUCTION PLATFORM"));

        auctionPlatformPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Customer Button
        JButton customerButton = new JButton("CUSTOMER");
        customerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openCustomerPanel();
            }
        });

        // Admin Button
        JButton adminButton = new JButton("ADMIN");
        adminButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openAdminPanel(frame);
            }
        });
        auctionPlatformPanel.add(customerButton);
        auctionPlatformPanel.add(adminButton);

        // Add AUCTION PLATFORM panel to the main panel
        mainPanel.add(Box.createVerticalGlue()); 
        mainPanel.add(auctionPlatformPanel);
        mainPanel.add(Box.createVerticalGlue());

        return mainPanel;
    }

    private static void openCustomerPanel() {
        AuctionApp auctionApp = new AuctionApp();
        String timerValue = "1:00";
        CustomerFrame customerFrame = new CustomerFrame(auctionApp, timerValue);
        customerFrame.display();
    }

    private static void openAdminPanel(JFrame frame) {
    	frame.dispose();
        new AuctionApp().createAdminFrame();
    }
}
