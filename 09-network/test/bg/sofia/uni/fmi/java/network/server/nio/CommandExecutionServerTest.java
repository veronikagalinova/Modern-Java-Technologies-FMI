package bg.sofia.uni.fmi.java.network.server.nio;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class CommandExecutionServerTest {

	private static Thread serverStarterThread;
	private static CommandExecutionServer commandExServer;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		serverStarterThread = new Thread() {
			
			@Override
			public void run() { 
				try (CommandExecutionServer ces = new CommandExecutionServer(CommandExecutionServer.SERVER_PORT);) {
					commandExServer = ces;
					commandExServer.start();
				} catch (Exception e) {
					System.out.println("An error has occured");
					e.printStackTrace();
				}
			}
		}; 
		serverStarterThread.start();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		commandExServer.stop();
		// Wake up the server so that it can exit
		serverStarterThread.interrupt();
	}

	@Test
	public void test01EchoCommandWithCorrectData() {
		assertEquals("what", sendRecvMsg("echo:what"));
	}
	
	@Test
	public void test02EchoCommandWithIncorrectData() {
		assertEquals("Missing Argument", sendRecvMsg("echo"));
	}
	
	@Test
	public void test03UnknownCommand() {
		assertEquals("Unknown command", sendRecvMsg("test"));
	}
	
	@Test
	public void test04GetHostnameCommand() throws UnknownHostException {
		assertEquals(InetAddress.getLocalHost().getHostName(), sendRecvMsg("gethostname"));
	}
	
	@Test
	public void test05CommandWithAdditionalDelimiter() {
		assertEquals("Incorrect command syntax", sendRecvMsg("echo:what:what"));
	}

	private String sendRecvMsg(String msg) {
		String response = "fail";
		try (Socket socket = new Socket(InetAddress.getLocalHost(), CommandExecutionServer.SERVER_PORT);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter out = new PrintWriter(socket.getOutputStream())) {
			System.out.println("Client " + socket + " connected to server");

			out.println(msg);
			out.flush();

			// Read the response from the server
			response = in.readLine();
		} catch (IOException e) {
			System.out.println("An error has occured " + e.getMessage());
			e.printStackTrace();
		}

		return response;
	}

}
