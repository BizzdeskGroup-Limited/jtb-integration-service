package com.bizzdesk.jtb.integration.kafka.interfaces;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface NonIndividualTaxPayersPagedChannel {
    @Output(value = "non-individual-tax-payers-paged")
    MessageChannel output();
}
