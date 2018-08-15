package com.ibm.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Nio server side
 * @author Logan
 */
public class NioServerTest {
	public static void main(String[] args) throws IOException {
		new Thread(new NioServerHandler(8888)).start();// 启动线程
	}
}

class NioServerHandler implements Runnable {

	Selector selector;
	ServerSocketChannel serverSocketChannel;

	public NioServerHandler(int port) throws IOException {
		selector = Selector.open(); // 开启多路复选器
		serverSocketChannel = ServerSocketChannel.open(); // 初始化服务器端通道
		serverSocketChannel.configureBlocking(false); // 配置服务器端为非阻塞式
		serverSocketChannel.socket().bind(new InetSocketAddress(port), port + 1);  // 配置监听端口
		// 配置服务器通道注册到多路复选器上, 设置当前状态为“可接受客户端”
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
	}

	@Override
	public void run() {
		SelectionKey key = null;
		while (true){
			try {
				// 设置等待某个通道准备的最长时间为1s
				selector.select(1 * 1000);
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
			// 获取多路选择器上的一选择的键集，这个键集是就绪状态的通道的集合，还有一个方法叫keys()，其返回的是注册到这个
			// 多路复选器上的键（包括就绪状态和费就绪状态）
			Set<SelectionKey> keys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = keys.iterator();
			while (iterator.hasNext()){
				key = iterator.next();
				iterator.remove();
				handleKey(key);
			}
		}
	}

	private void handleKey(SelectionKey key) {
		// 键是valid
		if(key.isValid()){
			// 键是Acceptable的，证明是服务器端的key
			if(key.isAcceptable()){
				// 转换成服务器
				ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
				try {
					// 就绪的通道
					SocketChannel socketChannel = serverSocketChannel.accept();
					socketChannel.configureBlocking(false);// 设置客户端通道为非阻塞
					socketChannel.register(selector,SelectionKey.OP_READ); // 注册客户端到多路复选器上，监听Read操作
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if(key.isReadable()){
				// 读取值
				SocketChannel socketChannel = (SocketChannel) key.channel();
				ByteBuffer requestBuffer = ByteBuffer.allocate(1024); // 初始化byteBuffer
				try {
					int requestLength = socketChannel.read(requestBuffer); // 读取到缓冲字节
					if(requestLength > 0){
						requestBuffer.flip();//
						byte[] request = new byte[requestBuffer.remaining()];
						// 字节缓冲区的内容复制到	字节数组中
						requestBuffer.get(request);
						String body = new String(request,"utf-8");
						System.out.println("request : " + body);
						if("Hello Server".equalsIgnoreCase(body)){
							byte[] response = "Hi, this is sever".getBytes();
							ByteBuffer responseBuffer = ByteBuffer.allocate(response.length);
							responseBuffer.put(response);
							responseBuffer.flip();// 把position放到头的位置
							socketChannel.write(responseBuffer);
						}
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}