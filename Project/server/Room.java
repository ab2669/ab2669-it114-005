package server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import common.Constants;
import common.Board;
import common.Payload;
import common.PayloadType;

public class Room implements AutoCloseable 
{
    private String name;
    private Board board;
    private boolean isRunning = false;
    protected List<ServerThread> clients = new ArrayList<ServerThread>();
    
    private final static String COMMAND_TRIGGER = "/";
    private final static String CREATE_ROOM = "createroom";
    private final static String JOIN_ROOM = "joinroom";
    private final static String DISCONNECT = "disconnect";
    private final static String LOGOUT = "logout";
    private final static String LOGOFF = "logoff";
    private static Logger logger = Logger.getLogger(Room.class.getName());

    ///START
    public Room(String name, int maxClients, long roomId, Board board) 
	{
        this.name = name;
        this.isRunning = true;
        this.board = board;
    }

    ///
    protected void broadcast(String message)
    {
        Payload payload = new Payload();
        payload.setPayloadType(PayloadType.MESSAGE);
        payload.setMessage(message);
        broadcast(payload);
    }
    
    protected void broadcast(Payload payload)
    {
        for (ServerThread client : clients)
        {
            try
            {
                client.send(payload);
            }
            catch (Exception e)
            {
                logger.warning("Failed to broadcast payload to client" + client.getClientId());
            }
        }
    }
    ///
    public Board getBoard()
    {
        return board;
    }

    /* 
    protected void broadcastBoardUpdate(Board board)
    {
        Payload payload = new Payload();
        payload.setType(PayloadType.BOARD_UPDATE);
        payload.setBoard(board);
        broadcast(payload);
    }
        */
    ///END

    public String getName() 
	{
        return name;
    }

    public boolean isRunning() 
	{
        return isRunning;
    }
    ///START
    protected synchronized void handleBoardCommand(ServerThread sender, int x, int y, String color)
    {

        if (board != null)
        {
            board.fillCell(x, y, color);

            Payload payload = new Payload();
            payload.setPayloadType(PayloadType.BOARD_UPDATE);
            payload.setBoard(board);

            broadcastBoardUpdate(sender.getClientId(), payload);
        }
    }
    /*
    protected synchronized void broadcastBoardUpdate(long senderClientId, Payload payload)
    {
        Iterator<ServerThread> iter = clients.iterator();
    while (iter.hasNext())
    {
        ServerThread client = iter.next();

        // Check if the client is not the sender
        if (client.getClientId() != senderClientId);
        {
            try
            {
                boolean messageSent = client.sendBoardUpdate(payload.getBoard());
                if (!messageSent)
                {
                    handleDisconnect(iter, client);
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
                handleDisconnect(iter, client);
            }
        }
    }

    // Print the updated board on the server side
    if (board != null && payload.getBoard() != null)
    {
        System.out.println("Updated Board:");
        payload.getBoard().printBoard();
    }
        /* 
        for (ServerThread client : clients)
        {
            if (client.getClientId() != senderClientId)
            {
                try
                {
                    client.send(payload);
                }
                catch (Exception e)
                {
                    logger.warning("Failed to broadcast board update to client " + client.getClientId());
                }
            }
        }
    }
    */

    ///
    // Room.java

    protected synchronized void broadcastBoardUpdate(long senderClientId, Payload payload)
    {
        Iterator<ServerThread> iter = clients.iterator();
        while (iter.hasNext())
        {
            ServerThread client = iter.next();
    
            // Check if the client is not the sender
            if (client.getClientId() != senderClientId) 
            {
                try 
                {
                    boolean messageSent = client.sendBoardUpdate(payload.getBoard());
                    if (!messageSent)
                    {
                        handleDisconnect(iter, client);
                    }
                } 
                catch (IOException e) 
                {
                    // Handle the IOException, you may choose to log it or take other actions.
                    e.printStackTrace();
                    handleDisconnect(iter, client);
                }
            }
        }
    
        // Print the updated board on the server side
        if (board != null && payload.getBoard() != null)
        {
            System.out.println("Updated Board:");
            payload.getBoard().printBoard();
        }
    }
///

    protected synchronized void addClient(ServerThread client) 
	{
        logger.info("Room addClient called");
        if (!isRunning) 
		{
            return;
        }
        client.setCurrentRoom(this);
        if (clients.indexOf(client) > -1) 
		{
            logger.warning("Attempting to add client that already exists in room");
        } 
		else 
		{
            clients.add(client);
            client.sendResetUserList();
            syncCurrentUsers(client);
            sendConnectionStatus(client, true);
        }
    }

    protected synchronized void removeClient(ServerThread client)
	{
        if (!isRunning) 
		{
            return;
        }
        try 
		{
            clients.remove(client);
        } 
		catch (Exception e) 
		{
            logger.severe(String.format("Error removing client from room %s", e.getMessage()));
            e.printStackTrace();
        }
        if (clients.size() > 0) 
		{
            sendConnectionStatus(client, false);
        }
        checkClients();
    }

    private void syncCurrentUsers(ServerThread client) 
	{
        Iterator<ServerThread> iter = clients.iterator();
        while (iter.hasNext()) 
		{
            ServerThread existingClient = iter.next();
            if (existingClient.getClientId() == client.getClientId()) 
			{
                continue;
            }
            boolean messageSent = client.sendExistingClient(existingClient.getClientId(),
                    existingClient.getClientName());
            if (!messageSent) 
			{
                handleDisconnect(iter, existingClient);
                break;
            }
        }
    }

    private void checkClients() 
	{
        // Cleanup if room is empty and not lobby
        if (!name.equalsIgnoreCase(Constants.LOBBY) && (clients == null || clients.size() == 0)) 
		{
            close();
        }
    }

