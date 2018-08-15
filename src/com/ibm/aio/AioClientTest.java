package com.ibm.aio;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

/**
 * Aio（Asynchronous IO）核信思想就是对Client和Server的操作进行回调函数的调用。
 * 达到异步的效果
 * @author Logan
 */
public class AioClientTest {
	public static void main(String[] args) throws IOException {
		int port = 9988;
		String host = "127.0.0.1";
		new Thread(new AioClientHandler(host, port)).start();
	}
}
class AioClientHandler implements Runnable {
	private String host;
	private int port;
	private CountDownLatch latch = null;
	private AsynchronousSocketChannel socketChannel = null ;
	public AioClientHandler(String host, int port) throws IOException {
		this.host = host;
		this.port = port;
		socketChannel = AsynchronousSocketChannel.open();
	}

	@Override
	public void run() {
		latch = new CountDownLatch(1);
		// 调用连接的回调函数
		socketChannel.connect(new InetSocketAddress(host, port),this,new AioConnectionClientHandler());
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private class AioConnectionClientHandler implements CompletionHandler<Void, AioClientHandler> {

		@Override
		public void completed(Void result, AioClientHandler attachment) {
			byte[] bytes = "Hello, this is Aio client".getBytes();
			ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
			byteBuffer.put(bytes);
			byteBuffer.flip();
			// 调用写的回调函数
			attachment.socketChannel.write(byteBuffer,byteBuffer,new AioWriteCompletionHandler());
		}

		@Override
		public void failed(Throwable exc, AioClientHandler attachment) {

		}

		private class AioWriteCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {
			@Override
			public void completed(Integer result, ByteBuffer attachment) {
				attachment.flip();
				byte[] bytes = new byte[attachment.remaining()];
				attachment.get(bytes);
				try {
					String responseBody = new String(bytes,"utf-8");
					System.out.println("Response : "+ responseBody);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void failed(Throwable exc, ByteBuffer attachment) {

			}
		}
	}
}
