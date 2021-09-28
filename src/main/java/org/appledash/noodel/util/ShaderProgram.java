package org.appledash.noodel.util;

import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;

import static org.lwjgl.opengl.GL20.*;

public final class ShaderProgram {
    private final int programId;

    private ShaderProgram(String vertexCode, String fragmentCode) {
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
    }

    public void setUniform3f(int location, float x, float y, float z) {
        glUseProgram(this.programId);
        glUniform3f(location, x, y, z);
        glUseProgram(0);
    }

    public int getUniformLocation(String name) {
        return glGetUniformLocation(this.programId, name);
    }

    public void use() {
        glUseProgram(this.programId);
    }

    public void delete() {
        glDeleteShader(this.programId);
    }

    public static ShaderProgram loadFromResources(String resourceBaseName) throws IOException {
        InputStream vertInput = ShaderProgram.class.getClassLoader().getResourceAsStream(resourceBaseName + ".vert");
        InputStream fragInput = ShaderProgram.class.getClassLoader().getResourceAsStream(resourceBaseName + ".frag");

        if (vertInput == null || fragInput == null) {
            throw new IOException("Could not find vertex/fragment shader");
        }

        String vertSource = new String(vertInput.readAllBytes(), StandardCharsets.UTF_8);
        String fragSource = new String(fragInput.readAllBytes(), StandardCharsets.UTF_8);

        return new ShaderProgram(vertSource, fragSource);
    }

    private static void checkShaderCompilationStatus(String tag, int shaderId, boolean isProgram) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pStatusBuffer = stack.mallocInt(1);
            IntBuffer pInfoLogLength = stack.mallocInt(1);

            if (isProgram) {
                glGetProgramiv(shaderId, GL_LINK_STATUS, pStatusBuffer);
                glGetProgramiv(shaderId, GL_INFO_LOG_LENGTH, pInfoLogLength);
            } else {
                glGetShaderiv(shaderId, GL_COMPILE_STATUS, pStatusBuffer);
                glGetShaderiv(shaderId, GL_INFO_LOG_LENGTH, pInfoLogLength);
            }

            if (pInfoLogLength.get() > 0) {
                throw new CompilationException(
                        pStatusBuffer.get(),
                        tag,
                        isProgram ? glGetProgramInfoLog(shaderId) : glGetShaderInfoLog(shaderId)
                );
            }
        }
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
