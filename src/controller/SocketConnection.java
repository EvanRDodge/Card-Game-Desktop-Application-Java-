package controller;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
//this class handles a socket and provides an interface for sending packets from and forwarding packets to an object that implements the socketed controller interface
public class SocketConnection implements Runnable{
	
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;
	private boolean running;
	private SocketedController controller;
	private Socket socket;
	
	public SocketConnection(SocketedController controller, Socket socket) {
		this.socket = socket;
		this.controller = controller;
		try {
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			inputStream = new ObjectInputStream(socket.getInputStream());
			new Thread (this).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//reads objects from the input stream and passes them to the controller
	@Override
	public void run() {
		running = true;
		while(running) {
			try {
				Object object = inputStream.readObject();
				controller.packetReceived(object, this);
			} 
			catch (EOFException | SocketException e) {
				running = false;
			}
			catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
		}
	}
	//sends an object through the output stream
	public void sendPacket(Object object) {
		try {
			outputStream.reset();
			outputStream.writeObject(object);
			outputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//closes the socket
	public void close() throws IOException {
		running = false;
		socket.close();
	}

}
