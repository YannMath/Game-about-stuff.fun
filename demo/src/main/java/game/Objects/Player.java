package game.Objects;

import java.awt.*;
import javax.swing.*;

import game.Main;
public class Player {
    private final ImagePanel IMAGE_CONTAINER;
    private final Color PLAYER_COLOR;

    private int money = 0;
    private int pos = 0;
    
    public Player(String imageFileName, Color c) {
        PLAYER_COLOR = c;

        var imageURL = getClass().getResource("/images/" + imageFileName);
        if (imageURL == null) {
            throw new IllegalArgumentException(
                "Could not find image: " + imageFileName
            );
        }
        ImageIcon icon = new ImageIcon(imageURL);

        IMAGE_CONTAINER = new ImagePanel(icon.getImage());
    }

    public void reduceMoney(int amountReduced) {
        this.money -= amountReduced;

        System.out.println("A player received/had to pay money - current money: " + money);
    }

    public void buy() {
        BoardTile targetTile = getCurrentTile();
        buy(targetTile);
    }

    public void buy(BoardTile t) {
        t.buy(this);
    }

    public void move(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Movement steps must not be negative: " + n);
        }

        int boardSize = Main.getBoardSize();
        if (boardSize <= 0) {
            throw new IllegalStateException("Cannot move on a board with no positions");
        }

        for (int step = 0; step < n; step++) {
            if (step > 0) {
                pauseBetweenSteps();
            }

            runOnEventDispatchThread(() -> {
                int oldPos = pos;
                int newPos = (oldPos + 1) % boardSize;
                BoardTile currentTile = Main.getTileByPos(oldPos);
                BoardTile targetTile = Main.getTileByPos(newPos);
                if (currentTile == null || targetTile == null) {
                    throw new IllegalStateException(
                        "Cannot move from board position " + oldPos + " to " + newPos
                        + ": a tile is missing"
                    );
                }

                currentTile.onLeave(this);
                pos = newPos;
                targetTile.onEnter(this);
                System.out.println("Player moved from position " + oldPos + " to " + newPos);
            });
        }
    }

    private void pauseBetweenSteps() {
        if (SwingUtilities.isEventDispatchThread()) {
            return;
        }

        try {
            Thread.sleep(250);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Player movement was interrupted", exception);
        }
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
            throw new IllegalStateException("Player movement was interrupted", exception);
        } catch (java.lang.reflect.InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException("Could not move player", cause);
        }
    }

    public JComponent getImageComponent() {
        return IMAGE_CONTAINER;
    }

    private static class ImagePanel extends JPanel {
        private final Image image;

        private ImagePanel(Image image) {
            this.image = image;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            int imageWidth = image.getWidth(this);
            int imageHeight = image.getHeight(this);
            if (imageWidth <= 0 || imageHeight <= 0 || getWidth() <= 0 || getHeight() <= 0) {
                return;
            }

            double scale = Math.min(
                (double) getWidth() / imageWidth,
                (double) getHeight() / imageHeight
            );
            int scaledWidth = (int) Math.ceil(imageWidth * scale);
            int scaledHeight = (int) Math.ceil(imageHeight * scale);
            int x = (getWidth() - scaledWidth) / 2;
            int y = (getHeight() - scaledHeight) / 2;

            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );
            graphics2D.drawImage(image, x, y, scaledWidth, scaledHeight, this);
            graphics2D.dispose();
        }
    }

    public void setPos(int pos) {this.pos = pos;}

    public Color getColor() {return this.PLAYER_COLOR;}
    public int getPos() {return pos;}
    public int getMoney() {return money;}
    public BoardTile getCurrentTile() {return Main.getTileByPos(pos);}
}
