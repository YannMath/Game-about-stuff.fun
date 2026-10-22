package game;

import javax.swing.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.awt.Color;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.List;

import com.formdev.flatlaf.FlatDarkLaf;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.LuaValue;

import game.Logic.Factories.TileFactory;
import game.Logic.Convert;
import game.Logic.Turn;
import game.UI.MainFrame;
import game.UI.GamePanel;
import game.Objects.Player;
import game.Objects.Dice;
import game.Objects.BoardTile;

public class Main {
    private static MainFrame mainFrame;

    private static Path luaDirectory;
    private static Globals globals;
    private static boolean running = false;
    private static List<Player> players = new ArrayList<>();
    private static List<Dice> dice = new ArrayList<>();
    private static Player currentPlayer;
    private static int amountOfPlayers = 1;
    private static int startMoney = 2000;
    private static int boardSize = -1;
    
    public static void main(String[] args) throws Exception {
        FlatDarkLaf.setup();
        luaSetup();

        Path scripts = Paths.get("demo", "src", "main", "resources", "lua");
        if (!Files.isDirectory(scripts)) {
            scripts = Paths.get("src", "main", "resources", "lua");
        }
        if (!Files.isDirectory(scripts)) {
            throw new IOException("[ERROR] Lua script directory not found: " + scripts.toAbsolutePath());
        }
        luaDirectory = scripts.toAbsolutePath().normalize();

        WatchService watcher = FileSystems.getDefault().newWatchService();
        luaDirectory.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY);

