
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.sql.*;

import static java.sql.DriverManager.getConnection;
import static org.junit.jupiter.api.Assertions.*;

public class ConnectionTest {
    private static Connection connection;
    @BeforeAll
    static void init() throws SQLException {
        connect("demo_simple_sql.db");
        createTable("CREATE TABLE IF NOT EXISTS employee_info\n" +
                "(id integer PRIMARY KEY,\n" +
                "first_name text NOT NULL,\n" +
                "last_name text NOT NULL,\n" +
                "phone_number text NOT NULL);");
        insertEmployeeInfo(1,"Андрей", "Гагарин", "+7 911 0000001");
        insertEmployeeInfo(2,"Петр", "Петров", "+7 911 0000002");
        insertEmployeeInfo(3,"Кирилл", "Сидоров", "+7 911 0000003");
        insertEmployeeInfo(4,"Фрэнсис", "Сирмайн", "+7 911 0000004");
    }

    private static void connect(String name) {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = getConnection("jdbc:sqlite:"+name);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Opened database successfully");
    }

    private static void createTable(String sql) {
        try{
            Statement stmt = connection.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void insertEmployeeInfo(Integer id, String firstName, String lastName, String phone) {
        String sql = "INSERT INTO employee_info(id, first_name, last_name, phone_number) VALUES(?,?,?,?)";
        try{
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, id);
            pstmt.setString(2, firstName);
            pstmt.setString(3, lastName);
            pstmt.setString(4, phone);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    void selectTest() {
        String sql = "SELECT * FROM employee_info";
        try {
            Statement stmt  = connection.createStatement();
            ResultSet rs    = stmt.executeQuery(sql);
            assertNotNull(rs);
            int countTableSize = 0;
            while (rs.next()) {
                countTableSize++;
                System.out.println
                        (rs.getInt("id") +  "\t" +
                        rs.getString("first_name") + "\t" +
                        rs.getString("last_name") + "\t" +
                        rs.getString("phone_number"));
            }
            assertEquals(4,countTableSize);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @ParameterizedTest
    @CsvSource({"Андрей, Гагарин", "Петр, Петров", "Кирилл, Сидоров", "Фрэнсис, Сирмайн"})
    void firstLastNameTest(String firstName, String lastName) throws SQLException {
        String sql = "SELECT * FROM employee_info WHERE first_name='" + firstName + "'";
        Statement stmt  = connection.createStatement();
        String nameString = "";
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            nameString = rs.getString(3);
        }
        Assertions.assertEquals(lastName, nameString);
    }

    @ParameterizedTest
    @CsvSource({"1, +7 911 0000001"})
    void idPhoneTest(Short id, String phoneNumber) throws SQLException {
        String sql = "SELECT * FROM employee_info WHERE id='" + id + "'";
        Statement stmt  = connection.createStatement();
        String nameString = "";
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            nameString = rs.getString(4);
        }
        Assertions.assertEquals(phoneNumber, nameString);
    }

    @AfterAll
    static void close() throws SQLException {
        String sqlDeleteInfo = "DELETE FROM employee_info";

        try {
            Statement stmt  = connection.createStatement();
            stmt.execute(sqlDeleteInfo);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        connection.close();
        System.out.println("Closed database successfully");
    }
}
