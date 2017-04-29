package org.popov.belezirev.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Server implements AutoCloseable {
	private static final boolean SERVER_IS_RUNNING = true;
	public static final int SERVER_PORT = 10513;
	private ServerSocket serverSocket;
	private List<ClientConnectionThread> clients;
	private List<PrintWriter> writers;

	public Server(int serverPort) {
		clients = new LinkedList<>();
		writers = new LinkedList<>();
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
		new Thread(() -> {
			try (Scanner commandsInputter = new Scanner(System.in)) {
				// COMMANDS LOGIC GOES HERE
			}
		}).start();

		while (SERVER_IS_RUNNING) {
			Socket clientSocket = serverSocket.accept();
			writers.add(new PrintWriter(clientSocket.getOutputStream()));
			ClientConnectionThread clientConnectionThread = new ClientConnectionThread(clientSocket, writers);
			clients.add(clientConnectionThread);
			System.out.println("Client connected to the server!");
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
		for (PrintWriter writer : writers) {
			writer.close();
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
