package com.moe365.moepi.net;

import java.util.List;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.moe365.moepi.geom.Polygon;
import com.moe365.moepi.geom.PreciseRectangle;
import com.moe365.moepi.net.impl.OverlayBroadcastChannel;
import com.moe365.moepi.net.impl.MjpegBroadcastChannel;
import com.moe365.moepi.net.impl.WsDataSource;

import au.edu.jcu.v4l4j.VideoFrame;

public class MPHttpServer {
	protected final Server server;
	protected final ServletContextHandler context;
	protected final WsDataSource source;
	protected final MjpegBroadcastChannel videoChannel;
	protected final OverlayBroadcastChannel overlayChannel;
	
	public MPHttpServer(int port, String staticDir, int width, int height) {
		this.server = new Server(port);
		this.context = new ServletContextHandler(ServletContextHandler.SESSIONS | ServletContextHandler.NO_SECURITY);
		context.setContextPath("/");
		server.setHandler(context);
		
		//Register servlets
		DefaultServlet staticServlet = new DefaultServlet();
		ServletHolder staticHolder = new ServletHolder("default", staticServlet);
		staticHolder.setInitParameter("resourceBase", staticDir);
		context.addServlet(staticHolder, "/*");
		
		//Set up WsDataSource
		this.source = new WsDataSource();
		context.addServlet(new ServletHolder(this.source), "/vdc.ws");
		
//		DataChannel random = new RandomlyBroadcastingChannel(this.source, 1000, "random");
//		this.source.registerChannel(random);
		this.videoChannel = new MjpegBroadcastChannel(this.source, 365, "Main MJPEG video stream", width, height);
		this.source.registerChannel(this.videoChannel);
		
		this.overlayChannel = new OverlayBroadcastChannel(this.source, 366, videoChannel.getId(), "Main video overlay");
		this.videoChannel.setMetadata("overlayChannelId", "" + this.overlayChannel.getId());
		this.source.registerChannel(this.overlayChannel);
	}
	
	
	public void start() throws Exception {
		server.start();
	}
	
	public void offerFrame(VideoFrame frame) {
		this.videoChannel.offerFrame(frame);
	}
	
	public void offerPolygons(List<Polygon> polygons) {
		
	}
	
	public void offerRectangles(List<PreciseRectangle> rectangles) {
		this.overlayChannel.broadcastRectangles(rectangles);
	}
	
	public void shutdown() throws Exception {
		this.server.stop();
	}
}
