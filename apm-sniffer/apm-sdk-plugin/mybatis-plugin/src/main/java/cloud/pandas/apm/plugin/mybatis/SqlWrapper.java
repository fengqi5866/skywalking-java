package cloud.pandas.apm.plugin.mybatis;

/**
 * @author fengqi
 */
public class SqlWrapper {

    private String id;

    private String sql;

    private Integer cost;

    public SqlWrapper() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Integer getCost() {
        return cost;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }

    @Override
    public String toString() {
        return "SqlWrapper{" +
            "id='" + id + '\'' +
            ", sql='" + sql + '\'' +
            ", cost=" + cost +
            '}';
    }
}
