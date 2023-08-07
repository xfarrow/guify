package controllers;

import java.net.InetAddress;
import java.net.UnknownHostException;

import code.Constants;
import code.SshEngine;
import code.GuiAbstractions.Implementations.JFrameFactory;
import code.GuiAbstractions.Interfaces.IFrameFactory;
import views.interfaces.ILoginFrame;

public class LoginController {
	
	private ILoginFrame frame;
	
	public LoginController() {
		try {
			frame = (ILoginFrame) JFrameFactory.createJFrame(IFrameFactory.LOGIN, this);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean Login(String host, String username, String password, String port) throws IllegalArgumentException {
		LoginCredentials.host = host;
		LoginCredentials.username = username;
		LoginCredentials.password = password;
		LoginCredentials.port = Integer.parseInt(port);
		
		if (SshEngine.connetion()) {
			frame.setVisible(false);
			new DesktopController().showFrame(true);;
			return true;
		}
		else {
			return false;
		}
	}
	
	public void ValidateInput(String host, String username, String password, String port) throws IllegalArgumentException {
		
		// Host Validation. Consider its necessity.
		try {
			InetAddress.getByName(host);
		} catch (UnknownHostException ex) {
			throw new IllegalArgumentException("Host could not be found", ex);
		}
		
		// Port Validation
		try {
			Integer.parseInt(port);
		}
		catch(NumberFormatException ex) {
			throw new IllegalArgumentException("Invalid port number", ex);
		}	
	}
	
	public void showFrame(boolean show) {
		frame.setVisible(show);
	}
	
	public String getTitle() {
		return Constants.APP_NAME + " " + Constants.VERSION;
	}
	
	public static class LoginCredentials{
		public static String host;
		public static String username;
		public static String password;
		public static int port;
	}
	
}
