package org.appledash.noodel;

import org.appledash.noodel.util.Mth;

public class Tesselator2D {
    private static final float GROW_FACTOR = 1.5f;

    private int capacity;
    private int usedCapacity;

    private float[] vertices;
    private float[] uvs;

    public Tesselator2D vertex(float x, float y, float u, float v) {
        this.ensureCapacity(this.usedCapacity + 2);

        this.vertices[this.usedCapacity] = x;
        this.vertices[this.usedCapacity + 1] = y;
        this.uvs[this.usedCapacity] = u;
        this.uvs[this.usedCapacity + 1] = v;

        this.usedCapacity += 2;

        return this;
    }

    public Tesselator2D vertices(float[] vertices, float[] uvs) {
        assert(vertices.length == uvs.length);

        this.ensureCapacity(this.usedCapacity + vertices.length);

        System.arraycopy(vertices, 0, this.vertices, this.usedCapacity, vertices.length);
        System.arraycopy(uvs, 0, this.uvs, this.usedCapacity, uvs.length);

        this.usedCapacity += vertices.length;

        return this;
    }

    public int getUsedCapacity() {
        return this.usedCapacity;
    }

    public float[] getVertices() {
        return this.vertices;
    }

    public float[] getUvs() {
        return this.uvs;
    }

    public void reset() {
        this.usedCapacity = 0;
    }

    private void ensureCapacity(int desiredCapacity) {
        if (this.capacity < desiredCapacity) {
            float[] newVertices = new float[Mth.ceil(desiredCapacity * GROW_FACTOR)];
            float[] newUvs = new float[newVertices.length];

            System.arraycopy(this.vertices, 0, newVertices, 0, this.vertices.length);
            System.arraycopy(this.uvs, 0, newUvs, 0, this.uvs.length);
            this.vertices = newVertices;
            this.uvs = newUvs;
            this.capacity = newVertices.length;
        }
    }
}
