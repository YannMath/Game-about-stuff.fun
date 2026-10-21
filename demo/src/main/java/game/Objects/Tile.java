package game.Objects;

import java.awt.*;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class Tile extends JLayeredPane {
    private final ImagePanel IMAGE_CONTAINER;
    private final JLabel NAME_CONTAINER;
    private final JLabel PRICE_CONTAINER;
    private final JPanel TITLE_LAYER = new JPanel(null);
    private final JPanel CHARACTER_LAYER = new JPanel(null);

    private String locationName;
    private int price = 0;
    private Player owner;
    private Player currentPlayerOnTile;
    private LuaValue onEnterFunc;
    private int pos;

    public Tile(String name, int price, String imageFileName, int pos) {
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
        CHARACTER_LAYER.setOpaque(false);

        IMAGE_CONTAINER = new ImagePanel(icon.getImage());
        TITLE_LAYER.add(IMAGE_CONTAINER);

        NAME_CONTAINER = new JLabel(locationName, SwingConstants.CENTER);
        NAME_CONTAINER.setFont(NAME_CONTAINER.getFont().deriveFont(11f));
        TITLE_LAYER.add(NAME_CONTAINER);

        PRICE_CONTAINER = new JLabel(Integer.toString(price) + "$", SwingConstants.CENTER);
        PRICE_CONTAINER.setFont(PRICE_CONTAINER.getFont().deriveFont(Font.BOLD, 20f));
        TITLE_LAYER.add(PRICE_CONTAINER);

        add(TITLE_LAYER, JLayeredPane.DEFAULT_LAYER);
        add(CHARACTER_LAYER, JLayeredPane.PALETTE_LAYER);
    }

    @Override
    public void doLayout() {
        Insets insets = getInsets();
        int x = insets.left;
        int y = insets.top;
        int width = getWidth() - insets.left - insets.right;
        int height = getHeight() - insets.top - insets.bottom;
        int imageHeight = height / 2;
        int nameHeight = height / 10;

        TITLE_LAYER.setBounds(x, y, width, height);
        CHARACTER_LAYER.setBounds(x, y, width, height);

        IMAGE_CONTAINER.setBounds(0, 0, width, imageHeight);
        NAME_CONTAINER.setBounds(0, imageHeight, width, nameHeight);
        PRICE_CONTAINER.setBounds(
            0,
            imageHeight + nameHeight,
            width,
            height - imageHeight - nameHeight
        );

        if (currentPlayerOnTile != null) {
            currentPlayerOnTile.getImageComponent().setBounds(
                width / 4,
                0,
                width / 2,
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

    public void buy(Player player) {
        owner = player;
        player.reduceMoney(price);

        System.out.println("Player bought " + locationName + " for " + price + "$");
    }

    public void onEnter(Player p) {
        if (onEnterFunc != null && !onEnterFunc.isnil()) {
            onEnterFunc.call(CoerceJavaToLua.coerce(p));
        }
        currentPlayerOnTile = p;
        CHARACTER_LAYER.add(p.getImageComponent());
        doLayout();
        CHARACTER_LAYER.revalidate();
        CHARACTER_LAYER.repaint();
    }

    public void onLeave(Player p) {
        if (currentPlayerOnTile == p) {
            CHARACTER_LAYER.remove(p.getImageComponent());
            currentPlayerOnTile = null;
            CHARACTER_LAYER.revalidate();
            CHARACTER_LAYER.repaint();
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