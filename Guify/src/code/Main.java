// In loving memory of L.B. and E.B.

package code;

import java.awt.EventQueue;

import controllers.LoginController;
public class Main {
	
	/**
	 * Guify's entry point
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new LoginController().showFrame(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
