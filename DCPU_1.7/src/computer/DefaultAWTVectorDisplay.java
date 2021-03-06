package computer;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Canvas;
import java.awt.Dimension;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector3f;

public class DefaultAWTVectorDisplay extends VirtualVectorDisplay {
	// protected static final Color INTENSE_BLACK = new Color(1f, 1f, 1f, .2f);
	// protected static final Color INTENSE_RED = new Color(1f, 0f, 0f, 1f);
	// protected static final Color INTENSE_GREEN = new Color(0f, 1f, 0f, 1f);
	// protected static final Color INTENSE_BLUE = new Color(0f, 0f, 1f, 1f);
	// protected static final Color BLACK = new Color(1f, 1f, 1f, .01f);
	// protected static final Color RED = new Color(1f, 0f, 0f, .25f);
	// protected static final Color GREEN = new Color(0f, 1f, 0f, .25f);
	// protected static final Color BLUE = new Color(0f, 0f, 1f, .25f);

	public Canvas canvas = new Canvas();

	public DefaultAWTVectorDisplay() {
		canvas.setPreferredSize(new Dimension(256, 256));
		canvas.setMinimumSize(new Dimension(256, 256));
		canvas.setMaximumSize(new Dimension(256, 256));
		canvas.setFocusable(true);

		Thread t = new Thread() {
			public void run() {
				while (!canvas.isDisplayable()) {
					Thread.yield();
				}

				try {
					Display.setParent(canvas);
					Display.create();

				} catch (LWJGLException e) {
					e.printStackTrace();
					System.exit(0);
				}
				glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
				glClearDepth(1.0f);
				glEnable(GL_DEPTH_TEST);
				glDepthFunc(GL_LEQUAL);
				glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

				final float h = (float) canvas.getWidth() / (float) canvas.getHeight();
				glViewport(0, 0, canvas.getWidth(), canvas.getHeight());
				glMatrixMode(GL_PROJECTION);
				glLoadIdentity();
				GLU.gluPerspective(45.0f, h, 1, 2000);
				glMatrixMode(GL_MODELVIEW);
				glLoadIdentity();

				while (canvas.isDisplayable()) {
					glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
					glLoadIdentity();
					glRotatef(-90, 0, 1, 0);
					glTranslatef(-512, 0, 0);
					glEnable(GL_BLEND);
					glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
					glLineWidth(2);
					glBegin(GL11.GL_LINES);
					Vertex last = null;
					synchronized (vertices) {
						for (Vertex v : vertices) {
							if (last != null) {
								color(v);
								Vector3f v1 = new Vector3f(last.x - 128, last.y - 128, last.z - 128);
								double theta = Math.atan2(v1.y, v1.x) + angle * Math.PI / 180;
								double d = Math.sqrt(v1.x * v1.x + v1.y * v1.y);
								glVertex3f((float) (d * Math.sin(theta)), v1.z, -(float) (d * Math.cos(theta)));
								v1.set(v.x - 128, v.y - 128, v.z - 128);
								theta = Math.atan2(v1.y, v1.x) + angle * Math.PI / 180;
								d = Math.sqrt(v1.x * v1.x + v1.y * v1.y);
								glVertex3f((float) (d * Math.sin(theta)), v1.z, -(float) (d * Math.cos(theta)));
							}
							last = v;
						}
						glEnd();
						if (vertices.size() > 0) {
							Vertex v = vertices.get((int) (Math.random() * vertices.size()));
							Vector3f v1 = new Vector3f(v.x - 128, v.y - 128, v.z - 128);
							double theta = Math.atan2(v1.y, v1.x) + angle * Math.PI / 180;
							double d = Math.sqrt(v1.x * v1.x + v1.y * v1.y);
							v1.set((float) (d * Math.sin(theta)), v1.z, -(float) (d * Math.cos(theta)));
							color(v);
							glLineWidth(.1f);
							glBegin(GL_LINES);
							glVertex3f(0, -512, 0);
							glVertex3f(v1.x, v1.y, v1.z);
							glEnd();
							glPointSize(8);
							glBegin(GL_POINTS);
							glVertex3f(v1.x, v1.y, v1.z);
							glEnd();
						}
					}
					glDisable(GL_BLEND);
					Display.update();
					try {
						Thread.sleep(1L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				Display.destroy();
			}

			private void color(Vertex v) {
				switch (v.color) {
				case 0:
					if (v.intensity) {
						glColor4f(.2f, .2f, .2f, .5f);
					} else {
						glColor4f(.1f, .1f, .1f, .1f);
					}
					break;
				case 1:
					if (v.intensity) {
						glColor4f(1f, 0f, 0f, .8f);
					} else {
						glColor4f(1f, 0f, 0f, .4f);
					}
					break;
				case 2:
					if (v.intensity) {
						glColor4f(0f, 1f, 0f, .8f);
					} else {
						glColor4f(0f, 1f, 0f, .4f);
					}
					break;
				case 3:
					if (v.intensity) {
						glColor4f(0f, 0f, 1f, .8f);
					} else {
						glColor4f(0f, 0f, 1f, .4f);
					}
				}
			}
		};
		t.start();
	}
}
