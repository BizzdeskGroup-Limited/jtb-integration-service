package com.bizzdesk.jtb.integration.helpers;

public class JTBContextPath {

    public static final String GENERATE_TOKEN = "/api/GetTokenID";
    public static final String GET_INDIVIDUAL_TAX_PAYERS = "/api/SBIR/Individual?tokenid={tokenID}";
    public static final String GET_NON_INDIVIDUAL_TAX_PAYERS = "/api/SBIR/NonIndividual?tokenid={tokenID}";
    public static final String GET_PAGED_INDIVIDUAL_TAX_PAYERS = "/api/SBIR/IndividualPaged?tokenid={tokenID}";
    public static final String GET_PAGED_NON_INDIVIDUAL_TAX_PAYERS = "/api/SBIR/NonIndividualPaged?tokenid={tokenID}";
    public static final String ADD_TAX_DETAILS = "/api/SBIR/AddTaxRecord?tokenid={tokenID}";
    public static final String ADD_ASSET_DETAILS = "/api/SBIR/AddAssetDetails?tokenid={tokenID}";

}
