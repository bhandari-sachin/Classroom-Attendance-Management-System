package frontend.ui;

import javafx.scene.Group;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;

public class IconFactory {
    private static final String ICON_STYLE_CLASS = "icon-shape";

    private IconFactory() {
        // Utility class
    }

    private static SVGPath path(String content) {
        SVGPath p = new SVGPath();
        p.setContent(content);
        p.setFill(null);
        p.setStrokeWidth(1.5);
        p.getStyleClass().add(ICON_STYLE_CLASS);
        return p;
    }

    private static Rectangle rectangle(double x, double y, double width, double height) {
        Rectangle r = new Rectangle(x, y, width, height);
        r.setFill(null);
        r.setStrokeWidth(1.5);
        r.getStyleClass().add(ICON_STYLE_CLASS);
        return r;
    }

    private static Circle circle(double centerX, double centerY, double radius) {
        Circle c = new Circle(centerX, centerY, radius);
        c.setFill(null);
        c.setStrokeWidth(1.5);
        c.getStyleClass().add(ICON_STYLE_CLASS);
        return c;
    }

    public static Group globe() {
        return new Group(
                path("M12 22.3201C17.5228 22.3201 22 17.8429 22 12.3201C22 6.79722 17.5228 2.32007 12 2.32007C6.47715 2.32007 2 6.79722 2 12.3201C2 17.8429 6.47715 22.3201 12 22.3201Z"),
                path("M2 12.3201H22"),
                path("M12 22.3201C13.933 22.3201 15.5 17.8429 15.5 12.3201C15.5 6.79722 13.933 2.32007 12 2.32007C10.067 2.32007 8.5 6.79722 8.5 12.3201C8.5 17.8429 10.067 22.3201 12 22.3201Z")
        );
    }

    public static Group user() {
        Circle head = new Circle(12, 7, 4);
        head.setFill(null);
        return new Group(
                path("M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"),
                head
        );
    }

    public static Group users() {
        Circle head = circle(9, 7, 4);

        return new Group(
                path("M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"),
                path("M16 3.128a4 4 0 0 1 0 7.744"),
                path("M22 21v-2a4 4 0 0 0-3-3.87"),
                head
        );
    }

    public static Group students() {
        return new Group (
                path("M21.42 10.922a1 1 0 0 0-.019-1.838L12.83 5.18a2 2 0 0 0-1.66 0L2.6 9.08a1 1 0 0 0 0 1.832l8.57 3.908a2 2 0 0 0 1.66 0z"),
                path("M22 10v6"),
                path("M6 12.5V16a6 3 0 0 0 12 0v-3.5")
                );
    }

    public static Group classes() {
        return new Group (
                path("M12 7v14"),
                path("M3 18a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1h5a4 4 0 0 1 4 4 4 4 0 0 1 4-4h5a1 1 0 0 1 1 1v13a1 1 0 0 1-1 1h-6a3 3 0 0 0-3 3 3 3 0 0 0-3-3z")
                );
    }

    public static Group rate() {
        return new Group (
                path("M3 3v16a2 2 0 0 0 2 2h16"),
                path("m19 9-5 5-4-4-3 3")
                );
    }

    public static Group reports() {
        Rectangle rect = rectangle(8, 2, 8, 4);

        return new Group (
                rect,
                path("M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"),
                path("m9 14 2 2 4-4")
                );
    }

    public static Group settings() {
        Circle circle1 = circle(12, 12, 2);
        Circle circle2 = circle(12, 12, 8);

        return new Group (
                path("M11 10.27 7 3.34"),
                path("m11 13.73-4 6.93"),
                path("M12 22v-2"),
                path("M12 2v2"),
                path("M14 12h8"),
                path("m17 20.66-1-1.73"),
                path("m17 3.34-1 1.73"),
                path("M2 12h2"),
                path("m20.66 17-1.73-1"),
                path("m20.66 7-1.73 1"),
                path("m3.34 17 1.73-1"),
                path("m3.34 7 1.73 1"),
                circle1,
                circle2
                );
    }

    public static Group date() {
        Rectangle rect = rectangle(3, 4, 18, 18);

        return new Group (
                path("M8 2v4"),
                path("M16 2v4"),
                rect,
                path("M3 10h18")
                );
    }

    public static Group present() {
        Circle circle = circle(12, 12, 10);

        return new Group (
                circle,
                path("m9 12 2 2 4-4")
                );
    }

    public static Group absent() {
        Circle circle = circle(12, 12, 10);

        return new Group (
                circle,
                path("m15 9-6 6"),
                path("m9 9 6 6")
                );
    }

    public static Group excused() {
        Circle circle = circle(12, 12, 10);

        return new Group (
                circle,
                path("M12 6v6l4 2")
                );
    }

    public static Group qr() {
        Rectangle rect = rectangle(7, 7, 5, 5);
        return new Group (
                path("M17 12v4a1 1 0 0 1-1 1h-4"),
                path("M17 3h2a2 2 0 0 1 2 2v2"),
                path("M17 8V7"),
                path("M21 17v2a2 2 0 0 1-2 2h-2"),
                path("M3 7V5a2 2 0 0 1 2-2h2"),
                path("M7 17h.01"),
                path("M7 21H5a2 2 0 0 1-2-2v-2"),
                rect
                );
    }
}