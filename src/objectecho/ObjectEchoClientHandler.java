/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package objectecho;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelState;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

/**
 * Handler implementation for the object echo client. It initiates the ping-pong
 * traffic between the object echo client and server by sending the first
 * message to the server.
 */
public class ObjectEchoClientHandler extends SimpleChannelUpstreamHandler {

	private static final Logger logger = Logger.getLogger(
			ObjectEchoClientHandler.class.getName());

	private final MyObject firstMessage;
	private final AtomicLong transferredMessages = new AtomicLong();

	/**
	 * Creates a client-side handler.
	 */
	public ObjectEchoClientHandler(int id) {
		this.firstMessage = new MyObject(id, 0, "an object", 9999);
	}

	public long getTransferredMessages() {
		return this.transferredMessages.get();
	}

	@Override
	public void handleUpstream(
			ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		if (e instanceof ChannelStateEvent &&
				((ChannelStateEvent) e).getState() != ChannelState.INTEREST_OPS) {
			ObjectEchoClientHandler.logger.info(e.toString());
		}
		super.handleUpstream(ctx, e);
	}

	@Override
	public void channelConnected(
			ChannelHandlerContext ctx, ChannelStateEvent e) {
		// Send the first message if this handler is a client-side handler.
		e.getChannel().write(this.firstMessage);
	}

	@Override
	public void messageReceived(
			ChannelHandlerContext ctx, MessageEvent e) {
		// Echo back the received object to the server.
		long iter = this.transferredMessages.incrementAndGet();
		MyObject myobj = (MyObject) e.getMessage();
		ObjectEchoClientHandler.logger.log(
				Level.INFO, "ID: " + this.firstMessage.getI() + " currently on iteration " + iter
						+ " counter at: " + myobj.getJ());
		myobj.setJ(myobj.getJ() + 1);

		if (iter > 100) {
			e.getChannel().close();
			return;
		}

		e.getChannel().write(myobj);
	}

	@Override
	public void exceptionCaught(
			ChannelHandlerContext ctx, ExceptionEvent e) {
		ObjectEchoClientHandler.logger.log(
				Level.WARNING,
				"Unexpected exception from downstream.",
				e.getCause());
		e.getChannel().close();
	}
}
