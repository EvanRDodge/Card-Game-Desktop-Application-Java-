package controller;

import javax.swing.SwingUtilities;

public class AppGameHub {
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new MainFrame();
			}
		});
	}
	
}
