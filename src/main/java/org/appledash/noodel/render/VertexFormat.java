package org.appledash.noodel.render;

public enum VertexFormat {
    POSITION_2D(new int[] { Float.BYTES * 2 }),
    POSITION_TEXTURE_2D(new int[] { Float.BYTES * 2, Float.BYTES * 2 }),
    POSITION_COLOR_2D(new int[] { Float.BYTES * 2, Float.BYTES * 4}),
    POSITION_COLOR_TEXTURE_2D(new int[] { Float.BYTES * 2, Float.BYTES * 4, Float.BYTES * 2 });

    public final int[] attributeSizes;

    VertexFormat(int[] attributeSizes) {
        this.attributeSizes = attributeSizes;
    }
}
