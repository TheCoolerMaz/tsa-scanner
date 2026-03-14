package com.tsascanner.graphics.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.crashinvaders.vfx.VfxRenderContext;
import com.crashinvaders.vfx.framebuffer.VfxFrameBuffer;
import com.crashinvaders.vfx.framebuffer.VfxPingPongWrapper;

/**
 * CRT monitor post-processing effect.
 * 
 * Simulates a retro CRT display with:
 * - Scanlines
 * - Screen curvature
 * - Chromatic aberration (RGB split)
 * - Edge vignette
 */
public class CrtEffect implements Effect {

    private static final String VERTEX_SHADER = 
        "attribute vec4 a_position;\n" +
        "attribute vec2 a_texCoord0;\n" +
        "varying vec2 v_texCoord;\n" +
        "void main() {\n" +
        "    v_texCoord = a_texCoord0;\n" +
        "    gl_Position = a_position;\n" +
        "}\n";

    private static final String FRAGMENT_SHADER = 
        "#ifdef GL_ES\n" +
        "precision mediump float;\n" +
        "#endif\n" +
        "uniform sampler2D u_texture0;\n" +
        "uniform vec2 u_resolution;\n" +
        "uniform float u_scanlineIntensity;\n" +
        "uniform float u_curvature;\n" +
        "uniform float u_chromaticAberration;\n" +
        "uniform float u_vignetteIntensity;\n" +
        "varying vec2 v_texCoord;\n" +
        "\n" +
        "vec2 curveUV(vec2 uv) {\n" +
        "    vec2 offset = (uv - 0.5) * 2.0;\n" +
        "    offset *= 1.0 + pow(abs(offset.yx), vec2(2.0)) * u_curvature;\n" +
        "    return (offset / 2.0) + 0.5;\n" +
        "}\n" +
        "\n" +
        "void main() {\n" +
        "    vec2 uv = curveUV(v_texCoord);\n" +
        "    if (uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0 || uv.y > 1.0) {\n" +
        "        gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);\n" +
        "        return;\n" +
        "    }\n" +
        "    \n" +
        "    // Chromatic aberration\n" +
        "    float r = texture2D(u_texture0, uv + vec2(u_chromaticAberration, 0.0)).r;\n" +
        "    float g = texture2D(u_texture0, uv).g;\n" +
        "    float b = texture2D(u_texture0, uv - vec2(u_chromaticAberration, 0.0)).b;\n" +
        "    vec3 color = vec3(r, g, b);\n" +
        "    \n" +
        "    // Scanlines\n" +
        "    float scanline = sin(uv.y * u_resolution.y * 3.14159) * 0.5 + 0.5;\n" +
        "    scanline = pow(scanline, 1.5);\n" +
        "    color *= 1.0 - (u_scanlineIntensity * (1.0 - scanline));\n" +
        "    \n" +
        "    // Vignette\n" +
        "    vec2 vignetteUV = uv * (1.0 - uv);\n" +
        "    float vignette = vignetteUV.x * vignetteUV.y * 15.0;\n" +
        "    vignette = pow(vignette, u_vignetteIntensity);\n" +
        "    color *= vignette;\n" +
        "    \n" +
        "    // Slight brightness boost\n" +
        "    color *= 1.1;\n" +
        "    \n" +
        "    gl_FragColor = vec4(color, 1.0);\n" +
        "}\n";

    private ShaderProgram program;
    private Mesh quad;
    private boolean disabled = false;
    
    private float intensity = 1.0f;
    private float scanlineIntensity = 0.15f;
    private float curvature = 0.03f;
    private float chromaticAberration = 0.002f;
    private float vignetteIntensity = 0.3f;
    
    private int resolutionWidth = 1280;
    private int resolutionHeight = 720;

    public CrtEffect() {
        program = new ShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        if (!program.isCompiled()) {
            Gdx.app.error("CrtEffect", "Shader compilation failed: " + program.getLog());
        }

        quad = new Mesh(true, 4, 6,
            new VertexAttribute(Usage.Position, 2, "a_position"),
            new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord0"));

        quad.setVertices(new float[] {
            -1f, -1f, 0f, 0f,
             1f, -1f, 1f, 0f,
             1f,  1f, 1f, 1f,
            -1f,  1f, 0f, 1f
        });
        quad.setIndices(new short[] { 0, 1, 2, 2, 3, 0 });

        rebind();
    }

    @Override
    public String getName() {
        return "CRT Monitor";
    }

    @Override
    public void setIntensity(float intensity) {
        this.intensity = intensity;
        updateUniforms();
    }

    @Override
    public float getIntensity() {
        return intensity;
    }

    private void updateUniforms() {
        if (program != null) {
            program.bind();
            program.setUniformf("u_scanlineIntensity", scanlineIntensity * intensity);
            program.setUniformf("u_curvature", curvature * intensity);
            program.setUniformf("u_chromaticAberration", chromaticAberration * intensity);
            program.setUniformf("u_vignetteIntensity", vignetteIntensity);
        }
    }

    @Override
    public void rebind() {
        program.bind();
        program.setUniformi("u_texture0", 0);
        program.setUniformf("u_resolution", resolutionWidth, resolutionHeight);
        updateUniforms();
    }

    @Override
    public void resize(int width, int height) {
        resolutionWidth = width;
        resolutionHeight = height;
        rebind();
    }

    @Override
    public void render(VfxRenderContext context, VfxPingPongWrapper buffers) {
        if (disabled) return;

        VfxFrameBuffer src = buffers.getSrcBuffer();
        
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        program.bind();
        program.setUniformf("u_resolution", resolutionWidth, resolutionHeight);
        
        src.getTexture().bind(0);
        quad.render(program, GL20.GL_TRIANGLES);
    }

    @Override
    public void update(float delta) {}

    @Override
    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public void dispose() {
        if (program != null) program.dispose();
        if (quad != null) quad.dispose();
    }
}
