package com.bizzdesk.jtb.integration.service;

import com.bizzdesk.jtb.integration.entity.redis.UtilsHash;
import com.bizzdesk.jtb.integration.helpers.JTBContextPath;
import com.bizzdesk.jtb.integration.helpers.ResponseCodes;
import com.bizzdesk.jtb.integration.kafka.interfaces.IndividualTaxPayersChannel;
import com.bizzdesk.jtb.integration.kafka.interfaces.IndividualTaxPayersPagedChannel;
import com.bizzdesk.jtb.integration.kafka.interfaces.NonIndividualTaxPayersChannel;
import com.bizzdesk.jtb.integration.kafka.interfaces.NonIndividualTaxPayersPagedChannel;
import com.bizzdesk.jtb.integration.repository.UtilsHashRepository;
import com.gotax.framework.library.error.handling.GoTaxException;
import com.gotax.framework.library.error.handling.TokenNotFoundException;
import com.gotax.framework.library.sbirs.helpers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
    @Value("${number.of.days}")
    private long daysDifference;
    private NonIndividualTaxPayersPagedChannel nonIndividualTaxPayersPagedChannel;
    private IndividualTaxPayersChannel individualTaxPayersChannel;
    private IndividualTaxPayersPagedChannel individualTaxPayersPagedChannel;
    private NonIndividualTaxPayersChannel nonIndividualTaxPayersChannel;

    private UtilsHashRepository utilsHashRepository;
    private RestTemplate restTemplate;

    @Autowired
    public SBIRService(UtilsHashRepository utilsHashRepository, RestTemplate restTemplate,
                       NonIndividualTaxPayersPagedChannel nonIndividualTaxPayersPagedChannel,
                       IndividualTaxPayersChannel individualTaxPayersChannel,
                       IndividualTaxPayersPagedChannel individualTaxPayersPagedChannel,
                       NonIndividualTaxPayersChannel nonIndividualTaxPayersChannel) {
        this.utilsHashRepository = utilsHashRepository;
        this.restTemplate = restTemplate;
        this.nonIndividualTaxPayersPagedChannel = nonIndividualTaxPayersPagedChannel;
        this.individualTaxPayersChannel = individualTaxPayersChannel;
        this.individualTaxPayersPagedChannel = individualTaxPayersPagedChannel;
        this.nonIndividualTaxPayersChannel = nonIndividualTaxPayersChannel;
    }

    @PostConstruct
    @Scheduled(fixedRate = 3600000)
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
            token = utilsHashOptional.get().getUtilValue();
        }
        return token;
    }

    @Scheduled(fixedRate = 604800000)
    public void getIndividualTaxPayers() throws TokenNotFoundException {
        DateRangeGenerator dateRangeGenerator = this.dateDifferenceCalculator();
        GetIndividualTaxPayersRequest getIndividualTaxPayersRequest = new GetIndividualTaxPayersRequest().setTodate(dateRangeGenerator.getTodate())
                .setFromdate(dateRangeGenerator.getFromdate());
        GetIndividualTaxPayersResponse getIndividualTaxPayersResponse;
        String contextPath = JTBContextPath.GET_INDIVIDUAL_TAX_PAYERS;
        logger.info(contextPath);
        logger.info(getIndividualTaxPayersRequest.toString());
        HttpEntity<GetIndividualTaxPayersRequest> httpEntity = new HttpEntity<>(getIndividualTaxPayersRequest);
        logger.info(getToken());
        Optional<GetIndividualTaxPayersResponse> optionalGetIndividualTaxPayersResponse = Optional.ofNullable(restTemplate.exchange(jtbHostURL.concat(contextPath), HttpMethod.POST, httpEntity, new ParameterizedTypeReference<GetIndividualTaxPayersResponse>() {
        }, getToken()).getBody());

        if(optionalGetIndividualTaxPayersResponse.isPresent()) {
            getIndividualTaxPayersResponse = optionalGetIndividualTaxPayersResponse.get();
            String responseCode = getIndividualTaxPayersResponse.getResponseCode();
            if(responseCode.equals(ResponseCodes.SUCCESSFUL)) {
                List<IndividualTaxPayerInformation> individualTaxPayerInformationList = getIndividualTaxPayersResponse.getTaxpayerList();
                if(!individualTaxPayerInformationList.isEmpty()) {
                    individualTaxPayersChannel.output().send(MessageBuilder.withPayload(individualTaxPayerInformationList).build());
                }
            }
        } else {
            throw new HttpClientErrorException(HttpStatus.NO_CONTENT, "Error in Processing Request To Get Individual Tax Payers");
        }
    }

    @Scheduled(fixedRate = 604800000)
    public void getNonIndividualTaxPayers() throws TokenNotFoundException {
        DateRangeGenerator dateRangeGenerator = this.dateDifferenceCalculator();
        GetNonIndividualTaxPayersRequest getNonIndividualTaxPayersRequest = new GetNonIndividualTaxPayersRequest().setFromdate(dateRangeGenerator.getFromdate())
                .setTodate(dateRangeGenerator.getTodate());
        GetNonIndividualTaxPayersResponse getNonIndividualTaxPayersResponse;
        String contextPath = JTBContextPath.GET_NON_INDIVIDUAL_TAX_PAYERS;
        logger.info(contextPath);
        HttpEntity<GetNonIndividualTaxPayersRequest> httpEntity = new HttpEntity<>(getNonIndividualTaxPayersRequest);
        Optional<GetNonIndividualTaxPayersResponse> optionalGetNonIndividualTaxPayersResponse = Optional.ofNullable(restTemplate.exchange(jtbHostURL.concat(contextPath), HttpMethod.POST, httpEntity, new ParameterizedTypeReference<GetNonIndividualTaxPayersResponse>() {}, getToken()).getBody());

        if(optionalGetNonIndividualTaxPayersResponse.isPresent()) {
            getNonIndividualTaxPayersResponse = optionalGetNonIndividualTaxPayersResponse.get();
            String responseCode = getNonIndividualTaxPayersResponse.getResponseCode();
            if(responseCode.equals(ResponseCodes.SUCCESSFUL)) {
                List<NonIndividualTaxPayerInformation> nonIndividualTaxPayerInformationList = getNonIndividualTaxPayersResponse.getTaxpayerList();
                if(!nonIndividualTaxPayerInformationList.isEmpty()) {
                    nonIndividualTaxPayersChannel.output().send(MessageBuilder.withPayload(nonIndividualTaxPayerInformationList).build());
                }
            }
        } else {
            throw new HttpClientErrorException(HttpStatus.NO_CONTENT, "Error in Processing Request To Get Non Individual Tax Payers");
        }
    }

    @Scheduled(fixedRate = 604800000)
    public void getNonIndividualTaxPayersPagedResponse() throws TokenNotFoundException {
        DateRangeGenerator dateRangeGenerator = this.dateDifferenceCalculator();
        GetNonIndividualTaxPayersPagedRequest getNonIndividualTaxPayersPagedRequest = new GetNonIndividualTaxPayersPagedRequest().setFromdate(dateRangeGenerator.getFromdate())
                .setTodate(dateRangeGenerator.getTodate())
                .setPage_number("1");
        GetNonIndividualTaxPayersPagedResponse getNonIndividualTaxPayersPagedResponse;
        String contextPath = JTBContextPath.GET_PAGED_NON_INDIVIDUAL_TAX_PAYERS;
        HttpEntity<GetNonIndividualTaxPayersPagedRequest> httpEntity = new HttpEntity<>(getNonIndividualTaxPayersPagedRequest);
        Optional<GetNonIndividualTaxPayersPagedResponse> optionalGetNonIndividualTaxPayersPagedResponse = Optional.ofNullable(restTemplate.exchange(jtbHostURL.concat(contextPath), HttpMethod.POST, httpEntity, new ParameterizedTypeReference<GetNonIndividualTaxPayersPagedResponse>() {}, getToken()).getBody());
        if(optionalGetNonIndividualTaxPayersPagedResponse.isPresent()) {
            getNonIndividualTaxPayersPagedResponse = optionalGetNonIndividualTaxPayersPagedResponse.get();
            String responseCode = getNonIndividualTaxPayersPagedResponse.getResponseCode();
            if(responseCode.equals(ResponseCodes.SUCCESSFUL)) {
                List<NonIndividualTaxPayerPagedInformation> nonIndividualTaxPayerPagedInformationList = getNonIndividualTaxPayersPagedResponse.getTaxpayerList();
                if(!nonIndividualTaxPayerPagedInformationList.isEmpty()) {
                    nonIndividualTaxPayersPagedChannel.output().send(MessageBuilder.withPayload(nonIndividualTaxPayerPagedInformationList).build());
                }
            }
        } else {
            throw new HttpClientErrorException(HttpStatus.NO_CONTENT, "Error in Processing Request To Get Non Individual Tax Payers With Paged Response");
        }
    }

    @Scheduled(fixedRate = 604800000)
    public void getIndividualTaxPayersPagedResponse() throws TokenNotFoundException {
        DateRangeGenerator dateRangeGenerator = this.dateDifferenceCalculator();
        GetIndividualTaxPayersPagedRequest getIndividualTaxPayersPagedRequest = new GetIndividualTaxPayersPagedRequest().setFromdate(dateRangeGenerator.getFromdate())
                .setTodate(dateRangeGenerator.getTodate())
                .setPage_number("1");
        GetIndividualTaxPayersPagedResponse getIndividualTaxPayersPagedResponse;
        String contextPath = JTBContextPath.GET_PAGED_INDIVIDUAL_TAX_PAYERS;
        HttpEntity<GetIndividualTaxPayersPagedRequest> httpEntity = new HttpEntity<>(getIndividualTaxPayersPagedRequest);
        Optional<GetIndividualTaxPayersPagedResponse> optionalGetIndividualTaxPayersPagedResponse = Optional.ofNullable(restTemplate.exchange(jtbHostURL.concat(contextPath), HttpMethod.POST, httpEntity, new ParameterizedTypeReference<GetIndividualTaxPayersPagedResponse>() {}, getToken()).getBody());
        if(optionalGetIndividualTaxPayersPagedResponse.isPresent()) {
            getIndividualTaxPayersPagedResponse = optionalGetIndividualTaxPayersPagedResponse.get();
            String responseCode = getIndividualTaxPayersPagedResponse.getResponseCode();
            if(responseCode.equals(ResponseCodes.SUCCESSFUL)) {
                List<IndividualTaxPayerPagedInformation> individualTaxPayerPagedInformationList = getIndividualTaxPayersPagedResponse.getTaxpayerList();
                if(!individualTaxPayerPagedInformationList.isEmpty()) {
                    individualTaxPayersPagedChannel.output().send(MessageBuilder.withPayload(individualTaxPayerPagedInformationList).build());
                }
            }
        } else {
            throw new HttpClientErrorException(HttpStatus.NO_CONTENT, "Error in Processing Request To Get Individual Tax Payers with Paged Response");
        }
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

    private DateRangeGenerator dateDifferenceCalculator() {

        LocalDate todayLocalDate = LocalDate.now().minusDays(1);
        String formattedTodayLocalDate = todayLocalDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        LocalDate todayMinus7DaysLocalDate = todayLocalDate.minus(daysDifference, ChronoUnit.DAYS);
        String formattedTodayMinus7DaysLocalDate = todayMinus7DaysLocalDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        return new DateRangeGenerator().setFromdate(formattedTodayMinus7DaysLocalDate)
                .setTodate(formattedTodayLocalDate);
    }

}
