package Rocxoiu_Mario;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BookingAppTest {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/postgres";
        String user = "postgres";
        String password = "Mr719782004";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            createTables(conn);
            insertTestData(conn);
            printRoomPrices(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS accommodation_room_fair_relation");
            stmt.execute("DROP TABLE IF EXISTS room_fair");
            stmt.execute("DROP TABLE IF EXISTS accommodation");

            stmt.execute("CREATE TABLE accommodation (" +
                    "id INT PRIMARY KEY, " +
                    "type VARCHAR(32), " +
                    "bed_type VARCHAR(32), " +
                    "max_guests INT, " +
                    "description VARCHAR(512))");

            stmt.execute("CREATE TABLE room_fair (" +
                    "id INT PRIMARY KEY, " +
                    "value DOUBLE PRECISION, " +
                    "season VARCHAR(32))");

            stmt.execute("CREATE TABLE accommodation_room_fair_relation (" +
                    "id INT PRIMARY KEY, " +
                    "accommodation_id INT, " +
                    "room_fair_id INT, " +
                    "FOREIGN KEY (accommodation_id) REFERENCES accommodation(id), " +
                    "FOREIGN KEY (room_fair_id) REFERENCES room_fair(id))");
        }
    }

    private static void insertTestData(Connection conn) throws SQLException {
        String insertAccommodation = "INSERT INTO accommodation (id, type, bed_type, max_guests, description) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement accStmt = conn.prepareStatement(insertAccommodation)) {
            accStmt.setInt(1, 1);
            accStmt.setString(2, "Single");
            accStmt.setString(3, "Single Bed");
            accStmt.setInt(4, 1);
            accStmt.setString(5, "A small room for one person");
            accStmt.executeUpdate();

            accStmt.setInt(1, 2);
            accStmt.setString(2, "Double");
            accStmt.setString(3, "Double Bed");
            accStmt.setInt(4, 2);
            accStmt.setString(5, "Comfortable room for two");
            accStmt.executeUpdate();
        }

        String insertRoomFair = "INSERT INTO room_fair (id, value, season) VALUES (?, ?, ?)";
        try (PreparedStatement fairStmt = conn.prepareStatement(insertRoomFair)) {
            fairStmt.setInt(1, 1);
            fairStmt.setDouble(2, 59.99);
            fairStmt.setString(3, "Low");
            fairStmt.executeUpdate();

            fairStmt.setInt(1, 2);
            fairStmt.setDouble(2, 89.99);
            fairStmt.setString(3, "High");
            fairStmt.executeUpdate();
        }

        String insertRelation = "INSERT INTO accommodation_room_fair_relation (id, accommodation_id, room_fair_id) VALUES (?, ?, ?)";
        try (PreparedStatement relStmt = conn.prepareStatement(insertRelation)) {
            relStmt.setInt(1, 1);
            relStmt.setInt(2, 1);
            relStmt.setInt(3, 1);
            relStmt.executeUpdate();

            relStmt.setInt(1, 2);
            relStmt.setInt(2, 2);
            relStmt.setInt(3, 2);
            relStmt.executeUpdate();
        }
    }

    private static void printRoomPrices(Connection conn) throws SQLException {
        String query = """
                SELECT a.type, a.bed_type, a.max_guests, rf.value, rf.season
                FROM accommodation a
                JOIN accommodation_room_fair_relation arfr ON a.id = arfr.accommodation_id
                JOIN room_fair rf ON rf.id = arfr.room_fair_id
                """;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("Room Prices:");
            while (rs.next()) {
                String type = rs.getString("type");
                String bedType = rs.getString("bed_type");
                int guests = rs.getInt("max_guests");
                double price = rs.getDouble("value");
                String season = rs.getString("season");

                System.out.printf("Type: %s | Bed: %s | Max Guests: %d | Price: %.2f | Season: %s%n",
                        type, bedType, guests, price, season);
            }
        }
    }
}
