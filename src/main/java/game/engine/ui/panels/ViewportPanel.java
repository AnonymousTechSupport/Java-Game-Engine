package game.engine.ui.panels;

import game.engine.ECS.components.CameraComponent;
import game.engine.ECS.components.ComponentType;
import game.engine.ECS.systems.RenderingSystem;
import game.engine.LevelEditor.EngineCameraController;
import game.engine.World;
import game.engine.render.Camera;
import game.engine.render.Framebuffer;
import game.engine.renderer.Renderer;
import game.engine.ui.core.UIComponentWithContext;
import game.engine.ui.core.UIContext;
import game.engine.ui.services.SelectionService;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiStyleVar;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/**
 * Viewport panel that renders the game scene into an FBO and displays it.
 * Handles editor camera navigation and input forwarding.
 */
public class ViewportPanel extends UIComponentWithContext {
    private final Framebuffer framebuffer;
    private final EngineCameraController editorCameraController;
    private final World world;
    private final RenderingSystem renderingSystem;
    private final Renderer renderer;
    private final game.engine.StateManager stateManager;
    private final SelectionService selectionService;

    public ViewportPanel(UIContext context, World world, RenderingSystem renderingSystem, Renderer renderer, game.engine.StateManager stateManager,
            SelectionService selectionService) {
        super(context);
        this.world = world;
        this.renderingSystem = renderingSystem;
        this.renderer = renderer;
        this.stateManager = stateManager;
        this.framebuffer = new Framebuffer();
        this.editorCameraController = new EngineCameraController();
        this.selectionService = selectionService;
        // Give editorCameraController a reasonable default smoothing for
        // immediate
        // response
        this.editorCameraController.setSmoothness(18.0f);
    }

    // Convert a point in world space to image pixel coordinates inside the
    // ImGui
    // image. Returns a Vector2f where x/y are image pixel coordinates and
    // (imageTopLeftX, imageTopLeftY) is the top-left of the image.
    private Vector2f worldToScreen(Vector2f worldPos, Camera viewCamera, int framebufferWidth, int framebufferHeight, float imageTopLeftX,
            float imageTopLeftY) {
        Vector2f pixel = viewCamera.worldToScreen(worldPos, framebufferWidth, framebufferHeight);
        return pixel.add(imageTopLeftX, imageTopLeftY);
    }

    @Override
    public void render() {
        // Push style to remove padding so image fills the panel
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0, 0);
        ImGui.begin("Viewport");

        ImVec2 availableRegion = ImGui.getContentRegionAvail();
        int availableWidth = Math.max(1, (int) availableRegion.x);
        int availableHeight = Math.max(1, (int) availableRegion.y);

        // Ensure framebuffer matches the available panel size
        ensureFramebufferSize(availableWidth, availableHeight);

        // Determine active camera
        Camera activeCamera = null;
        int mainCameraId = world.getMainCameraEntityId();

        // Use game camera when playing OR when paused (so paused shows game
        // camera)
        if ((stateManager.isPlaying() || stateManager.isPaused()) && mainCameraId != -1) {
            CameraComponent cameraComponent = (CameraComponent) world.getComponent(mainCameraId, ComponentType.CAMERA);
            if (cameraComponent != null) {
                activeCamera = cameraComponent.toCamera();
            }
        }

        if (activeCamera == null) {
            activeCamera = editorCameraController.getCamera();
        }

