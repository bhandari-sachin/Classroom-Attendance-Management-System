package frontend.ui;

import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.auth.Role;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class BaseAuthPageTest {

    @BeforeAll
    static void initJavaFx() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException alreadyStarted) {
            latch.countDown();
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX toolkit failed to start");
    }

    @Test
    void constructorShouldApplyRootPadding() {
        TestBaseAuthPage page = new TestBaseAuthPage();

        assertEquals(BaseAuthPage.ROOT_PADDING, page.getPadding().getTop());
        assertEquals(BaseAuthPage.ROOT_PADDING, page.getPadding().getRight());
        assertEquals(BaseAuthPage.ROOT_PADDING, page.getPadding().getBottom());
        assertEquals(BaseAuthPage.ROOT_PADDING, page.getPadding().getLeft());
    }

    @Test
    void validateAutoLoginShouldRedirectWhenStateExists() {
        TestBaseAuthPage page = new TestBaseAuthPage();
        TestRouter router = new TestRouter();
        TestJwtStore store = new TestJwtStore(new AuthState("token", Role.STUDENT, "Oscar"));

        page.callValidateAutoLogin(router, store);

        assertEquals("student-dashboard", router.lastRoute);
    }

    @Test
    void validateAutoLoginShouldNotRedirectWhenStateDoesNotExist() {
        TestBaseAuthPage page = new TestBaseAuthPage();
        TestRouter router = new TestRouter();
        TestJwtStore store = new TestJwtStore(null);

        page.callValidateAutoLogin(router, store);

        assertNull(router.lastRoute);
    }

    @Test
    void createTitleLabelShouldUseFallbackAndTitleStyle() {
        TestBaseAuthPage page = new TestBaseAuthPage();

        Label label = page.callCreateTitleLabel();

        assertEquals("Login Title", label.getText());
        assertTrue(label.getStyleClass().contains("title"));
    }

    @Test
    void createSubtitleLabelShouldUseFallbackAndSubtitleStyle() {
        TestBaseAuthPage page = new TestBaseAuthPage();

        Label label = page.callCreateSubtitleLabel();

        assertEquals("Subtitle Text", label.getText());
        assertTrue(label.getStyleClass().contains("subtitle"));
    }

    @Test
    void createMessageLabelShouldBeHiddenAndUnmanagedInitially() {
        TestBaseAuthPage page = new TestBaseAuthPage();

        Label label = page.callCreateMessageLabel();

        assertTrue(label.getStyleClass().contains("error-label"));
        assertFalse(label.isVisible());
        assertFalse(label.isManaged());
        assertEquals("", label.getText());
    }

    @Test
    void createPrimaryButtonShouldUseFallbackStyleAndMaxWidth() {
        TestBaseAuthPage page = new TestBaseAuthPage();

        Button button = page.callCreatePrimaryButton();

        assertEquals("Submit", button.getText());
        assertTrue(button.getStyleClass().contains("primary-btn"));
        assertEquals(Double.MAX_VALUE, button.getMaxWidth());
    }

    @Test
    void createLinkButtonShouldUseFallbackAndLinkStyle() {
        TestBaseAuthPage page = new TestBaseAuthPage();

        Button button = page.callCreateLinkButton();

        assertEquals("Go to signup", button.getText());
        assertTrue(button.getStyleClass().contains("link-button"));
    }

    @Test
    void createCardShouldApplyCardSettingsAndChildren() {
        TestBaseAuthPage page = new TestBaseAuthPage();
        Label first = new Label("First");
        Label second = new Label("Second");

        VBox card = page.callCreateCard(first, second);

        assertTrue(card.getStyleClass().contains("card"));
        assertEquals(BaseAuthPage.CARD_MAX_WIDTH, card.getMaxWidth());
        assertEquals(BaseAuthPage.CARD_PADDING, card.getPadding().getTop());
        assertEquals(BaseAuthPage.CARD_PADDING, card.getPadding().getRight());
        assertEquals(BaseAuthPage.CARD_PADDING, card.getPadding().getBottom());
        assertEquals(BaseAuthPage.CARD_PADDING, card.getPadding().getLeft());
        assertEquals(2, card.getChildren().size());
        assertSame(first, card.getChildren().get(0));
        assertSame(second, card.getChildren().get(1));
    }

    @Test
    void showCenteredCardShouldAddCardToChildrenAndCenterIt() {
        TestBaseAuthPage page = new TestBaseAuthPage();
        VBox card = new VBox();

        page.callShowCenteredCard(card);

        assertTrue(page.getChildren().contains(card));
        assertEquals(Pos.CENTER, StackPane.getAlignment(card));
    }

    @Test
    void showMessageShouldSetTextVisibleAndManaged() {
        Label label = new Label();

        BaseAuthPage.showMessage(label, "Invalid password");

        assertEquals("Invalid password", label.getText());
        assertTrue(label.isVisible());
        assertTrue(label.isManaged());
    }

    @Test
    void hideMessageShouldClearTextAndHideLabel() {
        Label label = new Label("Some message");
        label.setVisible(true);
        label.setManaged(true);

        BaseAuthPage.hideMessage(label);

        assertEquals("", label.getText());
        assertFalse(label.isVisible());
        assertFalse(label.isManaged());
    }

    @Test
    void safeTrimShouldReturnEmptyStringForNull() {
        assertEquals("", BaseAuthPage.safeTrim(null));
    }

    @Test
    void safeTrimShouldTrimValue() {
        assertEquals("hello", BaseAuthPage.safeTrim("  hello  "));
    }

    @Test
    void cleanMessageShouldReturnUnknownErrorForNull() {
        assertEquals("Unknown error", BaseAuthPage.cleanMessage(null));
    }

    @Test
    void cleanMessageShouldReturnUnknownErrorForBlank() {
        assertEquals("Unknown error", BaseAuthPage.cleanMessage("   "));
    }

    @Test
    void cleanMessageShouldReturnOriginalMessageWhenShortEnough() {
        assertEquals("Simple error", BaseAuthPage.cleanMessage("Simple error"));
    }

    @Test
    void cleanMessageShouldTruncateLongMessage() {
        String longMessage = "a".repeat(BaseAuthPage.MAX_ERROR_MESSAGE_LENGTH + 20);

        String cleaned = BaseAuthPage.cleanMessage(longMessage);

        assertEquals(BaseAuthPage.MAX_ERROR_MESSAGE_LENGTH + 3, cleaned.length());
        assertTrue(cleaned.endsWith("..."));
    }

    @Test
    void tShouldReturnFallbackWhenTranslationKeyIsMissing() {
        String result = BaseAuthPage.t("missing.key.for.test", "Fallback Text");

        assertEquals("Fallback Text", result);
    }

    private static class TestBaseAuthPage extends BaseAuthPage {

        Label callCreateTitleLabel() {
            return createTitleLabel("missing.key.for.test", "Login Title");
        }

        Label callCreateSubtitleLabel() {
            return createSubtitleLabel("missing.key.for.test", "Subtitle Text");
        }

        Label callCreateMessageLabel() {
            return createMessageLabel("error-label");
        }

        Button callCreatePrimaryButton() {
            return createPrimaryButton("missing.key.for.test", "Submit");
        }

        Button callCreateLinkButton() {
            return createLinkButton("missing.key.for.test", "Go to signup");
        }

        VBox callCreateCard(javafx.scene.Node... children) {
            return createCard(children);
        }

        void callShowCenteredCard(VBox card) {
            showCenteredCard(card);
        }

        void callValidateAutoLogin(AppRouter router, JwtStore store) {
            validateAutoLogin(router, store);
        }
    }

    private static class TestJwtStore extends JwtStore {
        private final AuthState state;

        TestJwtStore(AuthState state) {
            this.state = state;
        }

        @Override
        public Optional<AuthState> load() {
            return Optional.ofNullable(state);
        }
    }

    private static class TestRouter extends AppRouter {
        private String lastRoute;

        TestRouter() {
            super(new Scene(new StackPane()));
        }

        @Override
        public void go(String route) {
            this.lastRoute = route;
        }
    }
}