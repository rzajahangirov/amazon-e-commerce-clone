import { apiRequest } from './client';
import type { PaginationPayload } from './types';
import type {
  AddBrandMemberRequestDto,
  BrandAnalyticsResponseDto,
  BrandMemberResponseDto,
  BrandPostResponseDto,
  BrandProfileResponseDto,
  BrandUpdateRequestResponseDto,
  CategoryProposalResponseDto,
  CreateBrandCatalogProductRequestDto,
  CreateBrandPostRequestDto,
  CreateBrandUpdateRequestDto,
  CreateCategoryRequestDto,
  CreateVariantRequestDto,
  ProductResponseDto,
  ProductVariantDto,
  UpdateBrandMemberRoleRequestDto,
  UpdateBrandPostRequestDto,
  UpdateBrandProductRequestDto,
} from './brandTypes';

// ==========================================
// High-Fidelity Fallback Dataset (Mock Replica)
// ==========================================

export const MOCK_BRAND_PROFILE: BrandProfileResponseDto = {
  brand: {
    id: '2c363a71-6cd3-49c9-bc54-9b188b756ade',
    name: 'Anker Innovations Global',
    slug: 'anker-innovations',
    trademarkRegistrationNumber: 'TM-97412854',
    brandCountry: 'US',
    logoUrl: 'https://upload.wikimedia.org/wikipedia/commons/thumb/c/ca/Sony_logo.svg/320px-Sony_logo.svg.png',
    aboutText:
      'Nexus Innovations Global Technologies Inc. is a premier designer and manufacturer of consumer high-density power systems, smart wearable optics, and zero-latency wireless transmission hardware. Established in 2018 with principal engineering laboratories in Seattle, WA.',
    status: 'ACTIVE',
    ownerUserId: '1df9790f-b399-4d5e-a83f-9c5b967bf8c1',
    ownerName: 'Elena Vance',
    createdAt: '2026-01-10T10:00:00',
    updatedAt: '2026-09-27T10:00:00',
  },
  teamMembers: [
    {
      id: '4e12a843-c665-4c32-a883-e3a3c18ce706',
      brandId: '2c363a71-6cd3-49c9-bc54-9b188b756ade',
      brandName: 'Anker Innovations Global',
      userId: '1df9790f-b399-4d5e-a83f-9c5b967bf8c1',
      userFullName: 'Elena Vance',
      userEmail: 'elena@anker.com',
      department: 'Global Brand Governance',
      brandRole: 'BRAND_OWNER',
      phone: '+1 (555) 438-9210',
      assignedAt: '2026-01-10T10:00:00',
    },
    {
      id: '5f23b954-d776-4e43-b994-f4b4d29df817',
      brandId: '2c363a71-6cd3-49c9-bc54-9b188b756ade',
      brandName: 'Anker Innovations Global',
      userId: '2ea08910-c4aa-5e5f-b940-0d6c078cf9d2',
      userFullName: 'Sarah Jenkins',
      userEmail: 'sarah.j@anker.com',
      department: 'Brand Enforcement & Legal',
      brandRole: 'BRAND_SUPER_ADMIN',
      phone: '+1 (555) 234-8901',
      assignedAt: '2026-02-14T09:30:00',
    },
    {
      id: '6a34c065-e887-5f54-ca05-0e7d189ef928',
      brandId: '2c363a71-6cd3-49c9-bc54-9b188b756ade',
      brandName: 'Anker Innovations Global',
      userId: '3fb19021-d5bb-6f60-ca51-1e7d189da0e3',
      userFullName: 'Marcus Sterling',
      userEmail: 'marcus.s@nexusbrand.com',
      department: 'Catalog & Brand Enforcement',
      brandRole: 'BRAND_ADMIN',
      phone: '+1 (555) 382-9910',
      assignedAt: '2026-03-20T11:15:00',
    },
    {
      id: '7b45d176-f998-6a65-db16-2f8e290fa039',
      brandId: '2c363a71-6cd3-49c9-bc54-9b188b756ade',
      brandName: 'Anker Innovations Global',
      userId: '4gc20132-e6cc-7a71-db62-2f8e290eb1f4',
      userFullName: 'David Chen',
      userEmail: 'david.c@anker.com',
      department: 'E-Commerce Channel Ops',
      brandRole: 'BRAND_SELLER',
      phone: '+1 (555) 678-1234',
      assignedAt: '2026-04-05T14:20:00',
    },
    {
      id: '8c56e287-0aa9-7b76-ec27-3a9f301ab140',
      brandId: '2c363a71-6cd3-49c9-bc54-9b188b756ade',
      brandName: 'Anker Innovations Global',
      userId: '5hd31243-f7dd-8b82-ec73-3a9f301fc2a5',
      userFullName: 'Aisha Patel',
      userEmail: 'aisha.p@anker.com',
      department: 'Brand Marketing & Social Growth',
      brandRole: 'BRAND_MARKETING_MEMBER',
      phone: '+1 (555) 901-4567',
      assignedAt: '2026-05-12T16:45:00',
    },
  ],
};

