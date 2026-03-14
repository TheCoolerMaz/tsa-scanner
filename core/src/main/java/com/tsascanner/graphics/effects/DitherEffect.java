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
 * Ordered dithering post-processing effect.
 * 
 * Uses a 4x4 Bayer matrix to add dithering to gradients,
 * creating a retro/pixel-art aesthetic.
 */
public class DitherEffect implements Effect {

    private static final String VERTEX_SHADER = 
        "attribute vec4 a_position;\n" +
        "attribute vec2 a_texCoord0;\n" +
        "varying vec2 v_texCoord;\n" +
        "void main() {\n" +
        "    v_texCoord = a_texCoord0;\n" +
        "    gl_Position = a_position;\n" +
        "}\n";

    // GLES 1.00 compatible: uses mat4 instead of array constructor
    private static final String FRAGMENT_SHADER = 
        "#ifdef GL_ES\n" +
        "precision mediump float;\n" +
        "#endif\n" +
        "uniform sampler2D u_texture0;\n" +
        "uniform float u_strength;\n" +
        "varying vec2 v_texCoord;\n" +
        "\n" +
        "// 4x4 Bayer matrix as mat4 (GLES 1.00 compatible)\n" +
        "const mat4 bayerMatrix = mat4(\n" +
        "    0.0/16.0,  12.0/16.0,  3.0/16.0, 15.0/16.0,\n" +
        "    8.0/16.0,   4.0/16.0, 11.0/16.0,  7.0/16.0,\n" +
        "    2.0/16.0,  14.0/16.0,  1.0/16.0, 13.0/16.0,\n" +
        "   10.0/16.0,   6.0/16.0,  9.0/16.0,  5.0/16.0\n" +
        ");\n" +
        "\n" +
        "void main() {\n" +
        "    vec4 color = texture2D(u_texture0, v_texCoord);\n" +
        "    \n" +
        "    int x = int(mod(gl_FragCoord.x, 4.0));\n" +
        "    int y = int(mod(gl_FragCoord.y, 4.0));\n" +
        "    float ditherValue = bayerMatrix[x][y];\n" +
        "    \n" +
        "    color.rgb += (ditherValue - 0.5) * u_strength;\n" +
        "    \n" +
        "    gl_FragColor = color;\n" +
        "}\n";

    private ShaderProgram program;
    private Mesh quad;
    private boolean disabled = false;
    
    private float strength = 0.1f;

    public DitherEffect() {
        program = new ShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        if (!program.isCompiled()) {
            Gdx.app.error("DitherEffect", "Shader compilation failed: " + program.getLog());
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
        return "Dither";
    }

    @Override
    public void setIntensity(float intensity) {
        setStrength(intensity);
    }

    @Override
    public float getIntensity() {
        return strength;
    }

    public void setStrength(float strength) {
        this.strength = strength;
        if (program != null) {
            program.bind();
            program.setUniformf("u_strength", strength);
        }
    }

    @Override
    public void rebind() {
        program.bind();
        program.setUniformi("u_texture0", 0);
        program.setUniformf("u_strength", strength);
    }

    @Override
    public void resize(int width, int height) {
        // No resolution-dependent uniforms
    }

    @Override
    public void render(VfxRenderContext context, VfxPingPongWrapper buffers) {
        if (disabled) return;

        VfxFrameBuffer src = buffers.getSrcBuffer();
        
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        program.bind();
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
