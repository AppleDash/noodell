package org.appledash.noodel.util;

import org.appledash.noodel.render.VertexFormat;
import org.jetbrains.annotations.NotNull;

import static org.lwjgl.opengl.GL20.*;

public final class ShaderProgram {
    private final int programId;
    private final VertexFormat vertexFormat;
    private final int[] buffers;

    private ShaderProgram(String vertexCode, String fragmentCode, VertexFormat vertexFormat) {
        int vertexId = glCreateShader(GL_VERTEX_SHADER);
        int fragmentId = glCreateShader(GL_FRAGMENT_SHADER);

        this.programId = glCreateProgram();

        glShaderSource(vertexId, vertexCode);
        glShaderSource(fragmentId, fragmentCode);

        glCompileShader(vertexId);
        glCompileShader(fragmentId);

        try {
            checkShaderCompilationStatus("Vertex", vertexId, false);
            checkShaderCompilationStatus("Fragment", fragmentId, false);
        } catch (CompilationException e) {
            glDeleteShader(vertexId);
            glDeleteShader(fragmentId);
            this.delete();
            throw e;
        }

        glAttachShader(this.programId, vertexId);
        glAttachShader(this.programId, fragmentId);
        glLinkProgram(this.programId);

        try {
            checkShaderCompilationStatus("Linking", this.programId, true);
        } catch (CompilationException e) {
            glDeleteShader(vertexId);
            glDeleteShader(fragmentId);
            this.delete();
            throw e;
        }

        glDetachShader(this.programId, vertexId);
        glDetachShader(this.programId, fragmentId);
        glDeleteShader(vertexId);
        glDeleteShader(fragmentId);

        this.vertexFormat = vertexFormat;
        this.buffers = new int[vertexFormat.attributeSizes.length];

        glGenBuffers(this.buffers);
    }

    public int getUniformLocation(String name) {
        return glGetUniformLocation(this.programId, name);
    }

    public void use() {
        glUseProgram(this.programId);
    }

    public void delete() {
        glDeleteBuffers(this.buffers);
        glDeleteShader(this.programId);
    }

    public static @NotNull ShaderProgram loadFromResources(@NotNull String resourceBaseName, VertexFormat vertexFormat) {
        String vertSource = ResourceHelper.getText(resourceBaseName + ".vert");
        String fragSource = ResourceHelper.getText(resourceBaseName + ".frag");

        return new ShaderProgram(vertSource, fragSource, vertexFormat);
    }

    private static void checkShaderCompilationStatus(String tag, int shaderId, boolean isProgram) {
        int status;
        int infoLogLength;

        if (isProgram) {
            status = glGetProgrami(shaderId, GL_LINK_STATUS);
            infoLogLength = glGetProgrami(shaderId, GL_INFO_LOG_LENGTH);
        } else {
            status = glGetShaderi(shaderId, GL_COMPILE_STATUS);
            infoLogLength = glGetShaderi(shaderId, GL_INFO_LOG_LENGTH);
        }

        if (infoLogLength > 0) {
            throw new CompilationException(
                    status,
                    tag,
                    isProgram ? glGetProgramInfoLog(shaderId) : glGetShaderInfoLog(shaderId)
            );
        }
    }

    public VertexFormat getVertexFormat() {
        return vertexFormat;
    }

    public int[] getBuffers() {
        return buffers;
    }

    public static final class CompilationException extends RuntimeException {
        private final int status;
        private final String infoLog;

        private CompilationException(int status, String tag, String infoLog) {
            super("Shader " + tag + " compilation error, status = " + status + ", info log = " + infoLog);
            this.status = status;
            this.infoLog = infoLog;
        }

        public int getStatus() {
            return this.status;
        }

        public String getInfoLog() {
            return this.infoLog;
        }
    }
}
