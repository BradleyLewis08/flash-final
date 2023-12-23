package org.snlab.networkLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.snlab.flash.OutputManager;
import org.snlab.flash.ModelManager.ConflictFreeChanges;
import org.snlab.flash.ModelManager.InverseModel;
import org.snlab.flash.ModelManager.Ports.PersistentPorts;
import org.snlab.network.Device;
import org.snlab.network.Network;
import org.snlab.network.Pairs;
import org.snlab.network.Rule;

public class GenericLoader {

    public static Network getNetwork() throws Exception {
        Network n = getTopo();
        RequirementParser.parseAndAddRules("test_requirements.json", "custom", n);
        String[] deviceNames = n.getDeviceNames();

        for (String name : deviceNames) {
            Device device = n.getDevice(name);
            try {
                if (name.equals("h1") || name.equals("h2")) {
                    continue;
                }
                // List all current directories in current directory
                Scanner in = new Scanner(new File(name + "ap"));
                while (in.hasNextLine()) {
                    String line = in.nextLine();
                    String[] tokens = line.split(" ");
                    String pn = tokens[3].split("\\.")[0];
                    if (device.getPort(pn) == null) {
                        device.addPort(pn);
                    }
                    long ip = Long.parseLong(tokens[1]);
                    Rule rule = new Rule(device, ip, Integer.parseInt(tokens[2]), device.getPort(pn));
                    device.addInitialRule(rule);
                    n.addInitialRule(rule);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return n;
    }

    public static Network getTopo() throws Exception {
        try {
            Network n = TopologyParser.createNetwork("topology.out", "mininet");
            n.getAllDevices().forEach(device -> device.uid = Device.cnt++);
            return n;
        } catch (Exception e) {
            throw new Exception("Error parsing topology file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {

            Network n = getNetwork();
            System.out.println(n);

            // Build Equivalence Classes
            Pairs pairs = new Pairs("pairinput.txt", n);
            InverseModel verifier = new InverseModel(n, new PersistentPorts());
            ConflictFreeChanges cgs = verifier.insertMiniBatch(n.getInitialRules());
            verifier.update(cgs);

            // Build paths for all relevant source destination pairs
            verifier.buildPaths(pairs);

            // Pass the paths into an output manager
            OutputManager om = new OutputManager(pairs);
            om.dumpCsv("output/path_test");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}