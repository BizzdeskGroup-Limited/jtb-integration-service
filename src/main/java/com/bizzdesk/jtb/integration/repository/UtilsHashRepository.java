package com.bizzdesk.jtb.integration.repository;

import com.bizzdesk.jtb.integration.entity.redis.UtilsHash;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UtilsHashRepository extends CrudRepository<UtilsHash, String> {
}
