package org.appledash.noodel.render;

import org.appledash.noodel.util.Mth;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

public class Tesselator2D {
    private static final int INITIAL_CAPACITY = 64; /* chosen by fair dice roll */
    private static final float GROW_FACTOR = 1.5f;

    private final FloatBuffer[] verticesBuffer = new FloatBuffer[VertexFormat.values().length];

    public Tesselator2D() {
        for (int i = 0; i < this.verticesBuffer.length; i++) {
            this.verticesBuffer[i] = BufferUtils.createFloatBuffer(INITIAL_CAPACITY);
        }
    }

    /**
     * Put some vertices with UVs into the buffer. The same number of UVs as vertices must be supplied.
     * UVs must be interpolated with vertices: [vertex, uv, vertex, uv]
     *
     * @param vertices Vertices with UVs
     */
    public void putVertices(VertexFormat format, float[] vertices) {
        this.ensureSpace(format, vertices.length);
        this.verticesBuffer[format.ordinal()].put(vertices);
    }


    /**
     * Get the raw vertex data in the buffer.
     *
     * @return Float array of vertex data.
     */
    public FloatBuffer getVertices(VertexFormat format) {
        FloatBuffer buffer = this.verticesBuffer[format.ordinal()].duplicate();
        buffer.flip();
        return buffer;
    }

    /**
     * Reset the Tesselator, readying it for new vertices.
     */
    public void reset() {
        for (FloatBuffer floatBuffer : this.verticesBuffer) {
            floatBuffer.rewind();
        }
    }

    private void ensureSpace(VertexFormat format, int howMuchMore) {
        FloatBuffer buffer = this.verticesBuffer[format.ordinal()];
        int desiredCapacity = buffer.position() + howMuchMore;
        if (buffer.capacity() < desiredCapacity) {
            int newCapacity = Mth.ceil(desiredCapacity * GROW_FACTOR);
            FloatBuffer newVerticesBuffer = BufferUtils.createFloatBuffer(newCapacity);
            buffer.flip();

            newVerticesBuffer.put(buffer);

            this.verticesBuffer[format.ordinal()] = newVerticesBuffer;
        }
    }
}