export const MOCK_BRAND_ANALYTICS: BrandAnalyticsResponseDto = {
  totalBrandProducts: 184,
  totalBrandVariants: 642,
  totalActiveListings: 1289,
  totalTeamMembers: 18,
  totalBrandPosts: 86,
  totalBrandRevenue: 4892430.0,
  totalUnitsSold: 142850,
  unauthorizedSellerAlertsCount: 4,
  pendingCatalogCount: 12,
  topSellingProducts: [
    {
      productTitle: 'ApexPro Noise-Cancelling Wireless Headphones Gen-4',
      asin: 'B09V3HM1LR',
      totalUnitsSold: 34200,
      totalRevenue: 1025000.0,
    },
    {
      productTitle: 'UltraFast GaN 100W USB-C Dual Port Charger',
      asin: 'B08F52C53K',
      totalUnitsSold: 28400,
      totalRevenue: 852000.0,
    },
    {
      productTitle: 'ErgoGlide MagSafe Aluminum Desk Stand',
      asin: 'B0BFDJM75G',
      totalUnitsSold: 21100,
      totalRevenue: 633000.0,
    },
    {
      productTitle: 'PowerMatrix 26800mAh Portable Power Station',
      asin: 'B09L7QPR2V',
      totalUnitsSold: 18900,
      totalRevenue: 567000.0,
    },
    {
      productTitle: 'SoundWave Mini Waterproof Outdoor Speaker',
      asin: 'B07V2Q7K3W',
      totalUnitsSold: 14500,
      totalRevenue: 435000.0,
    },
    {
      productTitle: 'PureShield 9H Tempered Glass Screen Pack 3x',
      asin: 'B09T8P1M4N',
      totalUnitsSold: 12200,
      totalRevenue: 366000.0,
    },
  ],
};

