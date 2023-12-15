
package client;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import client.views.ChatPanel;
import client.views.ConnectionPanel;
import client.views.Menu;
import client.views.RoomsPanel;
import client.views.UserInputPanel;
import common.Board;
import common.Constants;

public class ClientUI extends JFrame implements IClientEvents, ICardControls, IClientEvents.ConnectionCallback
{
    CardLayout card = null;// accessible so we can call next() and previous()
    Container container;// accessible to be passed to card methods
    String originalTitle = null;
    private static Logger logger = Logger.getLogger(ClientUI.class.getName());
    private JPanel currentCardPanel = null;
    private Card currentCard = Card.CONNECT;

    private Hashtable<Long, String> userList = new Hashtable<Long, String>();

    private long myId = Constants.DEFAULT_CLIENT_ID;
    private JMenuBar menu;
    // Panels
    private ConnectionPanel csPanel;
    private UserInputPanel inputPanel;
    private RoomsPanel roomsPanel;
    private ChatPanel chatPanel;
    private String username;
    
    public ClientUI(String title) {
        super(title);// call the parent's constructor
        Client.INSTANCE.setClientEventsListener(this);
        originalTitle = title;
        container = getContentPane();
        container.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // System.out.println("Resized to " + e.getComponent().getSize());
                // rough concepts for handling resize
                container.setPreferredSize(e.getComponent().getSize());
                container.revalidate();
                container.repaint();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                // System.out.println("Moved to " + e.getComponent().getLocation());
            }
        });

        setMinimumSize(new Dimension(400, 400));
        // centers window
        setLocationRelativeTo(null);
        card = new CardLayout();
        container.setLayout(card);
        // menu
        menu = new Menu(this);
        this.setJMenuBar(menu);
        // separate views
        csPanel = new ConnectionPanel(this);
        inputPanel = new UserInputPanel(this);
        roomsPanel = new RoomsPanel(this);
        chatPanel = new ChatPanel(this);

        addPanel(Card.CONNECT.name(), csPanel);
        addPanel(Card.USER_INPUT.name(), inputPanel);
        addPanel(Card.ROOMS.name(), roomsPanel);
        addPanel(Card.CHAT.name(), chatPanel);
        // https://stackoverflow.com/a/9093526
        // this tells the x button what to do (updated to be controlled via a prompt)
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                int response = JOptionPane.showConfirmDialog(container,
                        "Are you sure you want to close this window?", "Close Window?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                    try {
                        Client.INSTANCE.sendDisconnect();
                    } catch (NullPointerException | IOException e) {
                        e.printStackTrace();
                    }
                    System.exit(0);
                }
            }
        });
        // lastly
        pack();// tells the window to resize itself and do the layout management
        setVisible(true);
    }

    private void findAndSetCurrentPanel() {
        for (Component c : container.getComponents()) {
            if (c.isVisible()) {
                currentCardPanel = (JPanel) c;
                currentCard = Enum.valueOf(Card.class, currentCardPanel.getName());
                System.out.println("Current card: " + currentCardPanel.getName());
                // if we're not connected don't access anything that requires a connection
                /* 
                if (myId == Constants.DEFAULT_CLIENT_ID && currentCard.ordinal() >= Card.CHAT.ordinal()) {
                    show(Card.CONNECT.name());
                }*/
                break;
            }
        }
        System.out.println(currentCardPanel.getName());
    }

    @Override
    public void next() {
        card.next(container);
        findAndSetCurrentPanel();
        container.revalidate();
        container.repaint();
        

        for (Component c : container.getComponents()) {
            System.out.println("Panel Name: " + c.getName() + ", Visibility: " + c.isVisible());
        }
    }

    @Override
    public void previous() {
        card.previous(container);
        findAndSetCurrentPanel();
    }

    @Override
    public void show(String cardName) {
        card.show(container, cardName);
        findAndSetCurrentPanel();
        System.out.println("Showing card: " + cardName);

        System.out.println("Current card panel visibility: " + currentCardPanel.isVisible());

        currentCardPanel.repaint();
        currentCardPanel.revalidate();
    }

    @Override
    public void addPanel(String cardName, JPanel panel) {
        container.add(cardName, panel);
    }

    @Override
    public void connect() {
        String username = inputPanel.getUsername();
        String host = csPanel.getHost();
        int port = csPanel.getPort();
        setTitle(originalTitle + " - " + username);

        try {
            
            Client.INSTANCE.connect(host, port, username, this);

            while (!Client.INSTANCE.isConnected()) {
                Thread.sleep(100);  
            }

            //show(Card.CHAT.name());
            next();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void handleSuccessfulConnection() {
        SwingUtilities.invokeLater(() -> show(Card.CHAT.name()));
    }

    // ... (existing code)

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    public static void main(String[] args) {
        new ClientUI("Client");
    }

    private String mapClientId(long clientId) {
        String clientName = userList.get(clientId);
        if (clientName == null) {
            clientName = "Server";
        }
        return clientName;
    }

    /**
     * Used to handle new client connects/disconnects or existing client lists (one
     * by one)
     * 
     * @param clientId
     * @param clientName
     * @param isConnect
     */
    private synchronized void processClientConnectionStatus(long clientId, String clientName, boolean isConnect) {
        if (isConnect) {
            System.out.println("Processing client connection status");
            if (!userList.containsKey(clientId)) {
                logger.log(Level.INFO, String.format("Adding %s[%s]", clientName, clientId));
                userList.put(clientId, clientName);
                chatPanel.addUserListItem(clientId, String.format("%s (%s)", clientName, clientId));
            }
        } else {
            if (userList.containsKey(clientId)) {
                logger.log(Level.INFO, String.format("Removing %s[%s]", clientName, clientId));
                userList.remove(clientId);
                chatPanel.removeUserListItem(clientId);
            }
            if (clientId == myId) {
                logger.log(Level.INFO, "I disconnected");
                myId = Constants.DEFAULT_CLIENT_ID;
                previous();
            }
        }
    }

    @Override
    public void onClientConnect(long clientId, String clientName, String message) {
        System.out.println("onClientConnect invoked");
        if (currentCard.ordinal() >= Card.CHAT.ordinal()) {
            System.out.println("Card is CHAT or higher");
            processClientConnectionStatus(clientId, clientName, true);
            chatPanel.addText(String.format("*%s %s*", clientName, message));
            System.out.println("Calling handleSuccessfulConnection");
            handleSuccessfulConnection();
        }
        else {
            System.out.println("Card is not CHAT or higher");
        }
    }

    @Override
    public void onConnectionSuccess() {
        System.out.println("Connection successful");
    }

    @Override
    public void onClientDisconnect(long clientId, String clientName, String message) {
        if (currentCard.ordinal() >= Card.CHAT.ordinal()) {
            processClientConnectionStatus(clientId, clientName, false);
            chatPanel.addText(String.format("*%s %s*", clientName, message));
        }
    }

    @Override
    public void onMessageReceive(long clientId, String message) {
        if (currentCard.ordinal() >= Card.CHAT.ordinal()) {
            String clientName = mapClientId(clientId);
            chatPanel.addText(String.format("%s: %s", clientName, message));
        }
    }

    @Override
    public void onReceiveClientId(long id) {
        if (myId == Constants.DEFAULT_CLIENT_ID) {
            myId = id;
            show(Card.CHAT.name());
        } else {
            logger.log(Level.WARNING, "Received client id after already being set, this shouldn't happen");
        }
    }

    @Override
    public void onResetUserList() {
        userList.clear();
        chatPanel.clearUserList();
    }

    @Override
    public void onSyncClient(long clientId, String clientName) {
        if (currentCard.ordinal() >= Card.CHAT.ordinal()) {
            processClientConnectionStatus(clientId, clientName, true);
        }
    }

    @Override
    public void onReceiveRoomList(String[] rooms, String message) {
        roomsPanel.removeAllRooms();
        if (message != null && message.length() > 0) {
            roomsPanel.setMessage(message);
        }
        if (rooms != null) {
            for (String room : rooms) {
                roomsPanel.addRoom(room);
            }
        }
    }

    @Override
    public void onRoomJoin(String roomName) {
        if (currentCard.ordinal() >= Card.CHAT.ordinal()) {
            chatPanel.addText("Joined room " + roomName);
        }
    }

	@Override
	public void updateBoard(Board board) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'updateBoard'");
	}

	@Override
	public void onUpdateBoard(Board board) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'onUpdateBoard'");
	}
}


