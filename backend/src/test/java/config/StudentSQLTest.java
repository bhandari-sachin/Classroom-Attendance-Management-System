package config;

import model.Student;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class StudentSQLTest {

    private static Object createResultSetProxy(boolean[] nextSeq, long[] longValues, String nameValue, String emailValue) {
        InvocationHandler handler = new InvocationHandler() {
            int idx = -1;
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String name = method.getName();
                if ("next".equals(name)) {
                    idx++;
                    return idx < nextSeq.length ? nextSeq[idx] : false;
                }
                if ("getLong".equals(name)) {
                    String col = (String) args[0];
                    if ("student_id".equals(col) || "id".equals(col)) {
                        return longValues != null && longValues.length>0 ? longValues[Math.max(0, Math.min(idx, longValues.length-1))] : 0L;
                    }
                    return 0L;
                }
                if ("getString".equals(name)) {
                    String col = (String) args[0];
                    if ("name".equals(col)) return nameValue;
                    if ("email".equals(col)) return emailValue;
                    return null;
                }
                throw new UnsupportedOperationException("ResultSet method not implemented: " + name);
            }
        };
        return Proxy.newProxyInstance(ResultSet.class.getClassLoader(), new Class[]{ResultSet.class}, handler);
    }

    private static Object createPreparedStatementProxy(Object resultSetProxy) {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String name = method.getName();
                if ("executeQuery".equals(name)) return resultSetProxy;
                if ("executeUpdate".equals(name)) return 1;
                if ("setLong".equals(name)) return null;
                if ("setString".equals(name)) return null;
                if ("close".equals(name)) return null;
                throw new UnsupportedOperationException("PreparedStatement method not implemented: " + name);
            }
        };
        return Proxy.newProxyInstance(PreparedStatement.class.getClassLoader(), new Class[]{PreparedStatement.class}, handler);
    }

    private static Connection createConnectionProxy(Object preparedStatementProxy) {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String name = method.getName();
                if ("prepareStatement".equals(name)) return preparedStatementProxy;
                if ("close".equals(name)) return null;
                throw new UnsupportedOperationException("Connection method not implemented: " + name);
            }
        };
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class[]{Connection.class}, handler);
    }

    @Test
    void testFindById_returnsStudent() throws Exception {
        Object rs = createResultSetProxy(new boolean[]{true}, new long[]{55L}, "Alice", "alice@example.com");
        Object stmt = createPreparedStatementProxy(rs);
        Connection conn = createConnectionProxy(stmt);

        DatabaseConnection.setConnectionProvider(() -> conn);
        try {
            StudentSQL sql = new StudentSQL();
            Student student = sql.findById(55L);

            Assertions.assertNotNull(student);
            Assertions.assertEquals(55L, student.getStudentId());
            Assertions.assertEquals("Alice", student.getFirstName());
            Assertions.assertEquals("alice@example.com", student.getEmail());
        } finally {
            DatabaseConnection.resetConnectionProvider();
        }
    }

    @Test
    void testFindById_returnsNullWhenNotFound() throws Exception {
        Object rs = createResultSetProxy(new boolean[]{false}, new long[]{}, "", "");
        Object stmt = createPreparedStatementProxy(rs);
        Connection conn = createConnectionProxy(stmt);

        DatabaseConnection.setConnectionProvider(() -> conn);
        try {
            StudentSQL sql = new StudentSQL();
            Student student = sql.findById(999L);

            Assertions.assertNull(student);
        } finally {
            DatabaseConnection.resetConnectionProvider();
        }
    }

    @Test
    void testFindByClassId_returnsList() throws Exception {
        Object rs = createResultSetProxy(new boolean[]{true, true, false}, new long[]{21L,22L}, "Bob", "bob@example.com");
        Object stmt = createPreparedStatementProxy(rs);
        Connection conn = createConnectionProxy(stmt);

        DatabaseConnection.setConnectionProvider(() -> conn);
        try {
            StudentSQL sql = new StudentSQL();
            List<Student> students = sql.findByClassId(10L);

            Assertions.assertNotNull(students);
            Assertions.assertEquals(2, students.size());
            boolean has21 = false;
            boolean has22 = false;
            for (Student s : students) {
                if (Long.valueOf(21L).equals(s.getStudentId())) has21 = true;
                if (Long.valueOf(22L).equals(s.getStudentId())) has22 = true;
            }
            Assertions.assertTrue(has21 && has22, "Expected student IDs 21 and 22 to be present");
        } finally {
            DatabaseConnection.resetConnectionProvider();
        }
    }

    @Test
    void testFindByClassId_emptyListWhenNoRows() throws Exception {
        Object rs = createResultSetProxy(new boolean[]{false}, new long[]{}, "", "");
        Object stmt = createPreparedStatementProxy(rs);
        Connection conn = createConnectionProxy(stmt);

        DatabaseConnection.setConnectionProvider(() -> conn);
        try {
            StudentSQL sql = new StudentSQL();
            List<Student> students = sql.findByClassId(123L);

            Assertions.assertNotNull(students);
            Assertions.assertTrue(students.isEmpty());
        } finally {
            DatabaseConnection.resetConnectionProvider();
        }
    }
}
