package cloud.pandas.apm.plugin.mybatis.interceptor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.fastjson.JSON;

import cloud.pandas.apm.plugin.mybatis.MybatisUtils;
import cloud.pandas.apm.plugin.mybatis.SqlWrapper;
import cn.hutool.http.HttpUtil;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

/**
 * @author fengqi
 */
public class MybatisExecutorInterceptor implements InstanceMethodsAroundInterceptor {
    private static final ILog LOGGER = LogManager.getLogger(MybatisExecutorInterceptor.class);

    private final ThreadLocal<Long> timeHolder = new ThreadLocal<>();

    @Override
    public void beforeMethod(EnhancedInstance enhancedInstance, Method method, Object[] objects, Class<?>[] classes,
        MethodInterceptResult methodInterceptResult) throws Throwable {
        timeHolder.set(System.currentTimeMillis());
    }

    @Override
    public Object afterMethod(EnhancedInstance enhancedInstance, Method method, Object[] objects, Class<?>[] classes,
        Object ret) throws Throwable {
        try {
            int cost = (int) (System.currentTimeMillis() - timeHolder.get());
            String id = MybatisUtils.getId(objects[0]);
            SqlWrapper sqlWrapper = (SqlWrapper) ContextManager.getRuntimeContext().get(id);
            Map<Integer, Object> paramsMap = MybatisUtils.registerParam();
            sqlWrapper.setCost(cost);
            format(sqlWrapper, paramsMap);
            ContextManager.getRuntimeContext().remove(id);
        } catch (Throwable throwable) {
            LOGGER.error("MybatisExecutorInterceptor: ", throwable);
        } finally {
            timeHolder.remove();
            MybatisUtils.unregisterParam();
        }
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance enhancedInstance, Method method, Object[] objects,
        Class<?>[] classes, Throwable throwable) {
        try {
            String id = MybatisUtils.getId(objects[0]);
            ContextManager.getRuntimeContext().remove(id);
        } catch (Throwable ignored){

        }
        timeHolder.remove();
        LOGGER.error("MybatisExecutorInterceptor: ", throwable);
    }

    public void format(SqlWrapper wrapper, Map<Integer, Object> paramsMap) {
        try {
            String sql = wrapper.getSql();
            String[] codes = (sql + System.lineSeparator()).split("\\?");
            int size = codes.length - 1;
            if (size != paramsMap.size()) {
                return;
            }
            List<Object> parameters = new ArrayList<>(paramsMap.values());
            String format = SQLUtils.format(sql, DbType.of(MybatisUtils.getDbType()), parameters);
            wrapper.setSql(format);
            HttpUtil.createPost(MybatisUtils.getUrl() + "/mybatis/log").body(JSON.toJSONString(wrapper))
                .execute();
        } catch (Exception e) {
            LOGGER.error("MybatisExecutorInterceptor: ", e);
        }
    }
}
