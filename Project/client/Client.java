package client;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Logger;

import common.Payload;
import common.PayloadType;
import common.RoomResultPayload;
import common.TimedEvent;
import server.GameRoom;
import common.Constants;
import common.Board;

public enum Client 
{
    Instance;

    Socket server = null;
    ObjectOutputStream out = null;
    ObjectInputStream in = null;
    final String ipAddressPattern = "/connect\\s+(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d{3,5})";
    final String localhostPattern = "/connect\\s+(localhost:\\d{3,5})";
    boolean isRunning = false;
    private Thread inputThread;
    private Thread fromServerThread;
    private String clientName = "";
    private long myClientId = Constants.DEFAULT_CLIENT_ID;
    private static Logger logger = Logger.getLogger(Client.class.getName());
    private GameRoom currentGameRoom;
    private Hashtable<Long, String> userList = new Hashtable<Long, String>();

    //
    private Board board;

    /// START - supposed to implement board from Board.java
    public void initializeBoard(int rows, int cols)
    {
        board = new Board(rows, cols);
    }

    public void printBoard()
    {
        if (board != null)
        {
            board.printBoard();
        }
        else
        {
            System.out.println("Board not initialized.");
        }
    }
    
    public void fillCell(int row, int col, String color)
    {
        if (board != null)
        {
            board.fillCell(row, col, color);

            Payload payload = new Payload();
            payload.setPayloadType(PayloadType.COORDINATES_AND_COLOR);
            payload.setX(row);
            payload.setY(col);
            payload.setColor(color);

            sendPayload(payload);
        }
        else
        {
            System.out.println("Board not initialized");
        }
    }

    ///END

    private void sendPayload(Payload payload)
    {
        try
        {
            if (out != null)
            {
                out.writeObject(payload);
            }
            else
            {
                System.out.println("ObjectOutputStream not initialized.");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public boolean isConnected() 
    {
        if (server == null) 
        {
            return false;
        }
        return server.isConnected() && !server.isClosed() && !server.isInputShutdown() && !server.isOutputShutdown();

    }

    private boolean connect(String address, int port) 
    {
        try 
        {
            server = new Socket(address, port);
            out = new ObjectOutputStream(server.getOutputStream());
            in = new ObjectInputStream(server.getInputStream());
            logger.info("Client connected");
            initializeBoard(20, 20);
            listenForServerPayload();
            sendConnect();
        } 
        catch (UnknownHostException e) 
        {
            e.printStackTrace();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        return isConnected();
    }

    @Deprecated // remove in Milestone3
    private boolean isConnection(String text) 
    {
        // https://www.w3schools.com/java/java_regex.asp
        return text.matches(ipAddressPattern)
                || text.matches(localhostPattern);
    }

    @Deprecated // remove in Milestone3
    private boolean isQuit(String text) 
    {
        return text.equalsIgnoreCase("/quit");
    }

    @Deprecated // remove in Milestone3
    private boolean isName(String text) 
    {
        if (text.startsWith("/name")) 
        {
            String[] parts = text.split(" ");
            if (parts.length >= 2) 
            {
                clientName = parts[1].trim();
                System.out.println("Name set to " + clientName);
            }
            return true;
        }
        return false;
    }

    @Deprecated // removing in Milestone3
    private boolean processClientCommand(String text) throws IOException 
    {
        if (isConnection(text)) 
        {
            if (clientName.isBlank()) 
            {
                System.out.println("You must set your name before you can connect via: /name your_name");
                return true;
            }
            String[] parts = text.trim().replaceAll(" +", " ").split(" ")[1].split(":");
            connect(parts[0].trim(), Integer.parseInt(parts[1].trim()));
            return true;
        }
        else if (text.startsWith("/guess"))
        {
            //String guessedWord = text.replace("/guess", "".trim());
            handleGuessCommand(text);
            return true;
        }
        ///START - Board Command implementation
        else if (text.startsWith("/board"))
        {
            handleBoardCommand(text);
            return true;
        } 
        ///END
        ///START
        else if(text.startsWith("/startguess"))
        {
            sendStartGuess();
            return true;
        }
        ///END
        else if(text.startsWith("/guess"))
        {
                ///////add something
        }
        else if (isQuit(text)) 
        {
            sendDisconnect();
            isRunning = false;
            return true;
        } 
        else if (isName(text)) 
        {
            return true;
        } 
        else if (text.startsWith("/joinroom") || text.startsWith("/updateroom")) //added updateroom command to refresh board 
        {
            String roomName = text.replace("/joinroom", "").replace("/updateroom", "").trim();
            sendJoinRoom(roomName);
            return true;
        } 
        else if (text.startsWith("/createroom")) 
        {
            String roomName = text.replace("/createroom", "").trim();
            sendCreateRoom(roomName);
            return true;
        } 
        else if (text.startsWith("/rooms")) 
        {
            String query = text.replace("/rooms", "").trim();
            sendListRooms(query);
            return true;
        } 
        else if (text.equalsIgnoreCase("/users")) 
        {
            Iterator<Entry<Long, String>> iter = userList.entrySet().iterator();
            System.out.println("Listing Local User List:");
            if (userList.size() == 0) 
            {
                System.out.println("No local users in list");
            }
            while (iter.hasNext()) 
            {
                Entry<Long, String> user = iter.next();
                System.out.println(String.format("%s[%s]", user.getValue(), user.getKey()));
            }
            return true;
        }
        return false;
    }

    // Send methods
    protected void sendListRooms(String query) throws IOException 
    {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.GET_ROOMS);
        p.setMessage(query);
        out.writeObject(p);
    }

    protected void sendJoinRoom(String roomName) throws IOException 
    {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.JOIN_ROOM);
        p.setMessage(roomName);
        out.writeObject(p);
    }

    protected void sendCreateRoom(String roomName) throws IOException 
    {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.CREATE_ROOM);
        p.setMessage(roomName);
        out.writeObject(p);
    }

    protected void sendDisconnect() throws IOException 
    {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.DISCONNECT);
        out.writeObject(p);
    }

