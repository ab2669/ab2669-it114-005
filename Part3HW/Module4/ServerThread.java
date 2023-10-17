package Module4;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * A server-side representation of a single client
 */
public class ServerThread extends Thread 
{
    private Socket client;
    private boolean isRunning = false;
    private ObjectOutputStream out;//exposed here for send()
    private Server server;// ref to our server so we can call methods on it
    // more easily
    private String clientName;

    private void info(String message) 
    {
        System.out.println(String.format("Thread[%s]: %s", getId(), message));
    }

    public ServerThread(Socket myClient, Server server, String clientName) 
    {
        info("Thread created");
        // get communication channels to single client
        this.client = myClient;
        this.server = server;
        this.clientName = clientName;
    }

    public String getClientName()
    {
        return clientName;
    }

    public void disconnect() 
    {
        info("Thread being disconnected by server");
        isRunning = false;
        cleanup();
    }

    public boolean send(String message) 
    {
        // added a boolean so we can see if the send was successful
        try 
        {
            out.writeObject(message);
            return true;
        } 
        catch (IOException e) 
        {
            info("Error sending message to client (most likely disconnected)");
            // comment this out to inspect the stack trace
            // e.printStackTrace();
            cleanup();
            return false;
        }
    }

    @Override
    public void run() 
    {
        info("Thread starting");
        try (ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream()); ObjectInputStream in = new ObjectInputStream(client.getInputStream());) 
        {
            this.out = out;
            isRunning = true;
            String fromClient;
            while (isRunning && (fromClient = (String) in.readObject()) != null) 
            {
                info("Received from client: " + fromClient);
                server.broadcast(fromClient, this.getId());

                if (fromClient.startsWith("roll ")) 
                {
                    server.handleDiceRoll(this, fromClient);
                } 
                else if (fromClient.equalsIgnoreCase("flip") || fromClient.equalsIgnoreCase("toss") || fromClient.equalsIgnoreCase("coin")) 
                {
                    server.handleCoinToss(this);
                } 
                else 
                {
                    // Handle other commands or normal messages
                    server.broadcast(clientName + ": " + fromClient, this.getId());
                }
            }
                
        } 
        catch (Exception e) 
        {
            // happens when client disconnects
            e.printStackTrace();
            info("Client disconnected");
        } 
        finally 
        {
            isRunning = false;
            info("Exited thread loop. Cleaning up connection");
            cleanup();
        }
    }

    private void cleanup() 
    {
        info("Thread cleanup() start");
        try 
        {
            client.close();
        } 
        catch (IOException e) 
        {
            info("Client already closed");
        }
        info("Thread cleanup() complete");
    }
}