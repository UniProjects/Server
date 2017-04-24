package org.popov.belezirev.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public class ClientConnectionThread extends Thread {
	private Socket clientSocket;
	private volatile boolean isServerRunning;
	private List<OutputStream> writers;

	public ClientConnectionThread(Socket socket, List<OutputStream> writers) {
		this.clientSocket = socket;
		this.writers = writers;
		isServerRunning = true;
	}

	@Override
	public void run() {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
			while (isServerRunning) {
				String msg = reader.readLine();
				if (msg != null) {
					broadcast(msg);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void broadcast(String msg) {
		writers.stream().forEach(writer -> {
			try {
				writer.write(msg.getBytes());
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		});
	}

	public void stopClientThread() {
		isServerRunning = false;
		if (clientSocket != null) {
			try {
				clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
