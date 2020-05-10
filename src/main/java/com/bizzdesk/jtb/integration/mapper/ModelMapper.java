package com.bizzdesk.jtb.integration.mapper;

import com.gotax.framework.library.entity.helpers.AssetResponse;
import com.gotax.framework.library.sbirs.helpers.AddAssetDetailsRequest;
import com.gotax.framework.library.sbirs.helpers.AddAssetDetailsResponse;

import java.text.SimpleDateFormat;

public class ModelMapper {

    public static AddAssetDetailsRequest mapAssetResponseToAddAssetDetailRequest(AssetResponse assetResponse) {
        return new AddAssetDetailsRequest().setAsset_type(assetResponse.getAssetTypeResponse().getAssetType())
                .setAsset_value(assetResponse.getAssetValue())
                .setDate_acquired(new SimpleDateFormat("dd-MM-yyyy").format(assetResponse.getDateOfPurchase()))
                .setDescription(assetResponse.getDescription())
                .setLocation(assetResponse.getLocationResponse().getLocation())
                .setTin(assetResponse.getTin());
    }

    public static AssetResponse mapAddAssetDetailResponseToAssetResponse(AssetResponse assetResponse, AddAssetDetailsResponse addAssetDetailsResponse) {
        return assetResponse.setJtbResponse(addAssetDetailsResponse.getResponseCode())
                .setJtbResponseDescription(addAssetDetailsResponse.getResponseDescription());
    }
}