export const MOCK_BRAND_PRODUCTS: ProductResponseDto[] = [
  {
    id: 'prod-001',
    categoryId: 'cat-audio',
    categoryName: 'Audio & Hi-Fi',
    title: 'ApexPro Noise-Cancelling Headphones Wireless',
    masterSku: 'MSKU-NEX-042',
    governanceStatus: 'ACTIVE_LOCKED',
    description: 'High-density wireless audio transmission with 48dB hybrid active noise cancellation.',
    status: 'ACTIVE',
    basePrice: 299.99,
    variants: [
      {
        id: 'var-001',
        asin: 'B09V3HM1LR',
        variantName: 'Midnight Black / 750ml Studio Pro Over-Ear',
        variantAttributes: { Color: 'Midnight Black', Size: 'Studio Pro' },
        createdAt: '2026-09-20T10:00:00',
      },
      {
        id: 'var-002',
        asin: 'B09V3JT5K2',
        variantName: 'Arctic Titanium / 750ml Studio Pro Over-Ear',
        variantAttributes: { Color: 'Arctic Titanium', Size: 'Studio Pro' },
        createdAt: '2026-09-21T11:00:00',
      },
      {
        id: 'var-003',
        asin: 'B09V4NN889',
        variantName: 'Obsidian Gold Limited Edition',
        variantAttributes: { Color: 'Obsidian Gold', Edition: 'Limited' },
        createdAt: '2026-09-22T14:30:00',
      },
    ],
    createdAt: '2026-09-12T10:00:00',
  },
  {
    id: 'prod-002',
    categoryId: 'cat-power',
    categoryName: 'Power Accessories',
    title: 'VoltCore MagSafe 10,000mAh Ultra-Slim Battery Pack',
    masterSku: 'MSKU-NEX-089',
    governanceStatus: 'ACTIVE_LOCKED',
    description: 'Ultra-slim magnetic power bank with 15W Qi2 wireless charging fast-lock.',
    status: 'ACTIVE',
    basePrice: 69.99,
    variants: [
      {
        id: 'var-004',
        asin: 'B08F52C53K',
        variantName: 'Matte Graphite / 10000mAh',
        variantAttributes: { Color: 'Matte Graphite', Capacity: '10000mAh' },
      },
    ],
    createdAt: '2026-09-15T09:00:00',
  },
  {
    id: 'prod-003',
    categoryId: 'cat-stands',
    categoryName: 'Mobile Stands',
    title: 'OmniFlex Pivot Mobile & Tablet Stand Dual Hinge',
    masterSku: 'MSKU-NEX-104',
    governanceStatus: 'UNDER_REVIEW',
    description: 'Aerospace-grade aluminum articulating dual-pivot desk mount.',
    status: 'ACTIVE',
    basePrice: 49.99,
    variants: [
      {
        id: 'var-005',
        asin: 'B0BFDJM75G',
        variantName: 'Space Grey Dual-Hinge',
        variantAttributes: { Finish: 'Space Grey' },
      },
    ],
    createdAt: '2026-09-24T16:20:00',
  },
  {
    id: 'prod-004',
    categoryId: 'cat-power',
    categoryName: 'Power Accessories',
    title: 'AeroStream 140W GaN 4-Port Fast Charging Hub',
    masterSku: 'MSKU-NEX-118',
    governanceStatus: 'DRAFT',
    description: 'Gallium Nitride multi-port high wattage distribution module.',
    status: 'DRAFT',
    basePrice: 119.99,
    variants: [],
    createdAt: '2026-09-26T18:00:00',
  },
];

export const MOCK_BRAND_POSTS: BrandPostResponseDto[] = [
  {
    id: 'post-001',
    brandId: '2c363a71-6cd3-49c9-bc54-9b188b756ade',
    brandName: 'Anker Innovations Global',
    authorUserId: 'user-001',
    authorName: 'Sarah Jenkins',
    imageUrl: 'https://images.unsplash.com/photo-1546435770-a3e426bf472b?auto=format&fit=crop&w=800&q=80',
    caption: 'Immersive silence. The ApexPro Gen-4 in Deep Graphite.',
    linkedAsin: 'B09V3HM1LR',
    reachCount: 14200,
    likesCount: 890,
    placement: 'PDP Live Feed',
    createdAt: '2026-10-24T10:00:00',
  },
  {
    id: 'post-002',
    brandId: '2c363a71-6cd3-49c9-bc54-9b188b756ade',
    brandName: 'Anker Innovations Global',
    authorUserId: 'user-002',
    authorName: 'Aisha Patel',
    imageUrl: 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=800&q=80',
    caption: 'Power everything with 100 watts of pocket-sized efficiency.',
    linkedAsin: 'B08F52C53K',
    reachCount: 9800,
    likesCount: 620,
    placement: 'Brand Store',
    createdAt: '2026-10-22T14:30:00',
  },
  {
    id: 'post-003',
    brandId: '2c363a71-6cd3-49c9-bc54-9b188b756ade',
    brandName: 'Anker Innovations Global',
    authorUserId: 'user-003',
    authorName: 'Carlos Mendez',
    imageUrl: 'https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?auto=format&fit=crop&w=800&q=80',
    caption: 'Minimalist ergonomics designed for creative professionals.',
    linkedAsin: 'B0BFDJM75G',
    reachCount: 18100,
    likesCount: 1100,
    placement: 'Follow Feed',
    createdAt: '2026-10-19T09:15:00',
  },
  {
    id: 'post-004',
    brandId: '2c363a71-6cd3-49c9-bc54-9b188b756ade',
    brandName: 'Anker Innovations Global',
    authorUserId: 'user-001',
    authorName: 'Sarah Jenkins',
    imageUrl: 'https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=800&q=80',
    caption: 'Off-grid adventure without sacrificing high-power electronics.',
    linkedAsin: 'B09L7QPR2V',
    reachCount: 22500,
    likesCount: 1800,
    placement: 'PDP Live Feed',
    createdAt: '2026-10-15T18:40:00',
  },
  {
    id: 'post-005',
    brandId: '2c363a71-6cd3-49c9-bc54-9b188b756ade',
    brandName: 'Anker Innovations Global',
    authorUserId: 'user-002',
    authorName: 'Aisha Patel',
    imageUrl: 'https://images.unsplash.com/photo-1545454675-3531b543be5d?auto=format&fit=crop&w=800&q=80',
    caption: 'IPX7 rugged durability meets audiophile precision.',
    linkedAsin: 'B07V2Q7K3W',
    reachCount: 11400,
    likesCount: 740,
    placement: 'Brand Store',
    createdAt: '2026-10-10T12:00:00',
  },
  {
    id: 'post-006',
    brandId: '2c363a71-6cd3-49c9-bc54-9b188b756ade',
    brandName: 'Anker Innovations Global',
    authorUserId: 'user-004',
    authorName: 'Marcus Sterling',
    imageUrl: 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=800&q=80',
    caption: 'Military standard impact resistance without touch latency.',
    linkedAsin: 'B09T8P1M4N',
    reachCount: 8300,
    likesCount: 510,
    placement: 'PDP Live Feed',
    createdAt: '2026-10-05T15:20:00',
  },
];

