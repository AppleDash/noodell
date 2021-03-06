#version 330 core

layout(location = 0) in vec2 vertexPosition_screenSpace;
layout(location = 1) in vec4 vertexColor;

out vec4 fragmentColor;

const int SCREEN_WIDTH = 800;
const int SCREEN_HEIGHT = 600;
const vec2 SCREEN_DIMENSIONS_OVER_2 = vec2(SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2);

void main() {
    vec2 vertexPosition_homoSpace = (vertexPosition_screenSpace - SCREEN_DIMENSIONS_OVER_2) / SCREEN_DIMENSIONS_OVER_2;

    gl_Position = vec4(vertexPosition_homoSpace, 0, 1);

    fragmentColor = vertexColor;
}
