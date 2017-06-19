package org.popov.belezirev.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.List;
import java.util.function.Supplier;

public class ClientConnectionThread extends Thread {
	private transient Socket clientSocket;
	private transient volatile boolean isServerRunning;
	private transient Supplier<List<PrintWriter>> clientsSupplier;
	private String clientUserName;
	private String clientPassword;

	public ClientConnectionThread(Socket socket, String clientUserName, String clientPassword,
			Supplier<List<PrintWriter>> clientsSupplier) {
		this.clientSocket = socket;
		this.clientPassword = clientPassword;
		isServerRunning = true;
		this.clientsSupplier = clientsSupplier;
		this.clientUserName = clientUserName;
		this.clientPassword = clientPassword;
	}

	@Override
	public void run() {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				PrintWriter writer = new PrintWriter(clientSocket.getOutputStream())) {
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
		for (PrintWriter clientWriter : clientsSupplier.get()) {
			clientWriter.write(MessageFormat.format("{0} says: {1}\n", clientUserName, msg));
			clientWriter.flush();
		}

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

	public Socket getClientSocket() {
		return clientSocket;
	}

	public String getClientUserName() {
		return clientUserName;
	}

}
