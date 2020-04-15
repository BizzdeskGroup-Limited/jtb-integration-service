package com.bizzdesk.jtb.integration.kafka.interfaces;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface IndividualTaxPayersChannel {
    @Output(value = "individual-tax-payers")
    MessageChannel output();
}
