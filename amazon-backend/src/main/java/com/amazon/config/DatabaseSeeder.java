package com.amazon.config;

import com.amazon.entity.*;
import com.amazon.enums.*;
import com.amazon.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Comprehensive database seeder that populates the application with realistic initial data
 * for testing and demonstration purposes.
 *
 * <p><b>Activation:</b> Controlled by the {@code app.seeder.enabled=true} property.
 * Automatically excluded from the {@code prod} profile via {@code @Profile("!prod")}.</p>
 *
 * <p><b>Idempotency:</b> All seed operations check for existing data before inserting.
 * Safe to run multiple times without creating duplicate entries.</p>
 *
 * <p>Adheres to Senior Backend Developer Guidelines Section 5 (Transactional integrity).</p>
 */
@Component
@Profile("!prod")
@ConditionalOnProperty(name = "app.seeder.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements ApplicationRunner {

    private static final String DEFAULT_PASSWORD = "Password123!";
    private static final String PLATFORM_ADMIN_EMAIL = "admin@amazon-platform.com";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final BrandMemberRepository brandMemberRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductListingRepository productListingRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReviewRepository reviewRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final PasswordEncoder passwordEncoder;

    // ── Cached references populated during seeding ──────────────────────────
    private final Map<String, Role> roleCache = new HashMap<>();
    private final Map<String, Category> categoryCache = new HashMap<>();
    private final Map<String, Brand> brandCache = new HashMap<>();
    private final Map<String, User> userCache = new HashMap<>();
    private final List<Product> allProducts = new ArrayList<>();
    private final List<ProductListing> allListings = new ArrayList<>();

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (roleRepository.count() > 0) {
            log.info("⏩ Database already seeded (roles exist). Skipping seeder.");
            return;
        }

        log.info("🌱 Starting database seeding...");
        long startTime = System.currentTimeMillis();

        seedRoles();
        seedCategories();
        seedBrandsAndUsers();
        seedProducts();
        seedOrders();
        seedReviews();
        seedWishlistItems();

        long duration = System.currentTimeMillis() - startTime;
        log.info("✅ Database seeding completed in {}ms.", duration);
    }

    // ════════════════════════════════════════════════════════════════════════
    // 1. ROLES
    // ════════════════════════════════════════════════════════════════════════

    private void seedRoles() {
        log.info("  → Seeding roles...");

        Map<String, String> roleDescriptions = Map.of(
                "ROLE_ADMIN", "Platform administrator with full system access",
                "ROLE_SELLER", "Authorized seller who can list and manage products",
                "ROLE_USER", "Standard customer account with shopping privileges"
        );

        roleDescriptions.forEach((name, description) -> {
            Role role = Role.builder()
                    .name(name)
                    .description(description)
                    .build();
            role = roleRepository.save(role);
            roleCache.put(name, role);
        });

        log.info("    ✓ Seeded {} roles.", roleCache.size());
    }

    // ════════════════════════════════════════════════════════════════════════
    // 2. CATEGORIES (Hierarchical)
    // ════════════════════════════════════════════════════════════════════════

    private void seedCategories() {
        log.info("  → Seeding categories...");

        // Root categories
        Category electronics = createCategory("Electronics", "electronics", null, 0);
        Category footwear = createCategory("Footwear", "footwear", null, 0);
        Category sportswear = createCategory("Sportswear", "sportswear", null, 0);
        Category smartDevices = createCategory("Smart Devices", "smart-devices", null, 0);
        Category homeAndKitchen = createCategory("Home & Kitchen", "home-and-kitchen", null, 0);
        Category fashion = createCategory("Fashion", "fashion", null, 0);

        // Sub-categories: Electronics
        createCategory("Smartphones", "smartphones", electronics, 1);
        createCategory("Laptops", "laptops", electronics, 1);
        createCategory("Tablets", "tablets", electronics, 1);
        createCategory("Audio & Headphones", "audio-headphones", electronics, 1);
        createCategory("Cameras", "cameras", electronics, 1);

        // Sub-categories: Footwear
        createCategory("Running Shoes", "running-shoes", footwear, 1);
        createCategory("Casual Sneakers", "casual-sneakers", footwear, 1);
        createCategory("Formal Shoes", "formal-shoes", footwear, 1);
        createCategory("Sports Cleats", "sports-cleats", footwear, 1);

        // Sub-categories: Sportswear
        createCategory("Training Apparel", "training-apparel", sportswear, 1);
        createCategory("Compression Wear", "compression-wear", sportswear, 1);
        createCategory("Sports Accessories", "sports-accessories", sportswear, 1);

        // Sub-categories: Smart Devices
        createCategory("Smartwatches", "smartwatches", smartDevices, 1);
        createCategory("Smart Home", "smart-home", smartDevices, 1);
        createCategory("Wearable Tech", "wearable-tech", smartDevices, 1);

        // Sub-categories: Home & Kitchen
        createCategory("Kitchen Appliances", "kitchen-appliances", homeAndKitchen, 1);
        createCategory("Home Decor", "home-decor", homeAndKitchen, 1);

        // Sub-categories: Fashion
        createCategory("Men's Clothing", "mens-clothing", fashion, 1);
        createCategory("Women's Clothing", "womens-clothing", fashion, 1);

        log.info("    ✓ Seeded {} categories.", categoryCache.size());
    }

    private Category createCategory(String name, String slug, Category parent, int level) {
        Category category = Category.builder()
                .name(name)
                .slug(slug)
                .parent(parent)
                .level(level)
                .isApproved(true)
                .build();
        category = categoryRepository.save(category);
        categoryCache.put(slug, category);
        return category;
    }

    // ════════════════════════════════════════════════════════════════════════
    // 3. BRANDS, USERS & SELLER PROFILES
    // ════════════════════════════════════════════════════════════════════════

    private void seedBrandsAndUsers() {
        log.info("  → Seeding users, brands, and seller profiles...");

        String encodedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);

        // ── Platform Admin ──
        User platformAdmin = createUser("Platform Admin", PLATFORM_ADMIN_EMAIL, encodedPassword,
                "+1-800-000-0001", "ROLE_ADMIN");
        userCache.put(PLATFORM_ADMIN_EMAIL, platformAdmin);

        // ── Test Customers ──
        for (int i = 1; i <= 5; i++) {
            String email = "customer" + i + "@test.com";
            User customer = createUser(
                    "Test Customer " + i, email, encodedPassword,
                    "+1-555-100-000" + i, "ROLE_USER");
            userCache.put(email, customer);
        }

        // ── Brand 1: Nike ──
        seedBrandEcosystem("Nike", "nike", "US-TM-NIKE-78945", "United States",
                "https://upload.wikimedia.org/wikipedia/commons/a/a6/Logo_NIKE.svg",
                "Just Do It. World's leading athletic footwear and apparel brand.",
                encodedPassword,
                new BrandUserSpec("nike.seller@test.com", "Nike Seller Manager", "+1-555-200-0001", BrandRole.BRAND_SELLER),
                new BrandUserSpec("nike.admin@test.com", "Nike Brand Admin", "+1-555-200-0002", BrandRole.BRAND_ADMIN),
                new BrandUserSpec("nike.marketing@test.com", "Nike Marketing Lead", "+1-555-200-0003", BrandRole.BRAND_MARKETING_MEMBER)
        );

        // ── Brand 2: Adidas ──
        seedBrandEcosystem("Adidas", "adidas", "DE-TM-ADIDAS-32178", "Germany",
                "https://upload.wikimedia.org/wikipedia/commons/2/20/Adidas_Logo.svg",
                "Impossible Is Nothing. Global sportswear and lifestyle brand.",
                encodedPassword,
                new BrandUserSpec("adidas.seller@test.com", "Adidas Seller Manager", "+1-555-300-0001", BrandRole.BRAND_SELLER),
                new BrandUserSpec("adidas.admin@test.com", "Adidas Brand Admin", "+1-555-300-0002", BrandRole.BRAND_ADMIN)
        );

        // ── Brand 3: Samsung ──
        seedBrandEcosystem("Samsung", "samsung", "KR-TM-SAMSUNG-55421", "South Korea",
                "https://upload.wikimedia.org/wikipedia/commons/2/24/Samsung_Logo.svg",
                "Imagine the Possibilities. Global leader in consumer electronics.",
                encodedPassword,
                new BrandUserSpec("samsung.seller@test.com", "Samsung Seller Manager", "+1-555-400-0001", BrandRole.BRAND_SELLER),
                new BrandUserSpec("samsung.admin@test.com", "Samsung Brand Admin", "+1-555-400-0002", BrandRole.BRAND_ADMIN)
        );

        // ── Brand 4: Apple ──
        seedBrandEcosystem("Apple", "apple", "US-TM-APPLE-10042", "United States",
                "https://upload.wikimedia.org/wikipedia/commons/f/fa/Apple_logo_black.svg",
                "Think Different. Premium consumer electronics and software ecosystem.",
                encodedPassword,
                new BrandUserSpec("apple.seller@test.com", "Apple Seller Manager", "+1-555-500-0001", BrandRole.BRAND_SELLER),
                new BrandUserSpec("apple.admin@test.com", "Apple Brand Admin", "+1-555-500-0002", BrandRole.BRAND_ADMIN)
        );

        // ── Brand 5: Sony ──
        seedBrandEcosystem("Sony", "sony", "JP-TM-SONY-88712", "Japan",
                "https://upload.wikimedia.org/wikipedia/commons/c/ca/Sony_logo.svg",
                "Be Moved. Premium electronics, gaming, and entertainment brand.",
                encodedPassword,
                new BrandUserSpec("sony.seller@test.com", "Sony Seller Manager", "+1-555-600-0001", BrandRole.BRAND_SELLER)
        );

        // ── Brand 6: Puma ──
        seedBrandEcosystem("Puma", "puma", "DE-TM-PUMA-66309", "Germany",
                "https://upload.wikimedia.org/wikipedia/commons/a/a7/Puma_Logo.svg",
                "Forever Faster. Performance-driven sports and lifestyle brand.",
                encodedPassword,
                new BrandUserSpec("puma.seller@test.com", "Puma Seller Manager", "+1-555-700-0001", BrandRole.BRAND_SELLER)
        );

        log.info("    ✓ Seeded {} brands, {} users.", brandCache.size(), userCache.size());
    }

    /**
     * Seeds a complete brand ecosystem: Brand entity, owner user, brand members, seller profiles.
     */
    private void seedBrandEcosystem(String name, String slug, String trademarkNumber, String country,
                                    String logoUrl, String aboutText, String encodedPassword,
                                    BrandUserSpec... members) {
        // Create brand owner (also a SELLER)
        String ownerEmail = slug + ".owner@test.com";
        User owner = createUser(name + " Owner", ownerEmail, encodedPassword,
                "+1-555-" + slug.hashCode() % 1000 + "-0000", "ROLE_SELLER");
        userCache.put(ownerEmail, owner);

        Brand brand = Brand.builder()
                .name(name)
                .slug(slug)
                .trademarkRegistrationNumber(trademarkNumber)
                .brandCountry(country)
                .logoUrl(logoUrl)
                .aboutText(aboutText)
                .status(BrandStatus.ACTIVE)
                .ownerUser(owner)
                .build();
        brand = brandRepository.save(brand);
        brandCache.put(slug, brand);

        // Brand owner membership
        createBrandMember(brand, owner, BrandRole.BRAND_OWNER);

        // Owner's seller profile
        createSellerProfile(owner, brand, name + " Official Store", "TAX-" + slug.toUpperCase() + "-001");

        // Additional brand members
        for (BrandUserSpec spec : members) {
            String roleName = spec.brandRole == BrandRole.BRAND_SELLER ? "ROLE_SELLER" : "ROLE_USER";
            User member = createUser(spec.fullName, spec.email, encodedPassword, spec.phone, roleName);
            userCache.put(spec.email, member);
            createBrandMember(brand, member, spec.brandRole);

            if (spec.brandRole == BrandRole.BRAND_SELLER) {
                createSellerProfile(member, brand, name + " Sub-Store", "TAX-" + slug.toUpperCase() + "-" + spec.email.hashCode() % 1000);
            }
        }
    }

    private User createUser(String fullName, String email, String encodedPassword, String phone, String roleName) {
        User user = User.builder()
                .fullName(fullName)
                .email(email)
                .passwordHash(encodedPassword)
                .phone(phone)
                .status(UserStatus.ACTIVE)
                .lastActiveAt(LocalDateTime.now().minusHours(ThreadLocalRandom.current().nextInt(1, 72)))
                .build();
        user = userRepository.save(user);
        user.addRole(roleCache.get(roleName));
        return userRepository.save(user);
    }

    private void createBrandMember(Brand brand, User user, BrandRole role) {
        BrandMember member = BrandMember.builder()
                .brand(brand)
                .user(user)
                .brandRole(role)
                .assignedAt(LocalDateTime.now())
                .build();
        brandMemberRepository.save(member);
    }

    private void createSellerProfile(User user, Brand brand, String storeName, String taxNumber) {
        SellerProfile profile = SellerProfile.builder()
                .user(user)
                .brand(brand)
                .storeName(storeName)
                .taxNumber(taxNumber)
                .businessAddress("123 Commerce Blvd, Suite " + ThreadLocalRandom.current().nextInt(100, 999))
                .bankAccountDetails("IBAN: XX00-XXXX-XXXX-" + ThreadLocalRandom.current().nextInt(1000, 9999))
                .isVerified(true)
                .build();
        sellerProfileRepository.save(profile);
    }

    // ════════════════════════════════════════════════════════════════════════
    // 4. PRODUCTS, VARIANTS & LISTINGS
    // ════════════════════════════════════════════════════════════════════════

    private void seedProducts() {
        log.info("  → Seeding products, variants, and listings...");

        // ── Nike Products ──
        User nikeSeller = userCache.get("nike.owner@test.com");
        Brand nike = brandCache.get("nike");

        seedProduct(nikeSeller, nike, "running-shoes", "Nike Air Max 270",
                "The Nike Air Max 270 features Nike's biggest heel Air unit yet for a super-soft ride that feels as impossible as it looks.",
                BigDecimal.valueOf(150.00), "https://images.unsplash.com/photo-1542291026-7eec264c27ff",
                new VariantSpec("B0NIKE270BLK", "Black / Size 10", Map.of("color", "Black", "size", "10")),
                new VariantSpec("B0NIKE270WHT", "White / Size 10", Map.of("color", "White", "size", "10")),
                new VariantSpec("B0NIKE270RED", "Red / Size 9", Map.of("color", "Red", "size", "9"))
        );

        seedProduct(nikeSeller, nike, "running-shoes", "Nike ZoomX Vaporfly NEXT%",
                "The Nike ZoomX Vaporfly NEXT% is the fast you've never felt before. It's a series of firsts: Nike's first full-length carbon fiber plate, first use of ZoomX foam, and first Vaporfly.",
                BigDecimal.valueOf(250.00), "https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa",
                new VariantSpec("B0NIKEVFLY01", "Green / Size 9", Map.of("color", "Green", "size", "9")),
                new VariantSpec("B0NIKEVFLY02", "Pink / Size 10", Map.of("color", "Pink", "size", "10"))
        );

        seedProduct(nikeSeller, nike, "training-apparel", "Nike Dri-FIT Training T-Shirt",
                "Nike Dri-FIT technology moves sweat away from your skin for quicker evaporation, helping you stay dry and comfortable.",
                BigDecimal.valueOf(35.00), "https://images.unsplash.com/photo-1581655353564-df123a1eb820",
                new VariantSpec("B0NIKEDFIT01", "Black / Medium", Map.of("color", "Black", "size", "M")),
                new VariantSpec("B0NIKEDFIT02", "Navy / Large", Map.of("color", "Navy", "size", "L"))
        );

        seedProduct(nikeSeller, nike, "sports-accessories", "Nike Elite Basketball Crew Socks",
                "Nike Elite socks provide targeted cushioning in high-impact zones. Dri-FIT technology keeps your feet dry.",
                BigDecimal.valueOf(22.00), "https://images.unsplash.com/photo-1586350977771-b3b0abd50c82",
                new VariantSpec("B0NIKESOCK01", "White / One Size", Map.of("color", "White", "size", "One Size"))
        );

        // ── Adidas Products ──
        User adidasSeller = userCache.get("adidas.owner@test.com");
        Brand adidas = brandCache.get("adidas");

        seedProduct(adidasSeller, adidas, "casual-sneakers", "Adidas Ultraboost 22",
                "Adidas Ultraboost 22 features a BOOST midsole that returns energy with every step. Primeknit+ upper adapts to your foot.",
                BigDecimal.valueOf(190.00), "https://images.unsplash.com/photo-1588361861040-ac9b1018f6d5",
                new VariantSpec("B0ADIUB22BLK", "Core Black / Size 10", Map.of("color", "Core Black", "size", "10")),
                new VariantSpec("B0ADIUB22WHT", "Cloud White / Size 11", Map.of("color", "Cloud White", "size", "11")),
                new VariantSpec("B0ADIUB22GRY", "Grey Six / Size 9", Map.of("color", "Grey Six", "size", "9"))
        );

        seedProduct(adidasSeller, adidas, "running-shoes", "Adidas Adizero Adios Pro 3",
                "The Adizero Adios Pro 3 is built for speed with ENERGYRODS 2.0 and LIGHTSTRIKE PRO cushioning for explosive energy return.",
                BigDecimal.valueOf(230.00), "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a",
                new VariantSpec("B0ADIZERO01", "Solar Red / Size 10", Map.of("color", "Solar Red", "size", "10")),
                new VariantSpec("B0ADIZERO02", "Core Black / Size 9", Map.of("color", "Core Black", "size", "9"))
        );

        seedProduct(adidasSeller, adidas, "compression-wear", "Adidas Techfit Compression Tights",
                "Adidas Techfit compression tights offer supportive fit and moisture-wicking AEROREADY technology.",
                BigDecimal.valueOf(55.00), "https://images.unsplash.com/photo-1556906781-9a412961c28c",
                new VariantSpec("B0ADITIGHT01", "Black / Medium", Map.of("color", "Black", "size", "M")),
                new VariantSpec("B0ADITIGHT02", "Navy / Large", Map.of("color", "Navy", "size", "L"))
        );

        // ── Samsung Products ──
        User samsungSeller = userCache.get("samsung.owner@test.com");
        Brand samsung = brandCache.get("samsung");

        seedProduct(samsungSeller, samsung, "smartphones", "Samsung Galaxy S24 Ultra",
                "The Samsung Galaxy S24 Ultra features a Snapdragon 8 Gen 3 processor, 200MP camera, and built-in S Pen for the ultimate mobile experience.",
                BigDecimal.valueOf(1299.99), "https://images.unsplash.com/photo-1610945415295-d9bbf067e59c",
                new VariantSpec("B0SAMGS24U01", "Titanium Black / 256GB", Map.of("color", "Titanium Black", "storage", "256GB")),
                new VariantSpec("B0SAMGS24U02", "Titanium Violet / 512GB", Map.of("color", "Titanium Violet", "storage", "512GB")),
                new VariantSpec("B0SAMGS24U03", "Titanium Gray / 1TB", Map.of("color", "Titanium Gray", "storage", "1TB"))
        );

        seedProduct(samsungSeller, samsung, "tablets", "Samsung Galaxy Tab S9 Ultra",
                "The Samsung Galaxy Tab S9 Ultra delivers a stunning 14.6-inch Dynamic AMOLED 2X display and Snapdragon 8 Gen 2 for Galaxy.",
                BigDecimal.valueOf(1199.99), "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0",
                new VariantSpec("B0SAMTABS901", "Graphite / 256GB", Map.of("color", "Graphite", "storage", "256GB")),
                new VariantSpec("B0SAMTABS902", "Beige / 512GB", Map.of("color", "Beige", "storage", "512GB"))
        );

        seedProduct(samsungSeller, samsung, "smart-home", "Samsung SmartThings Hub v3",
                "Central hub for Samsung SmartThings ecosystem. Connect and control Zigbee, Z-Wave, and Wi-Fi devices from one app.",
                BigDecimal.valueOf(69.99), "https://images.unsplash.com/photo-1558618666-fcd25c85f82e",
                new VariantSpec("B0SAMHUB0001", "White / Standard", Map.of("color", "White", "model", "Standard"))
        );

        seedProduct(samsungSeller, samsung, "audio-headphones", "Samsung Galaxy Buds3 Pro",
                "Samsung Galaxy Buds3 Pro feature 2-way speakers, Intelligent ANC, and 360 Audio for immersive listening.",
                BigDecimal.valueOf(249.99), "https://images.unsplash.com/photo-1590658268037-6bf12f032f55",
                new VariantSpec("B0SAMBUDS301", "Silver / Standard", Map.of("color", "Silver")),
                new VariantSpec("B0SAMBUDS302", "White / Standard", Map.of("color", "White"))
        );

        // ── Apple Products ──
        User appleSeller = userCache.get("apple.owner@test.com");
        Brand apple = brandCache.get("apple");

        seedProduct(appleSeller, apple, "smartphones", "iPhone 16 Pro Max",
                "iPhone 16 Pro Max features A18 Pro chip, 48MP Fusion camera system, and the longest battery life ever on an iPhone.",
                BigDecimal.valueOf(1199.00), "https://images.unsplash.com/photo-1591337676887-a217a6970a8a",
                new VariantSpec("B0APLIP16PM1", "Natural Titanium / 256GB", Map.of("color", "Natural Titanium", "storage", "256GB")),
                new VariantSpec("B0APLIP16PM2", "Desert Titanium / 512GB", Map.of("color", "Desert Titanium", "storage", "512GB")),
                new VariantSpec("B0APLIP16PM3", "Black Titanium / 1TB", Map.of("color", "Black Titanium", "storage", "1TB"))
        );

        seedProduct(appleSeller, apple, "laptops", "MacBook Pro 16-inch M3 Max",
                "MacBook Pro 16-inch powered by M3 Max delivers extraordinary performance for demanding pro workflows, with up to 128GB unified memory.",
                BigDecimal.valueOf(3499.00), "https://images.unsplash.com/photo-1517336714731-489689fd1ca8",
                new VariantSpec("B0APLMBP16M1", "Space Black / 36GB / 1TB", Map.of("color", "Space Black", "ram", "36GB", "storage", "1TB")),
                new VariantSpec("B0APLMBP16M2", "Silver / 48GB / 1TB", Map.of("color", "Silver", "ram", "48GB", "storage", "1TB"))
        );

        seedProduct(appleSeller, apple, "smartwatches", "Apple Watch Ultra 2",
                "Apple Watch Ultra 2 is the most rugged and capable Apple Watch with precision dual-frequency GPS, 36-hour battery life, and Action button.",
                BigDecimal.valueOf(799.00), "https://images.unsplash.com/photo-1434493789847-2f02dc6ca35d",
                new VariantSpec("B0APLAWU2001", "Titanium / 49mm / Alpine Loop", Map.of("case", "Titanium", "size", "49mm", "band", "Alpine Loop")),
                new VariantSpec("B0APLAWU2002", "Titanium / 49mm / Ocean Band", Map.of("case", "Titanium", "size", "49mm", "band", "Ocean Band"))
        );

        seedProduct(appleSeller, apple, "audio-headphones", "AirPods Pro 2nd Generation",
                "AirPods Pro 2nd generation with H2 chip deliver 2x more Active Noise Cancellation, Adaptive Transparency, and Personalized Spatial Audio.",
                BigDecimal.valueOf(249.00), "https://images.unsplash.com/photo-1606741965326-cb990ae01bb2",
                new VariantSpec("B0APLAPP2001", "White / USB-C", Map.of("color", "White", "connector", "USB-C"))
        );

        // ── Sony Products ──
        User sonySeller = userCache.get("sony.owner@test.com");
        Brand sony = brandCache.get("sony");

        seedProduct(sonySeller, sony, "audio-headphones", "Sony WH-1000XM5",
                "Industry-leading noise canceling with Auto NC Optimizer. 30-hour battery life. Multipoint connection.",
                BigDecimal.valueOf(349.99), "https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb",
                new VariantSpec("B0SNYXM5BLK", "Black / Standard", Map.of("color", "Black")),
                new VariantSpec("B0SNYXM5SLV", "Silver / Standard", Map.of("color", "Silver"))
        );

        seedProduct(sonySeller, sony, "cameras", "Sony Alpha A7 IV",
                "Full-frame mirrorless camera with 33MP Exmor R sensor, BIONZ XR processor, and Real-time Eye AF for humans, animals, and birds.",
                BigDecimal.valueOf(2499.99), "https://images.unsplash.com/photo-1516035069371-29a1b244cc32",
                new VariantSpec("B0SNYA7IV01", "Black / Body Only", Map.of("kit", "Body Only")),
                new VariantSpec("B0SNYA7IV02", "Black / 28-70mm Kit", Map.of("kit", "28-70mm Kit Lens"))
        );

        // ── Puma Products ──
        User pumaSeller = userCache.get("puma.owner@test.com");
        Brand puma = brandCache.get("puma");

        seedProduct(pumaSeller, puma, "casual-sneakers", "Puma RS-X Reinvention",
                "The Puma RS-X Reinvention merges retro running aesthetics with bold, modern design. RS cushioning technology for maximum comfort.",
                BigDecimal.valueOf(110.00), "https://images.unsplash.com/photo-1608231387042-66d1773070a5",
                new VariantSpec("B0PUMARSX01", "White-Royal / Size 10", Map.of("color", "White-Royal", "size", "10")),
                new VariantSpec("B0PUMARSX02", "Black-Red / Size 9", Map.of("color", "Black-Red", "size", "9"))
        );

        seedProduct(pumaSeller, puma, "training-apparel", "Puma EvoStripe Training Hoodie",
                "Puma EvoStripe hoodie with dryCELL moisture-wicking technology. Comfortable cotton-blend for training and casual wear.",
                BigDecimal.valueOf(65.00), "https://images.unsplash.com/photo-1556821840-3a63f95609a7",
                new VariantSpec("B0PUMAHOOD01", "Dark Gray / Large", Map.of("color", "Dark Gray", "size", "L")),
                new VariantSpec("B0PUMAHOOD02", "Black / Medium", Map.of("color", "Black", "size", "M"))
        );

        log.info("    ✓ Seeded {} products with variants and listings.", allProducts.size());
    }

    private void seedProduct(User seller, Brand brand, String categorySlug, String title,
                             String description, BigDecimal basePrice, String imageUrl,
                             VariantSpec... variantSpecs) {
        Category category = categoryCache.get(categorySlug);
        if (category == null) {
            log.warn("    ⚠ Category slug '{}' not found. Skipping product '{}'.", categorySlug, title);
            return;
        }

        long viewCount = ThreadLocalRandom.current().nextLong(50, 15000);
        long totalUnitsSold = ThreadLocalRandom.current().nextLong(5, 500);

        Product product = Product.builder()
                .seller(seller)
                .brand(brand)
                .category(category)
                .title(title)
                .description(description)
                .basePrice(basePrice)
                .mainImageUrl(imageUrl)
                .status(ProductStatus.ACTIVE)
                .viewCount(viewCount)
                .totalUnitsSold(totalUnitsSold)
                .averageRating(0.0)
                .totalReviews(0)
                .build();
        product = productRepository.save(product);
        allProducts.add(product);

        for (VariantSpec vs : variantSpecs) {
            ProductVariant variant = ProductVariant.builder()
                    .product(product)
                    .asin(vs.asin)
                    .variantName(vs.name)
                    .variantAttributes(new HashMap<>(vs.attributes))
                    .build();
            variant = productVariantRepository.save(variant);

            // Compute a ±15% price variance around basePrice for this listing
            double variance = 1.0 + (ThreadLocalRandom.current().nextDouble(-0.15, 0.10));
            BigDecimal listingPrice = basePrice.multiply(BigDecimal.valueOf(variance))
                    .setScale(2, RoundingMode.HALF_UP);
            int stock = ThreadLocalRandom.current().nextInt(10, 500);

            ProductListing listing = ProductListing.builder()
                    .productVariant(variant)
                    .seller(seller)
                    .sellerSku("SKU-" + vs.asin)
                    .price(listingPrice)
                    .stockQuantity(stock)
                    .fulfillmentType(ThreadLocalRandom.current().nextBoolean() ? FulfillmentType.FBA : FulfillmentType.FBM)
                    .isBuyboxWinner(true)
                    .status(ListingStatus.ACTIVE)
                    .build();
            listing = productListingRepository.save(listing);
            allListings.add(listing);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // 5. ORDERS & ORDER ITEMS
    // ════════════════════════════════════════════════════════════════════════

    private void seedOrders() {
        log.info("  → Seeding orders...");

        List<User> customers = List.of(
                userCache.get("customer1@test.com"),
                userCache.get("customer2@test.com"),
                userCache.get("customer3@test.com"),
                userCache.get("customer4@test.com"),
                userCache.get("customer5@test.com")
        );

        OrderStatus[] fulfillmentStatuses = {
                OrderStatus.DELIVERED, OrderStatus.DELIVERED, OrderStatus.DELIVERED,
                OrderStatus.SHIPPED, OrderStatus.PROCESSING, OrderStatus.CONFIRMED,
                OrderStatus.PENDING
        };

        int orderCount = 0;
        for (User customer : customers) {
            // Each customer gets 3-5 orders with 1-3 items each
            int numOrders = ThreadLocalRandom.current().nextInt(3, 6);
            for (int i = 0; i < numOrders; i++) {
                OrderStatus status = fulfillmentStatuses[ThreadLocalRandom.current().nextInt(fulfillmentStatuses.length)];
                Order order = createOrder(customer, status, orderCount++);
            }
        }

        log.info("    ✓ Seeded {} orders.", orderCount);
    }

    private Order createOrder(User customer, OrderStatus orderStatus, int orderIndex) {
        String orderNumber = String.format("ORD-%s-%06d", LocalDateTime.now().toLocalDate().toString().replace("-", ""), orderIndex + 1);

        int daysAgo = ThreadLocalRandom.current().nextInt(1, 90);
        LocalDateTime placedAt = LocalDateTime.now().minusDays(daysAgo);

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(customer)
                .shippingAddressId(UUID.randomUUID())
                .totalAmount(BigDecimal.ZERO)
                .status(orderStatus)
                .placedAt(placedAt)
                .build();
        order = orderRepository.save(order);

        // Add 1-3 items per order
        int numItems = ThreadLocalRandom.current().nextInt(1, 4);
        BigDecimal orderTotal = BigDecimal.ZERO;
        Set<Integer> usedListingIndices = new HashSet<>();

        for (int i = 0; i < numItems && i < allListings.size(); i++) {
            int listingIndex;
            do {
                listingIndex = ThreadLocalRandom.current().nextInt(allListings.size());
            } while (usedListingIndices.contains(listingIndex) && usedListingIndices.size() < allListings.size());
            usedListingIndices.add(listingIndex);

            ProductListing listing = allListings.get(listingIndex);
            int qty = ThreadLocalRandom.current().nextInt(1, 4);
            BigDecimal unitPrice = listing.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(qty));
            orderTotal = orderTotal.add(subtotal);

            OrderItemStatus itemStatus = mapOrderStatusToItemStatus(orderStatus);

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .listing(listing)
                    .productVariant(listing.getProductVariant())
                    .seller(listing.getSeller())
                    .unitPrice(unitPrice)
                    .quantity(qty)
                    .subtotal(subtotal)
                    .itemStatus(itemStatus)
                    .build();
            orderItemRepository.save(item);
        }

        order.setTotalAmount(orderTotal);
        return orderRepository.save(order);
    }

    private OrderItemStatus mapOrderStatusToItemStatus(OrderStatus orderStatus) {
        return switch (orderStatus) {
            case DELIVERED -> OrderItemStatus.DELIVERED;
            case SHIPPED -> OrderItemStatus.SHIPPED;
            case CANCELLED -> OrderItemStatus.CANCELLED;
            default -> OrderItemStatus.PENDING;
        };
    }

    // ════════════════════════════════════════════════════════════════════════
    // 6. REVIEWS
    // ════════════════════════════════════════════════════════════════════════

    private void seedReviews() {
        log.info("  → Seeding reviews...");

        List<User> customers = List.of(
                userCache.get("customer1@test.com"),
                userCache.get("customer2@test.com"),
                userCache.get("customer3@test.com"),
                userCache.get("customer4@test.com"),
                userCache.get("customer5@test.com")
        );

        String[] positiveComments = {
                "Absolutely love this product! Exceeded all my expectations. The quality is top-notch.",
                "Great value for money. Fast shipping and arrived in perfect condition.",
                "Been using it for a month now and I'm very impressed. Highly recommend!",
                "Excellent build quality and the performance is outstanding. Would buy again.",
                "This is exactly what I was looking for. Perfect fit and amazing comfort.",
                "Five stars! The attention to detail is remarkable. You can tell this is a premium product.",
                "Blew me away! Way better than the competitor's version I had before.",
                "Super comfortable and looks even better in person than in the photos."
        };

        String[] neutralComments = {
                "Good product overall but the packaging could be improved. Still satisfied with my purchase.",
                "Decent quality for the price point. Does what it's supposed to do.",
                "Works well enough. Nothing extraordinary but gets the job done reliably."
        };

        String[] negativeComments = {
                "Expected better quality at this price point. It's okay but not great.",
                "Took a while to get used to. Color is slightly different from what's shown online."
        };

        int reviewCount = 0;
        for (Product product : allProducts) {
            // 2-4 reviews per product from different customers
            int numReviews = ThreadLocalRandom.current().nextInt(2, Math.min(5, customers.size() + 1));
            List<User> shuffledCustomers = new ArrayList<>(customers);
            Collections.shuffle(shuffledCustomers);

            double ratingSum = 0;
            int ratingCount = 0;

            for (int i = 0; i < numReviews; i++) {
                User reviewer = shuffledCustomers.get(i);

                // Weighted rating distribution (skewed towards 4-5 stars like real products)
                int rating;
                double rand = ThreadLocalRandom.current().nextDouble();
                if (rand < 0.45) rating = 5;
                else if (rand < 0.75) rating = 4;
                else if (rand < 0.88) rating = 3;
                else if (rand < 0.95) rating = 2;
                else rating = 1;

                String comment;
                if (rating >= 4) {
                    comment = positiveComments[ThreadLocalRandom.current().nextInt(positiveComments.length)];
                } else if (rating == 3) {
                    comment = neutralComments[ThreadLocalRandom.current().nextInt(neutralComments.length)];
                } else {
                    comment = negativeComments[ThreadLocalRandom.current().nextInt(negativeComments.length)];
                }

                Review review = Review.builder()
                        .product(product)
                        .user(reviewer)
                        .rating(rating)
                        .comment(comment)
                        .build();
                reviewRepository.save(review);
                reviewCount++;

                ratingSum += rating;
                ratingCount++;
            }

            // Update denormalized fields on the product
            double avg = ratingSum / ratingCount;
            product.setAverageRating(Math.round(avg * 10.0) / 10.0);
            product.setTotalReviews(ratingCount);
            productRepository.save(product);
        }

        log.info("    ✓ Seeded {} reviews.", reviewCount);
    }

    // ════════════════════════════════════════════════════════════════════════
    // 7. WISHLIST ITEMS
    // ════════════════════════════════════════════════════════════════════════

    private void seedWishlistItems() {
        log.info("  → Seeding wishlist items...");

        List<User> customers = List.of(
                userCache.get("customer1@test.com"),
                userCache.get("customer2@test.com"),
                userCache.get("customer3@test.com")
        );

        int wishlistCount = 0;
        for (User customer : customers) {
            // Each customer wishlists 3-6 random products
            int numWishlist = ThreadLocalRandom.current().nextInt(3, 7);
            List<Product> shuffledProducts = new ArrayList<>(allProducts);
            Collections.shuffle(shuffledProducts);

            for (int i = 0; i < numWishlist && i < shuffledProducts.size(); i++) {
                WishlistItem item = WishlistItem.builder()
                        .user(customer)
                        .product(shuffledProducts.get(i))
                        .build();
                wishlistItemRepository.save(item);
                wishlistCount++;
            }
        }

        log.info("    ✓ Seeded {} wishlist items.", wishlistCount);
    }

    // ════════════════════════════════════════════════════════════════════════
    // INNER RECORDS (Data Carriers)
    // ════════════════════════════════════════════════════════════════════════

    private record BrandUserSpec(String email, String fullName, String phone, BrandRole brandRole) {}

    private record VariantSpec(String asin, String name, Map<String, Object> attributes) {}
}
