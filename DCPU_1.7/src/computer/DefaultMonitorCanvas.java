package computer;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class DefaultMonitorCanvas extends Canvas{
	private static final long serialVersionUID = 1L;
	private static final int SCALE = 3;
	final VirtualMonitor display = new VirtualMonitor();
  final VirtualKeyboard keyboard = new VirtualKeyboard(new AWTKeyMapping());

  
	public DefaultMonitorCanvas() {
    setPreferredSize(new Dimension(160 * SCALE, 128 * SCALE));
    setMinimumSize(new Dimension(160 * SCALE, 128 * SCALE));
    setMaximumSize(new Dimension(160 * SCALE, 128 * SCALE));
    setFocusable(true);
    addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent ke) {
        keyboard.keyPressed(ke.getKeyCode());
      }

      public void keyReleased(KeyEvent ke) {
        keyboard.keyReleased(ke.getKeyCode());
      }

      public void keyTyped(KeyEvent ke) {
        keyboard.keyTyped(ke.getKeyChar());
      }
    });

		Thread t = new Thread() {
      public void run() {
        try {
          BufferedImage img2 = new BufferedImage(160, 128, 2);
          BufferedImage img = new BufferedImage(128, 128, 2);
          int[] pixels = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
          display.setPixels(pixels);

          requestFocus();
          while (true) {
          	if (DefaultMonitorCanvas.this.isDisplayable()) {
	            display.render();
	            Graphics2D g = (Graphics2D) img2.getGraphics();
	            g.setColor(new Color(pixels[0x3000]));
	            g.fillRect(0, 0, 160, 128);
	            g.drawImage(img, 16, 16, 128, 128, null);
	            g.dispose();
	
	            g = (Graphics2D) getGraphics();
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
