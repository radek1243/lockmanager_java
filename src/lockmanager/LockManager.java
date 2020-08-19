package lockmanager;

import java.awt.EventQueue;


import window.MainWindow;

public class LockManager {

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				MainWindow window = new MainWindow();
				window.open();
			}
		});
	}

}
