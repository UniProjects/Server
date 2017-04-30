package org.popov.belezirev.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.function.Supplier;

public class ClientConnectionThread extends Thread {
	private Socket clientSocket;
	private volatile boolean isServerRunning;
    private Supplier<List<PrintWriter>> clientsSupplier;

    public ClientConnectionThread(Socket socket, Supplier<List<PrintWriter>> clientsSupplier) {
		this.clientSocket = socket;
		isServerRunning = true;
        this.clientsSupplier = clientsSupplier;
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
            clientWriter.write(msg + "\n");
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

}
