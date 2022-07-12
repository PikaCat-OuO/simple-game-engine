package components;

import editor.JImGui;
import imgui.ImGui;
import imgui.type.ImInt;
import org.jbox2d.dynamics.contacts.Contact;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import pikacat.GameObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

// 游戏对象的组件
public abstract class Component {

    // 全局ID计数器
    private static int ID_COUNTER = 0;

    // 该组件的UID
    private int uid = -1;

    // 这个组件属于哪一个游戏对象
    public transient GameObject gameObject = null;

    // 启动游戏组件
    public void start() {

    }

    // 在启动引擎时反序列化用的，这时可以在文件中原有的游戏组件的基础之上继续添加新组件而UID不冲突
    // 不然的话如果从文件读入就会导致UID覆盖
    public static void init (int maxUID) {
        ID_COUNTER = maxUID;
    }

    // 更新某一个组件
    public void editorUpdate(float deltaTime) {

    }

    // 更新某一个组件
    public void update(float deltaTime) {

    }

    public void imgui() {
        try {
            Field[] fields = this.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (Modifier.isTransient(field.getModifiers())) {
                    continue;
                }

                boolean isPrivate = !Modifier.isPublic(field.getModifiers());
                if (isPrivate) {
                    field.setAccessible(true);
                }

                Class type = field.getType();
                Object value = field.get(this);
                String name = field.getName();

                if (type == int.class) {
                    field.set(this, JImGui.dragInt(name, (int) value));
                } else if (type == float.class) {
                    field.set(this, JImGui.dragFloat(name, (float) value));
                } else if (type == boolean.class) {
                    boolean val = (boolean) value;
                    if (ImGui.checkbox(name + ": ", val)) {
                        field.set(this, !val);
                    }
                } else if (type == Vector2f.class) {
                    JImGui.drawVec2Control(name, (Vector2f) value);
                } else if (type == Vector3f.class) {
                    JImGui.drawVec3Control(name, (Vector3f) value);
                } else if (type == Vector4f.class) {
                    Vector4f val = (Vector4f) value;
                    float[] imVec = {val.x, val.y, val.z, val.w};
                    if (ImGui.dragFloat4(name + ": ", imVec)) {
                        val.set(imVec);
                    }
                } else if (type.isEnum()) {
                    String[] enumValues = getEnumValues(type);
                    String enumType = ((Enum) value).name();
                    ImInt index = new ImInt(indexOf(enumType, enumValues));
                    if (ImGui.combo(name, index, enumValues, enumValues.length)) {
                        field.set(this, type.getEnumConstants()[index.get()]);
                    }
                } else if (type == String.class) {
                    field.set(this, JImGui.inputText(name, (String) value));
                } else if (type == String[].class) {
                    String[] dialogs = (String[]) value;
                    for (String dialog : dialogs) {
                        dialog = JImGui.inputText(name, dialog);
                    }
                }

                if (isPrivate) {
                    field.setAccessible(false);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    // 碰撞处理流程
    public void preSolve(GameObject gameObject, Contact contact, Vector2f hitNormal) {

    }

    public void beginCollision(GameObject gameObject, Contact contact, Vector2f hitNormal) {

    }

    public void endCollision(GameObject gameObject, Contact contact, Vector2f hitNormal) {

    }

    public void postSolve(GameObject gameObject, Contact contact, Vector2f hitNormal) {

    }

    private <T extends Enum<T>> String[] getEnumValues(Class<T> enumType) {
        String[] enumValues = new String[enumType.getEnumConstants().length];
        int i = 0;
        for (T enumIntegerValue : enumType.getEnumConstants()) {
            enumValues[i] = enumIntegerValue.name();
            ++i;
        }
        return enumValues;
    }

    private int indexOf(String str, String[] arr) {
        for (int i = 0; i < arr.length; ++i) {
            if (arr[i].equals(str)) {
                return i;
            }
        }
        return -1;
    }

    public void generateUID() {
        if (this.uid == -1) {
            this.uid = ID_COUNTER++;
        }
    }

    public int getUID() {
        return this.uid;
    }

    public void destroy() {

    }

}
