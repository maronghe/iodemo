package com.ibm.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * Nio (No-block IO)的核心思想就是注册一个状态，然后就不用像BIO一样，去等待结束，
 * 单独拿出一个线程去轮训访问就绪的键集，不断的去处理键。什么时候有键，就什么时候处理，而不是等待，这就是非阻塞！
 * BIO 是面向流的，而NIO是面向缓冲区的。
 * @author Logan
 *
 */
public class NioClient {
	public static void main(String[] args) {
		String host = "127.0.0.1";
		int port = 8888;
		new Thread(new NioClientHandler(host, port)).start();
	}
}

class NioClientHandler implements Runnable {
	private String host;
	private int port;
	private Selector selector;
	private SocketChannel socketChannel;
	public NioClientHandler(String host, int port) {
		this.host = host;
		this.port = port;
	}

	@Override
	public void run() {
		init();
		connect();// 连接并写出
		while (true){
			try {
				selector.select(1 * 1000); // 配置等待时间
				// 获取就绪通道的键集
				Set<SelectionKey> keys = selector.selectedKeys();
				SelectionKey selectionKey = null;
				Iterator<SelectionKey> iterator = keys.iterator();
				while (iterator.hasNext()){
					selectionKey = iterator.next();
					iterator.remove();
					handleKey(selectionKey);
				}

			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
	}

	private void handleKey(SelectionKey selectionKey) {
		if (selectionKey.isValid()){
			if(selectionKey.isReadable()){
				ByteBuffer responseBuffer = ByteBuffer.allocate(1024); // 1024个字节
				try {
					int responseLength = socketChannel.read(responseBuffer); // 读取内容到responseBuffer中
					// 判断是否读到值
					if(responseLength > 0){
						responseBuffer.flip();// 移动position到头部
						byte[] bytes = new byte[responseBuffer.remaining()];
						//transfers bytes from this buffer into the given destination array.
						responseBuffer.get(bytes);
						String response = new String(bytes,"utf-8");
						System.out.println("Server response is :" + response);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if(selectionKey.isConnectable()){
				try {
					if (socketChannel.finishConnect()){
						socketChannel.register(selector, SelectionKey.OP_READ); // 变为读的
						write(socketChannel);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}


		}

	}

	private void connect() {

		try {
			// 判断连接是否成功
			if (socketChannel.connect(new InetSocketAddress(host, port))){
				socketChannel.register(selector, SelectionKey.OP_READ);// 注册读操作
				write(socketChannel);
			} else {
				socketChannel.register(selector, SelectionKey.OP_CONNECT);// 注册可连接状态
			}


		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void write(SocketChannel socketChannel) {
		byte[] request = "Hello server".getBytes();
		ByteBuffer requestBuffer = ByteBuffer.allocate(request.length);
		requestBuffer.put(request);
		requestBuffer.flip();
		try {
			socketChannel.write(requestBuffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void init() {
		try {
			// 初始化对象
			selector = Selector.open();
			socketChannel = SocketChannel.open();
			// 设置非阻塞方式
			socketChannel.configureBlocking(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