        // Render into Framebuffer
        int previousFramebufferId = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);

        float deltaTime = 0f;
        try {
            // Try to obtain a delta from the state manager if available
            deltaTime = stateManager.getDelta();
        } catch (Exception ignored) {
        }
        renderingSystem.renderToFramebuffer(framebuffer, activeCamera, world, renderer, deltaTime);

        if (previousFramebufferId != 0)
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousFramebufferId);

        // Draw the framebuffer texture into the ImGui image (flip vertically)
        ImGui.image(framebuffer.getTextureId(), (float) availableWidth, (float) availableHeight, 0, 1, 1, 0);

        // Get image rectangle on screen for overlays
        imgui.ImVec2 imageMin = ImGui.getItemRectMin();
        imgui.ImVec2 imageMax = ImGui.getItemRectMax();
        float imageTopLeftX = imageMin.x;
        float imageTopLeftY = imageMin.y;

        // Draw green outline while playing or paused
        if (stateManager.isPlaying() || stateManager.isPaused()) {
            drawPlayOutline(imageMin.x, imageMin.y, imageMax.x, imageMax.y);
        }

        // If a camera entity or camera component is selected, draw its capture
        // rect
        try {
            if (selectionService != null) {
                game.engine.ui.services.Selection selection = selectionService.getSelected();
                int selectedEntityId = -1;
                boolean isSelectedCameraComponent = false;
                if (selection != null) {
                    if (selection.isComponent() && selection.getComponentType() == ComponentType.CAMERA) {
                        selectedEntityId = selection.getEntityId();
                        isSelectedCameraComponent = true;
                    } else if (selection.isEntity()) {
                        selectedEntityId = selection.getEntityId();
                        // check if this entity has a camera component
                        if (world.getComponent(selectedEntityId, ComponentType.CAMERA) != null) {
                            isSelectedCameraComponent = true;
                        }
                    }
                }

                if (isSelectedCameraComponent && selectedEntityId != -1) {
                    CameraComponent cameraComponent = (CameraComponent) world.getComponent(selectedEntityId, ComponentType.CAMERA);
                    if (cameraComponent != null) {
                        drawCameraSelectionOverlay(cameraComponent, activeCamera, imageTopLeftX, imageTopLeftY);
                    }
                }
            }
        } catch (Exception e) {
            // In case selection service is not available or any error occurs,
            // just
            // skip drawing the overlay
        }

        // Input Handling: only update editor camera when viewport is hovered
        // and
        // we're in the editor
        if (ImGui.isItemHovered()) {
            handleEditorCameraInput();
        }

        ImGui.end();
        ImGui.popStyleVar();
    }

    public void cleanup() {
        framebuffer.delete();
    }

    // Ensure the framebuffer matches the requested size.
    private void ensureFramebufferSize(int requestedWidth, int requestedHeight) {
        if (requestedWidth != framebuffer.getWidth() || requestedHeight != framebuffer.getHeight()) {
            framebuffer.resize(requestedWidth, requestedHeight);
        }
    }

    // Draw a green outline when the game is playing or paused
    private void drawPlayOutline(float x0, float y0, float x1, float y1) {
        // Validate coordinates
        if (x1 <= x0 || y1 <= y0)
            return;

        final float thickness = 3f;
        final float rounding = 0f;
        final int color = ImGui.getColorU32(0f, 1f, 0f, 1f); // green

        ImGui.getWindowDrawList().addRect(x0, y0, x1, y1, color, rounding, 0, thickness);
    }

    // Draw the rotated capture rectangle for the selected camera.
    // Steps: compute camera half-extents in world units, rotate local corners
    // into world-space, project each corner into image pixels, then draw edges.
    private void drawCameraSelectionOverlay(CameraComponent cameraComponent, Camera viewCamera, float imageTopLeftX, float imageTopLeftY) {
        int framebufferWidth = framebuffer.getWidth();
        int framebufferHeight = framebuffer.getHeight();
        if (framebufferWidth <= 0 || framebufferHeight <= 0)
            return;

        // Half extents in world units for the cameraComponent
        float halfWidth = (framebufferWidth / cameraComponent.zoom) * 0.5f;
        float halfHeight = (framebufferHeight / cameraComponent.zoom) * 0.5f;

        // Local unrotated corners (clockwise)
        Vector2f[] localCorners = new Vector2f[] { new Vector2f(-halfWidth, -halfHeight), new Vector2f(halfWidth, -halfHeight),
                new Vector2f(halfWidth, halfHeight), new Vector2f(-halfWidth, halfHeight) };

        float rotationRadians = (float) Math.toRadians(cameraComponent.rotation);
        float cosRotation = (float) Math.cos(rotationRadians);
        float sinRotation = (float) Math.sin(rotationRadians);

        // project corners to image pixel coordinates (include image top-left
        // offset)
        Vector2f[] screenPoly = new Vector2f[localCorners.length];
        for (int i = 0; i < localCorners.length; ++i) {
            float localX = localCorners[i].x;
            float localY = localCorners[i].y;
            // rotate local corner and translate into world-space
            float worldX = localX * cosRotation - localY * sinRotation + cameraComponent.position.x;
            float worldY = localX * sinRotation + localY * cosRotation + cameraComponent.position.y;

            // world -> screen pixel coords (Camera.worldToScreen) and then add
            // image offset
            screenPoly[i] = worldToScreen(new Vector2f(worldX, worldY), viewCamera, framebuffer.getWidth(), framebuffer.getHeight(), imageTopLeftX,
                    imageTopLeftY);
        }

        // Draw polygon outline in pink/purple
        int overlayColor = ImGui.getColorU32(0.9f, 0.5f, 0.75f, 0.9f);
        final float lineThickness = 2f;
        for (int i = 0; i < localCorners.length; ++i) {
            int next = (i + 1) % localCorners.length;
            Vector2f a = screenPoly[i];
            Vector2f b = screenPoly[next];
            ImGui.getWindowDrawList().addLine(a.x, a.y, b.x, b.y, overlayColor, lineThickness);
        }
    }

    // Handle editor camera input while the viewport is hovered
    private void handleEditorCameraInput() {
        if (!stateManager.isPlaying()) {
            float deltaTime = 0f;
            try {
                deltaTime = stateManager.getDelta();
            } catch (Exception ignored) {
            }
            editorCameraController.update(deltaTime);
        }
    }
}
