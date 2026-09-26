package com.amazon.specification;

import com.amazon.dtos.product.request.ProductSearchRequestDto;
import com.amazon.entity.Brand;
import com.amazon.entity.Category;
import com.amazon.entity.Product;
import com.amazon.entity.ProductListing;
import com.amazon.enums.BrandStatus;
import com.amazon.enums.ListingStatus;
import com.amazon.enums.ProductStatus;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * JPA Specification builder for Amazon-style multi-criteria product search and discovery.
 * Prevents N+1 query leaks and handles complex filtering combinations.
 */
public class ProductSpecification {

    public static Specification<Product> buildSearchSpecification(
            ProductSearchRequestDto filter,
            Set<UUID> resolvedCategoryIds) {

        return (Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Public catalog baseline: ACTIVE product, APPROVED category, ACTIVE brand
            predicates.add(cb.equal(root.get("status"), ProductStatus.ACTIVE));

            Join<Product, Category> categoryJoin = root.join("category", JoinType.INNER);
            predicates.add(cb.isTrue(categoryJoin.get("isApproved")));

            Join<Product, Brand> brandJoin = root.join("brand", JoinType.LEFT);
            predicates.add(cb.or(
                    cb.isNull(root.get("brand")),
                    cb.equal(brandJoin.get("status"), BrandStatus.ACTIVE)
            ));

            // 2. Keyword query across title, description, brand name, and category name
            if (filter.getQuery() != null && !filter.getQuery().trim().isEmpty()) {
                String pattern = "%" + filter.getQuery().trim().toLowerCase() + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("title")), pattern);
                Predicate descLike = cb.like(cb.lower(cb.coalesce(root.get("description"), "")), pattern);
                Predicate brandLike = cb.like(cb.lower(cb.coalesce(brandJoin.get("name"), "")), pattern);
                Predicate catLike = cb.like(cb.lower(categoryJoin.get("name")), pattern);

                predicates.add(cb.or(titleLike, descLike, brandLike, catLike));
            }

            // 3. Category filter (including resolved child categories)
            if (resolvedCategoryIds != null) {
                if (resolvedCategoryIds.isEmpty()) {
                    predicates.add(cb.disjunction());
                } else {
                    predicates.add(root.get("category").get("id").in(resolvedCategoryIds));
                }
            }

            // 4. Brand filter
            if (filter.getBrandId() != null) {
                predicates.add(cb.equal(root.get("brand").get("id"), filter.getBrandId()));
            }

            // 5. Rating filter: averageRating >= minRating
            if (filter.getMinRating() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        cb.coalesce(root.get("averageRating"), 0.0),
                        filter.getMinRating()
                ));
            }

            // 6. Price range filter: matches basePrice or active listing prices
            if (filter.getMinPrice() != null) {
                Subquery<Long> minSubquery = query.subquery(Long.class);
                Root<ProductListing> listingRoot = minSubquery.from(ProductListing.class);
                minSubquery.select(cb.literal(1L))
                        .where(
                                cb.equal(listingRoot.get("productVariant").get("product"), root),
                                cb.equal(listingRoot.get("status"), ListingStatus.ACTIVE),
                                cb.greaterThanOrEqualTo(listingRoot.get("price"), filter.getMinPrice())
                        );

                predicates.add(cb.or(
                        cb.greaterThanOrEqualTo(root.get("basePrice"), filter.getMinPrice()),
                        cb.exists(minSubquery)
                ));
            }

            if (filter.getMaxPrice() != null) {
                Subquery<Long> maxSubquery = query.subquery(Long.class);
                Root<ProductListing> listingRoot = maxSubquery.from(ProductListing.class);
                maxSubquery.select(cb.literal(1L))
                        .where(
                                cb.equal(listingRoot.get("productVariant").get("product"), root),
                                cb.equal(listingRoot.get("status"), ListingStatus.ACTIVE),
                                cb.lessThanOrEqualTo(listingRoot.get("price"), filter.getMaxPrice())
                        );

                predicates.add(cb.or(
                        cb.lessThanOrEqualTo(root.get("basePrice"), filter.getMaxPrice()),
                        cb.exists(maxSubquery)
                ));
            }

            if (query != null) {
                query.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
