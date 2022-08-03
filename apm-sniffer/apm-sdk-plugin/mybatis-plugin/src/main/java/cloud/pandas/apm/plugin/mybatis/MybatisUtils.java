package cloud.pandas.apm.plugin.mybatis;

import java.util.LinkedHashMap;
import java.util.Map;

import cn.hutool.core.util.ReflectUtil;
import org.apache.skywalking.apm.agent.core.context.ContextManager;

/**
 * @author fengqi
 */
public class MybatisUtils {

    public static String getId(Object obj) {
        return (String) ReflectUtil.getFieldValue(obj, "id");
    }

    public static String getSql(Object obj) {
        return (String) ReflectUtil.getFieldValue(obj, "sql");
    }

    public static Map<Integer, Object> registerParam() {
        Map<Integer, Object> paramsMap = (Map<Integer, Object>) ContextManager.getRuntimeContext().get("paramsMap");
        if (paramsMap == null) {
            paramsMap = new LinkedHashMap<>();
            ContextManager.getRuntimeContext().put("paramsMap", paramsMap);
        }
        return paramsMap;
    }

    public static void unregisterParam() {
        ContextManager.getRuntimeContext().remove("paramsMap");
    }

    public static String getUrl() {
        return System.getProperty("apm.url", "http://localhost:5866") + "/" + System.getProperty("apm.project", "");
    }

    public static String getDbType() {
        return System.getProperty("mybatis.db_type", "mysql");
    }
}
