package cloud.pandas.apm.plugin.mybatis.interceptor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Objects;

import com.alibaba.fastjson.JSON;

import cloud.pandas.apm.plugin.mybatis.EDsl;
import cloud.pandas.apm.plugin.mybatis.ElasticsearchUtils;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.http.HttpUtil;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

/**
 * @author fengqi
 */
public class ElasticsearchRestInterceptor implements InstanceMethodsAroundInterceptor {

    private final ThreadLocal<Long> timeHolder = new ThreadLocal<>();

    @Override
    public void beforeMethod(EnhancedInstance enhancedInstance, Method method, Object[] objects, Class<?>[] classes,
        MethodInterceptResult methodInterceptResult) throws Throwable {
        timeHolder.set(System.currentTimeMillis());
    }

    @Override
    public Object afterMethod(EnhancedInstance enhancedInstance, Method method, Object[] objects, Class<?>[] classes,
        Object ret) throws Throwable {
        Long oldTime = timeHolder.get();
        try {
            handle(objects[0], oldTime);
        } catch (IOException ignored) {

        }
        timeHolder.remove();
        return ret;
    }

    private void handle(Object request, long oldTime) throws IOException {
        Object entity = ReflectUtil.getFieldValue(request, "entity");
        Object methodType = ReflectUtil.getFieldValue(request, "method");
        Object endpoint = ReflectUtil.getFieldValue(request, "endpoint");
        EDsl dsl = new EDsl();
        dsl.setMethod((String) methodType);
        dsl.setUri((String) endpoint);
        dsl.setCost((int) (System.currentTimeMillis() - oldTime));
        if (!Objects.isNull(entity)) {
            Long contentLength = ReflectUtil.invoke(entity, "getContentLength");
            InputStream is = ReflectUtil.invoke(entity, "getContent");
            byte[] bytes = new byte[contentLength.intValue()];
            int read = is.read(bytes);
            if (read > -1) {
                dsl.setBody(bytes);
                is.reset();
            }
        }
        HttpUtil.createPost(ElasticsearchUtils.getUrl() + "/elasticsearch/request").body(JSON.toJSONString(dsl)).execute();
    }

    @Override
    public void handleMethodException(EnhancedInstance enhancedInstance, Method method, Object[] objects,
        Class<?>[] classes, Throwable throwable) {

    }
}
