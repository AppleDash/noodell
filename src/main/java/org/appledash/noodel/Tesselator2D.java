package org.appledash.noodel;

import org.appledash.noodel.util.Mth;

import java.util.Arrays;

public class Tesselator2D {
    private static final int INITIAL_CAPACITY = 64; /* chosen by fair dice roll */
    private static final float GROW_FACTOR = 1.5f;

    private int capacity = INITIAL_CAPACITY;
    private int usedCapacity;

    private float[] vertices = new float[INITIAL_CAPACITY];
    private float[] uvs = new float[INITIAL_CAPACITY];

    /**
     * Put some vertices with UVs into the buffer. The same number of UVs as vertices must be supplied.
     *
     * @param vertices Vertices
     * @param uvs UVs
     */
    public void putVertices(float[] vertices, float[] uvs) {
        assert(vertices.length == uvs.length);

        this.ensureCapacity(this.usedCapacity + vertices.length);

        System.arraycopy(vertices, 0, this.vertices, this.usedCapacity, vertices.length);
        System.arraycopy(uvs, 0, this.uvs, this.usedCapacity, uvs.length);

        this.usedCapacity += vertices.length;
    }


    /**
     * Get how many floats are currently in each buffer.
     * This value can be divided by the number of dimensions in order to get the number of vertices.
     *
     * @return Number of floats in each buffer.
     */
    public int getUsedCapacity() {
        return this.usedCapacity;
    }

    /**
     * Get the raw vertex data in the buffer.
     *
     * @return Float array of vertex data.
     */
    public float[] getVertices() {
        return Arrays.copyOf(this.vertices, this.usedCapacity);
    }

    /**
     * Get the UV dats in the buffer.
     *
     * @return Float array of UV data.
     */
    public float[] getUvs() {
        return Arrays.copyOf(this.uvs, this.usedCapacity);
    }

    /**
     * Reset the Tesselator, readying it for new vertices.
     */
    public void reset() {
        this.usedCapacity = 0;
    }

    private void ensureCapacity(int desiredCapacity) {
        if (this.capacity < desiredCapacity) {
            int newCapacity = Mth.ceil(desiredCapacity * GROW_FACTOR);
            float[] newVertices = new float[newCapacity];
            float[] newUvs = new float[newCapacity];

            System.arraycopy(this.vertices, 0, newVertices, 0, this.vertices.length);
            System.arraycopy(this.uvs, 0, newUvs, 0, this.uvs.length);

            this.vertices = newVertices;
            this.uvs = newUvs;
            this.capacity = newCapacity;
        }
    }
}
