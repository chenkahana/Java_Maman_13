import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class Controller {
    private final int TILE_SIZE = 80;
    private final int COLUMNS = 7;
    private final int ROWS = 6;
    private final double BORDER_WIDTH = 0.5;

    @FXML
    private AnchorPane pane;

    private boolean isGameOver = false;
    private boolean bluesTurn = true;
    private Disc[][] grid = new Disc[COLUMNS][ROWS];
    private Pane discsRoot = new Pane();
    private Alert alert;


    public void initialize() {
        alert = new Alert(AlertType.NONE);
        pane.getChildren().add(discsRoot);
        Shape grid = new Rectangle((COLUMNS) * TILE_SIZE + BORDER_WIDTH, (ROWS) * TILE_SIZE + BORDER_WIDTH);
        grid.setFill(Color.BLACK);
        for (int rows = 0; rows < ROWS; rows++) {
            for (int column = 0; column < COLUMNS; column++) {
                Rectangle rectangle = new Rectangle(TILE_SIZE - BORDER_WIDTH, TILE_SIZE - BORDER_WIDTH);
                rectangle.setFill(Color.WHITE);
                rectangle.setTranslateX(column * (TILE_SIZE) + BORDER_WIDTH);
                rectangle.setTranslateY(rows * (TILE_SIZE) + BORDER_WIDTH);
                grid = Shape.subtract(grid, rectangle);
            }
        }
        List<Rectangle> columns = new ArrayList<>();
        for (int i = 0; i < COLUMNS; i++) {
            Rectangle column = new Rectangle(TILE_SIZE, (ROWS) * TILE_SIZE);
            column.setTranslateX(i * TILE_SIZE);
            column.setFill(Color.TRANSPARENT);
            column.setOnMouseEntered(e -> {
                column.setFill(Color.rgb(180, 85, 200, 0.3));
            });
            column.setOnMouseExited(e -> {
                column.setFill(Color.TRANSPARENT);
            });
            final int columnNum = i;
            column.setOnMouseClicked(e -> dropDisc(new Disc(bluesTurn, TILE_SIZE / 2), columnNum));
            columns.add(column);
        }
        pane.getChildren().add(grid);
        pane.getChildren().addAll(columns);
    }

    private void dropDisc(Disc disc, int column) {
        if (isGameOver) return;
        for (int row = ROWS - 1; row >= 0; row--) {
            if (getDisc(column, row) == null) {
                grid[column][row] = disc;
                discsRoot.getChildren().add(disc);
                disc.setTranslateX(column * (TILE_SIZE) + BORDER_WIDTH);
                TranslateTransition animation = new TranslateTransition(Duration.seconds(0.5), disc);
                animation.setToY(row * (TILE_SIZE) + BORDER_WIDTH);
                int finalRow = row;
                animation.setOnFinished(e -> {
                    isGameOver = false;
                    if (gameHasEnded(column, finalRow)) {
                        gameOver();
                    }
                    bluesTurn = !bluesTurn;
                });
                animation.play();
                isGameOver = true; // the idea is to make the game 'freeze' while a disc is being dropped.
                return;
            }
        }

        alert.setAlertType(AlertType.ERROR);
        alert.setHeaderText("CANNOT PLACE DISC HERE, IT'S FILLED!");
        alert.setContentText("Try placing your disc in a column with a free space.");
        alert.show();
    }

    private void gameOver() {
        isGameOver = true;
        alert.setAlertType(AlertType.INFORMATION);
        alert.setHeaderText("WINNER is: " + (bluesTurn ? "Blue" : "Red"));
        alert.setContentText("To play a new game press the 'clear' button");
        alert.show();
    }

    private boolean gameHasEnded(int column, int row) {
        int numberOfDiscToWinExcludingSelf = 3;

        List<Point2D> horizontal = IntStream.rangeClosed(column - numberOfDiscToWinExcludingSelf,
                column + numberOfDiscToWinExcludingSelf).mapToObj(currentColumn -> new Point2D(currentColumn, row)).toList();

        List<Point2D> vertical = IntStream.rangeClosed(row - numberOfDiscToWinExcludingSelf,
                row + numberOfDiscToWinExcludingSelf).mapToObj(currentRow -> new Point2D(column, currentRow)).toList();

        Point2D topLeft = new Point2D(column - numberOfDiscToWinExcludingSelf, row - numberOfDiscToWinExcludingSelf);
        List<Point2D> diagonal1 = IntStream.rangeClosed(0, 6).mapToObj(i -> topLeft.add(i, i)).toList();

        Point2D bottomLeft = new Point2D(column - numberOfDiscToWinExcludingSelf, row + numberOfDiscToWinExcludingSelf);
        List<Point2D> diagonal2 = IntStream.rangeClosed(0, 6).mapToObj(i -> bottomLeft.add(i, -i)).toList();

        return checkRange(vertical) || checkRange(horizontal) || checkRange(diagonal1) || checkRange(diagonal2);
    }

    private boolean checkRange(List<Point2D> points) {
        int chain = 0;
        for (Point2D point : points) {
            int column = (int) point.getX();
            int row = (int) point.getY();
            Disc disc = getDisc(column, row);
            if (disc != null && disc.isColorBlue() == bluesTurn) { //meaning- if the color of this disc is the color that is currently playing
                chain++;
                if (chain == 4) {
                    return true;
                }
            } else {
                chain = 0;
            }
        }
        return false;
    }

    private Disc getDisc(int column, int row) {
        if (column < 0 || column >= COLUMNS || row < 0 || row >= ROWS) {
            return null;
        }
        return grid[column][row];
    }

    public void clear() {
        bluesTurn = true;
        grid = new Disc[COLUMNS][ROWS];
        discsRoot = new Pane();
        pane.getChildren().removeAll(pane.getChildren());
        isGameOver = false;
        initialize();
    }
}
