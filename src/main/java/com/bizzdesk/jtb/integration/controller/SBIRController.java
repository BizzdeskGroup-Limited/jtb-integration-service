package com.bizzdesk.jtb.integration.controller;

import com.bizzdesk.jtb.integration.service.SBIRService;
import com.gotax.framework.library.error.handling.TokenNotFoundException;
import com.gotax.framework.library.sbirs.helpers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SBIRController {

    private SBIRService sbirService;

    @Autowired
    public SBIRController(SBIRService sbirService) {
        this.sbirService = sbirService;
    }

    @PostMapping(value = "/jtb/getIndividualTaxPayers", consumes = "application/json", produces = "application/json")
    public GetIndividualTaxPayersResponse getIndividualTaxPayers(@RequestBody GetIndividualTaxPayersRequest getIndividualTaxPayersRequest) throws TokenNotFoundException {
        return sbirService.getIndividualTaxPayers(getIndividualTaxPayersRequest);
    }

    @PostMapping(value = "/jtb/getIndividualTaxPayersPaged", consumes = "application/json", produces = "application/json")
    public GetIndividualTaxPayersPagedResponse getIndividualTaxPayersPaged(@RequestBody GetIndividualTaxPayersPagedRequest getIndividualTaxPayersPagedRequest) throws TokenNotFoundException {
        return sbirService.getIndividualTaxPayersPagedResponse(getIndividualTaxPayersPagedRequest);
    }

    @PostMapping(value = "/jtb/getNonIndividualTaxPayers", consumes = "application/json", produces = "application/json")
    public GetNonIndividualTaxPayersResponse getNonIndividualTaxPayers(@RequestBody GetNonIndividualTaxPayersRequest getNonIndividualTaxPayersRequest) throws TokenNotFoundException {
        return sbirService.getNonIndividualTaxPayers(getNonIndividualTaxPayersRequest);
    }

    @PostMapping(value = "/jtb/getNonIndividualTaxPayersPaged", consumes = "application/json", produces = "application/json")
    public GetNonIndividualTaxPayersPagedResponse getNonIndividualTaxPayersPaged(@RequestBody GetNonIndividualTaxPayersPagedRequest getNonIndividualTaxPayersPagedRequest) throws TokenNotFoundException {
        return sbirService.getNonIndividualTaxPayersPagedResponse(getNonIndividualTaxPayersPagedRequest);
    }

    @PostMapping(value = "/jtb/addTaxDetails", consumes = "application/json", produces = "application/json")
    public AddTaxDetailsResponse addTaxDetails(@RequestBody AddTaxDetailsRequest addTaxDetailsRequest) throws TokenNotFoundException {
        return sbirService.addTaxDetails(addTaxDetailsRequest);
    }

    @PostMapping(value = "/jtb/addAssetDetails", consumes = "application/json", produces = "application/json")
    public AddAssetDetailsResponse addAssetDetails(@RequestBody AddAssetDetailsRequest addAssetDetailsRequest) throws TokenNotFoundException {
        return sbirService.addAssetDetails(addAssetDetailsRequest);
    }
}
