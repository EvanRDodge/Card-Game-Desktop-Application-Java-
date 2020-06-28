package controller;
//interface of functions that should be implemented for multiplayer game controller type classes
public interface SocketedController {
	
	public static final int PORT = 8644;
	
	public abstract void packetReceived(Object object, SocketConnection connection);
		
	public abstract void close();
}
