package fiskie.gonav.scanner;

public class SpiralGenerator {
    /**
     * @param spiralRadius the radius of the spiral
     * @param callback     a SpiralGeneratorCallback implementing yield(x, y)
     */
    public void generate(int spiralRadius, SpiralGeneratorCallback callback) {
        int x = 0;
        int y = 0;
        int direction = 1;
        int circ = (spiralRadius * spiralRadius) + 1;
        int maxIterations = circ * circ;
        int cursor = 0;

        for (int m = 1; m <= circ + 1; m++) {
            while (2 * x * direction < m && cursor < maxIterations) {
                callback.yield(x, y);
                x += direction;
                cursor++;
            }

            while (2 * y * direction < m && cursor < maxIterations) {
                callback.yield(x, y);
                y += direction;
                cursor++;
            }

            direction = -1 * direction;

            if (cursor == maxIterations)
                break;
        }
    }

    public interface SpiralGeneratorCallback {
        void yield(int x, int y);
    }
}
