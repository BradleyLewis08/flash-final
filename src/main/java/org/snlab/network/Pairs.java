package org.snlab.network;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * Class for parsing and storying the IP pairs to be used in the network when
 * generating the paths
 */
public class Pairs {
    private HashMap<Long, HashMap<Long, Port>> destToSource;
    private HashMap<List<Long>, List<Port>> paths;
    private Network network;

    /**
     * Constructor
     */
    public Pairs(String filename, Network n) {
        destToSource = new HashMap<Long, HashMap<Long, Port>>();
        paths = new HashMap<List<Long>, List<Port>>();
        network = n;
        try {
            // List all current directories in current directory
            Scanner in = new Scanner(new File(filename));
            while (in.hasNextLine()) {
                String line = in.nextLine();
                String[] tokens = line.split(" ");
                Device device = network.getDevice(tokens[0]);
                Port port = device.getPort(tokens[1]);
                long srcIp = Long.parseLong(tokens[2]);
                long dstIp = Long.parseLong(tokens[3]);
                addPair(port, srcIp, dstIp);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // If the file exists open it and parse it line by line

    }

    /**
     * Getters
     */
    public long[] getDestinations() {
        // Return a list of all the destinations
        long[] destinations = new long[destToSource.size()];
        int i = 0;
        
        for (long dstIp : destToSource.keySet()) {
            destinations[i] = dstIp;
            i++;
        }

        return destinations;
    }

    public HashMap<Long, Port> getSources(long dstIp) {
        // Return the hashmap with all the source IPs and their associated devices
        return destToSource.get(dstIp);
    }

    public HashMap<List<Long>, List<Port>> getPaths() {
        return paths;
    }

    /**
     * Add a pair
     */
    public void addPair(Port port, long srcIp, long dstIp) {
        // Check if dstIp is inside the map
        HashMap<Long, Port> srcIpToDevice = destToSource.get(dstIp);
        if (srcIpToDevice == null) {
            srcIpToDevice = new HashMap<Long, Port>();
            srcIpToDevice.put(srcIp, port);
            destToSource.put(dstIp, srcIpToDevice);
        } else if (!srcIpToDevice.containsKey(srcIp) || !srcIpToDevice.get(srcIp).equals(port)) {
            srcIpToDevice.put(srcIp, port);
        }
    }

    /**
     * Add a path
     */
    public void addPath(List<Long> pair, List<Port> path) {
        paths.put(pair, path);
    }

    public void printPaths() {
        for (List<Long> pair : paths.keySet()) {
            System.out.println("--------");
            System.out.println("Source: " + pair.get(0));
            System.out.println("Destination: " + pair.get(1));
            for (Port port : paths.get(pair)) {
                System.out.println(port  + " " + port.getDevice().getName());
            }
        }
    }

}
