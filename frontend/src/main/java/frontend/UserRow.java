package frontend;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class UserRow {

    private final StringProperty user = new SimpleStringProperty();
    private final StringProperty type = new SimpleStringProperty();
    private final StringProperty enrolled = new SimpleStringProperty();

    public UserRow(String user, String type, String enrolled) {
        this.user.set(user);
        this.type.set(type);
        this.enrolled.set(enrolled);
    }

    public String getUser() {
        return user.get();
    }

    public String getType() {
        return type.get();
    }

    public String getEnrolled() {
        return enrolled.get();
    }

    public StringProperty userProperty() {
        return user;
    }

    public StringProperty typeProperty() {
        return type;
    }

    public StringProperty enrolledProperty() {
        return enrolled;
    }
}