package org.popov.belezirev.server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;

public class ClientGSONSerializator {
	private static final boolean APPEND = true;
	private Gson gson;

	public ClientGSONSerializator() {
		gson = new Gson();
	}

	public void serializeFile(String filepath, ClientConnectionThread client) {
		String clientGSONData = gson.toJson(client);
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath + "\\clientsData.json", APPEND))) {
			writer.write(clientGSONData);
			writer.flush();
		} catch (IOException ioException) {
			// TODO: handle exception
		}

	}
}
