package common;

import java.io.Serializable;

public class Payload implements Serializable 
{
    private static final long serialVersionUID = 1L;// change this if the class changes
    private PayloadType payloadType;
    private Board board;
    
    /*
    ab2669 
    11/14/23
    Passes Coordinates and and colors from client to server
    */
    private int x;
    private int y;
    private String color;
    private PayloadType type;
    
    public int getX()
    {
        return x;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public int getY()
    {
        return y;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    public String getColor()
    {
        return color;
    }

    public void setColor(String color)
    {
        this.color = color;
    }

    public void setType(PayloadType type)
    {
        this.type = type;
    }
    ///

    ///
    public Board getBoard()
    {
        return board;
    }

    public void setBoard(Board board)
    {
        this.board = board;
    }
    /*
    ab2669 
    11/14/23
    2) Only send updates if the coordinates change
    */

    private int lastX;
    private int lastY;
    
    ///

    public PayloadType getPayloadType() 
    {
        return payloadType;
    }

    public void setPayloadType(PayloadType payloadType) 
    {
        this.payloadType = payloadType;
    }

    private String clientName;

    public String getClientName()
    {
        return clientName;
    }

    /*
    ab2669 
    11/14/23
    2) Only send updates if the coordinates change
    */

    public Payload()
    {
        this.payloadType = PayloadType.MESSAGE;
    }

    public boolean hasCoordinatesChanged()
    {
        return x != lastX || y != lastY;
    }

    public void updateLastCoordinates()
    {
        lastX = x;
        lastY = y;
    }
    
    ///

    public void setClientName(String clientName) 
    {
        this.clientName = clientName;
    }

    private long clientId;

    public long getClientId() 
    {
        return clientId;
    }

    public void setClientId(long clientId)
    {
        this.clientId = clientId;
    }

    /*
    ab2669 
    11/14/23
    5) Clear boards when the game timer or when a correct guess is guessed
    */

    public void clearBoard()
    {
        x = 0;
        y = 0;
        color = null;
        lastX = 0;
        lastY = 0;
    }

    ///

    private String message;

    public String getMessage() 
    {
        return message;
    }

    public void setMessage(String message) 
    {
        this.message = message;
    }

    @Override
    public String toString() 
    {
        return String.format("Payload {ClientID:%d, ClientName:%s, Message:%s, x:%d, y:%d, Color:%s}",
        clientId, clientName, message, x, y, color);
        //changed format
                /*("Type[%s],ClientId[%s,] ClientName[%s], Message[%s]", getPayloadType().toString(),
                getClientId(), getClientName(),
                getMessage());*/
    }
}