package com.moe365.moepi.net;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.moe365.moepi.net.impl.RandomlyBroadcastingChannel;
import com.moe365.moepi.net.impl.WsDataSource;

public class ServerController {
	int port;
	Server server;

	public ServerController(int port) throws Exception {
		server = new Server(port);
		addWsHandler();
		server.start();
		server.join();
	}

	protected void addWsHandler() {
		WsDataSource source = new WsDataSource();
		final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS|ServletContextHandler.NO_SECURITY);
		RandomlyBroadcastingChannel ch = new RandomlyBroadcastingChannel(source, 100, "Test channel");
		source.registerChannel(ch);
		context.setContextPath("/");
		server.setHandler(context);
		context.addServlet(new ServletHolder(source), "/");
	}

	public static void main(String... fred) throws Exception {
		new ServerController(8080);
	}
}
