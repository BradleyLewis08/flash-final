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
    static public String[] devicenames = { "r0", "r1", "r2", "r3" };

    public static Network getNetwork() {
        Network n = getTopo();

        for (String name : devicenames) {
            Device device = n.getDevice(name);
            try {
                // List all current directories in current directory
                Scanner in = new Scanner(new File( name + "ap"));
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

    public static Network getTopo() {
        Network n = new Network("Internet2");

        for (String name : devicenames) {
            n.addDevice(name);
        }

        n.addLink("r0", "r0-eth0", "r1", "r1-eth0");
        n.addLink("r1", "r1-eth1", "r2", "r2-eth0");
        n.addLink("r2", "r2-eth1", "r3", "r3-eth0");

        n.getAllDevices().forEach(device -> device.uid = Device.cnt++);
        return n;
    }

    public static void main(String[] args) {
        System.out.println(getNetwork().getInitialRules().size());

        Network n = getNetwork();

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
    }
}