        SwingUtilities.invokeAndWait(() -> {
            mainFrame = new MainFrame();
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            globals.set("color", CoerceJavaToLua.coerce(Color.class));
            globals.set("board", CoerceJavaToLua.coerce(mainFrame.getGamePanel()));
            globals.set("tile", CoerceJavaToLua.coerce(new TileFactory()));
            globals.set("rollAllDice", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaValue.valueOf(rollDice());
                }
            });
            loadLuaFile("config_boardSetup");
            setup();
            mainFrame.pack();
            mainFrame.setLocationRelativeTo(null);
            mainFrame.setVisible(true);
        });
        startLuaWatcher(watcher);
        gameLoop();
    }

    private static void setup() {
        LuaValue playerSetup = loadLuaValue("config_player");
        if (!playerSetup.istable()) {
            throw new IllegalArgumentException(
                "[ERROR] config_player.lua must return an array of player tables"
            );
        }
        for (int i = 1; i <= playerSetup.length(); i++) {
            players.add(Convert.parsePlayerLuaToJava(playerSetup.get(i)));
        }

        LuaValue gameSetup = loadLuaValue("config_setup");
        if (gameSetup.istable()) {
            if (!gameSetup.get("amountOfExtraPlayers").isnil()) {
                amountOfPlayers = gameSetup.get("amountOfExtraPlayers").toint();
                for (int i = 0; i < amountOfPlayers; i++) {
                    Player p = new Player("Player", "default_player_cyan.png", Color.CYAN);
                    players.add(p);
                }
            }
            if (!gameSetup.get("startMoney").isnil()) {
                startMoney = gameSetup.get("startMoney").toint();
                for (Player p : players) {
                    p.reduceMoney(-startMoney);
                    
                }
            }
            if (!gameSetup.get("dice").isnil()) {
                LuaTable diceTable = (LuaTable) gameSetup.get("dice");

                for (int i = 1; i <= diceTable.length(); i++) { // it starts at one because of the array being created in Lua
                    int max = diceTable.get(i).toint();
                    Dice d = new Dice(max);
                    dice.add(d);

                    System.out.println("[LOG] Created new D-" + max);
                }
            }
            if (!gameSetup.get("boardSize").isnil()) {
                boardSize = gameSetup.get("boardSize").toint();
            } else {
                boardSize = mainFrame.getGamePanel().getBoardSize();
            }
            validateBoard();
        } else {
            throw new IllegalArgumentException(
                "[ERROR] The config_setup.lua file is either not present or is not correct"
            );
        }

        BoardTile startTile = mainFrame.getGamePanel().getTileByPos(0);
        if (startTile == null) {
            throw new IllegalStateException("[ERROR] The board does not contain a start tile at position 0");
        }
        for (Player p : players) {
            p.setPos(0);
            startTile.onEnter(p, false);
            System.out.println("[LOG] Added Player " + p.getName());
        }
    }

    private static void gameLoop() {
        running = true;
        int index = 0;

        while (running) {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            }

            if (players.isEmpty()) {
                throw new IllegalStateException("[ERROR] There are no players in the players list");
            }

            Turn.doTurn(players.get(index));
            index = (index + 1) % players.size();
        }
    }

    private static void validateBoard() {
        if (boardSize <= 0) {
            throw new IllegalArgumentException("[ERROR] boardSize must be greater than zero");
        }

        GamePanel board = mainFrame.getGamePanel();
        if (board.getBoardSize() != boardSize) {
            throw new IllegalStateException(
                "[ERROR] Configured boardSize " + boardSize + " does not match the "
                + board.getBoardSize() + " playable tiles"
            );
        }
        for (int pos = 0; pos < boardSize; pos++) {
            if (board.getTileByPos(pos) == null) {
                throw new IllegalStateException("[ERROR] The board is missing tile position " + pos);
            }
        }
    }
 
    private static void luaSetup() {
        globals = JsePlatform.standardGlobals();
    }

    public static void loadLuaFile(String fileName) {
        globals.loadfile(luaDirectory.resolve(fileName + ".lua").toString()).call();
    }

    public static LuaValue loadLuaValue(String fileName) {
        return globals.loadfile(luaDirectory.resolve(fileName + ".lua").toString()).call();
    }

    private static void startLuaWatcher(WatchService watcher) {
        Thread watcherThread = new Thread(() -> {
            Set<Path> changedFiles = new HashSet<>();
            while (!Thread.currentThread().isInterrupted()) {
                WatchKey key;
                try {
                    key = watcher.take();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (ClosedWatchServiceException exception) {
                    break;
                }

                if (!collectLuaChanges(key, changedFiles)) {
                    break;
                }

                while (true) {
                    WatchKey queuedKey;
                    try {
                        queuedKey = watcher.poll(200, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        return;
                    } catch (ClosedWatchServiceException exception) {
                        return;
                    }
                    if (queuedKey == null) {
                        break;
                    }
                    if (!collectLuaChanges(queuedKey, changedFiles)) {
                        return;
                    }
                }

                for (Path changedFile : changedFiles) {
                    try {
                        globals.loadfile(luaDirectory.resolve(changedFile).toString()).call();
                        System.out.println("[LOG] Reloaded Lua script: " + changedFile);
                    } catch (RuntimeException exception) {
                        System.err.println("[ERROR] Could not reload Lua script " + changedFile);
                        exception.printStackTrace();
                    }
                }
                changedFiles.clear();
            }
        }, "lua-file-watcher");
        watcherThread.setDaemon(true);
        watcherThread.start();
    }

    private static boolean collectLuaChanges(WatchKey key, Set<Path> changedFiles) {
        for (WatchEvent<?> event : key.pollEvents()) {
            if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                continue;
            }

            Path changedFile = (Path) event.context();
            if (changedFile.toString().endsWith(".lua")) {
                changedFiles.add(changedFile);
            }
        }
        return key.reset();
    }

    public static BoardTile getTileByPos(int pos) {
        GamePanel board = mainFrame.getGamePanel();
        return board.getTileByPos(pos);
    }

    public static int rollDice() {
        int sum = 0;
        for (Dice d : dice) {
            sum += d.roll();
        }

        System.out.println("[LOG] Rolled " + dice.size() + " dice with the result: " + sum);

        return sum;
    }

    public Player getCurrentPlayer() {return currentPlayer;}
    public static int getBoardSize() {return boardSize;}
}