package frontend;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import model.UserRole;

public class UserRow {
    private final StringProperty user = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty type = new SimpleStringProperty();
    private final StringProperty enrolled = new SimpleStringProperty();

    public UserRow(String user, String email, UserRole type, String enrolled) {
        this.user.set(user);
        this.email.set(email);
        this.type.set(String.valueOf(type));
        this.enrolled.set(enrolled);
    }

    public String getUser() { return user.get(); }
    public StringProperty userProperty() { return user; }
    public StringProperty emailProperty() { return email; }
    public StringProperty typeProperty() { return type; }
    public StringProperty enrolledProperty() { return enrolled; }
}
