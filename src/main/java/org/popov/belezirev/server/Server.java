package org.popov.belezirev.server;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Supplier;

import org.popov.belezirev.server.CLI.Command;
import org.popov.belezirev.server.CLI.CommandManager;

public class Server implements AutoCloseable {
	public static final int SERVER_PORT = 10513;
	private static final boolean SERVER_IS_RUNNING = true;
	private ServerSocket serverSocket;
	private List<ClientConnectionThread> clients;
	private List<PrintWriter> writers;
	private Supplier<List<PrintWriter>> clientsSupplier = () -> writers;
	private Command command;

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
			try (Scanner commandLineReader = new Scanner(System.in)) {
				String commandString = null;
				while (SERVER_IS_RUNNING) {
					commandString = commandLineReader.nextLine();
					try {
						command = CommandManager.createCommand(commandString, clients);
					} catch (Exception exception) {
						// TODO Auto-generated catch block
						exception.printStackTrace();
					}
					command.execute();
				}
			}
		}).start();

		while (SERVER_IS_RUNNING) {
			Socket clientSocket = serverSocket.accept();
			writers.add(new PrintWriter(clientSocket.getOutputStream()));
			String clientUserName = readClientUserName(clientSocket);
			ClientConnectionThread clientConnectionThread = new ClientConnectionThread(clientSocket, clientUserName,
					clientsSupplier);
			clients.add(clientConnectionThread);
			System.out.println("Client connected to the server!");
			clientConnectionThread.start();
		}
	}

	private String readClientUserName(Socket clientSocket) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		return reader.readLine();
	}

	@Override
	public void close() {
		if (serverSocket != null) {
			closeQuietly(serverSocket);
		}
		for (ClientConnectionThread clientConnectionThread : clients) {
			clientConnectionThread.stopClientThread();
		}
		for (PrintWriter writer : writers) {
			writer.close();
		}
		clients.clear();
	}

	private void closeQuietly(Closeable closable) {
		try {
			closable.close();
		} catch (IOException e) {
		}
	}

	public static void main(String[] args) {
		try (Server server = new Server(SERVER_PORT)) {
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
