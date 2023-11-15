package rabbit.gateway.admin.respository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import rabbit.gateway.common.entity.Service;

public interface ServiceRepository extends R2dbcRepository<Service, String> {
}
