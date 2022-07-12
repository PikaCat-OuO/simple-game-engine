package renderer;

import org.joml.*;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

public class Shader {

    private int shaderProgramID;

    private String vertexSource;
    private String fragmentSource;
    private String filePath;

    private boolean beginUsed = false;

    public Shader(String filePath) {
        this.filePath = filePath;
        try {
            String source = new String(Files.readAllBytes(Paths.get(filePath)));
            String[] splitString = source.split("(#type)( )+([a-zA-Z]+)");

            // 解析着色器文件
            int index = source.indexOf("#type") + 6;
            int eol = source.indexOf("\r\n", index);
            String firstPattern = source.substring(index, eol).trim();

            index = source.indexOf("#type", index) + 6;
            eol = source.indexOf("\r\n", index);
            String secondPattern = source.substring(index, eol).trim();

            if (firstPattern.equals("vertex")) {
                vertexSource = splitString[1];
            } else if (firstPattern.equals("fragment")) {
                fragmentSource = splitString[1];
            } else {
                throw new IOException(filePath + "中存在未知的标识符：'" + firstPattern + "'");
            }

            if (secondPattern.equals("vertex")) {
                vertexSource = splitString[2];
            } else if (secondPattern.equals("fragment")) {
                fragmentSource = splitString[2];
            } else {
                throw new IOException(filePath + "中存在未知的标识符：'" + firstPattern + "'");
            }

            assert vertexSource != null && fragmentSource != null : "错误：找不到着色器";

            // 置换最大材质的值
            this.fragmentSource = this.fragmentSource.replaceAll("MAX_TEXTURE_SIZE",
                    glGetInteger(GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS) + "");
        } catch (IOException e) {
            e.printStackTrace();
            assert false : "错误：不能打开着色器文件'" + filePath + "'";
        }
    }

    // 编译和链接着色器
    public void compile() {
        int vertexID, fragmentID;
        // 加载并编译顶点着色器
        vertexID = glCreateShader(GL_VERTEX_SHADER);

        // 将代码交给GPU编译
        glShaderSource(vertexID, vertexSource);
        glCompileShader(vertexID);

        // 检查编译错误
        if (GL_FALSE == glGetShaderi(vertexID, GL_COMPILE_STATUS)) {
            int len = glGetShaderi(vertexID, GL_INFO_LOG_LENGTH);
            System.out.println("错误：'" + filePath + "'\n\t顶点着色器编译错误。");
            System.out.println(glGetShaderInfoLog(vertexID, len));
            assert false : "";
        }

        // 加载并编译片段着色器
        fragmentID = glCreateShader(GL_FRAGMENT_SHADER);

        // 将代码交给GPU编译
        glShaderSource(fragmentID, fragmentSource);
        glCompileShader(fragmentID);

        // 检查编译错误
        if (GL_FALSE == glGetShaderi(fragmentID, GL_COMPILE_STATUS)) {
            int len = glGetShaderi(fragmentID, GL_INFO_LOG_LENGTH);
            System.out.println("错误：'" + filePath + "'\n\t片段着色器编译错误。");
            System.out.println(glGetShaderInfoLog(fragmentID, len));
            assert false : "";
        }

        // 链接顶点着色器和片段着色器
        shaderProgramID = glCreateProgram();
        glAttachShader(shaderProgramID, vertexID);
        glAttachShader(shaderProgramID, fragmentID);
        glLinkProgram(shaderProgramID);

        // 检查链接错误
        if (GL_FALSE == glGetProgrami(shaderProgramID, GL_LINK_STATUS)) {
            int len = glGetProgrami(shaderProgramID, GL_INFO_LOG_LENGTH);
            System.out.println("错误：'" + filePath + "'\n\t着色器链接出错。");
            System.out.println(glGetProgramInfoLog(shaderProgramID, len));
            assert false : "";
        }
    }

    public void use() {
        if (!beginUsed) {
            // 绑定着色器程序
            glUseProgram(shaderProgramID);
            beginUsed = true;
        }
    }

    public void detach() {
        // 解绑着色器程序
        glUseProgram(0);
        beginUsed = false;
    }

    // 更新着色器静态变量的值
    public void uploadMat4f(String varName, Matrix4f mat4) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        FloatBuffer matBuffer = BufferUtils.createFloatBuffer(16);
        mat4.get(matBuffer);
        glUniformMatrix4fv(varLocation, false, matBuffer);
    }

    // 更新着色器静态变量的值
    public void uploadMat3f(String varName, Matrix3f mat3) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        FloatBuffer matBuffer = BufferUtils.createFloatBuffer(9);
        mat3.get(matBuffer);
        glUniformMatrix3fv(varLocation, false, matBuffer);
    }

    // 更新着色器静态变量的值
    public void uploadVec4f(String varName, Vector4f vec) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        glUniform4f(varLocation, vec.x, vec.y, vec.z, vec.w);
    }

    // 更新着色器静态变量的值
    public void uploadVec3f(String varName, Vector3f vec) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        glUniform3f(varLocation, vec.x, vec.y, vec.z);
    }

    // 更新着色器静态变量的值
    public void uploadVec2f(String varName, Vector2f vec) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        glUniform2f(varLocation, vec.x, vec.y);
    }

    // 更新着色器静态变量的值
    public void uploadFloat(String varName, float val) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        glUniform1f(varLocation, val);
    }

    // 更新着色器静态变量的值
    public void uploadInt(String varName, int val) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        glUniform1i(varLocation, val);
    }

    // 上传材质
    public void uploadTexture(String varName, int slot) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        glUniform1i(varLocation, slot);
    }

    // 上传整型数据
    public void uploadIntArray(String varName, int[] array) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        glUniform1iv(varLocation, array);
    }
}
