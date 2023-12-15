package client.views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import client.Card;
import client.ICardControls;

public class UserInputPanel extends JPanel {
    private static Logger logger = Logger.getLogger(UserInputPanel.class.getName());
    private String username;

    public UserInputPanel(ICardControls controls) {
        super(new BorderLayout(10, 10));
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel userLabel = new JLabel("Username: ");
        JTextField userValue = new JTextField();
        JLabel userError = new JLabel();
        content.add(userLabel);
        content.add(userValue);
        content.add(userError);
        content.add(Box.createRigidArea(new Dimension(0, 200)));

        JButton pButton = new JButton("Previous");
        pButton.addActionListener((event) -> {
            controls.previous();
        });
        
        JButton nButton = new JButton("Connect");
        nButton.addActionListener((event) -> {
            handleUserInput(userValue.getText(), userError, controls);
            String enteredUsername = getUsername();
            controls.setUsername(enteredUsername);
            controls.next();
        });

        // button holder
        JPanel buttons = new JPanel();
        buttons.add(pButton);
        buttons.add(nButton);

        content.add(buttons);
        this.add(new JPanel(), BorderLayout.WEST);
        this.add(new JPanel(), BorderLayout.EAST);
        this.add(new JPanel(), BorderLayout.NORTH);
        this.add(new JPanel(), BorderLayout.SOUTH);
        this.add(content, BorderLayout.CENTER);
        this.setName(Card.USER_INFO.name());
        controls.addPanel(Card.USER_INFO.name(), this);
    }

    public String getUsername() {
        return username;
    }

    private void handleUserInput(String input, JLabel errorLabel, ICardControls controls) {
        boolean isValid = true;

        try {
            username = input.trim();
            if (username.length() == 0) {
                errorLabel.setText("Username must be provided");
                errorLabel.setVisible(true);
                isValid = false;
            }
        } catch (NullPointerException e) {
            errorLabel.setText("Username must be provided");
            errorLabel.setVisible(true);
            isValid = false;
        }
        if (isValid) {
            // System.out.println("Chosen username: " + username);
            logger.log(Level.INFO, "Chosen username: " + username);
            errorLabel.setVisible(false);
            controls.connect();
        }
    }
}

