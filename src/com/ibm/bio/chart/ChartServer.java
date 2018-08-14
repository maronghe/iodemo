package com.ibm.bio.chart;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChartServer {
	private int port;
	private ServerSocket serverSocket;

	public ChartServer(int port) {
		this.port = port;
	}

	private void init() {
		if(serverSocket == null){
			try {
				// 创建serverSocket对象
				this.serverSocket = new ServerSocket(port);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void start(){
		init();
		while (true){
			try {
				Socket socket = serverSocket.accept();
				new Thread(new ClientSocket(socket)).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
