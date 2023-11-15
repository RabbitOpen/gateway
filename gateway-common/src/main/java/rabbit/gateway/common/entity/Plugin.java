package rabbit.gateway.common.entity;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import rabbit.gateway.common.BaseEntity;
import rabbit.gateway.common.PluginName;
import rabbit.gateway.common.PluginType;
import rabbit.gateway.common.Schema;

/**
 * 插件
 */
@Table("plugin")
public class Plugin extends BaseEntity {

    /**
     * 插件名
     */
    @Column("name")
    private PluginName name;

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
    @Column("plugin_schema")
    private Schema schema;

    public PluginName getName() {
        return name;
    }

    public void setName(PluginName name) {
        this.name = name;
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
