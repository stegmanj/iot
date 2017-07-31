package de.farberg.spark.examples.streaming;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Supplier;

public class ServerSocketSource<T> {

	private MyRunnable<T> myRunnable;
	private Supplier<T> supplier;
	private Supplier<Integer> millisToSleep;

	public ServerSocketSource(Supplier<T> supplier) {
		this(supplier, null);
	}

	public ServerSocketSource(Supplier<T> supplier, Supplier<Integer> millisToSleep) {
		this.supplier = supplier;
		this.millisToSleep = millisToSleep;
		start();
	}

	private static class MyRunnable<T> implements Runnable {
		volatile boolean running = true;
		private Supplier<T> supplier;
		private int port = 0;
		private Supplier<Integer> millisToSleep;

		public MyRunnable(Supplier<T> supplier, Supplier<Integer> millisToSleep) {
			this.supplier = supplier;
			this.millisToSleep = millisToSleep;

		}

		@Override
		public void run() {

			try (ServerSocket serverSocket = new ServerSocket(0)) {
				this.port = serverSocket.getLocalPort();

				while (running) {
					Socket clientSocket = serverSocket.accept();
					PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

					while (running) {
						T x = supplier.get();
						if (x == null) {
							out.flush();
							out.close();
							break;
						}
						out.println(x);
						out.flush();
						if (millisToSleep != null)
							Thread.sleep(millisToSleep.get());
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void start() {
		if (isRunning())
			stop();
		myRunnable = new MyRunnable<>(supplier, millisToSleep);
		new Thread(myRunnable).start();
	}

	public synchronized void stop() {
		if (isRunning()) {
			myRunnable.running = false;
			myRunnable = null;
		}
	}

	public synchronized boolean isRunning() {
		return myRunnable != null;
	}

	public int getLocalPort() {
		return myRunnable.port;
	}

}
