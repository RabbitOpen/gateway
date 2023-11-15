package rabbit.gateway.common.entity;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import rabbit.gateway.common.BaseEntity;
import rabbit.gateway.common.PluginType;
import rabbit.gateway.common.Schema;
import rabbit.gateway.common.Scope;

/**
 * 插件
 */
@Table("plugin")
public class Plugin extends BaseEntity {

    /**
     * 插件名
     */
    @Column("name")
    private String name;

    /**
     * 作用范围
     */
    @Column("scope")
    private Scope scope;

    /**
     * 插件类型
     */
    @Column("type")
    private PluginType type;

    /**
     * 作用的对象，serviceCode
     */
    @Column("target")
    private String target;

    /**
     * 插件的schema
     */
    @Column("schema")
    private Schema schema;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public PluginType getType() {
        return type;
    }

    public void setType(PluginType type) {
        this.type = type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public <T extends Schema> T getSchema() {
        return (T) schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }
}
