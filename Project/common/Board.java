package common;

import java.io.Serializable;

import server.GameRoom;

public class Board implements Serializable
{
    //
    private GameRoom gameRoom;

    public GameRoom getGameRoom()
    {
        return gameRoom;
    }

    public void setGameRoom(GameRoom gameRoom)
    {
        this.gameRoom = gameRoom;
    }
    ///BOARD CREATION
    private String[][] cells;

    public Board(int rows, int cols)
    {
        cells = new String[rows][cols];
        initializeBoard();
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
    ///END

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
    
    private boolean isValidCell(int row, int col)
    {
        return row >= 0 && row < cells.length && col >= 0 && col < cells[row].length;
    }

    public int getRows()
    {
        return cells.length;
    }

    public int getCells()
    {
        return cells[0].length;
    }

/*
   @Override
public String toString() 
{
    StringBuilder result = new StringBuilder();

    // Print top border
    result.append("+");
    for (int i = 0; i < getCells(); i++) 
    {
        result.append("-");
    }
    result.append("+\n");

    // Print rows with borders
    for (int i = 0; i < getRows(); i++) 
    {
        result.append("|");
        for (int j = 0; j < getCells(); j++) 
        {
            result.append(cells[i][j]);
        }
        result.append("|\n");
    }

    // Print bottom border
    result.append("+");
    for (int i = 0; i < getCells(); i++) 
    {
        result.append("-");
    }
    result.append("+\n");

    return result.toString();
}
*/
}
    
