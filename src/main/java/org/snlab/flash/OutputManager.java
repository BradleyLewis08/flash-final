package org.snlab.flash;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

import org.snlab.network.Pairs;
import org.snlab.network.Port;

public class OutputManager {
    Pairs pairs;

    public OutputManager(Pairs pairs) {
        this.pairs = pairs;
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
