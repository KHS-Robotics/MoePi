package com.moe365.moepi;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.imageio.ImageIO;

import com.moe365.moepi.geom.Polygon;
import com.moe365.moepi.geom.PreciseRectangle;
import com.moe365.moepi.geom.Polygon.PointNode;
import com.moe365.moepi.geom.TargetType;
import com.moe365.moepi.net.MPHttpServer;
import com.moe365.moepi.processing.AbstractImageProcessor;
import com.moe365.moepi.processing.ContourTracer;
import com.moe365.moepi.processing.ImageProcessor;
import com.moe365.moepi.processing.DebuggingDiffGenerator;
import com.moe365.moepi.client.RioClient;
import com.moe365.moepi.client.StaticRioClient;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import au.edu.jcu.v4l4j.CaptureCallback;
import au.edu.jcu.v4l4j.Control;
import au.edu.jcu.v4l4j.ControlList;
import au.edu.jcu.v4l4j.ImageFormat;
import au.edu.jcu.v4l4j.ImagePalette;
import au.edu.jcu.v4l4j.JPEGFrameGrabber;
import au.edu.jcu.v4l4j.V4L4JConstants;
import au.edu.jcu.v4l4j.VideoDevice;
import au.edu.jcu.v4l4j.VideoFrame;
import au.edu.jcu.v4l4j.exceptions.ControlException;
import au.edu.jcu.v4l4j.exceptions.StateException;
import au.edu.jcu.v4l4j.exceptions.UnsupportedMethod;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;

/**
 * Main Class
 */
public class Main {
	private static final String VERSION = "1.4.0";

	// TARGET
	private static final int DEFAULT_TARGET_WIDTH = 14;
	private static final int DEFAULT_TARGET_HEIGHT = 21;

	// GPIO
	private static final Pin DEFAULT_PIN = RaspiPin.GPIO_00;
	private static final String DEFAULT_PIN_NAME = "Vision-LED-Pin";
	private static final PinState DEFAULT_PIN_STATE = PinState.LOW;

	// CAMERA
	private static final int DEFAULT_WIDTH = 640;
	private static final int DEFAULT_HEIGHT = 480;
  
	static {
		// Print java.library.path
		//System.out.println("java.library.path set to " + System.getProperty("java.library.path"));

		// Loads v4l4j. This MUST be included
		//System.out.println("Loading natives for v4l4j...");
		System.loadLibrary("v4l4j");
	}
	
	/**
   	* Main Method
   	*/
  	public static void main(String[] args) {
		try {
			final CommandLineParser parser = CommandLineParser.build();
      		final ParsedCommandLineArguments parsed = parser.apply(args);
      
      		if (parsed.isFlagSet("--help")) {
				System.out.println(parser.getHelpString());
				System.exit(0);
			}

			if(parsed.isFlagSet("--version")) {
				System.out.println("MoePi " + VERSION);
				System.exit(0);
			}

			final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
				final UncaughtExceptionHandler handler = (t, e) -> {
					System.err.println("Thread " + t + " had a problem!");
					e.printStackTrace();
				};

				@Override
				public Thread newThread(Runnable r) {
					Thread t = new Thread(r);
					t.setName("MoePi-" + t.getId());
					// Print all exceptions to stderr
					t.setUncaughtExceptionHandler(handler);
					return t;
				}
			});

			final MPHttpServer server = initServer(parsed, executor);
			final VideoDevice camera = initCamera(parsed);
			final GpioPinDigitalOutput gpioPin = initGpio(parsed);
			final RioClient rioClient = initRoboRioClient(parsed, executor);
			final AbstractImageProcessor<?> processor = initImageProcessor(parsed, rioClient, server);

      		// Run test, if required
      		if (parsed.isFlagSet("--test")) {
				final String target = parsed.get("--test");
				switch (target) {
					case "controls":
						testControls(camera);
					break;
			
					case "client":
						testClient(rioClient);
					break;
					
					case "processing":
						testProcessing(processor, parsed);
					break;

					case "sse":
						testSSE(server);
					break;
				
					default:
						System.err.println("Unknown test '" + target + "'");
				}

				if(camera != null) {
					camera.release();
				}

				System.exit(0);
			}

