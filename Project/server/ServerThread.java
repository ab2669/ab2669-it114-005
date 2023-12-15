package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import common.Constants;
import common.Payload;
import common.PayloadType;
import common.Phase;
import common.RoomResultPayload;
import common.Board;


public class ServerThread extends Thread 
{
    protected Socket client;
    private String clientName;
    private boolean isRunning = false;
    private ObjectOutputStream out;
    protected Room currentRoom;
    private static Logger logger = Logger.getLogger(ServerThread.class.getName());
    private long myClientId;
    private List<ServerThread> clients;
    private static Map<Long, String> clientColors = new HashMap<>();
    private Socket socket;
    //
    private Board board;
    
    public void sendPayload(Payload payload) 
    {
        try 
        {
            out.writeObject(payload);
            out.flush();
        } 
        catch (IOException e) 
        {
            logger.warning("Error sending payload: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public boolean sendPayload(long from, Payload payload) 
    {
        try 
        {
            payload.setClientId(from);
            out.writeObject(payload);
            out.flush();
            return true;
        } catch (IOException e) 
        {
            logger.warning("Error sending payload: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public void setClientId(long id) 
    {
        myClientId = id;
    }

    public long getClientId() 
    {
        return myClientId;
    }

    public void sendPayloadToClient(Payload payload, long clientId) throws IOException
    {
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        out.writeObject(payload);
    }

    public boolean isRunning() 
    {
        return isRunning;
    }
    
    public Board getBoard()
    {
        return board;
    }

    public void setBoard(Board board)
    {
        this.board = board;
    }

    public void fillCell(int x, int y, String color)
    {
        board.fillCell(x, y, color);
    }

    protected void setClientName(String name) 
    {
        if (name == null || name.isBlank()) 
        {
            logger.warning("Invalid name being set");
            return;
        }
        clientName = name;
    }

    public String getClientName() 
    {
        return clientName;
    }

    protected synchronized Room getCurrentRoom() 
    {
        return currentRoom;
    }

    protected synchronized void setCurrentRoom(Room room) 
    {
        if (room != null) 
        {
            currentRoom = room;
        } 
        else 
        {
            logger.info("Passed in room was null, this shouldn't happen");
        }
    }

    public void disconnect() 
    {
        sendConnectionStatus(myClientId, getClientName(), false);
        logger.info("Thread being disconnected by server");
        isRunning = false;
        cleanup();
    }

    public ServerThread(Socket myClient, Room room, Board board, List<ServerThread> clients) 
    {
        logger.info("ServerThread created");
        this.client = myClient;
        this.currentRoom = room;
        this.board = board;
        this.clients = clients;

    }

    // send methods
    public boolean sendPhaseSync(Phase phase) 
    {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.PHASE);
        p.setMessage(phase.name());
        return send(p);
    }

    public boolean sendReadyStatus(long clientId) 
    {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.READY);
        p.setClientId(clientId);
        return send(p);
    }

    public boolean sendRoomName(String name) 
    {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.JOIN_ROOM);
        p.setMessage(name);
        return send(p);
    }

    public boolean sendRoomsList(String[] rooms, String message) 
    {
        RoomResultPayload payload = new RoomResultPayload();
        payload.setRooms(rooms);
        if (message != null) 
        {
            payload.setMessage(message);
        }
        return send(payload);
    }

    public boolean sendExistingClient(long clientId, String clientName) 
    {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.SYNC_CLIENT);
        p.setClientId(clientId);
        p.setClientName(clientName);
        return send(p);
    }

    public boolean sendResetUserList() 
    {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.RESET_USER_LIST);
        return send(p);
    }

    public boolean sendClientId(long id) 
    {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.CLIENT_ID);
        p.setClientId(id);
        return send(p);
    }

