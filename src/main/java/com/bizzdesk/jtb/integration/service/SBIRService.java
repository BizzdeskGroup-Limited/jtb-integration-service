package com.bizzdesk.jtb.integration.service;

import com.bizzdesk.jtb.integration.entity.redis.UtilsHash;
import com.bizzdesk.jtb.integration.helpers.JTBContextPath;
import com.bizzdesk.jtb.integration.repository.UtilsHashRepository;
import com.gotax.framework.library.error.handling.GoTaxException;
import com.gotax.framework.library.error.handling.TokenNotFoundException;
import com.gotax.framework.library.sbirs.helpers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class SBIRService {

    Logger logger = Logger.getLogger(SBIRService.class.getName());
    @Value(value = "${jtb.username}")
    private String jtbUsername;
    @Value(value = "${jtb.password}")
    private String jtbPassword;
    @Value(value = "${jtb.client-name}")
    private String jtbClientName;
    @Value(value = "${jtb.host.url}")
    private String jtbHostURL;

    private UtilsHashRepository utilsHashRepository;
    private RestTemplate restTemplate;

    @Autowired
    public SBIRService(UtilsHashRepository utilsHashRepository, RestTemplate restTemplate) {
        this.utilsHashRepository = utilsHashRepository;
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    @Scheduled(fixedRate = 30000)
    public void generateToken() throws GoTaxException {

        GenerateTokenRequest generateTokenRequest = new GenerateTokenRequest().setEmail(jtbUsername)
                .setPassword(jtbPassword)
                .setClientname(jtbClientName);
        String contextPath = JTBContextPath.GENERATE_TOKEN;
        logger.log(Level.INFO, generateTokenRequest.toString());
        HttpEntity<GenerateTokenRequest> httpEntity = new HttpEntity<>(generateTokenRequest);
        Optional<GenerateTokenResponse> optionalGenerateTokenResponse = Optional.ofNullable(restTemplate.exchange(jtbHostURL.concat(contextPath), HttpMethod.POST, httpEntity, GenerateTokenResponse.class).getBody());
        if(optionalGenerateTokenResponse.isPresent()) {
            GenerateTokenResponse generateTokenResponse = optionalGenerateTokenResponse.get();
            if(generateTokenResponse.isSuccess()) {
                utilsHashRepository.save(new UtilsHash("token", generateTokenResponse.getTokenId()));
            } else {
                String errorMessage = generateTokenResponse.getErrorMsg();
                throw new GoTaxException(errorMessage);
            }
        }
    }

    private String getToken() throws TokenNotFoundException {
        String token = "";
        Optional<UtilsHash> utilsHashOptional = Optional.ofNullable(utilsHashRepository.findById("token").orElseThrow(
                () -> new TokenNotFoundException("No Token Found in Database")));
        if(utilsHashOptional.isPresent()) {
            token = utilsHashOptional.get().getUtilId();
        }
        return token;
    }

    public GetIndividualTaxPayersResponse getIndividualTaxPayers(GetIndividualTaxPayersRequest getIndividualTaxPayersRequest) throws TokenNotFoundException {
        GetIndividualTaxPayersResponse getIndividualTaxPayersResponse;
        String contextPath = JTBContextPath.GET_INDIVIDUAL_TAX_PAYERS;
        HttpEntity<GetIndividualTaxPayersRequest> httpEntity = new HttpEntity<>(getIndividualTaxPayersRequest);
        Optional<GetIndividualTaxPayersResponse> optionalGetIndividualTaxPayersResponse = Optional.ofNullable(restTemplate.exchange(jtbHostURL.concat(contextPath), HttpMethod.POST, httpEntity, GetIndividualTaxPayersResponse.class, getToken()).getBody());
        if(optionalGetIndividualTaxPayersResponse.isPresent()) {
            getIndividualTaxPayersResponse = optionalGetIndividualTaxPayersResponse.get();
        } else {
            throw new HttpClientErrorException(HttpStatus.NO_CONTENT, "Error in Processing Request To Get Individual Tax Payers");
        }
        return getIndividualTaxPayersResponse;
    }

    public GetNonIndividualTaxPayersResponse getNonIndividualTaxPayers(GetNonIndividualTaxPayersRequest getNonIndividualTaxPayersRequest) throws TokenNotFoundException {
        GetNonIndividualTaxPayersResponse getNonIndividualTaxPayersResponse;
        String contextPath = JTBContextPath.GET_NON_INDIVIDUAL_TAX_PAYERS;
        HttpEntity<GetNonIndividualTaxPayersRequest> httpEntity = new HttpEntity<>(getNonIndividualTaxPayersRequest);
        Optional<GetNonIndividualTaxPayersResponse> optionalGetNonIndividualTaxPayersResponse = Optional.ofNullable(restTemplate.exchange(jtbHostURL.concat(contextPath), HttpMethod.POST, httpEntity, GetNonIndividualTaxPayersResponse.class, getToken()).getBody());
        if(optionalGetNonIndividualTaxPayersResponse.isPresent()) {
            getNonIndividualTaxPayersResponse = optionalGetNonIndividualTaxPayersResponse.get();
        } else {
            throw new HttpClientErrorException(HttpStatus.NO_CONTENT, "Error in Processing Request To Get Non Individual Tax Payers");
        }
        return getNonIndividualTaxPayersResponse;
    }

    public GetNonIndividualTaxPayersPagedResponse getNonIndividualTaxPayersPagedResponse(GetNonIndividualTaxPayersPagedRequest getNonIndividualTaxPayersPagedRequest) throws TokenNotFoundException {
        GetNonIndividualTaxPayersPagedResponse getNonIndividualTaxPayersPagedResponse;
        String contextPath = JTBContextPath.GET_PAGED_NON_INDIVIDUAL_TAX_PAYERS;
        HttpEntity<GetNonIndividualTaxPayersPagedRequest> httpEntity = new HttpEntity<>(getNonIndividualTaxPayersPagedRequest);
        Optional<GetNonIndividualTaxPayersPagedResponse> optionalGetNonIndividualTaxPayersPagedResponse = Optional.ofNullable(restTemplate.exchange(jtbHostURL.concat(contextPath), HttpMethod.POST, httpEntity, GetNonIndividualTaxPayersPagedResponse.class, getToken()).getBody());
        if(optionalGetNonIndividualTaxPayersPagedResponse.isPresent()) {
            getNonIndividualTaxPayersPagedResponse = optionalGetNonIndividualTaxPayersPagedResponse.get();
        } else {
            throw new HttpClientErrorException(HttpStatus.NO_CONTENT, "Error in Processing Request To Get Non Individual Tax Payers With Paged Response");
        }
        return getNonIndividualTaxPayersPagedResponse;
    }

    public GetIndividualTaxPayersPagedResponse getIndividualTaxPayersPagedResponse(GetIndividualTaxPayersPagedRequest getIndividualTaxPayersPagedRequest) throws TokenNotFoundException {
        GetIndividualTaxPayersPagedResponse getIndividualTaxPayersPagedResponse;
        String contextPath = JTBContextPath.GET_PAGED_INDIVIDUAL_TAX_PAYERS;
        HttpEntity<GetIndividualTaxPayersPagedRequest> httpEntity = new HttpEntity<>(getIndividualTaxPayersPagedRequest);
        Optional<GetIndividualTaxPayersPagedResponse> optionalGetIndividualTaxPayersPagedResponse = Optional.ofNullable(restTemplate.exchange(jtbHostURL.concat(contextPath), HttpMethod.POST, httpEntity, GetIndividualTaxPayersPagedResponse.class, getToken()).getBody());
        if(optionalGetIndividualTaxPayersPagedResponse.isPresent()) {
            getIndividualTaxPayersPagedResponse = optionalGetIndividualTaxPayersPagedResponse.get();
        } else {
            throw new HttpClientErrorException(HttpStatus.NO_CONTENT, "Error in Processing Request To Get Individual Tax Payers with Paged Response");
        }
        return getIndividualTaxPayersPagedResponse;
    }

    public AddTaxDetailsResponse addTaxDetails(AddTaxDetailsRequest addTaxDetailsRequest) throws TokenNotFoundException {
        AddTaxDetailsResponse addTaxDetailsResponse;
        String contextPath = JTBContextPath.ADD_TAX_DETAILS;
        HttpEntity<AddTaxDetailsRequest> httpEntity = new HttpEntity<>(addTaxDetailsRequest);
        Optional<AddTaxDetailsResponse> optionalAddTaxDetailsResponse = Optional.ofNullable(restTemplate.exchange(jtbHostURL.concat(contextPath), HttpMethod.POST, httpEntity, AddTaxDetailsResponse.class, getToken()).getBody());
        if(optionalAddTaxDetailsResponse.isPresent()) {
            addTaxDetailsResponse = optionalAddTaxDetailsResponse.get();
        } else {
            throw new HttpClientErrorException(HttpStatus.NO_CONTENT, "Error in Processing Request To Add Tax Details");
        }
        return addTaxDetailsResponse;
    }

    public AddAssetDetailsResponse addAssetDetails(AddAssetDetailsRequest addAssetDetailsRequest) throws TokenNotFoundException {
        AddAssetDetailsResponse addAssetDetailsResponse;
        String contextPath = JTBContextPath.ADD_ASSET_DETAILS;
        HttpEntity<AddAssetDetailsRequest> httpEntity = new HttpEntity<>(addAssetDetailsRequest);
        Optional<AddAssetDetailsResponse> optionalAddAssetDetailsResponse = Optional.ofNullable(restTemplate.exchange(jtbHostURL.concat(contextPath), HttpMethod.POST, httpEntity, AddAssetDetailsResponse.class, getToken()).getBody());
        if(optionalAddAssetDetailsResponse.isPresent()) {
            addAssetDetailsResponse = optionalAddAssetDetailsResponse.get();
        } else {
            throw new HttpClientErrorException(HttpStatus.NO_CONTENT, "Error in Processing Request To Add Asset Details");
        }
        return addAssetDetailsResponse;
    }

}
