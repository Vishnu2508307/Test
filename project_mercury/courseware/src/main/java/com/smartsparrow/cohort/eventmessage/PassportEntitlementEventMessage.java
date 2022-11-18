package com.smartsparrow.cohort.eventmessage;

import java.util.Objects;

public class PassportEntitlementEventMessage {

    private final String pearsonUid;
    private final String productURN;
    private Boolean hasAccess;


    public PassportEntitlementEventMessage(String pearsonUid, String productURN) {
        this.pearsonUid = pearsonUid;
        this.productURN = productURN;
        hasAccess = false;
    }

    public PassportEntitlementEventMessage grantAccess() {
        hasAccess = true;
        return this;
    }

    public String getPearsonUid() {
        return pearsonUid;
    }

    /**
     * Gets the product urn which has the following format
     * <br> x-urn:revel:e6933083-95ec-4cb7-ae6b-ddd85e013826
     * @return the productURN
     */
    public String getProductURN() {
        return productURN;
    }

    public Boolean getHasAccess() {
        return hasAccess;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PassportEntitlementEventMessage that = (PassportEntitlementEventMessage) o;
        return Objects.equals(pearsonUid, that.pearsonUid) &&
                Objects.equals(productURN, that.productURN) &&
                Objects.equals(hasAccess, that.hasAccess);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pearsonUid, productURN, hasAccess);
    }

    @Override
    public String toString() {
        return "PassportEntitlementEventMessage{" +
                "pearsonUid='" + pearsonUid + '\'' +
                ", productId='" + productURN + '\'' +
                ", hasAccess=" + hasAccess +
                '}';
    }
}
