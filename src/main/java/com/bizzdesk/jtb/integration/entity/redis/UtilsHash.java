package com.bizzdesk.jtb.integration.entity.redis;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "utils_hash")
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class UtilsHash {

    @Id
    private String utilId;
    private String utilValue;
}
