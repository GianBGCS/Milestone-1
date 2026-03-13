import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerRepository {
    private static final String DB_URL = "jdbc:sqlite:hangar_system.db";
    private Connection connection;

    public CustomerRepository() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(DB_URL);
            if (this.connection != null) createTable();
        } catch (Exception e) {
            System.err.println("Database Connection Failed: " + e.getMessage());
        }
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS customers (id INTEGER PRIMARY KEY, name TEXT, phone TEXT, email TEXT)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public boolean isNameDuplicate(String name) {
        String sql = "SELECT 1 FROM customers WHERE name = ? COLLATE NOCASE";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            return pstmt.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    public boolean isPhoneDuplicate(String phone) {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT 1 FROM customers WHERE phone = ?")) {
            pstmt.setString(1, phone);
            return pstmt.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    public boolean isEmailDuplicate(String email) {
        String sql = "SELECT 1 FROM customers WHERE LOWER(email) = LOWER(?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            return pstmt.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    public void saveCustomer(Customer c) {
        String sql = "INSERT INTO customers(id, name, phone, email) VALUES(?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, c.getId());
            pstmt.setString(2, c.getName());
            pstmt.setString(3, c.getPhone());
            pstmt.setString(4, c.getEmail());
            pstmt.executeUpdate();
            System.out.println("\n[SUCCESS] Saved with ID: " + c.getId());
        } catch (SQLException e) { System.out.println("Save failed: " + e.getMessage()); }
    }

    public void searchCustomerById(int id) {
        String sql = "SELECT * FROM customers WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.isBeforeFirst()) {
                System.out.println("No customer found with ID: " + id);
                return;
            }
            while (rs.next()) {
                System.out.printf("ID: %d | Name: %s | Phone: %s | Email: %s%n",
                        rs.getInt("id"), rs.getString("name"), rs.getString("phone"), rs.getString("email"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM customers")) {
            while (rs.next()) {
                list.add(new Customer.Builder().setId(rs.getInt("id")).setName(rs.getString("name"))
                        .setPhone(rs.getString("phone")).setEmail(rs.getString("email")).build());
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean deleteOldRecord(int id) {
        try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM customers WHERE id = ?")) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public void close() { try { if (connection != null) connection.close(); } catch (SQLException e) {} }
}