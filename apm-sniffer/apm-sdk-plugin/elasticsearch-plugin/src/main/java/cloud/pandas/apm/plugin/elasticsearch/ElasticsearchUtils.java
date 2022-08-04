package cloud.pandas.apm.plugin.elasticsearch;

/**
 * @author fengqi
 */
public class ElasticsearchUtils {

    public static String getUrl() {
        return System.getProperty("apm.url", "http://localhost:5866") + "/" + System.getProperty("apm.project", "");
    }
}
