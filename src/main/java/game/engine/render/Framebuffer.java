package game.engine.render;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

import game.engine.logging.Logger;

/**
 * Encapsulates an OpenGL Framebuffer Object (FBO) with a color texture and depth renderbuffer.
 */
public class Framebuffer {
    private int framebufferId = 0;
    private int textureId = 0;
    private int renderbufferId = 0;
    private int width = 0;
    private int height = 0;

    public void create(int width, int height) {
        this.width = width;
        this.height = height;

        framebufferId = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, framebufferId);

        // Create color texture
        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureId, 0);

        // Create depth renderbuffer
        renderbufferId = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, renderbufferId);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, renderbufferId);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            Logger.error(Logger.RENDER, "Framebuffer is not complete!");
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void resize(int width, int height) {
        if (width == this.width && height == this.height) return;
        delete();
        create(width, height);
    }

    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, framebufferId);
    }

    public void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void delete() {
        if (framebufferId != 0) {
            try {
                glDeleteFramebuffers(framebufferId);
                glDeleteTextures(textureId);
                glDeleteRenderbuffers(renderbufferId);
            } catch (Exception e) {
                Logger.warn(Logger.RENDER, "Error deleting framebuffer resources: " + e.getMessage());
            } finally {
                framebufferId = 0;
                textureId = 0;
                renderbufferId = 0;
            }
        }
    }

    public int getTextureId() {
        return textureId;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
