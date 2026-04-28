package frontend.ui;

import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.auth.RoleRedirect;
import frontend.i18n.FrontendI18n;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Optional;

/**
 * Shared base class for authentication pages such as login and signup.
 *
 * <p>This class centralizes common UI behavior including:
 * translated titles and subtitles,
 * standard card layout,
 * message label helpers,
 * auto-login redirect handling,
 * and common text utility methods.</p>
 */
public abstract class BaseAuthPage extends StackPane {

    /**
     * Maximum width used by the authentication card container.
     */
    protected static final double CARD_MAX_WIDTH = 420;

    /**
     * Padding inside the authentication card.
     */
    protected static final int CARD_PADDING = 22;

    /**
     * Default vertical spacing between card children.
     */
    protected static final int CARD_SPACING = 12;

    /**
     * Padding applied to the page root.
     */
    protected static final int ROOT_PADDING = 24;

    /**
     * Maximum length of an error message shown in the UI.
     */
    protected static final int MAX_ERROR_MESSAGE_LENGTH = 200;

    /**
     * Creates the base authentication page and applies standard root padding.
     */
    protected BaseAuthPage() {
        setPadding(new Insets(ROOT_PADDING));
    }

    /**
     * Redirects the user immediately if a valid authentication state already exists.
     *
     * @param router application router used for navigation
     * @param jwtStore JWT store used to load persisted authentication state
     */
    protected void validateAutoLogin(AppRouter router, JwtStore jwtStore) {
        Optional<AuthState> existingState = jwtStore.load();
        existingState.ifPresent(state -> router.go(RoleRedirect.routeFor(state.role())));
    }

    /**
     * Creates a translated title label for the page.
     *
     * @param key translation key
     * @param fallback fallback text if the translation key is missing
     * @return styled title label
     */
    protected Label createTitleLabel(String key, String fallback) {
        Label title = new Label(t(key, fallback));
        title.getStyleClass().add("title");
        return title;
    }

    /**
     * Creates a translated subtitle label for the page.
     *
     * @param key translation key
     * @param fallback fallback text if the translation key is missing
     * @return styled subtitle label
     */
    protected Label createSubtitleLabel(String key, String fallback) {
        Label subtitle = new Label(t(key, fallback));
        subtitle.getStyleClass().add("subtitle");
        return subtitle;
    }

    /**
     * Creates a hidden message label with the given style class.
     *
     * <p>This is used for error or informational messages that should
     * only appear when needed.</p>
     *
     * @param styleClass CSS style class to apply
     * @return configured message label
     */
    protected Label createMessageLabel(String styleClass) {
        Label label = new Label();
        label.getStyleClass().add(styleClass);
        label.setVisible(false);
        label.setManaged(false);
        return label;
    }

    /**
     * Creates a primary action button with translated text.
     *
     * @param textKey translation key for button text
     * @param fallbackText fallback text if translation is missing
     * @return styled primary button
     */
    protected Button createPrimaryButton(String textKey, String fallbackText) {
        Button button = new Button(t(textKey, fallbackText));
        button.getStyleClass().add("primary-btn");
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    /**
     * Creates a link-style button with translated text.
     *
     * @param textKey translation key for button text
     * @param fallbackText fallback text if translation is missing
     * @return styled link button
     */
    protected Button createLinkButton(String textKey, String fallbackText) {
        Button button = new Button(t(textKey, fallbackText));
        button.getStyleClass().add("link-button");
        return button;
    }

    /**
     * Creates the main authentication card container.
     *
     * @param children nodes to place inside the card
     * @return styled card container
     */
    protected VBox createCard(Node... children) {
        VBox card = new VBox(CARD_SPACING);
        card.getStyleClass().add("card");
        card.setMaxWidth(CARD_MAX_WIDTH);
        card.setPadding(new Insets(CARD_PADDING));
        card.getChildren().addAll(children);
        return card;
    }

    /**
     * Centers the provided card inside the page and adds it to the root.
     *
     * @param card card container to display
     */
    protected void showCenteredCard(VBox card) {
        StackPane.setAlignment(card, Pos.CENTER);
        getChildren().add(card);
    }

    /**
     * Shows a message label with the given text.
     *
     * @param label target label
     * @param message message text to display
     */
    protected static void showMessage(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    /**
     * Hides a message label and clears its text.
     *
     * @param label target label
     */
    protected static void hideMessage(Label label) {
        label.setText("");
        label.setVisible(false);
        label.setManaged(false);
    }

    /**
     * Safely trims a string value.
     *
     * @param value input string
     * @return trimmed string, or empty string if null
     */
    protected static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Cleans an exception message before displaying it in the UI.
     *
     * <p>If the message is null or blank, a generic error is returned.
     * If the message is too long, it is truncated.</p>
     *
     * @param message raw message
     * @return cleaned message safe for UI display
     */
    protected static String cleanMessage(String message) {
        if (message == null || message.isBlank()) {
            return "Unknown error";
        }

        return message.length() > MAX_ERROR_MESSAGE_LENGTH
                ? message.substring(0, MAX_ERROR_MESSAGE_LENGTH) + "..."
                : message;
    }

    /**
     * Returns a translated text value, or the provided fallback if the key is missing.
     *
     * @param key translation key
     * @param fallback fallback text
     * @return translated text or fallback
     */
    protected static String t(String key, String fallback) {
        String value = FrontendI18n.t(key);
        return key.equals(value) ? fallback : value;
    }
}