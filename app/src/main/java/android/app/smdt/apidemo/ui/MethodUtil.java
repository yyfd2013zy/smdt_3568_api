package android.app.smdt.apidemo.ui;

import java.lang.reflect.Method;

public class MethodUtil {

    private static volatile Method setProperties = null;
    private static volatile Method getProperties = null;

    public static String PROP_ETH1_ENABLE = "persist.os.enable.eth1";

    /**
     * 反射调用获取系统属性
     *
     * @param prop         属性名
     * @param defaultValue 默认值
     * @return 属性值
     */
    public static String getSystemProperties(String prop, String defaultValue) {
        String value = defaultValue;
        try {
            if (null == getProperties) {
                synchronized (MethodUtil.class) {
                    if (null == getProperties) {
                        Class<?> cls = Class.forName("android.os.SystemProperties");
                        getProperties = cls.getDeclaredMethod("get", new Class<?>[]{String.class, String.class});
                    }
                }
            }
            value = (String) (getProperties.invoke(null, new Object[]{prop, defaultValue}));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * 反射调用设置系统属性
     *
     * @param prop  属性名
     * @param value 属性值
     */
    public static void setSystemProperties(String prop, String value) {

        try {
            if (null == setProperties) {
                synchronized (MethodUtil.class) {
                    if (null == setProperties) {
                        Class<?> cls = Class.forName("android.os.SystemProperties");
                        setProperties = cls.getDeclaredMethod("set", new Class<?>[]{String.class, String.class});
                    }
                }
            }
            setProperties.invoke(null, new Object[]{prop, value});
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
