package util;

import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextInputControl;

public final class RTLUtil {
    private RTLUtil() {
    }

    public static void apply(Parent root) {
        root.setNodeOrientation(
                I18n.isRTL()
                        ? NodeOrientation.RIGHT_TO_LEFT
                        : NodeOrientation.LEFT_TO_RIGHT
        );
    }

    public static void applyTextAlignment(Parent root) {
        root.lookupAll(".label").forEach(node -> {
            if (node instanceof Labeled labeled) {
                labeled.setNodeOrientation(
                        I18n.isRTL()
                                ? NodeOrientation.RIGHT_TO_LEFT
                                : NodeOrientation.LEFT_TO_RIGHT
                );
            }
        });

        root.lookupAll(".text-field, .password-field, .text-area").forEach(node -> {
            if (node instanceof TextInputControl input) {
                input.setNodeOrientation(
                        I18n.isRTL()
                                ? NodeOrientation.RIGHT_TO_LEFT
                                : NodeOrientation.LEFT_TO_RIGHT
                );
            }
        });
    }
}