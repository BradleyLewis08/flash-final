package org.snlab.networkLoader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.snlab.network.Device;
import org.snlab.network.Network;
import org.snlab.network.Rule;

public class JSONRequirementsParser {
	public static void parseAndAddRules(String requirementsFileName, Network n) {
		try {
			JsonObject requirements = NWJsonReader.readJson(
					"/Users/bradleylewis/Desktop/flash/src/main/java/org/snlab/networkLoader/test_requirements.json");

			JsonArray fibs = requirements.getJsonArray("fibsByDevice");
			for (JsonObject deviceToFile : fibs.getValuesAs(JsonObject.class)) {
				deviceToFile.forEach((device, value) -> {
					String fileName = value.toString().replace("\"", "");
					String deviceId = device.toString();
					addRules(fileName, deviceId, n);
				});
			}
		} catch (IOException e) {
			System.err.println("Error reading file: " + e.getMessage());
		}
	}

	private static void addRuleToNetwork(String deviceId, String ruleLine, Network n) {
		String[] tokens = ruleLine.split(" ");
		String pn = tokens[3].split("\\.")[0];
		Device device = n.getDevice(deviceId);
		if (device.getPort(pn) == null) {
			device.addPort(pn);
		}
		long ip = Long.parseLong(tokens[1]);
		Rule rule = new Rule(device, ip, Integer.parseInt(tokens[2]), device.getPort(pn));
		device.addInitialRule(rule);
		n.addInitialRule(rule);
	}

	private static void addRules(String filename, String deviceId, Network n) {
		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
			String line;
			Boolean entries = false; // True if we are in the entries section
			while ((line = reader.readLine()) != null) {
				String[] parts = line.trim().split("\\s+");
				if (parts[0].equals("Destination")) { // Next line is the rule
					entries = true;
					continue;
				}
				if (!entries) {
					continue;
				}
				String parsedRequirementLine = processLine(line, deviceId);
				if (parsedRequirementLine != null) {
					addRuleToNetwork(deviceId, parsedRequirementLine, n);
				}
			}
		} catch (IOException e) {
			System.err.println("Error reading file: " + e.getMessage());
		}
	}

	private static String processLine(String line, String deviceId) {
		String[] parts = line.trim().split("\\s+");
		if (parts.length < 7) {
			return null;
		}
		String destination = parts[0];
		String type = parts[1];
		String netIf = parts[6];

		if ("dest".equalsIgnoreCase(type)) {
			String ip = destination.split("/")[0];
			int subnet = Integer.parseInt(destination.split("/")[1]);
			int ipAsInt = ipToInt(ip);
			return "fw " + ipAsInt + " " + subnet + " " + netIf;
		}
		return null;
	}

	private static int ipToInt(String ipAddress) {
		try {
			InetAddress inetAddress = InetAddress.getByName(ipAddress);
			byte[] bytes = inetAddress.getAddress();
			return ByteBuffer.wrap(bytes).getInt();
		} catch (Exception e) {
			System.err.println("Invalid IP address: " + ipAddress);
			return 0;
		}
	}
}