export const MOCK_CATEGORY_PROPOSALS: CategoryProposalResponseDto[] = [
  {
    id: 'CP-2024-104',
    name: 'GaN High-Output Power Stations',
    slug: 'consumer-electronics/gan-high-output-power-stations',
    level: 2,
    isApproved: false,
    commercialJustification:
      'Current "External Battery Packs" node lacks technical classification for 100W+ Gallium Nitride devices. Rapid consumer demand requires dedicated classification.',
    createdAt: '2026-10-29T10:00:00',
  },
  {
    id: 'CP-2024-098',
    name: 'Audiophile Studio Planar Magnetic Headphones',
    slug: 'audio-hi-fi/planar-magnetic-studio-headphones',
    level: 2,
    isApproved: true,
    commercialJustification:
      'Planar driver transducers occupy an entirely different harmonic impedance tier than dynamic coil headphones.',
    createdAt: '2026-10-14T11:20:00',
  },
  {
    id: 'CP-2024-082',
    name: 'MagSafe Magnetic Multi-Device Wireless Docks',
    slug: 'cell-phones-accessories/magsafe-multi-docks',
    level: 2,
    isApproved: true,
    commercialJustification:
      'Qi2 standard alignment and 15W Qi2 verified module differentiation. Customers actively seek multi-device charging taxonomy.',
    createdAt: '2026-09-20T14:10:00',
  },
  {
    id: 'CP-2024-065',
    name: 'Ultra-Durable Ceramic Screen Armor',
    slug: 'screen-protectors/ceramic-shield-armor',
    level: 2,
    isApproved: false,
    commercialJustification:
      'Proprietary sintered nanocrystal matrix requires its own taxonomy separate from tempered glass.',
    createdAt: '2026-08-04T16:00:00',
  },
  {
    id: 'CP-2024-041',
    name: 'Smart Active Stylus with Haptic Feedback',
    slug: 'input-devices/haptic-active-stylus-pens',
    level: 2,
    isApproved: true,
    commercialJustification:
      'Linear resonant actuator integrated styluses require dual search facets for palm rejection and latency specs.',
    createdAt: '2026-07-12T09:45:00',
  },
];

