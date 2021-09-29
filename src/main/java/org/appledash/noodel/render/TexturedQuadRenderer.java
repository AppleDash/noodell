package org.appledash.noodel.render;

import org.appledash.noodel.texture.SpriteSheet;
import org.appledash.noodel.texture.Texture2D;
import org.appledash.noodel.util.ShaderProgram;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20.*;

public class TexturedQuadRenderer {
    private final int vertexBufferId;
    private final int uvBufferId;
    private final ShaderProgram shader;
    private final SpriteSheet spriteSheet;
    private final Tesselator2D tesselator2D = new Tesselator2D();

    public TexturedQuadRenderer(Texture2D texture, int spriteWidth, int spriteHeight) {
        this.spriteSheet = new SpriteSheet(texture, spriteWidth, spriteHeight);
        this.vertexBufferId = glGenBuffers();
        this.uvBufferId = glGenBuffers();

        this.shader = ShaderProgram.loadFromResources("shaders/2dtexture");
        this.shader.use();
        glUniform1i(this.shader.getUniformLocation("textureSampler"), 0);
    }

    public void putQuad(int x, int y, int w, int h, int spriteIndex) {
        int uv = this.spriteSheet.getSpriteUV(spriteIndex);
        int u = (uv >> Short.SIZE) & Short.MAX_VALUE;
        int v = uv & Short.MAX_VALUE;

        this.tesselator2D.putVertices(
                this.generateQuadVertices(x, y, w, h, u, v)
        );
    }


    public void draw() {
        FloatBuffer vertices = this.tesselator2D.getVertices();
        FloatBuffer uvs = this.tesselator2D.getVertices();

        this.shader.use();

        glActiveTexture(GL_TEXTURE0);
        this.spriteSheet.getTexture().bind();

        /* load in the vertices, at vertex index 0, 2, 4, etc */
        glEnableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, this.vertexBufferId);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, Float.BYTES * 4, 0);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        /* load in the UVs, at vertex index 1, 3, 5, etc */
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, this.uvBufferId);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, Float.BYTES * 4, Float.BYTES * 2); // offset 2
        glBufferData(GL_ARRAY_BUFFER, uvs, GL_STATIC_DRAW);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glDrawArrays(GL_TRIANGLES, 0, vertices.limit() / 2);

        glDisable(GL_BLEND);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        this.tesselator2D.reset();
    }

    public void delete() {
        glDeleteBuffers(this.vertexBufferId);
        glDeleteBuffers(this.uvBufferId);
        this.spriteSheet.delete();

        if (this.shader != null) {
            this.shader.delete();
        }
    }

    private float[] generateQuadVertices(int x, int y, int w, int h, int u, int v) {
        float tSz = this.spriteSheet.getTexture().getWidth();
        float bSz = this.spriteSheet.getSpriteWidth();

        return new float[]{
                x, y,       // bottom left
                (u / tSz), (v + bSz) / tSz,

                (x + w), y, // bottom right
                (u + bSz) / tSz, (v + bSz) / tSz,

                x, (y + h), // top left
                (u / tSz), (v / tSz),

                x, (y + h),  // top left
                (u / tSz), (v / tSz),

                (x + w), (y + h), // top right
                (u + bSz) / tSz, v / tSz,

                (x + w), y, // bottom right
                (u + bSz) / tSz, (v + bSz) / tSz
        };
    }
}
