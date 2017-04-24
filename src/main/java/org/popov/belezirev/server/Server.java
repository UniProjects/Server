package org.popov.belezirev.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Server implements AutoCloseable {
	public static final int SERVER_PORT = 10513;
	private ServerSocket serverSocket;
	private List<ClientConnectionThread> clients;
	private List<OutputStream> writers;

	public Server(int serverPort) {
		clients = new LinkedList<ClientConnectionThread>();
		writers = new ArrayList<>();
		serverInit(serverPort);
	}

	private void serverInit(int serverPort) {
		System.out.println("Server initializing...");
		try {
			serverSocket = new ServerSocket(serverPort);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("The server is running on port " + serverPort);
	}

	public void start() throws Exception {
		while (true) {
			Socket clientSocket = serverSocket.accept();
			writers.add(clientSocket.getOutputStream());
			ClientConnectionThread clientConnectionThread = new ClientConnectionThread(clientSocket, writers);
			clients.add(clientConnectionThread);
			System.out.println("Client connected to the server!");
			// clientConnectionThread.setDaemon(true);
			clientConnectionThread.start();
		}
	}

	@Override
	public void close() throws Exception {
		if (serverSocket != null) {
			serverSocket.close();
		}
		for (ClientConnectionThread clientConnectionThread : clients) {
			clientConnectionThread.stopClientThread();
		}
		clients.clear();
	}

	public static void main(String[] args) {
		try (Server server = new Server(SERVER_PORT)) {
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
