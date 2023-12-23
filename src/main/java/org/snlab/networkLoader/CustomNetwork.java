package org.snlab.networkLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.Scanner;

import org.snlab.network.Network;
import org.snlab.flash.ModelManager.ConflictFreeChanges;
import org.snlab.flash.ModelManager.InverseModel;
import org.snlab.flash.ModelManager.Ports.PersistentPorts;
import org.snlab.network.Device;
import org.snlab.network.Rule;

public class CustomNetwork {
	private static final String REQUIREMENTS_FILE = "/Users/bradleylewis/Desktop/flash/src/main/java/org/snlab/networkLoader/test_requirements.json"; // TODO
	private static final String TOPOLOGY_FILE = "/Users/bradleylewis/Documents/flash/src/main/java/org/snlab/networkLoader/topology.out";

	public Network network;

	public static void addRules(Network n) {
		String[] deviceNames = n.getDeviceNames();
		for (String name : deviceNames) {
			Device device = n.getDevice(name);
			try {
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
	}

	public CustomNetwork(String topologyConfigPath, String requirementsConfigPath) {
		try {
			Network n = TopologyParser.createNetwork(TOPOLOGY_FILE, "mininet");
			RequirementParser.parseAndAddRules(REQUIREMENTS_FILE, "custom", n);
			network = n;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Network getNetwork() {
		return network;
	}

	public static void main(String[] args) {
		Network custom = new CustomNetwork(TOPOLOGY_FILE, REQUIREMENTS_FILE).getNetwork();
		System.out.println(custom);
		// InverseModel verifier = new InverseModel(custom, new PersistentPorts());
	}
}