			if(camera != null) {
				System.out.println("Initializing frame capture callback...");

				// Get native capture format (format that the camera actually provides)
				// Whatever format is captured will be software converted to JPEG
				final ImageFormat imf = camera.getDeviceInfo().getFormatList().getNativeFormatOfType(ImagePalette.MJPEG);
				System.out.println("Capturing with format " + imf);

				final int jpegQuality = parsed.getOrDefault("--jpeg-quality", 100);
				final int width = parsed.getOrDefault("--width", DEFAULT_WIDTH);
				final int height = parsed.getOrDefault("--height", DEFAULT_HEIGHT);
				final JPEGFrameGrabber frameGrabber = camera.getJPEGFrameGrabber(width, height, 0, V4L4JConstants.STANDARD_WEBCAM, jpegQuality, imf);

				final int fpsNum = parsed.getOrDefault("--fps-num", 1);
				final int fpsDenom = parsed.getOrDefault("--fps-denom", 10);
				frameGrabber.setFrameInterval(fpsNum, fpsDenom);
				System.out.println("Framerate: " + frameGrabber.getFrameInterval());

				final AtomicBoolean ledState = new AtomicBoolean(false);
				final AtomicLong ledUpdateTimestamp = new AtomicLong(0);
				final int gpioDelay = parsed.getOrDefault("--gpio-delay", -5000);

				frameGrabber.setCaptureCallback(new CaptureCallback() {
					@Override
					public void nextFrame(VideoFrame frame) {
						try {
							boolean gpioState;
							//Drop frames taken before the LED had time to flash
							if (gpioPin != null) {
								long frameTimestamp = frame.getCaptureTime();
								if (frameTimestamp < 0) {
									frameTimestamp = Integer.toUnsignedLong((int) frameTimestamp);
								}
								final long newTimestamp = (System.nanoTime() / 1000) + gpioDelay; // in microseconds

								if (ledUpdateTimestamp.accumulateAndGet(frameTimestamp, (threshold, _frameTimestmp)->(_frameTimestmp >= threshold ? newTimestamp : threshold)) != newTimestamp) {
									//Drop frame (it was old)
									frame.recycle();
									return;
								}
								
								// Toggle GPIO pin (change the LED's state)
								gpioState = !ledState.get();
								gpioPin.setState(gpioState);
								ledState.set(gpioState);
							} else {
								gpioState = false;
							}

							if (server != null && !gpioState) {
								server.offerFrame(frame); // Only offer frames that were taken while the light was on 
							}
							
							if(processor != null) {
								processor.offerFrame(frame, gpioState);
							}

						} catch (Exception ex) {
							// Make sure to print any/all exceptions
							ex.printStackTrace();

							try {
								if (server != null)
									server.shutdown();
							} catch (Exception ex1) {
								ex1.printStackTrace();
							}

							throw ex;
						}
					}
		
					@Override
					public void exceptionReceived(V4L4JException ex) {
						ex.printStackTrace();

						frameGrabber.stopCapture();
						processor.stop();
						camera.release();

						System.exit(1);
					}
				});
		
				System.out.println("Starting Frame Capture Callback...");
				frameGrabber.startCapture();
			}

