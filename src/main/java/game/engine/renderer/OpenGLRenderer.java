package game.engine.renderer;

import static org.lwjgl.opengl.GL11.*;

import game.engine.logging.Logger;
import game.engine.render.Camera;
import org.joml.Vector2f;
import org.joml.Vector4f;

/**
 * Small OpenGL renderer implementation manages rendering state. It depends only
 * on a RenderSurface for size information and has no knowledge of
 * WindowHandler.
 */
public class OpenGLRenderer implements Renderer {
    public OpenGLRenderer() {
        glClearColor(0.1f, 0.12f, 0.15f, 1.0f);
        Logger.info(Logger.RENDER, "OpenGLRenderer created.");
    }

    @Override
    public void beginFrame(RenderContext context) {
        beginFrame(context, null);
    }

    @Override
    public void beginFrame(RenderContext context, Camera camera) {
        Logger.trace(Logger.RENDER, "beginFrame");
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, context.width, context.height);

        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();

        if (camera != null) {
            // Camera uses center origin logic
            float worldWidth = context.width / camera.zoom;
            float worldHeight = context.height / camera.zoom;
            glOrtho(-worldWidth / 2.0f, worldWidth / 2.0f, -worldHeight / 2.0f, worldHeight / 2.0f, -1, 1);
        } else {
            // Default 0 to width/height (bottom-left origin)
            glOrtho(0, context.width, 0, context.height, -1, 1);
        }

        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        if (camera != null) {
            glRotatef(-camera.rotation, 0, 0, 1);
            // Camera now stores position as a Vector2f
            glTranslatef(-camera.position.x, -camera.position.y, 0);
        }
    }

    @Override
    public void drawRect(Vector2f position, Vector2f size, Vector4f color) {
        Logger.trace(Logger.RENDER, () -> "drawRect at (" + position.x + ", " + position.y + ")");
        glColor3f(color.x, color.y, color.z);
        glBegin(GL_QUADS);
        glVertex2f(position.x, position.y);
        glVertex2f(position.x + size.x, position.y);
        glVertex2f(position.x + size.x, position.y + size.y);
        glVertex2f(position.x, position.y + size.y);
        glEnd();
        glColor3f(1f, 1f, 1f);
    }

    @Override
    public void drawBall(Vector2f position, float radius, Vector4f color) {
        Logger.trace(Logger.RENDER, () -> "drawBall at (" + position.x + ", " + position.y + ")");
        int segments = 24;
        glColor3f(color.x, color.y, color.z);
        glBegin(GL_TRIANGLE_FAN);
        glVertex2f(position.x, position.y);
        for (int i = 0; i <= segments; i++) {
            double theta = 2.0 * Math.PI * i / segments;
            float dx = (float) (Math.cos(theta) * radius);
            float dy = (float) (Math.sin(theta) * radius);
            glVertex2f(position.x + dx, position.y + dy);
        }
        glEnd();
        glColor3f(1f, 1f, 1f);
    }

    @Override
    public void pushMatrix() {
        glPushMatrix();
    }

    @Override
    public void popMatrix() {
        glPopMatrix();
    }

    @Override
    public void translate(Vector2f offset) {
        glTranslatef(offset.x, offset.y, 0);
    }

    @Override
    public void rotate(float angle) {
        glRotatef(angle, 0, 0, 1);
    }

    @Override
    public void scale(Vector2f scale) {
        glScalef(scale.x, scale.y, 1);
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
