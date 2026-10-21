package game.UI;

import javax.swing.*;

public class MainFrame extends JFrame {
    private final MainPanel mainPanel = new MainPanel();

    public MainFrame() {
        add(mainPanel);
    }

    public GamePanel getGamePanel() {
        return mainPanel.getGamePanel();
    }
}
