package game.Objects;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.LineBorder;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class DisplayTile extends Tile {
    private LuaValue interactionFunc;
    private String text;
    private final JLabel label = new JLabel("", SwingConstants.CENTER);
    private final AtomicBoolean interactionRunning = new AtomicBoolean();
    
    public DisplayTile(String txt) {
        setPreferredSize(new Dimension(160, 160));
        setMinimumSize(new Dimension(100, 100));
        setBorder(new LineBorder(Color.GRAY));
        TOP_LAYER.setOpaque(false);
        TILE_LAYER.add(label);
        add(TILE_LAYER, JLayeredPane.DEFAULT_LAYER);
        add(TOP_LAYER, JLayeredPane.PALETTE_LAYER);
        addInteractionListener(TILE_LAYER);
        addInteractionListener(TOP_LAYER);
        addInteractionListener(label);
        setText(txt);
    }

    @Override
    public void doLayout() {
        super.doLayout();
        Insets insets = TILE_LAYER.getInsets();
        label.setBounds(
            insets.left,
            insets.top,
            TILE_LAYER.getWidth() - insets.left - insets.right,
            TILE_LAYER.getHeight() - insets.top - insets.bottom
        );
    }

    @Override
    public void interact(Player p) {
        if (interactionFunc == null || interactionFunc.isnil()
                || !interactionRunning.compareAndSet(false, true)) {
            return;
        }

        LuaValue callback = interactionFunc;
        SwingWorker<LuaValue, Void> interaction = new SwingWorker<>() {
            @Override
            protected LuaValue doInBackground() {
                return callback.call(
                    CoerceJavaToLua.coerce(p),
                    CoerceJavaToLua.coerce(DisplayTile.this)
                );
            }

            @Override
            protected void done() {
                try {
                    LuaValue result = get();
                    if (result.isstring()) {
                        setText(result.tojstring());
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    System.err.println("[ERROR] Interrupted while handling display tile interaction");
                    exception.printStackTrace();
                } catch (ExecutionException exception) {
                    Throwable cause = exception.getCause();
                    System.err.println("[ERROR] Display tile interaction failed");
                    cause.printStackTrace();
                } finally {
                    interactionRunning.set(false);
                }
            }
        };
        interaction.execute();
    }

    public void setInteraction(LuaValue interactionFunc) {
        this.interactionFunc = interactionFunc;
    }

    public void setText(String text) {
        runOnEventDispatchThread(() -> setTextOnEventDispatchThread(text));
    }

    private void setTextOnEventDispatchThread(String text) {
        this.text = text;
        label.setText(text);
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
            throw new IllegalStateException("[ERROR] Interrupted while updating display tile", exception);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException("[ERROR] Could not update display tile", cause);
        }
    }

    public String getText() {return text;}
}
