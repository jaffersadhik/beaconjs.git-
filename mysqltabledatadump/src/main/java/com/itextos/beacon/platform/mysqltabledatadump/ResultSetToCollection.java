package com.itextos.beacon.platform.mysqltabledatadump;

import java.sql.*;
import java.util.*;

public class ResultSetToCollection {
    public static List<Map<String, Object>> resultSetToList(ResultSet rs) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(metaData.getColumnName(i), rs.getObject(i));
            }
            list.add(row);
        }
        return list;
    }

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/your_database", "username", "password");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM your_table")) {

            List<Map<String, Object>> data = resultSetToList(rs);
            System.out.println(data);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
