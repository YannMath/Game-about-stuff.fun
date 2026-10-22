package game.Objects;

import java.awt.*;
import javax.swing.*;

import game.Main;
public class Player {
    private final ImagePanel IMAGE_CONTAINER;

    private int money = 0;
    private int pos = 0;
    
    public Player(String imageFileName) {
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

    public void move(int n) {
        int newPos = (pos + n) % Main.getBoardSize();
        System.out.println("Player is now at position: " + newPos);
        System.out.println("Old position: " + pos);
        Tile currentTile = Main.getTileByPos(pos);
        Tile targetTile = Main.getTileByPos(newPos);
        this.pos = newPos;

        currentTile.onLeave(this);
        targetTile.onEnter(this);
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
}
