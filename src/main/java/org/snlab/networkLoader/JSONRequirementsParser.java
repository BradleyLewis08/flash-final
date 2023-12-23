package org.snlab.networkLoader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
			JsonObject requirements = NWJsonReader.readJson("test_requirements.json");

			JsonArray fibs = requirements.getJsonArray("fibsByDevice");
			System.out.println(fibs);
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
		Rule rule = new Rule(device, Long.parseLong(tokens[1]), Integer.parseInt(tokens[2]), device.getPort(pn));
		device.addInitialRule(rule);
		n.addInitialRule(rule);
	}

	private static long convertIpToLong(String ipAddress) {
		String[] ipAddressInArray = ipAddress.split("/")[0].split("\\.");

		long result = 0;
		for (int i = 0; i < ipAddressInArray.length; i++) {
			int power = 3 - i;
			int octet = Integer.parseInt(ipAddressInArray[i]);
			result += octet * Math.pow(256, power);
		}

		return result;
	}

	private static void addRules(String filename, String deviceId, Network n) {
		String outputFilename = deviceId + "ap";

		try (BufferedReader reader = new BufferedReader(new FileReader(filename));
				PrintWriter writer = new PrintWriter(new FileWriter(outputFilename, true))) {

			String line;
			boolean entries = false; // True if we are in the entries section
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
				System.out.println("DeviceID " + deviceId + ": " + parsedRequirementLine);
				if (parsedRequirementLine != null) {
					// addRuleToNetwork(deviceId, parsedRequirementLine, n);
					writer.println(parsedRequirementLine); // Write the parsed line to the file
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
			long ipAsLong = convertIpToLong(ip);
			return "fw " + ipAsLong + " " + subnet + " " + netIf;
		}
		return null;
	}
}
