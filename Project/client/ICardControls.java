package client;

import javax.swing.JPanel;

import common.Board;

public interface ICardControls 
{
    void next();
    void previous();
    void show(String cardName);
    void addPanel(String name, JPanel panel);
    void connect();
    void updateBoard(Board board);
    void onUpdateBoard(Board board);
    void setUsername(String username);
}
