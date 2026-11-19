package game.Objects;

import java.awt.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.*;

import game.Main;
public class Player {
    private final ImagePanel IMAGE_CONTAINER;
    private final Color PLAYER_COLOR;

    private boolean movementEnabled = false;
    private String name = "Player";
    private int money = 0;
    private int pos = 0;
    private Movement activeMovement;

    private static class Movement {
        private int ticks;
        private int remainingSteps;
        private boolean enterOnCompletion;
        private final CountDownLatch finished = new CountDownLatch(1);
        private final AtomicBoolean cancelled = new AtomicBoolean();
        private final AtomicReference<Throwable> failure = new AtomicReference<>();
        private volatile Timer timer;

        private Movement(int steps, int ticks) {
            remainingSteps = steps;
            enterOnCompletion = true;
            this.ticks = ticks;
        }
    }
    
    public Player(String name, String imageFileName, Color c) {
        PLAYER_COLOR = c;
        this.name = name;

        var imageURL = getClass().getResource("/images/" + imageFileName);
        if (imageURL == null) {
            throw new IllegalArgumentException(
                "[ERROR] Could not find image: " + imageFileName
            );
        }
        ImageIcon icon = new ImageIcon(imageURL);

        IMAGE_CONTAINER = new ImagePanel(icon.getImage());
    }

    public void reduceMoney(int amountReduced) {
        this.money -= amountReduced;

        System.out.println("[LOG] " + name + " had to pay " + amountReduced + "$ - current money: " + money);
    }

    public void giveMoney(int amountGiven) {
        this.money += amountGiven;

        System.out.println("[LOG] " + name + " received " + amountGiven + "$ - current money: " + money);
    }

    public void buy() {
        runOnEventDispatchThread(() -> {
            BoardTile targetTile = getCurrentTile();
            buy(targetTile);
        });
    }

    public void buy(BoardTile t) {
        runOnEventDispatchThread(() -> t.buy(this));
    }

    public void payRent() {
        BoardTile targetTile = getCurrentTile();
        targetTile.payRent(this);
    }

    public void move(int n) {
        move(n, 250);
    }

    public void move(int n, int ticks) {
        if (!movementEnabled) return;
        if (ticks <= 0) {
            throw new IllegalArgumentException("Ticks must be positive");
        }
        if (n < 0) {
            throw new IllegalArgumentException("[ERROR] Movement steps must not be negative: " + n);
        }
        if (n == 0) {
            return;
        }
        if (SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("[ERROR] Player movement must be started off the event dispatch thread");
        }

        int boardSize = Main.getBoardSize();
        if (boardSize <= 0) {
            throw new IllegalStateException("[ERROR] Cannot move on a board with no positions");
        }

        Movement movement = new Movement(n, ticks);
        synchronized (this) {
            if (activeMovement != null) {
                throw new IllegalStateException("[ERROR] Player is already moving");
            }
            activeMovement = movement;
        }

        SwingUtilities.invokeLater(() -> {
            if (movement.cancelled.get()) {
                movement.finished.countDown();
                return;
            }

            movement.timer = new Timer(movement.ticks, event -> advanceMovement(movement, boardSize));
            movement.timer.setInitialDelay(0);
            if (movement.cancelled.get()) {
                movement.timer.stop();
                movement.finished.countDown();
                return;
            }
            movement.timer.start();
        });

        try {
            movement.finished.await();
        } catch (InterruptedException exception) {
            movement.cancelled.set(true);
            if (movement.timer != null) {
                SwingUtilities.invokeLater(() -> {
                    movement.timer.stop();
                    movement.finished.countDown();
                });
            }
            Thread.currentThread().interrupt();
            throw new IllegalStateException("[ERROR] Player movement was interrupted", exception);
        } finally {
            synchronized (this) {
                if (activeMovement == movement) {
                    activeMovement = null;
                }
            }
        }

        Throwable failure = movement.failure.get();
        if (failure instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        if (failure instanceof Error error) {
            throw error;
        }
        disableMovement();
        payRent();
    }

    private void advanceMovement(Movement movement, int boardSize) {
        try {
            if (movement.cancelled.get()) {
                movement.timer.stop();
                movement.finished.countDown();
                return;
            }

            moveOneStep(boardSize);
            movement.remainingSteps--;
            if (movement.remainingSteps == 0 && movement.enterOnCompletion) {
                movement.enterOnCompletion = false;
                Main.getTileByPos(pos).onEnter(this);
            }
            if (movement.remainingSteps == 0) {
                movement.timer.stop();
                movement.finished.countDown();
            }
        } catch (RuntimeException | Error exception) {
            movement.failure.set(exception);
            movement.timer.stop();
            movement.finished.countDown();
        }
    }

    private void moveOneStep(int boardSize) {
        if (!movementEnabled) return;
        int oldPos = pos;
        int newPos = (oldPos + 1) % boardSize;
        BoardTile currentTile = Main.getTileByPos(oldPos);
        BoardTile targetTile = Main.getTileByPos(newPos);
        if (currentTile == null || targetTile == null) {
            throw new IllegalStateException(
                "[ERROR] Cannot move from board position " + oldPos + " to " + newPos
                + ": a tile is missing"
            );
        }

        currentTile.onLeave(this);
        pos = newPos;
        targetTile.onPass(this);
    }

    public void moveTo(int targetPos, int ticks) {
        if (ticks <= 0) {
            throw new IllegalArgumentException("Ticks must be positive");
        }

        Movement movement;

        synchronized (this) {
            movement = activeMovement;
        }
        
        if (movement == null) {
            throw new IllegalStateException("Player is not moving");
        }
        
        movement.ticks = ticks;
        
        if (movement.timer != null) {
            movement.timer.setDelay(ticks);
        }

        moveTo(targetPos);
    }

    public void moveTo(int targetPos) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("[ERROR] Destination movement must be requested on the event dispatch thread");
        }

        int boardSize = Main.getBoardSize();
        if (targetPos < 0 || targetPos >= boardSize) {
            throw new IllegalArgumentException("[ERROR] Destination is outside the board: " + targetPos);
        }
        if (Main.getTileByPos(targetPos) == null) {
            throw new IllegalStateException("[ERROR] Cannot move to missing board position " + targetPos);
        }

        Movement movement;
        synchronized (this) {
            movement = activeMovement;
        }
        if (movement == null) {
            throw new IllegalStateException("[ERROR] Destination movement can only be requested during a move");
        }

        int additionalSteps = Math.floorMod(targetPos - pos, boardSize);
        if (additionalSteps > 0) {
            movement.remainingSteps += additionalSteps;
            movement.enterOnCompletion = false;
        }
    }

    public void moveTo(String locationName) {
        BoardTile targetTile = Main.getTileByName(locationName);
        moveTo(targetTile.getPos());
    }

    public void moveTo(String locationName, int ticks) {
        BoardTile targetTile = Main.getTileByName(locationName);
        moveTo(targetTile.getPos(), ticks);
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
            throw new IllegalStateException("[ERROR] Player movement was interrupted", exception);
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
    public void disableMovement() {this.movementEnabled = false;}
    public void enableMovement() {this.movementEnabled = true;}

    public String getName() {return name;}
    public Color getColor() {return this.PLAYER_COLOR;}
    public int getPos() {return pos;}
    public int getMoney() {return money;}
    public BoardTile getCurrentTile() {return Main.getTileByPos(pos);}
}