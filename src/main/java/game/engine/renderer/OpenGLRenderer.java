package game.engine.renderer;

import static org.lwjgl.opengl.GL11.*;

import game.engine.logging.Logger;

/**
 * Small OpenGL renderer implementation manages rendering state.
 * It depends only on a RenderSurface for size information and has no knowledge of WindowHandler.
 */
public class OpenGLRenderer implements Renderer {
    private final RenderSurface surface;

    public OpenGLRenderer(RenderSurface surface) {
        this.surface = surface;
        glClearColor(0.1f, 0.12f, 0.15f, 1.0f);
        Logger.info(Logger.RENDER, "OpenGLRenderer created.");
    }

    @Override
    public void beginFrame(RenderContext ctx) {
        Logger.trace(Logger.RENDER, "beginFrame");
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, ctx.width, ctx.height);

        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, ctx.width, 0, ctx.height, -1, 1);

        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();
    }

    @Override
    public void drawRect(float x, float y, int width, int height, float r, float g, float b) {
        Logger.trace(Logger.RENDER, () -> "drawRect at (" + x + ", " + y + ")");
        glColor3f(r, g, b);
        glBegin(GL_QUADS);
        glVertex2f(x, y);
        glVertex2f(x + width, y);
        glVertex2f(x + width, y + height);
        glVertex2f(x, y + height);
        glEnd();
        glColor3f(1f,1f,1f);
    }

    @Override
    public void drawBall(float x, float y, float radius, float r, float g, float b) {
        Logger.trace(Logger.RENDER, () -> "drawBall at (" + x + ", " + y + ")");
        // simple triangle fan approximation
        int segments = 24;
        glColor3f(r, g, b);
        glBegin(GL_TRIANGLE_FAN);
        glVertex2f(x, y);
        for (int i = 0; i <= segments; i++) {
            double theta = 2.0 * Math.PI * i / segments;
            float dx = (float)(Math.cos(theta) * radius);
            float dy = (float)(Math.sin(theta) * radius);
            glVertex2f(x + dx, y + dy);
        }
        glEnd();
        glColor3f(1f,1f,1f);
    }

    @Override
    public void endFrame() {
        Logger.trace(Logger.RENDER, "endFrame");
        glPopMatrix(); // MODELVIEW
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
    }

    @Override
    public void cleanup() {
        Logger.info(Logger.RENDER, "Cleaning up renderer resources.");
    }
}
