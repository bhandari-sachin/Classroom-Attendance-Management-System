package config;

import model.Session;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

public class SessionSQLTest {

    private static Object createResultSetProxy(boolean[] nextSeq, long[] longs, Date[] dates, String[] strings) {
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
                    if ("id".equals(col)) {
                        return (long) (longs != null && longs.length > 0 ? longs[0] : 0L);
                    }
                    if ("class_id".equals(col)) {
                        return (long) (longs != null && longs.length > 1 ? longs[1] : (longs != null && longs.length > 0 ? longs[0] : 0L));
                    }
                    return 0L;
                }
                if ("getDate".equals(name)) {
                    String col = (String) args[0];
                    if ("session_date".equals(col) && dates != null && dates.length > 0) return dates[0];
                    return null;
                }
                if ("getString".equals(name)) {
                    String col = (String) args[0];
                    if ("qr_token".equals(col) && strings != null && strings.length > 0) return strings[0];
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
    void testFindById_returnsSession() throws Exception {
        Object rs = createResultSetProxy(new boolean[]{true}, new long[]{7L,3L}, new Date[]{Date.valueOf(LocalDate.of(2024,1,1))}, new String[]{"qr-abc"});
        Object stmt = createPreparedStatementProxy(rs);
        Connection conn = createConnectionProxy(stmt);

        DatabaseConnection.setConnectionProvider(() -> conn);
        try {
            SessionSQL sql = new SessionSQL();
            Session session = sql.findById(7L);

            Assertions.assertNotNull(session);
            Assertions.assertEquals(7L, session.getSessionId());

            // check private fields via reflection
            java.lang.reflect.Field classIdField = Session.class.getDeclaredField("classId");
            classIdField.setAccessible(true);
            Object classId = classIdField.get(session);
            Assertions.assertEquals(3L, ((Number) classId).longValue());

            java.lang.reflect.Field qrField = Session.class.getDeclaredField("QRCode");
            qrField.setAccessible(true);
            Object qr = qrField.get(session);
            Assertions.assertEquals("qr-abc", qr);
        } finally {
            DatabaseConnection.resetConnectionProvider();
        }
    }

    @Test
    void testFindByClassId_returnsList() throws Exception {
        Object rs = createResultSetProxy(new boolean[]{true, false}, new long[]{11L,5L}, new Date[]{Date.valueOf(LocalDate.of(2024,2,2))}, new String[]{});
        Object stmt = createPreparedStatementProxy(rs);
        Connection conn = createConnectionProxy(stmt);

        DatabaseConnection.setConnectionProvider(() -> conn);
        try {
            SessionSQL sql = new SessionSQL();
            List<Session> sessions = sql.findByClassId(5L);

            Assertions.assertNotNull(sessions);
            Assertions.assertEquals(1, sessions.size());
            Assertions.assertEquals(11L, sessions.get(0).getSessionId());
        } finally {
            DatabaseConnection.resetConnectionProvider();
        }
    }

    @Test
    void testUpdateQRCode_callsExecuteUpdate() throws Exception {
        Object rs = createResultSetProxy(new boolean[]{}, new long[]{}, new Date[]{}, new String[]{});
        Object stmt = createPreparedStatementProxy(rs);
        Connection conn = createConnectionProxy(stmt);

        DatabaseConnection.setConnectionProvider(() -> conn);
        try {
            SessionSQL sql = new SessionSQL();
            sql.updateQRCode(42L, "new-code");
            // ensure no exceptions
        } finally {
            DatabaseConnection.resetConnectionProvider();
        }
    }
}
