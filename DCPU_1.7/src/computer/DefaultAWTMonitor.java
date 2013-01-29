package computer;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class DefaultAWTMonitor extends VirtualMonitor {
	private static final int SCALE = 3;
	
	public Canvas canvas = new Canvas();
	
	public DefaultAWTMonitor() {
		canvas.setPreferredSize(new Dimension(160 * SCALE, 128 * SCALE));
		canvas.setMinimumSize(new Dimension(160 * SCALE, 128 * SCALE));
		canvas.setMaximumSize(new Dimension(160 * SCALE, 128 * SCALE));
		canvas.setFocusable(true);

		Thread t = new Thread() {
      public void run() {
        try {
          BufferedImage img2 = new BufferedImage(160, 128, 2);
          BufferedImage img = new BufferedImage(128, 128, 2);
          int[] pixels = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
          setPixels(pixels);

          canvas.requestFocus();
          while (true) {
          	if (canvas.isDisplayable()) {
	            render();
	            Graphics2D g = (Graphics2D) img2.getGraphics();
	            g.setColor(new Color(pixels[0x3000]));
	            g.fillRect(0, 0, 160, 128);
	            g.drawImage(img, 16, 16, 128, 128, null);
	            g.dispose();
	
	            g = (Graphics2D) canvas.getGraphics();
	            g.drawImage(img2, 0, 0, 160 * SCALE, 128 * SCALE, null);
	            g.dispose();
          	}
            Thread.sleep(1L);
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
    t.start();
	}
}
