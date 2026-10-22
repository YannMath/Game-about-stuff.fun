package game.Objects;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class BoardTile extends Tile {
    private final ImagePanel IMAGE_CONTAINER;
    private final JLabel NAME_CONTAINER;
    private final JLabel PRICE_CONTAINER;

    private String locationName;
    private int price;
    private Player owner;
    private final List<Player> playersOnTile = new ArrayList<>();
    private LuaValue onEnterFunc;
    private int pos;

    public BoardTile(String name, int price, String imageFileName, int pos) {
        var imageURL = getClass().getResource("/images/" + imageFileName);
        if (imageURL == null) {
            throw new IllegalArgumentException(
                "Could not find image: " + imageFileName
            );
        }
        ImageIcon icon = new ImageIcon(imageURL);
        this.locationName = name;
        this.price = price;
        this.pos = pos;

        setPreferredSize(new Dimension(160, 160));
        setMinimumSize(new Dimension(100, 100));
        setBorder(BorderFactory.createLineBorder(Color.GRAY));
        TOP_LAYER.setOpaque(false);

        IMAGE_CONTAINER = new ImagePanel(icon.getImage());
        TILE_LAYER.add(IMAGE_CONTAINER);

        NAME_CONTAINER = new JLabel(locationName, SwingConstants.CENTER);
        NAME_CONTAINER.setFont(NAME_CONTAINER.getFont().deriveFont(11f));
        TILE_LAYER.add(NAME_CONTAINER);

        PRICE_CONTAINER = new JLabel(Integer.toString(price) + "$", SwingConstants.CENTER);
        PRICE_CONTAINER.setFont(PRICE_CONTAINER.getFont().deriveFont(Font.BOLD, 20f));
        TILE_LAYER.add(PRICE_CONTAINER);

        add(TILE_LAYER, JLayeredPane.DEFAULT_LAYER);
        add(TOP_LAYER, JLayeredPane.PALETTE_LAYER);
    }

    @Override
    public void doLayout() {
        super.doLayout();

        Insets insets = getInsets();
        int width = getWidth() - insets.left - insets.right;
        int height = getHeight() - insets.top - insets.bottom;
        int imageHeight = height / 2;
        int nameHeight = height / 10;

        IMAGE_CONTAINER.setBounds(0, 0, width, imageHeight);
        NAME_CONTAINER.setBounds(0, imageHeight, width, nameHeight);
        PRICE_CONTAINER.setBounds(
            0,
            imageHeight + nameHeight,
            width,
            height - imageHeight - nameHeight
        );

        int playerWidth = playersOnTile.isEmpty() ? 0 : width / playersOnTile.size();
        for (int index = 0; index < playersOnTile.size(); index++) {
            playersOnTile.get(index).getImageComponent().setBounds(
                index * playerWidth,
                0,
                playerWidth,
                imageHeight
            );
        }
    }

    private static class ImagePanel extends JPanel {
        private final Image image;

        private ImagePanel(Image image) {
            this.image = image;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            int imageWidth = image.getWidth(this);
            int imageHeight = image.getHeight(this);
            if (imageWidth <= 0 || imageHeight <= 0 || getWidth() <= 0 || getHeight() <= 0) {
                return;
            }

            double scale = Math.max(
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

    public void buy(Player player) {    // TODO: Add the LuaValue buyable so that they cannot buy the start tile anymore, duh!
        if (isOwned()) {System.out.println("[NON PROGRAM ERROR] Tile is already owned"); return;}
        if (player.getMoney() < price) {System.out.println("[NON PROGRAM ERROR] You don't have enough money to buy this property"); return;}
        owner = player;
        player.reduceMoney(price);
        PRICE_CONTAINER.setText("");
        PRICE_CONTAINER.setOpaque(true);
        PRICE_CONTAINER.setBackground(player.getColor());

        System.out.println("[LOG] " + player.getName() + " bought " + locationName + " for " + price + "$");
    }

    public void onEnter(Player p) {     // TODO: Differentiate between onPass and onEnter
        onEnter(p, true);
    }

    public void onEnter(Player p, boolean triggerOnEnter) {
        if (!playersOnTile.contains(p)) {
            playersOnTile.add(p);
            TOP_LAYER.add(p.getImageComponent());
        }
        doLayout();
        TOP_LAYER.revalidate();
        TOP_LAYER.repaint();
        if (triggerOnEnter && onEnterFunc != null && !onEnterFunc.isnil()) {
            onEnterFunc.call(CoerceJavaToLua.coerce(p));
        }
    }

    public void onLeave(Player p) {
        if (playersOnTile.remove(p)) {
            TOP_LAYER.remove(p.getImageComponent());
            doLayout();
            TOP_LAYER.revalidate();
            TOP_LAYER.repaint();
        }
    }

    public void setOnEnter(LuaValue onEnterFunc) {
        this.onEnterFunc = onEnterFunc;
    }

    public boolean isOwned() {return owner != null;}
    public Player getOwner() {return owner;}
    public String getName() {return locationName;}
    public int getPos() {return pos;}
}