export const MOCK_UPDATE_REQUESTS: BrandUpdateRequestResponseDto[] = [
  {
    id: 'CR-2024-8819',
    brandId: '2c363a71-6cd3-49c9-bc54-9b188b756ade',
    proposedBrandName: 'Nexus Innovations Global Technologies Inc.',
    proposedTrademarkNo: 'USPTO Reg #97412854 (Amended Serial #)',
    officialLegalJustification: 'Corporate Reincorporation / Legal Name Change and International Trademark Amendment.',
    status: 'PENDING',
    createdAt: '2026-10-28T14:00:00',
  },
  {
    id: 'CR-2023-4109',
    brandId: '2c363a71-6cd3-49c9-bc54-9b188b756ade',
    proposedBrandName: 'Anker Innovations Global',
    proposedTrademarkNo: 'USPTO Reg #97412854',
    officialLegalJustification: 'Added Class 28 to USPTO Record: Amended goods for interactive hardware line.',
    status: 'APPROVED',
    createdAt: '2025-11-12T10:00:00',
  },
  {
    id: 'CR-2022-1988',
    brandId: '2c363a71-6cd3-49c9-bc54-9b188b756ade',
    proposedBrandName: 'Anker Innovations',
    proposedTrademarkNo: 'USPTO Reg #97412854',
    officialLegalJustification: 'Primary Trademark Specimen Update: Replaced product packaging visual specimens.',
    status: 'APPROVED',
    createdAt: '2024-08-04T09:30:00',
  },
];

// In-memory state caches to allow fluid client interaction in fallback mode
let memoryProducts: ProductResponseDto[] = [...MOCK_BRAND_PRODUCTS];
let memoryPosts: BrandPostResponseDto[] = [...MOCK_BRAND_POSTS];
let memoryMembers: BrandMemberResponseDto[] = [...MOCK_BRAND_PROFILE.teamMembers];
let memoryProposals: CategoryProposalResponseDto[] = [...MOCK_CATEGORY_PROPOSALS];
let memoryUpdateRequests: BrandUpdateRequestResponseDto[] = [...MOCK_UPDATE_REQUESTS];

// ==========================================
// Brand Dashboard API Service
// ==========================================

