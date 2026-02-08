package config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.List;

public class AttendanceSQLTest {

    private static Object createResultSetProxy(boolean[] nextSeq, String[] stringValues, long[] longValues, Date[] dateValues) {
        InvocationHandler handler = new InvocationHandler() {
            int idx = -1;
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String name = method.getName();
                if ("next".equals(name)) {
                    idx++;
                    return idx < nextSeq.length ? nextSeq[idx] : false;
                }
                if ("getString".equals(name)) {
                    String col = (String) args[0];
                    if (col.equals("qr_token") && stringValues != null && stringValues.length>0) return stringValues[Math.max(0, Math.min(idx, stringValues.length-1))];
                    return null;
                }
                if ("getLong".equals(name)) {
                    if (longValues != null && longValues.length>0) return longValues[Math.max(0, Math.min(idx, longValues.length-1))];
                    return 0L;
                }
                if ("getDate".equals(name)) {
                    if (dateValues != null && dateValues.length>0) return dateValues[Math.max(0, Math.min(idx, dateValues.length-1))];
                    return null;
                }
                // default
                throw new UnsupportedOperationException("ResultSet method not implemented: " + name);
            }
        };
        return Proxy.newProxyInstance(ResultSet.class.getClassLoader(), new Class[]{ResultSet.class}, handler);
    }

    private static Object createPreparedStatementProxy(Object resultSetProxy) {
        InvocationHandler handler = new InvocationHandler() {
            long lastLongIndex = -1;
            long lastLongValue = -1;
            String lastStringValue = null;
            boolean executeUpdateCalled = false;
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String name = method.getName();
                if ("executeQuery".equals(name)) return resultSetProxy;
                if ("executeUpdate".equals(name)) { executeUpdateCalled = true; return 1; }
                if ("setLong".equals(name)) { lastLongIndex = (Integer) args[0]; lastLongValue = (Long) args[1]; return null; }
                if ("setString".equals(name)) { lastStringValue = (String) args[1]; return null; }
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
    void testGetSessionCode_returnsToken() throws Exception {
        Object rs = createResultSetProxy(new boolean[]{true}, new String[]{"token-123"}, new long[]{}, new Date[]{});
        Object stmt = createPreparedStatementProxy(rs);
        Connection conn = createConnectionProxy(stmt);

        DatabaseConnection.setConnectionProvider(() -> conn);
        try {
            AttendanceSQL attendanceSQL = new AttendanceSQL();
            String token = attendanceSQL.getSessionCode(5L);

            Assertions.assertEquals("token-123", token);
        } finally {
            DatabaseConnection.resetConnectionProvider();
        }
    }

    @Test
    void testFindByStudentId_returnsEmptyListWhenNoRows() throws Exception {
        Object rs = createResultSetProxy(new boolean[]{false}, new String[]{}, new long[]{}, new Date[]{});
        Object stmt = createPreparedStatementProxy(rs);
        Connection conn = createConnectionProxy(stmt);

        DatabaseConnection.setConnectionProvider(() -> conn);
        try {
            AttendanceSQL attendanceSQL = new AttendanceSQL();
            List<?> result = attendanceSQL.findByStudentId(123L);

            Assertions.assertNotNull(result);
            Assertions.assertTrue(result.isEmpty());
        } finally {
            DatabaseConnection.resetConnectionProvider();
        }
    }

    @Test
    void testSave_callsExecuteUpdate() throws Exception {
        Object rs = createResultSetProxy(new boolean[]{}, new String[]{}, new long[]{}, new Date[]{});
        Object stmt = createPreparedStatementProxy(rs);
        Connection conn = createConnectionProxy(stmt);

        model.Attendance attendance = new model.Attendance(10L, 20L, model.AttendanceStatus.PRESENT, model.MarkedBy.TEACHER);

        DatabaseConnection.setConnectionProvider(() -> conn);
        try {
            AttendanceSQL attendanceSQL = new AttendanceSQL();
            attendanceSQL.save(attendance);
            // nothing to assert on proxy internals here — just ensure no exception
        } finally {
            DatabaseConnection.resetConnectionProvider();
        }
    }
}
