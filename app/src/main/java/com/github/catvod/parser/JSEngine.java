package com.github.catvod.parser;


import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * JS解析封装
 */
public class JSEngine {
    private final Class clazz;
    private String allFunctions = "";//js方法语句

    public JSEngine() {
        this.clazz = JSEngine.class;
        initJSStr();//初始化js语句
    }

    private void initJSStr() {
        /**
         * 在此处可以看到 javaContext、javaLoader的应用，
         * 基本使用原理应该是利用类名、类加载器和上下文去获取JSEngine的类和方法
         * 注意method的输入参数类型与本地方法的对应
         */
        allFunctions =
                " var ScriptAPI = java.lang.Class.forName(\"" + JSEngine.class.getName() + "\", true, javaLoader);\n" +
                        " var methodGetValue=  ScriptAPI.getMethod(\"getValue\", [java.lang.String]);\n" +
                        " function getValue(key) {\n" +
                        "       return  methodGetValue.invoke(javaContext,key);\n" +
                        " }\n" +
                        " var methodSetValue=ScriptAPI.getMethod(\"setValue\",[java.lang.Object,java.lang.Object]);\n" +
                        " function setValue(key,value) {\n" +
                        "       methodSetValue.invoke(javaContext,key,value);\n" +
                        " }\n";
    }

    //本地java方法
    public void setValue(Object keyStr, Object o) {
        System.out.println("JSEngine output - setValue : " + keyStr.toString() + " ------> " + o.toString());
    }

    //本地java方法
    public String getValue(String keyStr) {
        System.out.println("JSEngine output - getValue : " + keyStr.toString());
        return "获取到值了";
    }

    /**
     * 执行JS
     *
     * @param js js执行代码 eg: "var v1 = getValue('Ta');setValue(‘key’，v1);"
     */
    public void runScript(String js) {
        String runJSStr = allFunctions + "\n" + js;//运行js = allFunctions + js
        org.mozilla.javascript.Context rhino = org.mozilla.javascript.Context.enter();
        rhino.setOptimizationLevel(-1);
        try {
            Scriptable scope = rhino.initStandardObjects();

            ScriptableObject.putProperty(scope, "javaContext", org.mozilla.javascript.Context.javaToJS(this, scope));//配置属性 javaContext:当前类JSEngine的上下文
            ScriptableObject.putProperty(scope, "javaLoader", org.mozilla.javascript.Context.javaToJS(clazz.getClassLoader(), scope));//配置属性 javaLoader:当前类的JSEngine的类加载器

            rhino.evaluateString(scope, runJSStr, clazz.getSimpleName(), 1, null);
        } finally {
            org.mozilla.javascript.Context.exit();
        }
    }
}
