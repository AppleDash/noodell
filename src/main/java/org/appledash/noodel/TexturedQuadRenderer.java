package org.appledash.noodel;

import org.appledash.noodel.util.ShaderProgram;

import java.io.IOException;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

public class TexturedQuadRenderer {
    private static final int QUAD_VERTICES = 6;
    private final int vertexBufferId;
    private final int uvBufferId;
    private final ShaderProgram shader;
    private final int textureUniformId;
    private final SpriteSheet spriteSheet;
    private Tesselator2D tesselator2D = new Tesselator2D();

    public TexturedQuadRenderer(Texture2D texture, int spriteWidth, int spriteHeight) {
        this.spriteSheet = new SpriteSheet(texture, spriteWidth, spriteHeight);
        this.vertexBufferId = glGenBuffers();
        this.uvBufferId = glGenBuffers();

        try {
            this.shader = ShaderProgram.loadFromResources("shaders/2d");
            this.textureUniformId = this.shader.getUniformLocation("textureSampler");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load shader!", e);
        }
    }

    public void putQuad(int x, int y, int w, int h, int spriteIndex) {
        int uv = this.spriteSheet.getSpriteUV(spriteIndex);
        int u = (uv >> Short.SIZE) & Short.MAX_VALUE;
        int v = uv & Short.MAX_VALUE;

        float tSz = this.spriteSheet.getTexture().getWidth();
        float bSz = this.spriteSheet.getSpriteWidth();

        this.tesselator2D.reset();
        this.tesselator2D.vertices(

        )

      /*  this.putData(new float[]{
                x, y, // bottom left
                (x + w), y, // bottom right
                x, (y + h), // top left

                x, (y + h), // top left
                (x + w), (y + h), // top right
                (x + w), y // bottom right
        }, new float[] {
                (u / tSz), (v + bSz) / tSz,
                (u + bSz) / tSz, (v + bSz) / tSz,
                (u / tSz), (v / tSz),
                (u / tSz), (v / tSz),
                (u + bSz) / tSz, v / tSz,
                (u + bSz) / tSz, (v + bSz) / tSz
        });*/
    }

    public void draw() {
        this.shader.use();

        glActiveTexture(GL_TEXTURE0);
        this.spriteSheet.getTexture().bind();
        glUniform1i(this.textureUniformId, 0);

        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, this.vertexBufferId);
        glVertexAttribPointer(
                0,
                2,
                GL_FLOAT,
                false,
                0,
                0
        );

        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, this.uvBufferId);
        glVertexAttribPointer(
                1,
                2,
                GL_FLOAT,
                false,
                0,
                0
        );

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glDrawArrays(GL_TRIANGLES, 0, QUAD_VERTICES);

        glDisable(GL_BLEND);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
    }

    public void delete() {
        glDeleteBuffers(this.vertexBufferId);
        glDeleteBuffers(this.uvBufferId);
        this.spriteSheet.delete();

        if (this.shader != null) {
            this.shader.delete();
        }
    }

    private void putData(float[] vertices, float[] uvs) {
        assert (vertices.length == QUAD_VERTICES * 2);
        assert (uvs.length == QUAD_VERTICES * 2);

        glBindBuffer(GL_ARRAY_BUFFER, this.vertexBufferId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, this.uvBufferId);
        glBufferData(GL_ARRAY_BUFFER, uvs, GL_STATIC_DRAW);
    }
}
