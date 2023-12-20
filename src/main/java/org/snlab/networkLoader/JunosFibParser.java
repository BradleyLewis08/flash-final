package org.snlab.networkLoader;

import java.io.BufferedReader;
import java.io.FileReader;

import org.snlab.network.Network;

public class JunosFibParser {
	public static void parseAndAddRules(String fibFileName, Network n) {
		try (BufferedReader reader = new BufferedReader(new FileReader(fibFileName))) {
			String line;
			int lineIndex = 0;
		} catch (Exception e) {
			System.out.println("Error reading requirements file");
		}
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
}
