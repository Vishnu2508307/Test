package com.smartsparrow.graphql.type.mutation;

import java.util.Objects;

import com.smartsparrow.competency.data.ItemAssociation;

import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;

@GraphQLType(description = "Payload for mutation to create an association between items")
public class ItemAssociationCreatePayload {

    private ItemAssociation association;

    @GraphQLQuery (description = "The created association")
    public ItemAssociation getAssociation() {
        return association;
    }

    public ItemAssociationCreatePayload setAssociation(ItemAssociation association) {
        this.association = association;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemAssociationCreatePayload that = (ItemAssociationCreatePayload) o;
        return Objects.equals(association, that.association);
    }

    @Override
    public int hashCode() {
        return Objects.hash(association);
    }
}
