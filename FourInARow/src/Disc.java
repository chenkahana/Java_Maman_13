import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Disc extends Circle {

    private boolean isColorBlue;

    public boolean isColorBlue() {
        return isColorBlue;
    }

    public Disc(boolean isColorBlue, double size) {
        super(size, isColorBlue ? Color.BLUE : Color.RED);
        this.isColorBlue = isColorBlue;
        setCenterX(size);
        setCenterY(size);
    }
}
