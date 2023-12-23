package org.snlab.networkLoader;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.snlab.network.Network;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class TopologyParser {
	public static Network createTopologyFromJSON(String filename) throws IOException {
		ArrayList<String> deviceIds = new ArrayList<String>();
		// Create a JSON reader for the file
		JsonObject jsonConfig;
		try {
			jsonConfig = NWJsonReader.readJson(filename);
		} catch (IOException e) {
			// TODO: Handle exception
			throw e;
		}
		Network n = new Network();
		String networkName = jsonConfig.getString("name");
		n.setName(networkName);

		JsonArray devicesArray = jsonConfig.getJsonArray("devices");
		for (JsonObject deviceObj : devicesArray.getValuesAs(JsonObject.class)) {
			String deviceId = deviceObj.getString("deviceId");
			if (deviceIds.contains(deviceId)) {
				throw new IOException("Duplicate device ID: " + deviceId);
			}
			n.addDevice(deviceId); // Each should be unique
		}

		// Add links from the links field
		JsonArray linksArray = jsonConfig.getJsonArray("links");
		for (JsonObject linkObj : linksArray.getValuesAs(JsonObject.class)) {
			JsonObject sourceObj = linkObj.getJsonObject("source");
			JsonObject destinationObj = linkObj.getJsonObject("destination");

			String sourceDeviceId = sourceObj.getString("deviceId");
			String sourcePortId = sourceObj.getString("portId");
			String destinationDeviceId = destinationObj.getString("deviceId");
			String destinationPortId = destinationObj.getString("portId");
			n.addLink(sourceDeviceId, sourcePortId, destinationDeviceId, destinationPortId);
		}
		return n;
	}

	// Mininet includes both directions of the link, i.e h1:eth1-h2:eth2 and
	// h2:eth2-h1:eth1. We should only include one of these in the calls to addLink.
	public static Boolean linkAdded(String[] linkParts, ArrayList<String> addedLinks) {
		Arrays.sort(linkParts);
		return addedLinks.contains(String.join(":", linkParts));
	}

	public static Network createTopologyFromMininet(String mininetConfigFileName) {
		try (BufferedReader reader = new BufferedReader(new FileReader(mininetConfigFileName))) {
			String line;
			int lineIndex = 0;
			Network network = new Network();
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				System.out.println(line);
				// First line of config file contains all devices
				if (lineIndex == 0) {
					String[] devices = line.split("\\s+");
					for (String device : devices) {
						network.addDevice(device);
					}
				} else {
					String[] parts = line.split("\\s+"); // First part is device name, second part is links
					assert parts.length == 4;
					network.addLink(parts[0], parts[1], parts[2], parts[3]);
				}
				lineIndex++;
			}
			return network;
		} catch (IOException e) {
			e.printStackTrace();
			return null; // or handle the exception as per your requirement
		}
	}

	// device names can be generated

	public static Network createNetwork(String filename, String fileType) throws IOException {
		if (fileType.equals("custom")) {
			return createTopologyFromJSON(filename);
		} else if (fileType.equals("mininet")) { // Allow dumps from mininet
			return createTopologyFromMininet(filename);
		} else {
			throw new IOException("Invalid file type: " + fileType);
		}
	}
}
