package common;

import java.io.Serializable;

import server.GameRoom;

public class Board implements Serializable
{
    private GameRoom gameRoom;
    private String[][] cells;

    

    public GameRoom getGameRoom()
    {
        return gameRoom;
    }

    public void setGameRoom(GameRoom gameRoom)
    {
        this.gameRoom = gameRoom;
    }

    public String getCellColor(int row, int col)
    {
        if (isValidCell(row, col))
        {
            return cells[row][col];
        }
        else
        {
            System.out.println("Invalid cell coordinates");
            return null;
        }
    }

    public void fillCell(int row, int col, String color)
    {
        if (isValidCell(row, col))
        {
            cells[row][col] = color;
        }
        else
        {
            System.out.println("Invalid cell coordinates");
        }
    }
    
    public void updateBoard(Board newBoard) 
    {
        this.cells = newBoard.cells;
    }
    

    public int getRows()
    {
        return cells.length;
    }

    public int getCells()
    {
        return cells[0].length;
    }
    
    public Board(int rows, int cols)
    {
        cells = new String[rows][cols];
        initializeBoard();
    }
    
    public String printBoard()
    {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < cells.length; i++) 
        {
            for (int j = 0; j < cells[i].length; j++) 
            {
                result.append("+---");
            }
                result.append("+\n");
    
            for (int j = 0; j < cells[i].length; j++) 
            {
                result.append("| " + (cells[i][j].equals(" ") ? "   " : cells[i][j]) + " ");
            }
            result.append("|\n");
        }
    
        for (int j = 0; j < cells[0].length; j++) 
        {
            result.append("+---");
        }
        result.append("+\n");

        return result.toString();
    }

    private boolean isValidCell(int row, int col)
    {
        return row >= 0 && row < cells.length && col >= 0 && col < cells[row].length;
    }

    private void initializeBoard()
    {
        for (int i = 0; i < cells.length; i++)
        {
            for (int j = 0; j< cells[i].length; j++)
            {
                cells[i][j] = " ";
            }
        }
    }
}
    
