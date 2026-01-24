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
            String searchPattern = criteria.getQuery().toLowerCase().trim() + "%";
            predicates.add(cb.like(cb.lower(root.get("name")), searchPattern));
        }


        if (criteria.getProductType() != null && !criteria.getProductType().isEmpty()) {
            for (String type : criteria.getProductType()) {
                if (!StringUtils.hasText(type)) continue;


                Subquery<Long> subquery = query.subquery(Long.class);
                Root<PlantProtectionProduct> subRoot = subquery.from(PlantProtectionProduct.class);
                Join<PlantProtectionProduct, ProductType> subJoin = subRoot.join("productTypes");

                subquery.select(subRoot.get("id"));
                subquery.where(
                        cb.equal(subRoot.get("id"), root.get("id")),
                        cb.equal(cb.lower(subJoin.get("name")), type.toLowerCase().trim())
                );
                predicates.add(cb.exists(subquery));
            }
        }


        if (criteria.getActiveSubstance() != null && !criteria.getActiveSubstance().isEmpty()) {
            for (String substance : criteria.getActiveSubstance()) {
                if (!StringUtils.hasText(substance)) continue;

                Subquery<Long> subquery = query.subquery(Long.class);
                Root<PlantProtectionProduct> subRoot = subquery.from(PlantProtectionProduct.class);
                Join<PlantProtectionProduct, ProductActiveSubstance> subAsLink = subRoot.join("activeSubstances");
                Join<ProductActiveSubstance, ActiveSubstance> subAs = subAsLink.join("activeSubstance");

                subquery.select(subRoot.get("id"));
                subquery.where(
                        cb.equal(subRoot.get("id"), root.get("id")),
                        cb.like(cb.lower(subAs.get("name")), "%" + substance.toLowerCase().trim() + "%")
                );
                predicates.add(cb.exists(subquery));
            }
        }


        if (criteria.getCropName() != null && !criteria.getCropName().isEmpty()) {
            for (String crop : criteria.getCropName()) {
                if (!StringUtils.hasText(crop)) continue;

                Subquery<Long> subquery = query.subquery(Long.class);
                Root<PlantProtectionProduct> subRoot = subquery.from(PlantProtectionProduct.class);
                Join<PlantProtectionProduct, ProductUsage> subUsage = subRoot.join("usages");
                Join<ProductUsage, Crop> subCrop = subUsage.join("crop");

                subquery.select(subRoot.get("id"));
                subquery.where(
                        cb.equal(subRoot.get("id"), root.get("id")),
                        cb.like(cb.lower(subCrop.get("name")), "%" + crop.toLowerCase().trim() + "%")
                );
                predicates.add(cb.exists(subquery));
            }
        }


        if (criteria.getPestName() != null && !criteria.getPestName().isEmpty()) {
            for (String pest : criteria.getPestName()) {
                if (!StringUtils.hasText(pest)) continue;

                Subquery<Long> subquery = query.subquery(Long.class);
                Root<PlantProtectionProduct> subRoot = subquery.from(PlantProtectionProduct.class);
                Join<PlantProtectionProduct, ProductUsage> subUsage = subRoot.join("usages");
                Join<ProductUsage, Pest> subPest = subUsage.join("pest");

                subquery.select(subRoot.get("id"));
                subquery.where(
                        cb.equal(subRoot.get("id"), root.get("id")),
                        cb.like(cb.lower(subPest.get("name")), "%" + pest.toLowerCase().trim() + "%")
                );
                predicates.add(cb.exists(subquery));
            }
        }

        predicates.add(cb.isTrue(root.get("isActive")));

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}