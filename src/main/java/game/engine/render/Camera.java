package game.engine.render;

import org.joml.Vector2f;
import org.joml.Vector4f;
import org.joml.Matrix4f;

/**
 * Represents a 2D orthographic camera.
 * World coordinate system: +Y up.
 */
public class Camera {
    public final Vector2f position = new Vector2f();
    public float rotation = 0.0f; // in degrees
    public float zoom = 1.0f;

    public Camera(float x, float y) {
        this.position.set(x, y);
    }

    public Camera() { this(0f, 0f); }

    /**
     * Converts screen coordinates (pixels, top-left origin) to world coordinates.
     * @param screenX Pixel X from left
     * @param screenY Pixel Y from top
     * @param framebufferWidth Framebuffer width in pixels
     * @param framebufferHeight Framebuffer height in pixels
     * @return World position as Vector2f
     */
    public Vector2f screenToWorld(float screenX, float screenY, int framebufferWidth, int framebufferHeight) {
        // Build projection and view matrices matching OpenGLRenderer
        Matrix4f proj = getProjectionMatrix(framebufferWidth, framebufferHeight);
        Matrix4f view = getViewMatrix();

        // Combine and invert
        Matrix4f pv = new Matrix4f(proj).mul(view);
        pv.invert();

        // Convert screen pixel -> NDC
        float ndcX = (screenX / (float)framebufferWidth) * 2.0f - 1.0f;
        float ndcY = 1.0f - (screenY / (float)framebufferHeight) * 2.0f;

        Vector4f clip = new Vector4f(ndcX, ndcY, 0f, 1f);
        Vector4f world = pv.transform(clip);
        if (world.w != 0f) {
            world.x /= world.w;
            world.y /= world.w;
        }
        return new Vector2f(world.x, world.y);
    }

    /**
     * Construct the view matrix that transforms world coordinates into camera/view space.
     * Matches the OpenGL renderer usage (glRotatef(-rotation); glTranslatef(-position)).
     */
    public Matrix4f getViewMatrix() {
        Matrix4f view = new Matrix4f().identity();
        // Apply rotation then translation to match renderer: M = R * T
        view.rotate((float)Math.toRadians(-rotation), 0f, 0f, 1f);
        view.translate(-position.x, -position.y, 0f);
        return view;
    }

    /**
     * Construct an orthographic projection matching the renderer's glOrtho call.
     */
    public Matrix4f getProjectionMatrix(int framebufferWidth, int framebufferHeight) {
        float worldWidth = framebufferWidth / zoom;
        float worldHeight = framebufferHeight / zoom;
        float left = -worldWidth * 0.5f;
        float right = worldWidth * 0.5f;
        float bottom = -worldHeight * 0.5f;
        float top = worldHeight * 0.5f;
        return new Matrix4f().setOrtho(left, right, bottom, top, -1f, 1f);
    }

    /**
     * Convert a world-space position into framebuffer pixel coordinates (top-left origin).
     * Uses the same projection/view matrices as the renderer.
     */
    public Vector2f worldToScreen(Vector2f worldPos, int framebufferWidth, int framebufferHeight) {
        Matrix4f proj = getProjectionMatrix(framebufferWidth, framebufferHeight);
        Matrix4f view = getViewMatrix();
        Matrix4f pv = new Matrix4f(proj).mul(view);

        // Transform world position into clip space
        Vector4f world4 = new Vector4f(worldPos.x, worldPos.y, 0f, 1f);
        Vector4f clip = pv.transform(world4);
        if (clip.w != 0f) {
            clip.x /= clip.w;
            clip.y /= clip.w;
        }

        // NDC [-1,1] -> pixel coords (top-left origin)
        float pixelX = ((clip.x + 1f) * 0.5f) * framebufferWidth;
        float pixelY = ((1f - clip.y) * 0.5f) * framebufferHeight;
        return new Vector2f(pixelX, pixelY);
    }

    // Helper to get view/projection logic for renderer
    public float getWorldWidth(int framebufferWidth) { return framebufferWidth / zoom; }
    public float getWorldHeight(int framebufferHeight) { return framebufferHeight / zoom; }
}
