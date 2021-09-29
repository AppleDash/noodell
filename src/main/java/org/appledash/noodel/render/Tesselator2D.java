package org.appledash.noodel.render;

import org.appledash.noodel.util.Mth;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

public class Tesselator2D {
    private static final int INITIAL_CAPACITY = 64; /* chosen by fair dice roll */
    private static final float GROW_FACTOR = 1.5f;

    private FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(INITIAL_CAPACITY);

    /**
     * Put some vertices with UVs into the buffer. The same number of UVs as vertices must be supplied.
     * UVs must be interpolated with vertices: [vertex, uv, vertex, uv]
     *
     * @param vertices Vertices with UVs
     */
    public void putVertices(float[] vertices) {
        this.ensureCapacity(this.verticesBuffer.position() + vertices.length);
        this.verticesBuffer.put(vertices);
    }


    /**
     * Get the raw vertex data in the buffer.
     *
     * @return Float array of vertex data.
     */
    public FloatBuffer getVertices() {
        FloatBuffer buffer = this.verticesBuffer.duplicate();
        buffer.flip();
        return buffer;
    }

    /**
     * Reset the Tesselator, readying it for new vertices.
     */
    public void reset() {
        this.verticesBuffer.rewind();
    }

    private void ensureCapacity(int desiredCapacity) {
        if (this.verticesBuffer.capacity() < desiredCapacity) {
            int newCapacity = Mth.ceil(desiredCapacity * GROW_FACTOR);
            FloatBuffer newVerticesBuffer = BufferUtils.createFloatBuffer(newCapacity);
            this.verticesBuffer.flip();

            newVerticesBuffer.put(this.verticesBuffer);

            this.verticesBuffer = newVerticesBuffer;
        }
    }
}
