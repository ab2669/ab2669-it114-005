package Module4;
//cd "OneDrive - NJIT"/"IT 114"/GitHub_Clone_Test/ab2669-it114-005/Part3HW


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Server 
{
    int port = 3001;
    // connected clients
    private List<ServerThread> clients = new ArrayList<ServerThread>();

    //Problem 3. Dice Roller
    //UCID: ab2669
    //10/16/23
    /*This code defines a method handleDiceRoll to process dice roll commands from clients. 
    It splits the command, generates random rolls, calculates the total, and broadcasts the result with the client's name. 
    It handles exceptions for invalid commands and provides feedback to clients.*/
    private Random random = new Random();

    public void handleDiceRoll(ServerThread client, String command)
    {
        String[] parts = command.split(" ");
        if (parts.length == 2)
        {
            try
            {
                int numDice = Integer.parseInt(parts[1].split("d")[0]);
                int numSides = Integer.parseInt(parts[1].split("d")[1]);
                int total = 0;
            
                StringBuilder resultMessage = new StringBuilder(client.getClientName() + " rolled " + parts[1] + " and got: ");

                for (int i = 0; i < numDice; i++) 
                {
                    int roll = random.nextInt(numSides) + 1;
                    total += roll;
                    resultMessage.append(roll).append(" ");
                }

                resultMessage.append("(Total: ").append(total).append(")");
                broadcast(resultMessage.toString(), client.getId());
            } 
            catch (NumberFormatException e) 
            {
                client.send("Invalid dice roll command. Use 'roll #d#'.");
            }
        } 
        else 
        {
            client.send("Invalid dice roll command. Use 'roll #d#'.");
        }
    }

    //Problem 2. Coin Toss
    //UCID: ab2669
    //10/16/23
    //This method, handleCoinToss, simulates a coin toss and broadcasts the result (heads or tails) with the client's name to all connected clients.
    public void handleCoinToss(ServerThread client) 
    {
        String result = (random.nextBoolean()) ? "heads" : "tails";
        broadcast(client.getClientName() + " flipped a coin and got " + result, client.getId());
    }
    

    private void start(int port) 
    {
        this.port = port;
        // server listening
        try (ServerSocket serverSocket = new ServerSocket(port);) 
        {
            Socket incoming_client = null;
            System.out.println("Server is listening on port " + port);
            do {
                System.out.println("waiting for next client");
                if (incoming_client != null) 
                {
                    System.out.println("Client connected");
                    ServerThread sClient = new ServerThread(incoming_client, this, "NameofUser");
                    
                    clients.add(sClient);
                    sClient.start();
                    incoming_client = null;
                    
                }
            } while ((incoming_client = serverSocket.accept()) != null);
        } 
        catch (IOException e) 
        {
            System.err.println("Error accepting connection");
            e.printStackTrace();
        } 
        finally 
        {
            System.out.println("closing server socket");
        }
    }
    protected synchronized void disconnect(ServerThread client) 
    {
		long id = client.getId();
        client.disconnect();
		broadcast("Disconnected", id);
	}
    
    protected synchronized void broadcast(String message, long id) 
    {
        if(processCommand(message, id))
        {
            return;
        }
        
        message = String.format("User[%d]: %s", id, message);
        
        Iterator<ServerThread> it = clients.iterator();
        while (it.hasNext()) 
        {
            ServerThread client = it.next();
            boolean wasSuccessful = client.send(message);
            if (!wasSuccessful) 
            {
                System.out.println(String.format("Removing disconnected client[%s] from list", client.getId()));
                it.remove();
                broadcast("Disconnected", id);
            }
        }
    }

    private boolean processCommand(String message, long clientId)
    {
        System.out.println("Checking command: " + message);
        if(message.equalsIgnoreCase("disconnect"))
        {
            Iterator<ServerThread> it = clients.iterator();
            while (it.hasNext()) 
            {
                ServerThread client = it.next();
                if(client.getId() == clientId)
                {
                    it.remove();
                    disconnect(client);
                    
                    break;
                }
            }
            return true;
        }
        return false;
    }
    public static void main(String[] args) 
    {
        System.out.println("Starting Server");
        Server server = new Server();
        int port = 3000;
        try 
        {
            port = Integer.parseInt(args[0]);
        } 
        catch (Exception e) 
        {
            // can ignore, will either be index out of bounds or type mismatch
            // will default to the defined value prior to the try/catch
        }
        server.start(port);
        System.out.println("Server Stopped");
    }
}