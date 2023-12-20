package org.snlab.networkLoader;

import java.io.FileReader;
import java.io.IOException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class NWJsonReader {
	public static JsonObject readJson(String fileName) throws IOException {
		JsonReader reader = Json.createReader(new FileReader(fileName));
		JsonObject jsonConfig = reader.readObject();
		reader.close();
		return jsonConfig;
	}
}
