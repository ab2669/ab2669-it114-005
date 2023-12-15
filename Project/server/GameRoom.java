package server;

import java.util.Random;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import common.Constants;
import common.Payload;
import common.PayloadType;
import common.Phase;
import common.TimedEvent;
import common.Board;


public class GameRoom extends Room 
{
    Phase currentPhase = Phase.READY;
    private static Logger logger = Logger.getLogger(GameRoom.class.getName());
    private TimedEvent readyTimer = null;
    private ConcurrentHashMap<Long, ServerPlayer> players = new ConcurrentHashMap<Long, ServerPlayer>();
    private Board board;
    private List<ServerThread> clients = new ArrayList<>();
    private long playerId;
    private List<String> drawingWords = new ArrayList<>();
    private String wordToDraw;
    protected Phase phase;

    public void setPlayerId(long playerId)
    {
        this.playerId = playerId;
    }

    public long getPlayerId()
    {
        return playerId;
    }
    
    public List<ServerThread> getClients()
    {
        return clients;
    }

    public GameRoom(String name, int maxClients, long roomId, Board board)
    {
        super(name, maxClients, roomId, board);
        this.phase = Phase.LOBBY;
        this.board = new Board(20, 20);
        initializeDrawingWords();
    }

    //Board - START
    public void initializeBoard()
    {
        this.board = new Board(20,20);
    }

    protected void broadcastBoardUpdate()
    {
        Payload boardUpdatePayload = new Payload();
        boardUpdatePayload.setPayloadType(PayloadType.BOARD_UPDATE);
        boardUpdatePayload.setBoard(board);
    
        broadcastBoardUpdate(Constants.DEFAULT_CLIENT_ID, boardUpdatePayload);
    }

    
    public Board getBoard()
    {
        return board;
    }

    // Actions when /startguess command is started
    private void initializeDrawingWords()
    {
        drawingWords.add("Dog");
        drawingWords.add("Cat");
        logger.info("Drawing words initialized: " + drawingWords);
    }
    
    public String getWordToDraw()
    {
        return wordToDraw;
    }

    public String pickWordToDraw()
    {
        Random random = new Random();
        int index = random.nextInt(drawingWords.size());
        return drawingWords.get(index);
    }

    public void broadcastStartGuess(Payload payload) 
    {
        Payload startGuessPayload = new Payload();
        startGuessPayload.setPayloadType(PayloadType.START_GUESS);
    
        if (payload == null || payload.getClientId() == -1) 
        {
            sendMessage(null, "Guessing phase started! You have 2 minutes to draw: ");
        } 
        else 
        {
            sendMessage(getClientById(payload.getClientId()), "Starting a new round. Word to draw: " + payload.getMessage());
        }
        broadcast(startGuessPayload);
    }
    
    public synchronized void processGuess(ServerThread client, String guessedWord) throws IOException 
    {
        if (guessedWord.equalsIgnoreCase(wordToDraw)) 
        {
        String correctGuessMessage = String.format("%s guessed the correct word!", client.getClientName());
        sendMessage(client, correctGuessMessage);
        } 
        else 
        {
        String incorrectGuessMessage = String.format("%s guessed: %s (incorrect)", client.getClientName(), guessedWord);
        sendMessage(client, incorrectGuessMessage);
        }
    }
    
    public void notifyIncorrectGuess(long clientId) 
    {
        Payload payload = new Payload();
        payload.setPayloadType(PayloadType.INCORRECT_GUESS);
        payload.setMessage("Your guess is incorrect!");
        sendPayload(getClientById(clientId), payload);
    }
    
    public void broadcastCorrectGuess(long clientId, String correctWord) 
    {
        Payload correctGuessPayload = new Payload();
        correctGuessPayload.setPayloadType(PayloadType.CORRECT_GUESS);
    
        String message = "Congratulations! Player " + clientId + " guessed the word: " + correctWord;
        correctGuessPayload.setMessage(message);
        broadcast(correctGuessPayload);
    }

    public void startNewRound()
    {
        logger.info("Starting a new round");
        int timerDuration = 20;
        readyTimer = new TimedEvent(timerDuration, () -> {}, () -> endGuessingPhase());
        players.values().forEach(player -> player.setReady(false));
        wordToDraw = pickWordToDraw();
        logger.info("Word to draw: " + wordToDraw);
        broadcastNewRound();
    }

    private void endGuessingPhase()
    {
        broadcast("Guessing ended!");

        endRound();
    }