    protected void sendConnect() throws IOException 
    {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.CONNECT);
        p.setClientName(clientName);
        out.writeObject(p);
    }

    protected void sendMessage(String message) throws IOException 
    {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.MESSAGE);
        p.setMessage(message);
        p.setClientName(clientName);
        out.writeObject(p);
    }

    public void setCurrentGameRoom(GameRoom gameRoom) throws IOException
    {
        try
        {
            this.currentGameRoom = gameRoom;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }   
    }
    

    private void handleGuessCommand(String text) {
        // Split the text to extract the guessed word
        String[] parts = text.split(" ", 2); // Split into two parts
        if (parts.length == 2) {
            String guessedWord = parts[1].trim();
            sendGuessCommand(guessedWord);
        } else {
            System.out.println("Invalid guess command. Usage: /guess word");
        }
    }
    
    private void sendGuessCommand(String guessedWord) {
        // Send the guess command to the server
        try {
            Payload payload = new Payload();
            payload.setPayloadType(PayloadType.GUESS); // Change to GUESS payload type
            payload.setMessage(guessedWord);
            out.writeObject(payload);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception based on your requirements
        }
    }
    
    
    // end send methods
    @Deprecated // remove in Milestone3
    private void listenForKeyboard() 
    {
        inputThread = new Thread() 
        {
            @Override
            public void run() 
            {
                logger.info("Listening for input");
                try (Scanner si = new Scanner(System.in);) 
                {
                    String line = "";
                    isRunning = true;
                    while (isRunning) 
                    {
                        try 
                        {
                            logger.info("Waiting for input");
                            line = si.nextLine();
                            if (!processClientCommand(line)) 
                            {
                                if (isConnected()) {
                                    if (line != null && line.trim().length() > 0)
                                    {
                                        sendMessage(line);
                                    }

                                } 
                                else 
                                {
                                    logger.info("Not connected to server");
                                }
                            }
                        } 
                        catch (Exception e) 
                        {
                            logger.warning("Connection dropped");
                            break;
                        }
                    }
                    logger.info("Exited loop");
                } 
                catch (Exception e) 
                {
                    e.printStackTrace();
                } 
                finally 
                {
                    close();
                }
            }
        };
        inputThread.start();
    }
    
    ///START CHECKS FOR BOARD COMMAND
    private void handleBoardCommand(String text) throws IOException
    {
        String[] parts = text.split(" ");
        if (parts.length >=4)
        {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            String color = parts[3];

            sendBoardCommand(x, y, color);
        }
        else
        {
            System.out.println("Invalid board command. Usage: /board x y color");
        }
    }

    private void sendBoardCommand(int x, int y, String color) throws IOException
    {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.BOARD_COMMAND);
        p.setX(x);
        p.setY(y);
        p.setColor(color);

        out.writeObject(p);
    }
    ///END

    private void listenForServerPayload() {
        fromServerThread = new Thread() {
            @Override
            public void run() {
                try {
                    Payload fromServer;
                    // while we're connected, listen for objects from the server
                    while (isRunning && isConnected() && (fromServer = (Payload) in.readObject()) != null) {
                        processPayload(fromServer);
                    }
                    logger.info("listenForServerPayload() loop exited");
                } catch (IOException e) {
                    logger.warning("IOException in listenForServerPayload: " + e.getMessage());
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    logger.warning("ClassNotFoundException in listenForServerPayload: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    logger.info("Stopped listening to server input");
                    close();
                }
            }
        };
        fromServerThread.start(); // start the thread
    }
    

    protected String getClientNameById(long id) 
    {
        if (userList.containsKey(id)) 
        {
            return userList.get(id);
        }
        if (id == Constants.DEFAULT_CLIENT_ID) 
        {
            return "[Server]";
        }
        return "unknown user";
    }

    private void processPayload(Payload p) throws IOException 
    {
        switch (p.getPayloadType()) 
        {
            case CONNECT:
                if (!userList.containsKey(p.getClientId())) 
                {
                    userList.put(p.getClientId(), p.getClientName());
                }
                System.out.println(String.format("*%s %s*",
                        p.getClientName(),
                        p.getMessage()));
                break;
            case DISCONNECT:
                if (userList.containsKey(p.getClientId())) 
                {
                    userList.remove(p.getClientId());
                }
                if (p.getClientId() == myClientId) 
                {
                    myClientId = Constants.DEFAULT_CLIENT_ID;
                }
                System.out.println(String.format("*%s %s*",
                        p.getClientName(),
                        p.getMessage()));
                break;
            case SYNC_CLIENT:
                if (!userList.containsKey(p.getClientId())) 
                {
                    userList.put(p.getClientId(), p.getClientName());
                }
                break;
            case MESSAGE:
                System.out.println(String.format("%s: %s",
                        getClientNameById(p.getClientId()),
                        p.getMessage()));
                break;
            case CLIENT_ID:
                if (myClientId == Constants.DEFAULT_CLIENT_ID) 
                {
                    myClientId = p.getClientId();
                } 
                else 
                {
                    logger.warning("Receiving client id despite already being set");
                }
                break;
            case GET_ROOMS:
                RoomResultPayload rp = (RoomResultPayload) p;
                System.out.println("Received Room List:");
                if (rp.getMessage() != null) 
                {
                    System.out.println(rp.getMessage());
                } 
                else 
                {
                    for (int i = 0, l = rp.getRooms().length; i < l; i++) 
                    {
                        System.out.println(String.format("%s) %s", (i + 1), rp.getRooms()[i]));
                    }
                }
                break;
                ///
            case BOARD_UPDATE:
                handleBoardUpdate(p);
                break;
                ///
            case START_GUESS:
                handleStartGuess(p);
                break;
            case RESET_USER_LIST:
                userList.clear();
                break;
            default:
                logger.warning(String.format("Unhandled Payload type: %s", p.getPayloadType()));
                break;

        }
    }

    @Deprecated // removing in Milestone3
    public void start() throws IOException 
    {
        listenForKeyboard();
    }

    ///START - notifies user that board is updated
    private boolean isGameRoomInitialized = false;

    private void handleBoardUpdate(Payload p)
    {
        Board updatedBoard = p.getBoard();
        if (updatedBoard != null)
        {   
            if(board == null)
            {
                initializeBoard(updatedBoard.getRows(), updatedBoard.getCells());
            }
            this.board.updateBoard(updatedBoard);
            System.out.println("Board is updated!");
            updatedBoard.printBoard();
        
            if (updatedBoard.getGameRoom() != null)
            {
                currentGameRoom = updatedBoard.getGameRoom();
                isGameRoomInitialized = true;
            }
            else
            {
                logger.warning("Received BOARD_UPDATE payload with null board.");
            }
        }
    }
    ///END

    ///START
    private void handleStartGuess(Payload p) throws IOException {
        if (isGameRoomInitialized && p.getMessage().equalsIgnoreCase("/startguess")) {   
            logger.info("Debug: currentGameRoom is not null");
    
            Payload startGuessPayload = new Payload();
            startGuessPayload.setPayloadType(PayloadType.START_GUESS);
    
            String wordToDraw = currentGameRoom.getWordToDraw();
            String startGuessMessage = "Starting a new round. Word to draw: " + wordToDraw;
            sendMessage("[Server]: " + startGuessMessage);
            startGuessPayload.setMessage(startGuessMessage);
    
            // Send the word information to all clients
            out.writeObject(startGuessPayload);
            startGuessTimer();
        }
    }
    

    private void startGuessTimer() 
    {
        int timerDuration = 120;
        TimedEvent guessTimer = new TimedEvent(timerDuration, this::handleGuessTimerExpiration);
    
        this.guessTimer = guessTimer;
    }
    
    private TimedEvent guessTimer;
    
    private void cancelGuessTimer() 
    {
        if (guessTimer != null) 
        {
            guessTimer.cancel();
        }
    }

    private void handleGuessTimerExpiration()
    {
        if (currentGameRoom != null)
        {
            currentGameRoom.endRound();
            cancelGuessTimer();
        }
    }


    ///END
    /* 
    private void sendGuess(String guessedWord)
    {
        Payload guessPayload = new Payload();
        guessPayload.setPayloadType(PayloadType.GUESS);
        guessPayload.setMessage(guessedWord);

        try {
            out.writeObject(guessPayload);
        } catch (IOException e) {
            e.printStackTrace();
        }
    } */

    private void sendStartGuess()
    {
        Payload payload = new Payload();
        payload.setPayloadType(PayloadType.START_GUESS);
        try
        {
            out.writeObject(payload);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void close() 
    {
        myClientId = Constants.DEFAULT_CLIENT_ID;
        userList.clear();
        try 
        {
            inputThread.interrupt();
        } 
        catch (Exception e) 
        {
            System.out.println("Error interrupting input");
            e.printStackTrace();
        }
        try 
        {
            fromServerThread.interrupt();
        } 
        catch (Exception e) 
        {
            System.out.println("Error interrupting listener");
            e.printStackTrace();
        }
        try 
        {
            System.out.println("Closing output stream");
            out.close();
        } 
        catch (NullPointerException ne) 
        {
            System.out.println("Server was never opened so this exception is ok");
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        try 
        {
            System.out.println("Closing input stream");
            in.close();
        } 
        catch (NullPointerException ne) 
        {
            System.out.println("Server was never opened so this exception is ok");
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
        try 
        {
            System.out.println("Closing connection");
            server.close();
            System.out.println("Closed socket");
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        } 
        catch (NullPointerException ne) 
        {
            System.out.println("Server was never opened so this exception is ok");
        }
    }

    @Deprecated // removing in Milestone3
    public static void main(String[] args) 
    {
        try 
        {
            // if start is private, it's valid here since this main is part of the class
            Client.Instance.start();

        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }
}