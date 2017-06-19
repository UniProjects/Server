package org.popov.belezirev.server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

public class ClientGSONSerializator {
	private static final boolean APPEND = true;
	private Gson gson;

	public ClientGSONSerializator() {
		gson = new GsonBuilder().addSerializationExclusionStrategy(new ExclusionStrategy() {

			@Override
			public boolean shouldSkipField(FieldAttributes f) {
				return f.getAnnotation(Expose.class) == null;
			}

			@Override
			public boolean shouldSkipClass(Class<?> clazz) {
				return false;
			}
		}).setPrettyPrinting().create();
	}

	public void serializeFile(Path filepath, ClientConnectionThread client) throws IOException {
		String clientGSONData = gson.toJson(client);
		if (!Files.exists(filepath)) {
			Files.createFile(filepath);
		}
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath.toString(), APPEND))) {
			writer.write(clientGSONData);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
