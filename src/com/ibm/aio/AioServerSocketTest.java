package com.ibm.aio;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

/**
 * Aio 是针对 Nio 之后的进行了异步处理
 */
public class AioServerSocketTest {
	public static void main(String[] args) throws IOException {
		new Thread(new AioServerSocketHandler(9988)).start();
	}
}
class AioServerSocketHandler implements Runnable {
	private int port;
	private CountDownLatch latch = null;
	private AsynchronousServerSocketChannel serverSocketChannel = null;
	public AioServerSocketHandler(int port) throws IOException {
		this.port = port;
		serverSocketChannel = AsynchronousServerSocketChannel.open();
		serverSocketChannel.bind(new InetSocketAddress(this.port));
	}

	@Override
	public void run() {
		// 创建latch对象
		latch = new CountDownLatch(1);
		// 调用accept的回调函数
		serverSocketChannel.accept(this, new AioAcceptCompletionHandler());
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private class AioAcceptCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, AioServerSocketHandler> {

		@Override
		public void completed(AsynchronousSocketChannel result, AioServerSocketHandler attachment) {
			// 再次调用accept 实现异步接收其他的客户端的连接
			attachment.serverSocketChannel.accept(attachment,this);
			ByteBuffer request = ByteBuffer.allocate(1024);
			// 调用读的回调函数
			result.read(request,request,new AioReadCompletionHandler(result));
		}

		@Override
		public void failed(Throwable exc, AioServerSocketHandler attachment) {

		}
	}

	private class AioReadCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {

		private AsynchronousSocketChannel socketChannel;
		public AioReadCompletionHandler(AsynchronousSocketChannel result) {
			this.socketChannel = result;
		}

		@Override
		public void completed(Integer result, ByteBuffer attachment) {
			attachment.flip();
			byte[] request = new byte[attachment.remaining()];
			attachment.get(request);
			try {
				String requestBody = new String(request,"utf-8");
				System.out.println("request body : " + requestBody);
				write();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		private void write() {
			byte[] response = "Hello this is aio server".getBytes();
			ByteBuffer responseBuffer = ByteBuffer.allocate(response.length);
			responseBuffer.put(response);
			responseBuffer.flip();
			// 调用匿名回调函数
			socketChannel.write(responseBuffer, responseBuffer, new CompletionHandler<Integer, ByteBuffer>() {
				@Override
				public void completed(Integer result, ByteBuffer attachment) {
					if(attachment.hasRemaining()){// 如果还没写完
						socketChannel.write(attachment,attachment,this);
					}
				}

				@Override
				public void failed(Throwable exc, ByteBuffer attachment) {

				}
			});
		}

		@Override
		public void failed(Throwable exc, ByteBuffer attachment) {

		}
	}
}