    public boolean sendMessage(long clientId, String message) 
    {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.MESSAGE);
        p.setClientId(clientId);
        p.setMessage(message);
        return send(p);
    }

    public boolean sendConnectionStatus(long clientId, String who, boolean isConnected) 
    {
        Payload p = new Payload();
        p.setPayloadType(isConnected ? PayloadType.CONNECT : PayloadType.DISCONNECT);
        p.setClientId(clientId);
        p.setClientName(who);
        p.setMessage(String.format("%s the room %s", (isConnected ? "Joined" : "Left"), currentRoom.getName()));
        return send(p);
    }

    ///
    public boolean sendCoordinatesAndColor(long senderClientId, int x, int y, String color)
    {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.COORDINATES_AND_COLOR);
        p.setClientId(senderClientId);
        p.setX(x);
        p.setY(y);
        p.setColor(color);
        return send(p);
    }

    protected boolean send(Payload payload) 
    {
        try 
        {
            logger.log(Level.FINE, "Outgoing payload: " + payload);
            out.writeObject(payload);
            logger.log(Level.INFO, "Sent payload: " + payload);
            return true;
        } 
        catch (IOException e) 
        {
            logger.info("Error sending message to client (most likely disconnected)");
            e.printStackTrace();
            cleanup();
            return false;
        } 
        catch (NullPointerException ne) 
        {
            logger.info("Message was attempted to be sent before outbound stream was opened: " + payload);
            //e.printStackTrace();
            return true;
        }
    }

    // end send methods

    @Override
    public void run() 
    {
        try (ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(client.getInputStream());) 
        {
            this.out = out;
            isRunning = true;
            Payload fromClient;
            
            while (isRunning && (fromClient = (Payload) in.readObject()) != null) 
            {
                logger.info("Received from client: " + fromClient);
                processPayload(fromClient);
                handleMessage(fromClient);

                if (fromClient.getPayloadType() == PayloadType.MESSAGE)
                {
                    handleGuessCommand(fromClient);
                }
                else if (fromClient.getPayloadType() == PayloadType.COMMAND)
                {
                    handleClientCommand(fromClient);
                }
                else
                {
                    processPayload(fromClient);
                }
                
            }
        } 
        catch (IOException | ClassNotFoundException e) 
        {
            // happens when client disconnects
            e.printStackTrace();
            logger.info("Client disconnected");
        } 
        finally 
        {
            isRunning = false;
            logger.info("Exited thread loop. Cleaning up connection");
            cleanup();
        }
    }

    private void handleMessage(Payload payload) {
        if (payload.getPayloadType() == PayloadType.MESSAGE) {
            String message = payload.getMessage();
            if (message != null) {
                if (message.startsWith("/guess")) {
                    handleGuessCommand(payload);
                } else {
                    // If it's not a guess command, treat it as a regular message
                    Room currentRoom = getCurrentRoom();
                    if (currentRoom != null) {
                        currentRoom.sendMessage(this, message);
                    }
                }
            }
        }
    }

    private void processPayload(Payload p) 
    {
        switch (p.getPayloadType()) 
        {
            case CONNECT:
                setClientName(p.getClientName());
                break;
            case DISCONNECT:
                Room.disconnectClient(this, getCurrentRoom());
                break;
            case MESSAGE:
                if (currentRoom != null) 
                {
                    currentRoom.sendMessage(this, p.getMessage());
                } 
                else 
                {
                    logger.log(Level.INFO, "Migrating to lobby on message with null room");
                    Room.joinRoom(Constants.LOBBY, this);
                }
                ///
                if (p.getMessage().startsWith("/guess"))
                {
                    handleGuessCommand(p);
                }
                break;
            case GET_ROOMS:
                Room.getRooms(p.getMessage().trim(), this);
                break;
                ///
            case BOARD_COMMAND:
                handleBoardCommand(p);
                break;
            case COMMAND:
                handleClientCommand(p);
                break;
            case BOARD_UPDATE:
                handleBoardUpdate(p);
                break;
            case COORDINATES_AND_COLOR:
                handleCoordinatesAndColor(p.getX(), p.getY(), p.getColor());
                break;
            case START_GUESS:
                handleStartGuessCommand(p);
                break;
            case GUESS:
                try
                {
                    handleGuessPayload(p);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                break;
            case CREATE_ROOM:
                Room.createRoom(p.getMessage().trim(), this);
                break;
            case JOIN_ROOM:
                Room.joinRoom(p.getMessage().trim(), this);
                break;
            case READY:
                try 
                {
                    ((GameRoom) currentRoom).setReady(this);
                } 
                catch (Exception e) 
                {
                    logger.severe(String.format("There was a problem during readyCheck %s", e.getMessage()));
                    e.printStackTrace();
                }
                break;
            //Handles Color Updates
            case COLOR_UPDATE:
                handleColorUpdate(p);
                break;
            default:
                break;
        }

    }

    //
    private void handleCoordinatesAndColor(int x, int y, String color)
    {
        currentRoom.broadcastCoordinatesAndColor(myClientId, x, y, color);
    }
    
    private void handleGuessPayload(Payload p) throws IOException 
    {
        String guessedWord = p.getMessage();
        if (getCurrentRoom() != null && getCurrentRoom() instanceof GameRoom) 
        {
            GameRoom currentGameRoom = (GameRoom) getCurrentRoom();
            currentGameRoom.processGuess(this, guessedWord);
        }
    }

    private void handleGuessCommand(Payload commandPayload) {
        try {
            Room currentRoom = getCurrentRoom();
    
            if (commandPayload.getPayloadType() == PayloadType.MESSAGE) {
                // Handle regular chat message (e.g., broadcast it to the room)
                if (currentRoom != null) {
                    currentRoom.sendMessage(this, commandPayload.getMessage());
                }
            }
        } catch (Exception e) {
            logger.warning("Error handling guess command: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    


    /*
    private void handleGuessCommand(Payload commandPayload) 
    {
        try 
        {
            GameRoom currentGameRoom = (GameRoom) getCurrentRoom();
            setCurrentRoom(currentGameRoom);
    
            if (currentGameRoom != null && isPlayer()) 
            {
                String message = commandPayload.getMessage();
    
                if (message != null && message.startsWith("/guess")) 
                {
                    String[] parts = message.split(" ");
                    if (parts.length >= 2) 
                    {
                        String guessedWord = parts[1];
                        currentGameRoom.processGuess(this, guessedWord);
                    } 
                    else 
                    {
                        logger.warning("Invalid /guess command. Usage: /guess <word>");
                    }
                }
            }
        } 
        catch (Exception e) 
        {
            logger.warning("Error handling guess command: " + e.getMessage());
            e.printStackTrace();
        }
    }
    */
    
    private void handleColorUpdate(Payload p)
    {
        String color = p.getColor();
        if (color != null && !color.isEmpty())
        {
            if (!color.equals(getCurrentColor()))
            {
                broadcastColorUpdate(color);
                setCurrentColor(color);
            }
        }
    }

    private void handleBoardUpdate(Payload p)
    {
        Board updatedBoard = p.getBoard();
        if (updatedBoard != null)
        {
            this.board.updateBoard(updatedBoard);
            //this.board.toString();
            System.out.println("Board is updated!");
        }
        else
        {
            logger.warning("Received BOARD_UPDATE payload with null board.");
        }
    }

    private void broadcastColorUpdate(String color)
    {
        if (currentRoom != null)
        {
            currentRoom.sendMessage(this, String.format("Color updated to %s", color));
        }
    }

    private String setCurrentColor(String color)
    {
        clientColors.put(myClientId, color);
        return color;
    }

    private String getCurrentColor()
    {
        return clientColors.getOrDefault(myClientId, "");
    }

    private void handleBoardCommand(Payload p)
    {
        int x = p.getX();
        int y = p.getY();
        String color = p.getColor();

        GameRoom currentGameRoom = (GameRoom) getCurrentRoom();
        
        if (currentGameRoom != null && currentGameRoom.getBoard() != null) 
        {
            currentGameRoom.getBoard().fillCell(x, y, color);
            currentGameRoom.broadcastBoardUpdate();
        } 
        else 
        {
            logger.warning("Cannot handle board command. Room or board not initialized.");
        }
    }

    private void handleClientCommand(Payload commandPayload)
    {
        String command = commandPayload.getMessage();

        if (command.startsWith("/startguess"))
        {
            handleStartGuessCommand(commandPayload);
        }
    }

    private void handleStartGuessCommand(Payload commandPayload) 
    {
        try 
        {
            Room currentRoom = getCurrentRoom();
    
            if (currentRoom instanceof GameRoom) 
            {
                GameRoom currentGameRoom = (GameRoom) currentRoom;
    
                if (isPlayer()) 
                {
                    String wordToDraw = currentGameRoom.getWordToDraw();
                    if (wordToDraw != null && !wordToDraw.isEmpty()) 
                    {
                        sendStartGuessToPlayer(wordToDraw, commandPayload.getClientId());
                    } 
                    else 
                    {
                        logger.warning("Word to draw is not available.");
                        return;
                    }
                }
                currentGameRoom.startNewRound();
                currentGameRoom.broadcastStartGuess(commandPayload);
            } 
            else 
            {
                logger.warning("Cannot start guessing. Game room not initialized.");
            }
        } 
        catch (Exception e) 
        {
            logger.warning("Error handling startguess command: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void sendStartGuessToPlayer(String wordToDraw, long clientId) throws IOException 
    {
        Payload payload = new Payload();
        payload.setPayloadType(PayloadType.START_GUESS);
        payload.setMessage("Starting a new round. Word to draw: " + wordToDraw);
    
        payload.setClientId(clientId);
    
        sendPayloadToClient(payload, clientId);
    }
    
    private boolean isPlayer()
    {
        if (clients == null || clients.isEmpty())
        {
            return false;
        }
        return myClientId < clients.get(0).getClientId();
    }
    
    protected void broadcastBoardUpdate() 
    {
        if (currentRoom != null) 
        {
            List<ServerThread> roomClients = currentRoom.getClients();
            for (ServerThread client : roomClients) 
            {
                if (client != this) 
                {
                    try 
                    {
                        if (currentRoom instanceof GameRoom)
                        {
                            GameRoom gameRoom = (GameRoom) currentRoom;
                            client.sendBoardUpdate(gameRoom.getBoard());
                        }
                        else
                        {
                            logger.warning("Cannot send board update. Room is not a GameRoom.");
                        }
                    } 
                    catch (IOException e) 
                    {
                        logger.warning("Failed to broadcast board update to client " + client.getClientId());
                    }
                }
            }
        
        } 
        else 
        {
            logger.warning("Cannot broadcast board update. GameRoom is null.");
        }
    }

    protected boolean sendBoardUpdate(Board board) throws IOException 
    {
        Payload payload = new Payload();
        payload.setPayloadType(PayloadType.BOARD_UPDATE);
        payload.setBoard(board);

        try
        {
            out.writeObject(payload);
            return true;
        }
        catch (IOException e)
        {
            logger.warning("Failed to send BOARD_UPDATE payload to client " + getClientId());
            e.printStackTrace();
            return false;
        }
    }

    private void cleanup() 
    {
        logger.info("Thread cleanup() start");
        try 
        {
            if (out != null) 
            {
                out.close();
            }
            client.close();
            logger.info("Client disconnected intentionally");
        } 
        catch (IOException e) 
        {
            logger.info("Error closing client resources");
        }
        logger.info("Thread cleanup() complete");
    }

}