export const brandApi = {
  // 1. Profile & Team
  async getBrandProfile(): Promise<BrandProfileResponseDto> {
    try {
      const resp = await apiRequest<BrandProfileResponseDto>('/brand-dashboard/profile', {
        method: 'GET',
      });
      if (resp.data?.brand) {
        return resp.data;
      }
      return MOCK_BRAND_PROFILE;
    } catch (err) {
      console.warn('Backend unavailable, using mock brand profile:', err);
      return {
        ...MOCK_BRAND_PROFILE,
        teamMembers: memoryMembers,
      };
    }
  },

  async submitUpdateRequest(
    dto: CreateBrandUpdateRequestDto,
  ): Promise<BrandUpdateRequestResponseDto> {
    try {
      const resp = await apiRequest<BrandUpdateRequestResponseDto>(
        '/brand-dashboard/update-request',
        {
          method: 'POST',
          body: JSON.stringify(dto),
        },
      );
      if (resp.data) {
        memoryUpdateRequests.unshift(resp.data);
        return resp.data;
      }
    } catch (err) {
      console.warn('Backend update request failed, applying optimistic update:', err);
    }

    const created: BrandUpdateRequestResponseDto = {
      id: `CR-2024-${Math.floor(1000 + Math.random() * 9000)}`,
      brandId: MOCK_BRAND_PROFILE.brand.id,
      proposedBrandName: dto.proposedBrandName || null,
      proposedLogoUrl: dto.proposedLogoUrl || null,
      proposedAboutText: dto.proposedAboutText || null,
      proposedTrademarkNo: dto.proposedTrademarkNo || null,
      officialLegalJustification: dto.officialLegalJustification || null,
      status: 'PENDING',
      createdAt: new Date().toISOString(),
    };
    memoryUpdateRequests.unshift(created);
    return created;
  },

  async getUpdateRequests(): Promise<BrandUpdateRequestResponseDto[]> {
    return memoryUpdateRequests;
  },

  // 2. Members & Roles
  async getBrandMembers(): Promise<BrandMemberResponseDto[]> {
    try {
      const resp = await apiRequest<BrandMemberResponseDto[]>('/brand-dashboard/members', {
        method: 'GET',
      });
      if (resp.data && resp.data.length > 0) {
        memoryMembers = resp.data;
        return resp.data;
      }
    } catch (err) {
      console.warn('Backend getBrandMembers failed, returning cached members:', err);
    }
    return memoryMembers;
  },

  async addBrandMember(dto: AddBrandMemberRequestDto): Promise<BrandMemberResponseDto> {
    try {
      const resp = await apiRequest<BrandMemberResponseDto>('/brand-dashboard/members', {
        method: 'POST',
        body: JSON.stringify(dto),
      });
      if (resp.data) {
        memoryMembers.push(resp.data);
        return resp.data;
      }
    } catch (err) {
      console.warn('Backend addBrandMember failed, performing optimistic add:', err);
    }

    const newMember: BrandMemberResponseDto = {
      id: `member-${Date.now()}`,
      brandId: MOCK_BRAND_PROFILE.brand.id,
      brandName: MOCK_BRAND_PROFILE.brand.name,
      userId: `user-${Date.now()}`,
      userFullName: dto.fullName,
      userEmail: dto.email,
      department: dto.department || 'Brand Enforcement',
      brandRole: dto.brandRole,
      phone: dto.phone || null,
      assignedAt: new Date().toISOString(),
    };
    memoryMembers.push(newMember);
    return newMember;
  },

  async updateBrandMemberRole(
    memberId: string,
    dto: UpdateBrandMemberRoleRequestDto,
  ): Promise<BrandMemberResponseDto> {
    try {
      const resp = await apiRequest<BrandMemberResponseDto>(
        `/brand-dashboard/members/${memberId}/role`,
        {
          method: 'PUT',
          body: JSON.stringify(dto),
        },
      );
      if (resp.data) {
        memoryMembers = memoryMembers.map((m) => (m.id === memberId ? resp.data! : m));
        return resp.data;
      }
    } catch (err) {
      console.warn('Backend updateBrandMemberRole failed, updating in-memory:', err);
    }

    const target = memoryMembers.find((m) => m.id === memberId);
    if (target) {
      target.brandRole = dto.brandRole;
      return target;
    }
    throw new Error('Member not found');
  },

  async removeBrandMember(memberId: string): Promise<void> {
    try {
      await apiRequest<void>(`/brand-dashboard/members/${memberId}`, {
        method: 'DELETE',
      });
    } catch (err) {
      console.warn('Backend removeBrandMember failed, updating in-memory:', err);
    }
    memoryMembers = memoryMembers.filter((m) => m.id !== memberId);
  },

  // 3. Brand Catalog Products & ASINs
  async getBrandProducts(
    page = 0,
    size = 10,
  ): Promise<PaginationPayload<ProductResponseDto>> {
    try {
      const resp = await apiRequest<PaginationPayload<ProductResponseDto>>(
        '/brand-dashboard/products',
        {
          method: 'GET',
          query: { page, size },
        },
      );
      if (resp.data && resp.data.content && resp.data.content.length > 0) {
        return resp.data;
      }
    } catch (err) {
      console.warn('Backend getBrandProducts failed, returning in-memory products:', err);
    }

    const start = page * size;
    const content = memoryProducts.slice(start, start + size);
    return {
      content,
      pageNumber: page,
      pageSize: size,
      totalElements: memoryProducts.length,
      totalPages: Math.ceil(memoryProducts.length / size),
      last: start + size >= memoryProducts.length,
    };
  },

  async createBrandProductTemplate(
    dto: CreateBrandCatalogProductRequestDto,
  ): Promise<ProductResponseDto> {
    try {
      const resp = await apiRequest<ProductResponseDto>('/brand-dashboard/products', {
        method: 'POST',
        body: JSON.stringify(dto),
      });
      if (resp.data) {
        memoryProducts.unshift(resp.data);
        return resp.data;
      }
    } catch (err) {
      console.warn('Backend createBrandProductTemplate failed, applying optimistic product:', err);
    }

    const newProd: ProductResponseDto = {
      id: `prod-${Date.now()}`,
      categoryId: dto.categoryId,
      categoryName: 'Consumer Electronics',
      title: dto.title,
      masterSku: dto.masterSku,
      governanceStatus: dto.governanceStatus || 'ACTIVE_LOCKED',
      description: dto.description,
      status: 'ACTIVE',
      basePrice: 199.99,
      variants: [],
      createdAt: new Date().toISOString(),
    };
    memoryProducts.unshift(newProd);
    return newProd;
  },

  async createBrandProductVariant(
    productId: string,
    dto: CreateVariantRequestDto,
  ): Promise<ProductVariantDto> {
    try {
      const resp = await apiRequest<ProductVariantDto>(
        `/brand-dashboard/products/${productId}/variants`,
        {
          method: 'POST',
          body: JSON.stringify(dto),
        },
      );
      if (resp.data) {
        const prod = memoryProducts.find((p) => p.id === productId);
        if (prod) {
          prod.variants.push(resp.data);
        }
        return resp.data;
      }
    } catch (err) {
      console.warn('Backend createBrandProductVariant failed, applying optimistic variant:', err);
    }

    const newVariant: ProductVariantDto = {
      id: `var-${Date.now()}`,
      productId,
      asin: dto.asin,
      variantName: dto.variantName,
      variantAttributes: dto.variantAttributes,
      createdAt: new Date().toISOString(),
    };
    const targetProd = memoryProducts.find((p) => p.id === productId);
    if (targetProd) {
      targetProd.variants = targetProd.variants || [];
      targetProd.variants.push(newVariant);
    }
    return newVariant;
  },

  async updateBrandProduct(
    id: string,
    dto: UpdateBrandProductRequestDto,
  ): Promise<ProductResponseDto> {
    try {
      const resp = await apiRequest<ProductResponseDto>(`/brand-dashboard/products/${id}`, {
        method: 'PUT',
        body: JSON.stringify(dto),
      });
      if (resp.data) {
        memoryProducts = memoryProducts.map((p) => (p.id === id ? resp.data! : p));
        return resp.data;
      }
    } catch (err) {
      console.warn('Backend updateBrandProduct failed, updating in-memory:', err);
    }

    const prod = memoryProducts.find((p) => p.id === id);
    if (prod) {
      if (dto.title) prod.title = dto.title;
      if (dto.description) prod.description = dto.description;
      if (dto.masterSku) prod.masterSku = dto.masterSku;
      if (dto.governanceStatus) prod.governanceStatus = dto.governanceStatus;
      return prod;
    }
    throw new Error('Product not found');
  },

  async deleteBrandProduct(id: string): Promise<void> {
    try {
      await apiRequest<void>(`/brand-dashboard/products/${id}`, {
        method: 'DELETE',
      });
    } catch (err) {
      console.warn('Backend deleteBrandProduct failed, updating in-memory:', err);
    }
    memoryProducts = memoryProducts.filter((p) => p.id !== id);
  },

  // 4. Marketing Posts
  async getBrandPosts(
    page = 0,
    size = 10,
  ): Promise<PaginationPayload<BrandPostResponseDto>> {
    try {
      const resp = await apiRequest<PaginationPayload<BrandPostResponseDto>>(
        '/brand-dashboard/posts',
        {
          method: 'GET',
          query: { page, size },
        },
      );
      if (resp.data && resp.data.content && resp.data.content.length > 0) {
        return resp.data;
      }
    } catch (err) {
      console.warn('Backend getBrandPosts failed, returning in-memory posts:', err);
    }

    const start = page * size;
    const content = memoryPosts.slice(start, start + size);
    return {
      content,
      pageNumber: page,
      pageSize: size,
      totalElements: memoryPosts.length,
      totalPages: Math.ceil(memoryPosts.length / size),
      last: start + size >= memoryPosts.length,
    };
  },

  async createBrandPost(dto: CreateBrandPostRequestDto): Promise<BrandPostResponseDto> {
    try {
      const resp = await apiRequest<BrandPostResponseDto>('/brand-dashboard/posts', {
        method: 'POST',
        body: JSON.stringify(dto),
      });
      if (resp.data) {
        memoryPosts.unshift(resp.data);
        return resp.data;
      }
    } catch (err) {
      console.warn('Backend createBrandPost failed, applying optimistic post:', err);
    }

    const newPost: BrandPostResponseDto = {
      id: `post-${Date.now()}`,
      brandId: MOCK_BRAND_PROFILE.brand.id,
      brandName: MOCK_BRAND_PROFILE.brand.name,
      authorUserId: 'user-001',
      authorName: 'Elena Vance',
      imageUrl: dto.imageUrl,
      caption: dto.caption,
      linkedAsin: dto.linkedAsin || null,
      reachCount: dto.reachCount ?? 0,
      likesCount: dto.likesCount ?? 0,
      placement: 'PDP Live Feed',
      createdAt: new Date().toISOString(),
    };
    memoryPosts.unshift(newPost);
    return newPost;
  },

  async updateBrandPost(
    id: string,
    dto: UpdateBrandPostRequestDto,
  ): Promise<BrandPostResponseDto> {
    try {
      const resp = await apiRequest<BrandPostResponseDto>(`/brand-dashboard/posts/${id}`, {
        method: 'PUT',
        body: JSON.stringify(dto),
      });
      if (resp.data) {
        memoryPosts = memoryPosts.map((p) => (p.id === id ? resp.data! : p));
        return resp.data;
      }
    } catch (err) {
      console.warn('Backend updateBrandPost failed, updating in-memory:', err);
    }

    const post = memoryPosts.find((p) => p.id === id);
    if (post) {
      if (dto.imageUrl) post.imageUrl = dto.imageUrl;
      if (dto.caption) post.caption = dto.caption;
      if (dto.linkedAsin !== undefined) post.linkedAsin = dto.linkedAsin;
      if (dto.reachCount !== undefined) post.reachCount = dto.reachCount;
      if (dto.likesCount !== undefined) post.likesCount = dto.likesCount;
      return post;
    }
    throw new Error('Post not found');
  },

  async deleteBrandPost(id: string): Promise<void> {
    try {
      await apiRequest<void>(`/brand-dashboard/posts/${id}`, {
        method: 'DELETE',
      });
    } catch (err) {
      console.warn('Backend deleteBrandPost failed, updating in-memory:', err);
    }
    memoryPosts = memoryPosts.filter((p) => p.id !== id);
  },

  // 5. Category Taxonomy Proposals
  async getCategoryProposals(): Promise<CategoryProposalResponseDto[]> {
    return memoryProposals;
  },

  async proposeCategory(
    dto: CreateCategoryRequestDto,
  ): Promise<CategoryProposalResponseDto> {
    try {
      const resp = await apiRequest<CategoryProposalResponseDto>(
        '/brand-dashboard/categories',
        {
          method: 'POST',
          body: JSON.stringify(dto),
        },
      );
      if (resp.data) {
        memoryProposals.unshift(resp.data);
        return resp.data;
      }
    } catch (err) {
      console.warn('Backend proposeCategory failed, applying optimistic proposal:', err);
    }

    const newProposal: CategoryProposalResponseDto = {
      id: `CP-2024-${Math.floor(100 + Math.random() * 900)}`,
      parentId: dto.parentId || null,
      name: dto.name,
      slug: dto.slug,
      level: dto.level,
      isApproved: false,
      commercialJustification: dto.commercialJustification || null,
      createdAt: new Date().toISOString(),
    };
    memoryProposals.unshift(newProposal);
    return newProposal;
  },

  // 6. Brand Analytics
  async getBrandAnalytics(): Promise<BrandAnalyticsResponseDto> {
    try {
      const resp = await apiRequest<BrandAnalyticsResponseDto>(
        '/brand-dashboard/analytics',
        {
          method: 'GET',
        },
      );
      if (resp.data && resp.data.totalBrandRevenue !== undefined) {
        return resp.data;
      }
    } catch (err) {
      console.warn('Backend getBrandAnalytics failed, returning fallback metrics:', err);
    }
    return MOCK_BRAND_ANALYTICS;
  },
};
