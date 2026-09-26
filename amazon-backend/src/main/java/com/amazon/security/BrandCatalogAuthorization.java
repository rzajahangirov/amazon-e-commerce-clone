package com.amazon.security;

import com.amazon.enums.BrandRole;
import com.amazon.enums.BrandStatus;
import com.amazon.repository.BrandMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component("brandCatalogAuthorization")
@RequiredArgsConstructor
public class BrandCatalogAuthorization {
    private static final Set<BrandRole> CATALOG_ADMIN_ROLES = EnumSet.of(
            BrandRole.BRAND_OWNER, BrandRole.BRAND_SUPER_ADMIN, BrandRole.BRAND_ADMIN);

    private final BrandMemberRepository brandMemberRepository;

    public boolean canManage(String email) {
        if (email == null) return false;
        return brandMemberRepository.findFirstByUserEmail(email)
                .map(member -> CATALOG_ADMIN_ROLES.contains(member.getBrandRole())
                        && member.getBrand() != null
                        && member.getBrand().getStatus() == BrandStatus.ACTIVE
                        && member.getBrand().getDeletedAt() == null)
                .orElse(false);
    }
}
