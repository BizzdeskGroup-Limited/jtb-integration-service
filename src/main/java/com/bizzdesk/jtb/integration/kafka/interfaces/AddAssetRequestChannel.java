package com.bizzdesk.jtb.integration.kafka.interfaces;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface AddAssetRequestChannel {

    @Input(value = "add-asset-request")
    SubscribableChannel input();
}
