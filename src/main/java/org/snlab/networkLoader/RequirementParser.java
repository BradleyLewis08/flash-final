package org.snlab.networkLoader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.snlab.network.Device;
import org.snlab.network.Network;
import org.snlab.network.Rule;

public class RequirementParser {
	public static void parseAndAddRules(String requirementsFileName, String requirementsFileType, Network n) {
		if (requirementsFileType.equals("junos")) {
			JunosFibParser.parseAndAddRules(requirementsFileName, n);
		}
		if (requirementsFileType.equals("custom")) {
			JSONRequirementsParser.parseAndAddRules(requirementsFileName, n);
		} else if (requirementsFileType.equals("mininet")) { // TODO implement
			MininetRequirementsParser.parseAndAddRules(requirementsFileName, n);
		} else {
			System.err.println("Invalid requirements file type: " + requirementsFileType);
		}
	}
}
