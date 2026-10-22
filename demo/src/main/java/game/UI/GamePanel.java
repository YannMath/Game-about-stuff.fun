package game.UI;

import javax.swing.*;
import java.awt.*;
import org.luaj.vm2.LuaValue;

import game.Logic.Convert;
import game.Objects.BoardTile;

public class GamePanel extends JPanel {
    private final JPanel BOARD_PANEL = new JPanel();
    private GridBagConstraints gbc = new GridBagConstraints();

    
    public GamePanel() {
        setLayout(new BorderLayout());
        BOARD_PANEL.setLayout(new GridBagLayout());

        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        add(BOARD_PANEL, BorderLayout.CENTER);
    }

    public void clearBoard() {
        runOnEventDispatchThread(() -> {
            BOARD_PANEL.removeAll();
            BOARD_PANEL.revalidate();
            BOARD_PANEL.repaint();
        });
    }

    public void addTile(String name, int price, String imageFileName, int pos, int posX, int posY) {
        runOnEventDispatchThread(() -> {
            gbc.gridy = posY; 
            gbc.gridx = posX;

            BOARD_PANEL.add(new BoardTile(name, price, imageFileName, pos), gbc);
            BOARD_PANEL.revalidate();
            BOARD_PANEL.repaint();
        });
    }

    public void loadBoard(LuaValue boardLayout) {
        if (!boardLayout.istable()) {
            throw new IllegalArgumentException("[ERROR] Board layout must be a Lua table");
        }

        runOnEventDispatchThread(() -> {
            BOARD_PANEL.removeAll();

            for (int y = 1; y <= boardLayout.length(); y++) {
                LuaValue row = boardLayout.get(y);
                if (!row.istable()) {
                    throw new IllegalArgumentException(
                        "Board row " + y + " must be a Lua table"
                    );
                }

                for (int x = 1; x <= row.length(); x++) {
                    LuaValue tileValue = row.get(x);
                    BoardTile currentTile;
                    if (tileValue.istable()) {
                        currentTile = Convert.parseTileLuaToJava(tileValue);
                    } else if (tileValue.isuserdata(BoardTile.class)) {
                        currentTile = BoardTile.class.cast(tileValue.checkuserdata(BoardTile.class));
                    } else {
                        throw new IllegalArgumentException(
                            "[ERROR] Expected a Lua table or Tile userdata at (" + x + ", " + y
                            + "), but got: " + tileValue.typename()
                        );
                    }

                    if (!currentTile.getName().equals("EMPTY")) {
                        gbc.gridx = x - 1;
                        gbc.gridy = y - 1;
                        BOARD_PANEL.add(currentTile, gbc);
                    }
                }
            }

            BOARD_PANEL.revalidate();
            BOARD_PANEL.repaint();
        });
    }

    private void runOnEventDispatchThread(Runnable action) {
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
            return;
        }

        try {
            SwingUtilities.invokeAndWait(action);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("[ERROR] Interrupted while updating the board", exception);
        } catch (java.lang.reflect.InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException("[ERROR] Could not update the board", cause);
        }
    }

    public BoardTile getTileByPos(int pos) {
        for (Component component : BOARD_PANEL.getComponents()) {
            if (component instanceof BoardTile tile) {
                if (tile.getPos() == pos) {
                    return tile;
                }
            }
        }
        return null;
    }

    public int getBoardSize() {
        int tileCount = 0;
        for (Component component : BOARD_PANEL.getComponents()) {
            if (component instanceof BoardTile tile && tile.getPos() >= 0) {
                tileCount++;
            }
        }
        return tileCount;
    }
}