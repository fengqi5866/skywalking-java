package cloud.pandas.apm.plugin.elasticsearch;

/**
 * @author fengqi
 */
public class EDsl {

    private String method;

    private String uri;

    private byte[] body;

    private Integer cost;

    public EDsl() {
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public Integer getCost() {
        return cost;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }
}
