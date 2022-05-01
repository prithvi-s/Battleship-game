package server;

import model.Board;
import server.messages.ChatMessage;
import server.messages.MoveMessage;
import server.messages.NotificationMessage;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;import java.sql.*;


public class Player extends Thread {

    private Socket socket;
    private MatchRoom matchRoom;
    private String name = "";
    private ObjectOutputStream out;
    private Game game;
    private Board board;
    private HashMap<String, Player> requestList;
    private String ownKey;
    private String requestedGameKey;

    public Player(Socket socket, MatchRoom matchRoom) {
        this.socket = socket;
        this.matchRoom = matchRoom;
        matchRoom.assignKey(this);
        matchRoom.addPlayer(this);
        this.requestList = new HashMap<>();
        System.out.println(socket.getRemoteSocketAddress().toString() +
                " connected");
    }
    @Override
    public void run() {
        super.run();
        try {
            out = new ObjectOutputStream(new BufferedOutputStream(
                    socket.getOutputStream()));
            out.flush();
            ObjectInputStream in = new ObjectInputStream(
                    socket.getInputStream());

            Object input;

            while ((input = in.readObject()) != null) {
                if (input instanceof String[]) {
                    String[] array = (String[]) input;
                    int length = array.length;

                    if (length > 0) {
                        String message = array[0];

                        switch (message) {
                        case "join":
                            matchRoom.parse(this, array);
                            break;
                        case "name":
                            if (length != 2 || array[1] == null ||
                                    array[1].equals("")) {
                                writeNotification(NotificationMessage.INVALID_NAME);
                            } else if (matchRoom.playerNameExists(array[1])) {
                                writeNotification(NotificationMessage.NAME_TAKEN);
                            } else {
                                name = array[1];
                                writeNotification(NotificationMessage.NAME_ACCEPTED);
                                matchRoom.sendMatchRoomList();
                            }
                            break;
                        }
                    }
                } else if (input instanceof Board) {
                    Board board = (Board) input;
                    if (Board.isValid(board) && game != null) {
                        writeNotification(NotificationMessage.BOARD_ACCEPTED);
                        this.board = board;
                        game.checkBoards();
                    } else if (game == null) {
                        writeNotification(NotificationMessage.NOT_IN_GAME);
                    } else {
                        writeNotification(NotificationMessage.INVALID_BOARD);
                    }
                } else if (input instanceof MoveMessage) {
                    if (game != null) {
                        game.applyMove((MoveMessage) input, this);
                    }
                } else if (input instanceof ChatMessage) {
                    if (game != null) {
                        Player opponent = game.getOpponent(this);
                        if (opponent != null) {
                            opponent.writeObject(input);
                        }
                    }
                }
            }
        } catch (IOException e) {
            if (game != null) {
                leaveGame();
            } else {
                matchRoom.removeWaitingPlayer(this);
            }
            matchRoom.removePlayer(this);
            System.out.println(socket.getRemoteSocketAddress().toString() +
                    " connected");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void setGame(Game game) {
        this.game = game;
    }
    public String getPlayerName() {
        return name;
    }
    public void writeMessage(String message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void writeObject(Object object) {
        try {
            out.writeObject(object);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void writeNotification(int notificationMessage, String... text) {
        try {
            NotificationMessage nm = new NotificationMessage(
                    notificationMessage, text);
            out.writeObject(nm);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public Board getBoard() {
        return this.board;
    }
    public synchronized void sendRequest(Player requester) {
        requestList.put(requester.getOwnKey(), requester);
        requester.requestedGameKey = this.ownKey;
        writeNotification(NotificationMessage.NEW_JOIN_GAME_REQUEST,
                requester.getOwnKey(), requester.getPlayerName());
    }
    public synchronized void requestAccepted(Player opponent) {
        opponent.requestList.remove(ownKey);
        requestedGameKey = null;
        writeNotification(NotificationMessage.JOIN_GAME_REQUEST_ACCEPTED);
    }
    public synchronized void requestRejected(Player opponent) {
        opponent.requestList.remove(ownKey);
        requestedGameKey = null;
        writeNotification(NotificationMessage.JOIN_GAME_REQUEST_REJECTED);
    }
    public void setOwnKey(String ownKey) {
        this.ownKey = ownKey;
    }
    public String getOwnKey() {
        return ownKey;
    }
    public void setRequestedGameKey(String key) {
        this.requestedGameKey = key;
    }
    public String getRequestedGameKey() {
        return requestedGameKey;
    }
    public void rejectAll() {
        for (Player p : requestList.values()) {
            p.requestRejected(this);
        }
    }
    public void leaveGame() {
        if (game != null) {
            Player opponent = game.getOpponent(this);
            opponent.writeNotification(NotificationMessage.OPPONENT_DISCONNECTED);
            game.killGame();
        }
    }

}
