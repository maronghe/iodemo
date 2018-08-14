package com.ibm.bio.chart;

import java.io.IOException;
import java.net.ServerSocket;

public class ChartServerTest {
	public static void main(String[] args) {
		ChartServer chartServer = new ChartServer(9999);
		chartServer.start();
	}


}
