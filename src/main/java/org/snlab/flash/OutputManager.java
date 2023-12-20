package org.snlab.flash;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.snlab.network.Pairs;
import org.snlab.network.Port;

public class OutputManager {
    Pairs pairs;

    public OutputManager(Pairs pairs) {
        this.pairs = pairs;
    }

    public void dumpSql(String config) {

        // Read in auth details
        String[] authDetails = new String[3];

        try {
            Scanner in = new Scanner(new File(config));
            int ind = 0;
            while (in.hasNextLine()) {
                String line = in.nextLine();
                authDetails[ind] = line;
                ind++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Connect to Database
        Connection connection = null;

        try {
            System.out.println("Connecting to the database");
            connection = DriverManager.getConnection(authDetails[0], authDetails[1], authDetails[2]);
        } catch (SQLException e) {
            System.out.println("Error connecting to the database");
            e.printStackTrace();
        }

        // Write to Database
        try {
            DatabaseMetaData dbm = connection.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "all-paths", null);
            
            // If all_paths table doesn't exist, create it
            if (!tables.next()) {
                String createTableSql = "CREATE TABLE all_paths ("
                        + "head_ip VARCHAR(64),"
                        + "tail_ip VARCHAR(64),"
                        + "source_se VARCHAR(64),"
                        + "dest_se VARCHAR(64),"
                        + "hop_idx INT"
                        + ")";
                Statement stmt = connection.createStatement();
                stmt.execute(createTableSql);
            }

            // Wipe the table
            String deleteSql = "DELETE FROM all_paths";
            Statement deleteStmt = connection.createStatement();
            deleteStmt.execute(deleteSql);

            // Insert data into the table
            HashMap<List<Long>, List<Port>> paths = pairs.getPaths();
            for (List<Long> path : paths.keySet()) {
                String srcIp = convertIp(path.get(0));
                String destIp = convertIp(path.get(1));
                List<Port> ports = paths.get(path);

                for (int i = 0; i < ports.size() - 1; i++) {
                    String insertSql = "INSERT INTO all_paths (head_ip, tail_ip, source_se, dest_se, hop_idx) VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement insertStmt = connection.prepareStatement(insertSql);

                    insertStmt.setString(1, srcIp);
                    insertStmt.setString(2, destIp);
                    insertStmt.setString(3, ports.get(i).getName());
                    insertStmt.setString(4, ports.get(i + 1).getName());
                    insertStmt.setInt(5, i);

                    insertStmt.executeUpdate();
                }
            }

            connection.close();
        } catch (SQLException e) {
            System.out.println("Error checking for the 'all-paths' table");
            e.printStackTrace();
        }
    }

    public void dumpCsv(String filename) {
        HashMap<List<Long>, List<Port>> paths = pairs.getPaths();

        try (PrintWriter writer = new PrintWriter(new File(filename + ".csv"))) {
            writer.println("head_ip,tail_ip,source_se,dest_se,hop_idx");

            for (List<Long> path : paths.keySet()) {
                String srcIp = convertIp(path.get(0));
                String destIp = convertIp(path.get(1));
                List<Port> ports = paths.get(path);

                for (int i = 0; i < ports.size() - 1; i++) {
                    writer.println(srcIp + "," +
                            destIp + "," +
                            ports.get(i).getName() + "," +
                            ports.get(i + 1).getName() + "," +
                            i);
                }
            }
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the CSV file");
            e.printStackTrace();
        }

    }

    public String convertIp(long ip) {
        // convert decimal ip into binary form
        String binary = Long.toBinaryString(ip);
        // pad with zeros to make 32 bits
        binary = String.format("%32s", binary).replace(' ', '0');

        // split into 4 bytes
        String[] bytes = new String[4];
        bytes[0] = binary.substring(0, 8);
        bytes[1] = binary.substring(8, 16);
        bytes[2] = binary.substring(16, 24);
        bytes[3] = binary.substring(24, 32);

        // convert each byte into decimal
        int[] decimal = new int[4];
        for (int i = 0; i < 4; i++) {
            decimal[i] = Integer.parseInt(bytes[i], 2);
        }

        return decimal[0] + "." + decimal[1] + "." + decimal[2] + "." + decimal[3];
    }
}
