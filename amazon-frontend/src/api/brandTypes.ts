export type BrandRole =
  | 'BRAND_OWNER'
  | 'BRAND_SUPER_ADMIN'
  | 'BRAND_ADMIN'
  | 'BRAND_SELLER'
  | 'BRAND_MARKETING_MEMBER';

export type GovernanceStatus = 'ACTIVE_LOCKED' | 'DRAFT' | 'UNDER_REVIEW';

export type BrandUpdateRequestStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export type ProductStatus = 'DRAFT' | 'ACTIVE' | 'ARCHIVED' | 'SUPPRESSED';

export interface BrandResponseDto {
  id: string;
  name: string;
  slug: string;
  trademarkRegistrationNumber?: string | null;
  brandCountry?: string | null;
  logoUrl?: string | null;
  aboutText?: string | null;
  status: 'ACTIVE' | 'SUSPENDED' | 'PENDING';
  ownerUserId?: string | null;
  ownerName?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface BrandMemberResponseDto {
  id: string;
  brandId: string;
  brandName: string;
  userId: string;
  userFullName: string;
  userEmail: string;
  department?: string | null;
  brandRole: BrandRole;
  phone?: string | null;
  assignedAt?: string;
}

export interface BrandProfileResponseDto {
  brand: BrandResponseDto;
  teamMembers: BrandMemberResponseDto[];
}

export interface ProductVariantDto {
  id: string;
  productId?: string;
  asin: string;
  variantName: string;
  variantAttributes: Record<string, string>;
  createdAt?: string;
}

export interface ProductResponseDto {
  id: string;
  sellerId?: string | null;
  sellerName?: string | null;
  brandId?: string | null;
  brandName?: string | null;
  categoryId: string;
  categoryName?: string | null;
  title: string;
  masterSku?: string | null;
  governanceStatus?: GovernanceStatus;
  description?: string | null;
  basePrice?: number | null;
  status: ProductStatus;
  variants: ProductVariantDto[];
  averageRating?: number;
  totalReviews?: number;
  totalUnitsSold?: number;
  viewCount?: number;
  mainImageUrl?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface BrandPostResponseDto {
  id: string;
  brandId: string;
  brandName?: string;
  authorUserId?: string;
  authorName?: string;
  imageUrl: string;
  caption: string;
  linkedAsin?: string | null;
  reachCount: number;
  likesCount: number;
  placement?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface BrandUpdateRequestResponseDto {
  id: string;
  brandId: string;
  brandName?: string;
  requestedByUserId?: string;
  requestedByUserEmail?: string;
  proposedBrandName?: string | null;
  proposedLogoUrl?: string | null;
  proposedAboutText?: string | null;
  proposedTrademarkNo?: string | null;
  officialLegalJustification?: string | null;
  status: BrandUpdateRequestStatus;
  rejectionReason?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface TopSellingProductDto {
  productTitle: string;
  asin: string;
  totalUnitsSold: number;
  totalRevenue: number;
}

export interface BrandAnalyticsResponseDto {
  totalBrandProducts: number;
  totalBrandVariants: number;
  totalActiveListings: number;
  totalTeamMembers: number;
  totalBrandPosts: number;
  totalBrandRevenue: number;
  totalUnitsSold: number;
  unauthorizedSellerAlertsCount: number;
  pendingCatalogCount: number;
  topSellingProducts: TopSellingProductDto[];
}

export interface CategoryProposalResponseDto {
  id: string;
  parentId?: string | null;
  name: string;
  slug: string;
  level: number;
  isApproved: boolean;
  commercialJustification?: string | null;
  rejectionReason?: string | null;
  createdAt?: string;
}

// Request DTOs
export interface CreateBrandUpdateRequestDto {
  proposedBrandName?: string;
  proposedLogoUrl?: string;
  proposedAboutText?: string;
  proposedTrademarkNo?: string;
  officialLegalJustification?: string;
}

export interface AddBrandMemberRequestDto {
  fullName: string;
  email: string;
  password?: string;
  phone?: string;
  department?: string;
  brandRole: BrandRole;
}

export interface UpdateBrandMemberRoleRequestDto {
  brandRole: BrandRole;
}

export interface CreateBrandCatalogProductRequestDto {
  categoryId: string;
  title: string;
  masterSku: string;
  governanceStatus: GovernanceStatus;
  description: string;
}

export interface CreateVariantRequestDto {
  asin: string;
  variantName: string;
  variantAttributes: Record<string, string>;
}

export interface UpdateBrandProductRequestDto {
  title?: string;
  description?: string;
  masterSku?: string;
  governanceStatus?: GovernanceStatus;
}

export interface CreateBrandPostRequestDto {
  imageUrl: string;
  caption: string;
  linkedAsin?: string;
  reachCount?: number;
  likesCount?: number;
}

export interface UpdateBrandPostRequestDto {
  imageUrl?: string;
  caption?: string;
  linkedAsin?: string;
  reachCount?: number;
  likesCount?: number;
}

export interface CreateCategoryRequestDto {
  parentId?: string;
  name: string;
  slug: string;
  level: number;
  commercialJustification?: string;
}
