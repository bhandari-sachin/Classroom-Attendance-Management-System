package service;

import model.User;
import model.UserRole;
import config.UserSQL;

import java.util.List;

public class UserService {

    private final UserSQL userSQL;

    public int getEnrolledClasses(Long userId) {
        return userSQL.countUserClasses(userId);
    }

    public UserService(UserSQL userSQL) {
        this.userSQL = userSQL;
    }

    public List<User> getAllUsers() {
        return userSQL.findAll();
    }

    public List<User> filterByRole(UserRole role, User currentUser) {
        return userSQL.findByRole(role);
    }

    public void deleteUser(Long id, User currentUser) {
        userSQL.delete(id);
    }

    public List<User> searchUsers(String keyword, User currentUser) {
        return userSQL.search(keyword);
    }
}
