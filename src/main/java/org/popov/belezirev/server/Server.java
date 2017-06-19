package org.popov.belezirev.server;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;

public class Server implements AutoCloseable {
	private static final Path USERS_FILE_LOCATION = Paths.get(System.getProperty("user.home"), "users.json");
	private static final String VALID_USERNAME_RESPONSE = "valid_username";
	private static final String GUI_CLIENT_TYPE = "gui";
	public static final int SERVER_PORT = 10513;
	private static final boolean SERVER_IS_RUNNING = true;
	private ServerSocket serverSocket;
	private List<ClientConnectionThread> clients;
	private List<PrintWriter> writers;
	private Supplier<List<PrintWriter>> clientsSupplier = () -> writers;
	private ClientGSONSerializator clientGSONSerializator;

	public Server(int serverPort) {
		clients = new LinkedList<>();
		writers = Collections.synchronizedList(new LinkedList<>());
		serverInit(serverPort);
		clientGSONSerializator = new ClientGSONSerializator();
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

		while (SERVER_IS_RUNNING) {
			Socket clientSocket = serverSocket.accept();
			PrintWriter clientWriter = new PrintWriter(clientSocket.getOutputStream());
			BufferedReader clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			String clientUserName = readInitialMessageFromClient(clientReader);
			if (isUsernameAvailable(clientUserName)) {
				writers.add(clientWriter);
				sendUsernameConfirmationResponse(clientWriter);
				String clientPassword = readInitialMessageFromClient(clientReader);
				String clientType = readInitialMessageFromClient(clientReader);
				ClientConnectionThread clientConnectionThread = new ClientConnectionThread(clientSocket, clientUserName,
						DigestUtils.md5Hex(clientPassword), clientsSupplier);
				clientGSONSerializator.serializeFile(USERS_FILE_LOCATION, clientConnectionThread);
				clients.add(clientConnectionThread);
				if (GUI_CLIENT_TYPE.equals(clientType)) {
					sendAllUserNames(clientWriter);
				}
				System.out.println("Client connected to the server!");
				clientConnectionThread.start();
			} else {
				sendUsernameDeclineResponse(clientWriter);
			}
		}
	}

	private void sendUsernameConfirmationResponse(PrintWriter clientWriter) {
		clientWriter.println(VALID_USERNAME_RESPONSE);
		clientWriter.flush();
	}

	private void sendUsernameDeclineResponse(PrintWriter clientWriter) {
		clientWriter.println("penis");
		clientWriter.flush();
	}

	private Boolean isUsernameAvailable(final String username) {
		for (ClientConnectionThread client : clients) {
			if (client.getClientUserName().equals(username)) {
				return false;
			}
		}
		return true;
	}

	private void sendAllUserNames(PrintWriter clientWriter) {
		clientWriter
				.println(clients.stream().map(client -> client.getClientUserName()).collect(Collectors.joining(",")));
		clientWriter.flush();
	}

	private String readInitialMessageFromClient(BufferedReader reader) throws IOException {
		return reader.readLine();
	}

	@Override
	public void close() {
		for (ClientConnectionThread clientConnectionThread : clients) {
			clientConnectionThread.stopClientThread();
		}
		for (PrintWriter writer : writers) {
			if (writer != null) {
				writer.close();
			}
		}
		if (serverSocket != null) {
			closeQuietly(serverSocket);
		}
		try {
			Files.delete(USERS_FILE_LOCATION);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