			System.out.println("Done.");
		} catch(Exception ex) {
			System.err.println("********** FATAL ERROR! **********");
			ex.printStackTrace();

			System.exit(1);
		}
  	}

  	private static GpioPinDigitalOutput initGpio(final ParsedCommandLineArguments args) {
		if (args.isFlagSet("--no-gpio")) {
			System.out.println("GPIO DISABLED");
			return null;
		}
		
		// Get the GPIO object
		final GpioController gpio = GpioFactory.getInstance();
		GpioPinDigitalOutput pin;
		if (args.isFlagSet("--gpio-pin")) {
			// Get the GPIO pin by name
			String pinName = args.get("--gpio-pin");
			if(pinName == null || pinName.trim().isEmpty()) {
				System.out.println("The --gpio-pin flag was set but the --gpio-pin arg is either not set or invalid! Using default pin.");
				// Get the default pin
				pin = gpio.provisionDigitalOutputPin(DEFAULT_PIN, DEFAULT_PIN_NAME, DEFAULT_PIN_STATE);
			}

			if (!pinName.startsWith("GPIO ") && pinName.matches("\\d+")) {
				pinName = "GPIO " + pinName.trim();
			}
			
			pin = gpio.provisionDigitalOutputPin(RaspiPin.getPinByName(pinName), DEFAULT_PIN_NAME, DEFAULT_PIN_STATE);
		} else {
			// Get the default pin 
			pin = gpio.provisionDigitalOutputPin(DEFAULT_PIN, DEFAULT_PIN_NAME, DEFAULT_PIN_STATE);
		}

		System.out.println("Using Pin " + pin.getPin());
		pin.setMode(PinMode.DIGITAL_OUTPUT);
		pin.setState(false);//turn it off

		return pin;
	}

  	private static VideoDevice initCamera(final ParsedCommandLineArguments args) throws V4L4JException {
		if (args.isFlagSet("--no-camera")) {
			System.out.println("CAMERA DISABLED");
			return null;
		}

		final String devName = args.getOrDefault("--camera", "/dev/video0");
		System.out.print("Attempting to connect to camera @ " + devName + "...\t");

		VideoDevice device;
		try {
			device = new VideoDevice(devName);
		} catch (V4L4JException ex) {
			System.out.println("ERROR");
			throw ex;
		}

		Runtime.getRuntime().addShutdownHook(new Thread(()->{
			device.releaseControlList();
			device.releaseFrameGrabber();
			System.out.println("Closing " + devName);
			device.release(false);
		}));

		try {
			final boolean gpioDisabled = args.isFlagSet("--no-gpio");

			ControlList controls = device.getControlList();

			// TODO: this line throws an exception if set to anything other than 1
			// but not sure of the root cause
			controls.getControl("Exposure, Auto").setValue(args.getOrDefault("--exposure-auto", 1));
			 
			Control absExposureControl = controls.getControl("Exposure (Absolute)");
			final int absExposureValue = args.getOrDefault("--exposure-absolute", 19);
			absExposureControl.setValue(absExposureValue);

			// Contrast
			Control contrastControl = controls.getControl("Contrast");
			final int maxContrastValue = contrastControl.getMaxValue();
			final int contrastValue = args.getOrDefault("--contrast", maxContrastValue);
			contrastControl.setValue(contrastValue);

			// White Balance
			// Control whiteBalanceControl = controls.getControl(don't remember what goes here);
			// whiteBalanceControl.setValue(whiteBalanceControl.getMaxValue())

			// Saturation
			Control saturationControl = controls.getControl("Saturation");
			final int maxSaturationValue = saturationControl.getMaxValue();
			final int saturationValue = args.getOrDefault("--saturation", maxSaturationValue);
			saturationControl.setValue(saturationValue);

			// Sharpness
			Control sharpnessControl = controls.getControl("Sharpness");
			final int maxSharpnessValue = sharpnessControl.getMaxValue();
			final int sharpnessValue = args.getOrDefault("--sharpness", maxSharpnessValue);
			sharpnessControl.setValue(sharpnessValue);
			
			// Brightness
			Control brightnessControl = controls.getControl("Brightness");
			final int brightnessValue = args.getOrDefault("--brightness", gpioDisabled ? 180 : 42);
			brightnessControl.setValue(brightnessValue);
			
		} catch (ControlException | UnsupportedMethod | StateException ex) {
			ex.printStackTrace();
		} finally {
			device.releaseControlList();
		}
		
		System.out.println("Connected to Camera.");

		return device;
	}

  	private static RioClient initRoboRioClient(final ParsedCommandLineArguments args, ExecutorService executor) throws IOException {
		if (args.isFlagSet("--no-udp")) {
			System.out.println("CLIENT DISABLED (reason: cli)");
			return null;
		}
		
		final int port = args.getOrDefault("--udp-port", RioClient.RIO_PORT);
		//int retryTime = args.getOrDefault("--mdns-resolve-retry", RioClient.RESOLVE_RETRY_TIME);
		if (port <= 0) {
			System.out.println("CLIENT DISABLED (reason: negative port number)");
			return null;
		}

		if((port < 5800 || port > 5810) && !args.isFlagSet("--port-override")) {
			System.out.println("CLIENT DISABLED (reason: port number violates the rules, must be 5800-5810 or use --port-override)");
			return null;
		}

		final String address = args.getOrDefault("--udp-target", RioClient.RIO_ADDRESS);

		return new StaticRioClient(RioClient.SERVER_PORT, new InetSocketAddress(address, port));
	}

  	/**
	 * Create and initialize the server
	 * @param args the command line arguments
	 * @return server, if created, or null
	 * @throws IOException
	 */
	protected static MPHttpServer initServer(final ParsedCommandLineArguments args, ExecutorService executor) throws IOException {
		if(args.isFlagSet("--no-server")) {
			System.out.println("MOE.js DISABLED (reason: cli)");
			return null;
		}

		final int port = args.getOrDefault("--moejs-port", 5800);
		if(port <= 0) {
			System.out.println("MOE.js DISABLED (reason: negative port number)");
			return null;
		}

		System.out.println("Starting MOE.js on +:" + port);
		
		final int width = args.getOrDefault("--width", DEFAULT_WIDTH);
		final int height = args.getOrDefault("--height", DEFAULT_HEIGHT);

		final MPHttpServer server = new MPHttpServer(port, args.getOrDefault("--moejs-dir", "../moe.js/build"), width, height);
		try {
			server.start();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

		return server;
	}

  	private static AbstractImageProcessor<?> initImageProcessor(final ParsedCommandLineArguments args, final RioClient rioClient, final MPHttpServer server) {
		if (args.isFlagSet("--no-process")) {
			System.out.println("PROCESSOR DISABLED");
			return null;
		}

		System.out.println("Starting Image Processor...");

		final int width = args.getOrDefault("--width", DEFAULT_WIDTH);
		final int height = args.getOrDefault("--height", DEFAULT_HEIGHT);

		AbstractImageProcessor<?> processor;
		if(args.isFlagSet("--trace-contours")) {
			System.out.println("--trace-contours flag set: Using Contour Tracer");
			
			processor = new ContourTracer(width, height, polygons -> {
				for (Polygon polygon : polygons) {
					PointNode node = polygon.getStartingPoint();
					
					// Scale
					do {
						node = node.set(node.getX() / width, node.getY() / height);
					} while (!(node = node.next()).equals(polygon.getStartingPoint()));
				}

				if (server != null) {
					server.offerPolygons(polygons);
				}
			});
		} else {
			final boolean gpioDisabled = args.isFlagSet("--no-gpio");

			final boolean saveDiff = args.isFlagSet("--save-diff") || args.isFlagSet("--test");
			final String saveDir = saveDiff ? args.getOrDefault("--save-dir", "img") : null;

			final int targetWidth = args.getOrDefault("--target-width", DEFAULT_TARGET_WIDTH);
			final int targetHeight = args.getOrDefault("--target-height", DEFAULT_TARGET_HEIGHT);
			System.out.println("Target Dimensions: " + targetWidth + "x" + targetHeight);

			processor = new ImageProcessor(width, height, targetWidth, targetHeight, rectangles -> {
				try {
					if (rioClient != null) {
						if (rectangles.isEmpty()) {
							rioClient.writeNoneFound();
						} 
						else if (rectangles.size() == 1) {
							rioClient.writeOneFound(rectangles.get(0));
						} 
						else {
							// send the largest rectangles to the Rio
							rioClient.writeTwoFound(rectangles.get(0), rectangles.get(1));
						}
					}
				} catch (IOException | NullPointerException ex) {
					ex.printStackTrace();
				}

				// Offer the rectangles to be put in the SSE stream, if gpio is enabled
				if (server != null && !gpioDisabled) {
					server.offerRectangles(rectangles);
				}

			}, saveDiff, saveDir);
		}

		return processor.start();
  	}

  	protected static void testControls(VideoDevice device) throws ControlException, UnsupportedMethod, StateException {
		System.out.println("RUNNING TEST :: CONTROLS");
		ControlList controls = device.getControlList();
		for (Control control : controls.getList()) {
			switch (control.getType()) {
				case V4L4JConstants.CTRL_TYPE_STRING:
					System.out.print("String control: " + control.getName() + " - min: " + control.getMinValue() + " - max: "
							+ control.getMaxValue() + " - step: " + control.getStepValue() + " - value: ");
					try {
						System.out.println(control.getStringValue());
					} catch (V4L4JException ve) {
						System.out.println(" ERROR");
						ve.printStackTrace();
					}
					break;
				case V4L4JConstants.CTRL_TYPE_LONG:
					System.out.print("Long control: " + control.getName() + " - value: ");
					try {
						System.out.println(control.getLongValue());
					} catch (V4L4JException ve) {
						System.out.println(" ERROR");
					}
					break;
				case V4L4JConstants.CTRL_TYPE_DISCRETE:
					Map<String, Integer> valueMap = control.getDiscreteValuesMap();
					System.out.print("Menu control: " + control.getName() + " - value: ");
					try {
						int value = control.getValue();
						System.out.print(value);
						try {
							System.out.println(" (" + control.getDiscreteValueName(control.getDiscreteValues().indexOf(value)) + ")");
						} catch (Exception e) {
							System.out.println(" (unknown)");
						}
					} catch (V4L4JException ve) {
						System.out.println(" ERROR");
					}
					System.out.println("\tMenu entries:");
					for (String s : valueMap.keySet())
						System.out.println("\t\t" + valueMap.get(s) + " - " + s);
					break;
				default:
					System.out.print("Control: " + control.getName() + " - min: " + control.getMinValue() + " - max: " + control.getMaxValue()
							+ " - step: " + control.getStepValue() + " - value: ");
					try {
						System.out.println(control.getValue());
					} catch (V4L4JException ve) {
						System.out.println(" ERROR");
					}
			}
		}
		
		device.releaseControlList();
  	}
  
  	protected static void testClient(final RioClient client) throws IOException, InterruptedException {
		System.out.println("RUNNING TEST :: CLIENT");
		// just spews out UDP packets on a 3s loop
		while (true) {
			System.out.println("Writing r0");
			client.writeNoneFound();
			Thread.sleep(1000);
			System.out.println("Wrinting r1");
			client.writeOneFound(1.0, 2.0, 3.0, 4.0, TargetType.NONE);
			Thread.sleep(1000);
			System.out.println("Writing r2");
			client.writeTwoFound(1.0, 2.0, 3.0, 4.0, TargetType.NONE, 5.0, 6.0, 7.0, 8.2, TargetType.NONE);
			Thread.sleep(1000);
		}
  	}
  
  	protected static void testProcessing(AbstractImageProcessor<?> _processor, final ParsedCommandLineArguments args) throws IOException, InterruptedException {
		System.out.println("RUNNING TEST :: PROCESSING");

		File dir = new File(args.get("--test-images"));
		ImageProcessor processor = (ImageProcessor) _processor;
		final int width = args.getOrDefault("--width", DEFAULT_WIDTH);
		final int height = args.getOrDefault("--height", DEFAULT_HEIGHT);
		final String saveDir = args.getOrDefault("--save-dir", "img");

		int numImages = 0;
		List<Color> colors = Arrays.asList(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.PINK);
		while (true) {
			File onImgFile = new File(dir.getAbsolutePath(), "on" + numImages + ".png");
			File offImgFile = new File(dir.getAbsolutePath(), "off" + numImages + ".png");
			if (!(onImgFile.exists() && offImgFile.exists())) {
				break;
			}

			System.out.println("========== IMAGE " + numImages + " ===========");
			BufferedImage onImg = ImageIO.read(onImgFile);
			BufferedImage offImg = ImageIO.read(offImgFile);
			List<PreciseRectangle> rectangles = processor.apply(onImg, offImg);
			BufferedImage out = ((DebuggingDiffGenerator)processor.diff).imgFlt;
			System.out.println("Found rectangles " + rectangles);
			
			Graphics2D g = out.createGraphics();
			int i = 0; // color index
			for (PreciseRectangle rect : rectangles) {
				if(i == colors.size()) {
					System.out.println("WARNING: Ran out of colors since more than six targets were detected! Only drew the first six.");
					break;
				}

				// draw box with a new color
				g.setColor(colors.get(i));
				g.drawRect((int)(rect.getX() * width), (int) (rect.getY() * height), (int) (rect.getWidth() * width), (int) (rect.getHeight() * height));

				// increment color index
				i++;
			}
			g.dispose();

			File outFlt = new File(saveDir, "img-" + numImages + "/flt" + numImages + ".png");
			ImageIO.write(out, "PNG", outFlt);

			numImages++;
		}
	}

	protected static void testSSE(final MPHttpServer server) throws InterruptedException {
		System.out.println("RUNNING TEST :: SSE");
		while (!Thread.interrupted()) {
			List<PreciseRectangle> rects = new ArrayList<>(2);
			server.offerRectangles(rects);
			Thread.sleep(1000);
			rects.add(new PreciseRectangle(0.0,0.0,0.2,0.2));
			server.offerRectangles(rects);
			Thread.sleep(1000);
			rects.add(new PreciseRectangle(0.25,0.75,0.3,0.1));
			server.offerRectangles(rects);
			Thread.sleep(1000);
		}
	}
}
