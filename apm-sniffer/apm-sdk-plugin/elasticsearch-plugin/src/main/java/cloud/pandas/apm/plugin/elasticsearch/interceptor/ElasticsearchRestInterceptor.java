package cloud.pandas.apm.plugin.elasticsearch.interceptor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Objects;

import com.alibaba.fastjson.JSON;

import cloud.pandas.apm.plugin.elasticsearch.EDsl;
import cloud.pandas.apm.plugin.elasticsearch.ElasticsearchUtils;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.http.HttpUtil;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

/**
 * @author fengqi
 */
public class ElasticsearchRestInterceptor implements InstanceMethodsAroundInterceptor {

    private static final ILog LOGGER = LogManager.getLogger(ElasticsearchRestInterceptor.class);

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
            handle(objects[0], cost);
        } catch (IOException e) {
            LOGGER.error("ElasticsearchRestInterceptor: ", e);
        }
        timeHolder.remove();
        return ret;
    }

    private void handle(Object request, int cost) throws IOException {
        Object entity = ReflectUtil.getFieldValue(request, "entity");
        Object methodType = ReflectUtil.getFieldValue(request, "method");
        Object endpoint = ReflectUtil.getFieldValue(request, "endpoint");
        EDsl dsl = new EDsl();
        dsl.setMethod((String) methodType);
        dsl.setUri((String) endpoint);
        dsl.setCost(cost);
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
        HttpUtil.createPost(ElasticsearchUtils.getUrl() + "/elasticsearch/request").body(JSON.toJSONString(dsl))
            .execute();
    }

    @Override
    public void handleMethodException(EnhancedInstance enhancedInstance, Method method, Object[] objects,
        Class<?>[] classes, Throwable throwable) {
        LOGGER.error("ElasticsearchRestInterceptor: ", throwable);
        timeHolder.remove();
    }
}