    public List<ServerThread> getClients()
    {
        return clients;
    }

    
    @Deprecated // not used in my project as of this lesson, keeping it here in case things
    private boolean processCommands(String message, ServerThread client) 
	{
        boolean wasCommand = false;
        try 
		{
            if (message.startsWith(COMMAND_TRIGGER)) 
			{
                String[] comm = message.split(COMMAND_TRIGGER);
                String part1 = comm[1];
                String[] comm2 = part1.split(" ");
                String command = comm2[0];
                String roomName;
                wasCommand = true;
                switch (command) 
				{
                    case CREATE_ROOM:
                        roomName = comm2[1];
                        Room.createRoom(roomName, client);
                        break;
                    case JOIN_ROOM:
                        roomName = comm2[1];
                        Room.joinRoom(roomName, client);
                        break;
                    case DISCONNECT:
                    case LOGOUT:
                    case LOGOFF:
                        Room.disconnectClient(client, this);
                        break;
                    default:
                        wasCommand = false;
                        break;
                }
            }
        } 
		catch (Exception e) 
		{
            e.printStackTrace();
        }
        return wasCommand;
    }

    // Command helper methods
    protected static void getRooms(String query, ServerThread client) 
	{
        String[] rooms = Server.INSTANCE.getRooms(query).toArray(new String[0]);
        client.sendRoomsList(rooms,
                (rooms != null && rooms.length == 0) ? "No rooms found containing your query string" : null);
    }

    protected static void createRoom(String roomName, ServerThread client) 
	{
        if (Server.INSTANCE.createNewRoom(roomName)) 
		{
            Room.joinRoom(roomName, client);
        } 
		else 
		{
            client.sendMessage(Constants.DEFAULT_CLIENT_ID, String.format("Room %s already exists", roomName));
        }
    }

    
    protected static void joinRoom(String roomName, ServerThread client) 
	{
        if (!Server.INSTANCE.joinRoom(roomName, client)) 
		{
            client.sendMessage(Constants.DEFAULT_CLIENT_ID, String.format("Room %s doesn't exist", roomName));
        }
    }

    protected static void disconnectClient(ServerThread client, Room room) 
	{
        client.disconnect();
        room.removeClient(client);
    }
    // end command helper methods

    public ServerThread getClientById(long clientId) 
    {
        for (ServerThread client : clients) 
        {
            if (client.getClientId() == clientId) 
            {
                return client;
            }
        }
        return null; // Return null if the client with the specified ID is not found
    }

    protected synchronized void sendMessage(ServerThread sender, String message) 
	{
        if (!isRunning) 
		{
            return;
        }
        logger.info(String.format("Sending message to %s clients", clients.size()));
        if (sender != null && processCommands(message, sender)) 
		{
            // it was a command, don't broadcast
            return;
        }
        long from = sender == null ? Constants.DEFAULT_CLIENT_ID : sender.getClientId();
        Iterator<ServerThread> iter = clients.iterator();
        while (iter.hasNext()) 
		{
            ServerThread client = iter.next();
            boolean messageSent = client.sendMessage(from, message);
            if (!messageSent) 
			{
                handleDisconnect(iter, client);
            }
        }
    }

    protected synchronized void sendPayload(ServerThread sender, Payload payload) {
        if (!isRunning) {
            return;
        }
    
        logger.info(String.format("Sending payload to %s clients", clients.size()));
    
        if (sender != null && processCommands(payload.getMessage(), sender)) {
            // It was a command, don't broadcast
            return;
        }
    
        long from = sender == null ? Constants.DEFAULT_CLIENT_ID : sender.getClientId();
        Iterator<ServerThread> iter = clients.iterator();
    
        while (iter.hasNext()) {
            ServerThread client = iter.next();
            boolean messageSent = client.sendPayload(from, payload);
            
            if (!messageSent) {
                handleDisconnect(iter, client);
            }
        }
    }

    protected synchronized void sendConnectionStatus(ServerThread sender, boolean isConnected) 
	{
        Iterator<ServerThread> iter = clients.iterator();
        while (iter.hasNext()) 
		{
            ServerThread receivingClient = iter.next();
            boolean messageSent = receivingClient.sendConnectionStatus(
                    sender.getClientId(),
                    sender.getClientName(),
                    isConnected);
            if (!messageSent) 
			{
                handleDisconnect(iter, receivingClient);
            }
        }
    }

	///START
	protected synchronized void broadcastCoordinatesAndColor(long senderClientId, int x, int y, String color)
	{
		Iterator<ServerThread> iter = clients.iterator();
		while (iter.hasNext())
		{
			ServerThread client = iter.next();

			if(client.getClientId() != senderClientId)
			{
				boolean messageSent = client.sendCoordinatesAndColor(senderClientId, x, y, color);
				if (!messageSent)
				{
					handleDisconnect(iter, client);
				}
			}
		}
	}
    ///END

    protected void handleDisconnect(Iterator<ServerThread> iter, ServerThread client) 
	{
        if (iter != null) 
		{
            iter.remove();
        }
        else 
		{
            Iterator<ServerThread> iter2 = clients.iterator();
            while (iter2.hasNext()) 
			{
                ServerThread th = iter2.next();
                if (th.getClientId() == client.getClientId()) 
				{
                    iter2.remove();
                    break;
                }
            }
        }
        logger.info(String.format("Removed client %s", client.getClientName()));
        sendMessage(null, client.getClientName() + " disconnected");
        checkClients();
    }

    public void close() 
	{
        Server.INSTANCE.removeRoom(this);
        isRunning = false;
        clients.clear();
    }
}