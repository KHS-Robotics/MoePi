package com.moe365.moepi.processing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

public class DebuggingDiffGenerator extends DiffGenerator {
	
	protected final AtomicInteger i = new AtomicInteger(0);
	public BufferedImage imgFlt;
	private String saveLoc;

	public DebuggingDiffGenerator(int frameMinX, int frameMinY, int frameMaxX, int frameMaxY, int tolerance, String saveLoc) {
		super(frameMinX, frameMinY, frameMaxX, frameMaxY, tolerance);
		this.saveLoc = saveLoc;
	}
	
	@Override
	public RichBinaryImage apply(BufferedImage onImg, BufferedImage offImg) {
		// boolean array of the results. A cell @ result[y][x] is only
		int height = this.frameMaxY - this.frameMinY;
		int width = this.frameMaxX - this.frameMinX;
		boolean[][] result = new boolean[height][width];
		BufferedImage imgR = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		BufferedImage imgG = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		BufferedImage imgB = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		imgFlt = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		int[] pxOn = new int[3], pxOff = new int[3];
		for (int y = frameMinY; y < frameMaxY; y++) {
			//Y index into result array
			final int idxY = y - frameMinY;
			for (int x = frameMinX; x < frameMaxX; x++) {
				//X index into result array
				final int idxX = x - frameMinX;
				
				AbstractImageProcessor.splitRGB(onImg.getRGB(x, y), pxOn);
				AbstractImageProcessor.splitRGB(offImg.getRGB(x, y), pxOff);
				int dR = pxOn[0] - pxOff[0];
				int dG =  pxOn[1] - pxOff[1];
				int dB =  pxOn[2] - pxOff[2];
				if (dG > tolerance && (dR < dG - 10 || dR < tolerance)) {//TODO fix
					result[idxY][idxX] = true;
					imgFlt.setRGB(x, y, 0xFFFFFF);
				}
				imgR.setRGB(x, y, AbstractImageProcessor.saturateByte(dR) << 16);
				imgG.setRGB(x, y, AbstractImageProcessor.saturateByte(dG) << 8);
				imgB.setRGB(x, y, AbstractImageProcessor.saturateByte(dB));
			}
		}
		try {
			int num = i.getAndIncrement();
			File imgDir = new File(saveLoc + "/img-" + num);

			if (!(imgDir.exists() && imgDir.isDirectory()))
				imgDir.mkdirs();
			
			File file = new File(imgDir, "delta" + num + ".png");
			System.out.println("Saving image to " + file);
			ImageIO.write(imgR, "PNG", new File(imgDir, "dr" + num + ".png"));
			ImageIO.write(imgG, "PNG", new File(imgDir, "dg" + num + ".png"));
			ImageIO.write(imgB, "PNG", new File(imgDir, "db" + num + ".png"));
			ImageIO.write(onImg, "PNG", new File(imgDir, "on" + num + ".png"));
			ImageIO.write(offImg, "PNG", new File(imgDir, "off" + num + ".png"));
			ImageIO.write(imgFlt, "PNG", file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return new RichBinaryImage(result, imgFlt);
	}
	
	public static class RichBinaryImage implements BinaryImage {
		boolean[][] data;
		BufferedImage diffImg;
		
		public RichBinaryImage(boolean[][] data, BufferedImage diffImg) {
			this.data = data;
			this.diffImg = diffImg;
		}
		
		@Override
		public boolean test(int x, int y) {
			return this.data[y][x];
		}
		
	}
}
