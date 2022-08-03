package cloud.pandas.apm.plugin.mybatis.interceptor;

import java.lang.reflect.Method;

import cloud.pandas.apm.plugin.mybatis.MybatisUtils;
import cloud.pandas.apm.plugin.mybatis.SqlWrapper;

import cn.hutool.core.exceptions.UtilException;
import cn.hutool.core.util.ReflectUtil;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

/**
 * @author fengqi
 */
public class MybatisStatementHandlerInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(EnhancedInstance enhancedInstance, Method method, Object[] objects, Class<?>[] classes,
        MethodInterceptResult methodInterceptResult) throws Throwable {
        try {
            Object mappedStatement = ReflectUtil.getFieldValue(enhancedInstance, "mappedStatement");
            String id = MybatisUtils.getId(mappedStatement);
            Object boundSql = ReflectUtil.getFieldValue(enhancedInstance, "boundSql");
            String sql = MybatisUtils.getSql(boundSql);
            SqlWrapper sqlWrapper = new SqlWrapper();
            sqlWrapper.setId(id);
            sqlWrapper.setSql(sql);
            ContextManager.getRuntimeContext().put(id, sqlWrapper);
        } catch (Throwable ignored) {

        }
    }

    @Override
    public Object afterMethod(EnhancedInstance enhancedInstance, Method method, Object[] objects, Class<?>[] classes,
        Object ret) throws Throwable {
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance enhancedInstance, Method method, Object[] objects,
        Class<?>[] classes, Throwable throwable) {

    }
}
