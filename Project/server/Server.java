package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;

import common.Board;
import common.Constants;
import common.Payload;
import common.PayloadType;

public enum Server 
{
    INSTANCE;

    int port = 3000;
    private static Logger logger = Logger.getLogger(Server.class.getName());
    private List<Room> rooms = new ArrayList<Room>();
    private Room lobby = null;// default room
    private long nextClientId = 1;

    private Queue<ServerThread> incomingClients = new LinkedList<ServerThread>();
    private volatile boolean isRunning = false;

    private void start(int port) 
    {
        this.port = port;
        try (ServerSocket serverSocket = new ServerSocket(port);) 
        {
            Socket incoming_client = null;
            logger.info(String.format("Server is listening on port %s", port));
            isRunning = true;
            startQueueManager();
            // create a lobby on start
            lobby = new Room(Constants.LOBBY, port, nextClientId, null);
            rooms.add(lobby);
            do 
            {
                logger.info("Waiting for next client");
                if (incoming_client != null) 
                {
                    logger.info("Client connected");
                    ServerThread sClient = new ServerThread(incoming_client, lobby, null, null);
                    sClient.start();
                    incomingClients.add(sClient);
                    incoming_client = null;
                }
            } 
            while ((incoming_client = serverSocket.accept()) != null);
        }
        catch (IOException e)
        {
            logger.severe("Error accepting connection");
            e.printStackTrace();
        }
        finally 
        {
            logger.info("Closing Server Socket");
        }
    }

    void startQueueManager() 
    {
        new Thread() 
        {
            @Override
            public void run() 
            {
                while (isRunning) 
                {
                    try 
                    {
                        Thread.sleep(5);
                    } 
                    catch (InterruptedException e) 
                    {
                        e.printStackTrace();
                    }
                    if (incomingClients.size() > 0) 
                    {
                        ServerThread ic = incomingClients.peek();
                        if (ic != null) 
                        {
                            // wait for the thread to start and for the client to send the client name
                            // (username)
                            if (ic.isRunning() && ic.getClientName() != null) 
                            {
                                handleIncomingClient(ic);
                                incomingClients.poll();
                            }
                        }
                    }
                }
            }
        }
        .start();
    }

    void handleIncomingClient(ServerThread client) 
    {
        client.setClientId(nextClientId);// server reference
        client.sendClientId(nextClientId);// client reference
        nextClientId++;
        if (nextClientId < 0) // will use overflow to reset our counter
        {
            nextClientId = 1;
        }
        joinRoom(Constants.LOBBY, client);
    }

    
    private Room getRoom(String roomName) 
    {
        for (int i = 0, l = rooms.size(); i < l; i++) 
        {
            if (rooms.get(i).getName().equalsIgnoreCase(roomName)) 
            {
                return rooms.get(i);
            }
        }
        return null;
    }

    protected synchronized boolean joinRoom(String roomName, ServerThread client) 
    {
        Room newRoom = roomName.equalsIgnoreCase(Constants.LOBBY) ? lobby : getRoom(roomName);
        Room oldRoom = client.getCurrentRoom();
        if (newRoom != null && roomName != null) 
        {
            if (oldRoom != null && oldRoom != newRoom) 
            {
                logger.info(String.format("Client %s leaving old room %s", client.getClientName(), oldRoom.getName()));
                oldRoom.removeClient(client);
            }
            logger.info(String.format("Client %s joining new room %s", client.getClientName(), newRoom.getName()));
            newRoom.addClient(client);

            Board board = newRoom.getBoard();
            if (board != null)
            {
                Payload boardPayload = new Payload();
                boardPayload.setPayloadType(PayloadType.BOARD_UPDATE);
                boardPayload.setBoard(board);

                logger.info(String.format("Board Details: \n%s", board.printBoard()));
                newRoom.broadcastBoardUpdate(client.getClientId(), boardPayload);
            }

            return true;
        }
        return false;
    }

    protected synchronized boolean createNewRoom(String roomName) 
    {
        if (getRoom(roomName) != null) 
        {
            logger.info(String.format("Room %s already exists", roomName));
            return false;
        } 
        else 
        {
            Board board = new Board(20,20);

            GameRoom room = new GameRoom(roomName, port, nextClientId, board);
            rooms.add(room);
            
            logger.info(String.format("Created new room %s", roomName));
            return true;
        }
        /*
        roomName = roomName.trim();

        if(roomName.isEmpty())
        {
            logger.info("Invalid room name (empty)");
            return false;
        }

        if (getRoom(roomName) != null)
        {
            Board board = new Board(20,20);

            GameRoom room = new GameRoom(roomName, port, nextClientId, board);
            rooms.add(room);
            logger.info(String.format("Created new room %s", roomName));
            return true;
        }
        else
        {
            logger.info(String.format("Room %s already exists", roomName));
            return false;
        }
        */
        /* 
        Room existingRoom = getRoom(roomName);
        System.out.println("Existing Room: " + existingRoom);
        if (getRoom(roomName) != null) 
        {
            //Creating new board for room
            Board board = new Board(20, 20);
            //Create new room with the given board
            GameRoom room = new GameRoom(roomName, port, nextClientId, board);
            rooms.add(room);
            logger.info(String.format("Created new room %s", roomName));
            return true;
        } 
        else 
        {
            logger.info(String.format("Room %s already exits", roomName));
            return false;
        }
        */
    }

    
    protected synchronized List<String> getRooms(String query) 
    {
        return getRooms(query, 10);
    }

    protected synchronized List<String> getRooms(String query, int limit) 
    {
        List<String> matchedRooms = new ArrayList<String>();
        synchronized (rooms) 
        {
            Iterator<Room> iter = rooms.iterator();
            while (iter.hasNext()) 
            {
                Room r = iter.next();
                if (r.isRunning() && r.getName().toLowerCase().contains(query.toLowerCase())) 
                {
                    matchedRooms.add(r.getName());
                    if (matchedRooms.size() >= limit) 
                    {
                        break;
                    }
                }
            }
        }
        return matchedRooms;
    }

    protected synchronized void removeRoom(Room r) 
    {
        if (rooms.removeIf(room -> room == r)) 
        {
            logger.info(String.format("Removed empty room %s", r.getName()));
        }
    }

    protected synchronized void broadcast(String message) 
    {
        if (processCommand(message)) 
        {
            return;
        }
        // loop over rooms and send out the message
        Iterator<Room> it = rooms.iterator();
        while (it.hasNext()) 
        {
            Room room = it.next();
            if (room != null) 
            {
                room.sendMessage(null, message);
            }
        }
    }

    //START
    private boolean processCommand(String message) 
    {
        System.out.println("Checking command: " + message);
        
        if (message.startsWith(PayloadType.CREATE_ROOM.toString()))
        {
            String roomName = message.replace(PayloadType.CREATE_ROOM.toString(), "".trim());
            handleCreateRoom(roomName);
            return true;
        }
        return false;
    }

    private void handleCreateRoom(String roomName)
    {
        roomName = roomName.trim();
        createNewRoom(roomName);
    }

    //END
    public static void main(String[] args) 
    {
        Server.logger.info("Starting server");
        int port = Server.INSTANCE.port;
        try 
        {
            port = Integer.parseInt(args[0]);
        } 
        catch (Exception e) 
        {
            // can ignore, will either be index out of bounds or type mismatch
            // will default to the defined value prior to the try/catch
        }
        Server.INSTANCE.start(port);
        Server.logger.info("Server stopped");
    }
}