    private void broadcastNewRound()
    {
        Payload newRoundPayload = new Payload();
        newRoundPayload.setPayloadType(PayloadType.NEW_ROUND);
        newRoundPayload.setMessage("Starting a new round. Word to draw: " + wordToDraw);

        for (ServerThread client : getClients()) 
        {
            client.send(newRoundPayload);
        }
    }

    public void endRound()
    {
        broadcastRoundEnd();
    }

    private void broadcastRoundEnd()
    {
        String message = "Round ended.";
        sendMessage(null,message);
    }

    public boolean isRoundComplete()
    {
        return false;
    }
    // End of Guess Implementation
    
    @Override
    protected void addClient(ServerThread client) 
    {
        logger.info("Adding client as player");
        players.computeIfAbsent(client.getClientId(), id -> 
        {
            ServerPlayer player = new ServerPlayer(client);
            super.addClient(client);
            logger.info(String.format("Total clients %s", clients.size()));
            return player;
        });
    }

    protected void setReady(ServerThread client) 
    {
        logger.info("Ready check triggered");
        if (currentPhase != Phase.READY) 
        {
            logger.warning(String.format("readyCheck() incorrect phase: %s", Phase.READY.name()));
            return;
        }
        if (readyTimer == null) 
        {
            sendMessage(null, "Ready Check Initiated, 30 seconds to join");
            readyTimer = new TimedEvent(30, () -> 
            {
                readyTimer = null;
                readyCheck(true);
            });
        }
        players.values().stream().filter(p -> p.getClient().getClientId() == client.getClientId()).findFirst()
                .ifPresent(p -> 
                {
                    p.setReady(true);
                    logger.info(String.format("Marked player %s[%s] as ready", p.getClient().getClientName(), p
                            .getClient().getClientId()));
                    syncReadyStatus(p.getClient().getClientId());
                });
        readyCheck(false);
    }

    private void readyCheck(boolean timerExpired) 
    {
        if (currentPhase != Phase.READY) 
        {
            return;
        }
        long numReady = players.values().stream().filter(ServerPlayer::isReady).count();
        if (numReady >= Constants.MINIMUM_PLAYERS) 
        {

            if (timerExpired) 
            {
                sendMessage(null, "Ready Timer expired, starting session");
                start();
            } 
            else if (numReady >= players.size()) 
            {
                sendMessage(null, "Everyone in the room marked themselves ready, starting session");
                if (readyTimer != null) {
                    readyTimer.cancel();
                    readyTimer = null;
                }
                start();
            }

        } 
        else 
        {
            if (timerExpired) 
            {
                resetSession();
                sendMessage(null, "Ready Timer expired, not enough players. Resetting ready check");
            }
        }
    }

    private void start() 
    {
        updatePhase(Phase.IN_PROGRESS);
        sendMessage(null, "Session started");
        new TimedEvent(30, () -> resetSession())
                .setTickCallback((time) -> {
                    sendMessage(null, String.format("Example running session, time remaining: %s", time));
                });
    }

    private synchronized void resetSession() 
    {
        players.values().stream().forEach(p -> p.setReady(false));
        updatePhase(Phase.READY);
        sendMessage(null, "Session ended, please intiate ready check to begin a new one");
    }

    private void updatePhase(Phase phase) 
    {
        if (currentPhase == phase) {
            return;
        }
        currentPhase = phase;

        Iterator<ServerPlayer> iter = players.values().stream().iterator();
        while (iter.hasNext()) 
        {
            ServerPlayer client = iter.next();
            boolean success = client.getClient().sendPhaseSync(currentPhase);
            if (!success) {
                handleDisconnect(client);
            }
        }
    }

    protected void handleDisconnect(ServerPlayer player) 
    {
        if (players.containsKey(player.getClient().getClientId())) 
        {
            players.remove(player.getClient().getClientId());
            super.handleDisconnect(null, player.getClient());
            logger.info(String.format("Total clients %s", clients.size()));
            sendMessage(null, player.getClient().getClientName() + " disconnected");
            if (players.isEmpty()) {
                close();
            }
        }
    }



    private void syncReadyStatus(long clientId) 
    {
        Iterator<ServerPlayer> iter = players.values().stream().iterator();
        while (iter.hasNext()) 
        {
            ServerPlayer client = iter.next();
            boolean success = client.getClient().sendReadyStatus(clientId);
            if (!success) 
            {
                handleDisconnect(client);
            }
        }
    }

    public boolean isWordToDrawAvailable() 
    {
        return wordToDraw != null && !wordToDraw.isEmpty();
    }
}