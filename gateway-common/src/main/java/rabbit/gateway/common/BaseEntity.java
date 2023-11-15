package rabbit.gateway.common;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

public abstract class BaseEntity {

    @Id
    @Column("id")
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
