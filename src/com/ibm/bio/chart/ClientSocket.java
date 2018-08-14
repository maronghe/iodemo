package com.ibm.bio.chart;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 客户端socket
 * @author Logan
 */
public class ClientSocket implements Runnable {
	//创建socket集合
	public static Map<String,Socket> socketMap = new HashMap<String,Socket>();
	//client name
	private String name;
	private DataInputStream dataInputStream = null;
	private DataOutputStream dataOutputStream = null;
	private Socket socket;

	public ClientSocket(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		init();// 初始化对象
		String line ;
		while (true){
			try {
				line = dataInputStream.readUTF();
				if("list".equals(line)){
					//show list
					listClients();
				}


			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void init() {
		if (dataInputStream == null){
			try {
				this.dataInputStream = new DataInputStream(socket.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(dataOutputStream == null){
			try {
				this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		saveClient();
	}

	private void saveClient() {
		socketMap.put(getClientName(),socket);
		listClients();
	}

	private void listClients() {
		Set<String> socketSet = socketMap.keySet();
		if(socketSet != null){
			try {
				dataOutputStream.writeUTF(socketSet.toString());// 输出keys
				dataOutputStream.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public String getClientName() {
		try {
			name = dataInputStream.readUTF();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return name;
	}
}
