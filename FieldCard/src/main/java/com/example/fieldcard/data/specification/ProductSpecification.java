package com.example.fieldcard.data.specification;

import com.example.fieldcard.dto.request.SearchCriteriaDto;
import com.example.fieldcard.data.entity.*;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;


public class ProductSpecification implements Specification<PlantProtectionProduct> {

    private final SearchCriteriaDto criteria;

    public ProductSpecification(SearchCriteriaDto criteria) {
        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(Root<PlantProtectionProduct> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        query.distinct(true);

        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.hasText(criteria.getQuery())) {
            String searchPattern = criteria.getQuery().toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(root.get("name")), searchPattern));
        }

        if (StringUtils.hasText(criteria.getProductType())) {
            Join<PlantProtectionProduct, ProductType> typeJoin = root.join("productTypes", JoinType.LEFT);
            predicates.add(cb.equal(cb.lower(typeJoin.get("name")), criteria.getProductType().toLowerCase()));
        }

        if (StringUtils.hasText(criteria.getActiveSubstance())) {
            Join<PlantProtectionProduct, ProductActiveSubstance> asLinkJoin = root.join("activeSubstances", JoinType.LEFT);
            Join<ProductActiveSubstance, ActiveSubstance> asJoin = asLinkJoin.join("activeSubstance", JoinType.LEFT);
            predicates.add(cb.equal(cb.lower(asJoin.get("name")), criteria.getActiveSubstance().toLowerCase()));
        }

        if (StringUtils.hasText(criteria.getCropName())) {
            Join<PlantProtectionProduct, ProductUsage> usageJoin = root.join("usages", JoinType.LEFT);
            Join<ProductUsage, Crop> cropJoin = usageJoin.join("crop", JoinType.LEFT);
            predicates.add(cb.equal(cb.lower(cropJoin.get("name")), criteria.getCropName().toLowerCase()));
        }

        if (StringUtils.hasText(criteria.getPestName())) {
            Join<PlantProtectionProduct, ProductUsage> usageJoin = root.join("usages", JoinType.LEFT);
            Join<ProductUsage, Pest> pestJoin = usageJoin.join("pest", JoinType.LEFT);
            predicates.add(cb.equal(cb.lower(pestJoin.get("name")), criteria.getPestName().toLowerCase()));
        }

        predicates.add(cb.isTrue(root.get("isActive")));

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}