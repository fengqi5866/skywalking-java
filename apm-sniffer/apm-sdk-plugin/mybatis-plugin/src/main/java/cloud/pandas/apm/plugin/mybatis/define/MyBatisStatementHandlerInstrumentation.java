package cloud.pandas.apm.plugin.mybatis.define;

import cloud.pandas.apm.plugin.mybatis.MyBatisMethodMatch;
import cloud.pandas.apm.plugin.mybatis.interceptor.MybatisStatementHandlerInterceptor;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;

import static org.apache.skywalking.apm.agent.core.plugin.match.NameMatch.byName;

/**
 * @author fengqi
 */
public class MyBatisStatementHandlerInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {
    @Override
    public ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return new ConstructorInterceptPoint[0];
    }

    @Override
    public InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[] {
            new InstanceMethodsInterceptPoint() {
                @Override
                public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return MyBatisMethodMatch.INSTANCE.getMyBatisStatementHandlerMethodMatcher();
                }

                @Override
                public String getMethodsInterceptor() {
                    return MybatisStatementHandlerInterceptor.class.getName();
                }

                @Override
                public boolean isOverrideArgs() {
                    return false;
                }
            }
        };
    }

    @Override
    public ClassMatch enhanceClass() {
        return byName("org.apache.ibatis.executor.statement.BaseStatementHandler");
    }
}
