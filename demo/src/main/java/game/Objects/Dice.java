package game.Objects;

import java.util.Random;

public class Dice {
    private final int min;
    private final int max;
    private static final Random RANDOM = new Random();
    
    public Dice(int min, int max) {
        if (min < 1 || max < min) {
            throw new IllegalArgumentException(
                "[ERROR] Dice range must satisfy 1 <= min <= max; received " + min + ".." + max
            );
        }
        this.min = min;
        this.max = max;
    }

    public Dice(int max) {
        this(1, max);
    }

    public int roll() {
        return randomInt(min, max);
    }

    public static int randomInt(int min, int max) {
        if (max < min) {
            throw new IllegalArgumentException(
                "[ERROR] Random range must satisfy min <= max; received " + min + ".." + max
            );
        }
        return RANDOM.nextInt(max - min + 1) + min;
    }

    public static int randomInt(int max) {
        if (max < 0) {
            throw new IllegalArgumentException("[ERROR] Random bound must not be negative: " + max);
        }
        return RANDOM.nextInt(max + 1);
    }
}
