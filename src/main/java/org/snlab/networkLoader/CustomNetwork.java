package org.snlab.networkLoader;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.HashSet;

import org.snlab.network.Network;
import org.snlab.flash.ModelManager.ConflictFreeChanges;
import org.snlab.flash.ModelManager.InverseModel;
import org.snlab.flash.ModelManager.Ports.PersistentPorts;
import org.snlab.network.Device;
import org.snlab.network.Rule;

public class CustomNetwork {
	private static final String REQUIREMENTS_FILE = "/Users/bradleylewis/Desktop/flash/src/main/java/org/snlab/networkLoader/test_requirements.json;"; // TODO
	private static final String TOPOLOGY_FILE = "/Users/alexshi/Documents/Yale/F23/CPSC434/flash/src/main/java/org/snlab/networkLoader/testnet.config";

	public Network network;

	public CustomNetwork(String topologyConfigPath, String requirementsConfigPath) {
		try {
			Network n = TopologyParser.createNetwork(TOPOLOGY_FILE, "mininet");
			// RequirementParser.parseAndAddRules(REQUIREMENTS_FILE, "custom", n);
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
