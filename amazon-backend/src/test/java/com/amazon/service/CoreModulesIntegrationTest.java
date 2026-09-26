package com.amazon.service;

import com.amazon.dtos.auth.request.LoginRequestDto;
import com.amazon.dtos.auth.request.RegisterRequestDto;
import com.amazon.dtos.auth.response.AuthResponseDto;
import com.amazon.dtos.cart.request.AddToCartRequestDto;
import com.amazon.dtos.cart.request.UpdateCartItemRequestDto;
import com.amazon.dtos.cart.response.CartResponseDto;
import com.amazon.dtos.category.request.CreateCategoryRequestDto;
import com.amazon.dtos.category.response.CategoryResponseDto;
import com.amazon.dtos.listing.request.CreateListingRequestDto;
import com.amazon.dtos.listing.response.ProductListingResponseDto;
import com.amazon.dtos.order.request.CheckoutRequestDto;
import com.amazon.dtos.order.response.OrderResponseDto;
import com.amazon.dtos.product.request.CreateProductRequestDto;
import com.amazon.dtos.product.request.CreateVariantRequestDto;
import com.amazon.dtos.product.response.ProductResponseDto;
import com.amazon.dtos.product.response.ProductVariantResponseDto;
import com.amazon.enums.FulfillmentType;
import com.amazon.enums.ListingStatus;
import com.amazon.enums.OrderStatus;
import com.amazon.enums.ProductStatus;
import com.amazon.payloads.ResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CoreModulesIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductVariantService productVariantService;

    @Autowired
    private ProductListingService productListingService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Test
    @DisplayName("Module 1 (User & Auth): Register and Login customer")
    void testUserRegistrationAndLogin() {
        RegisterRequestDto registerDto = RegisterRequestDto.builder()
                .fullName("John Doe")
                .email("johndoe@example.com")
                .password("Password123!")
                .phone("+1234567890")
                .build();

        ResponseDto<AuthResponseDto> registerResponse = authService.register(registerDto);
        assertNotNull(registerResponse.getData());
        assertNotNull(registerResponse.getData().getToken());
        assertEquals("John Doe", registerResponse.getData().getUser().getFullName());
        assertEquals("johndoe@example.com", registerResponse.getData().getUser().getEmail());

        LoginRequestDto loginDto = new LoginRequestDto("johndoe@example.com", "Password123!");
        ResponseDto<AuthResponseDto> loginResponse = authService.login(loginDto);
        assertNotNull(loginResponse.getData());
        assertNotNull(loginResponse.getData().getToken());
    }

    @Test
    @DisplayName("Module 2 (Catalog): Create Category, Product, Variant, and Listing")
    void testCatalogFlow() {
        // 1. Create seller
        RegisterRequestDto sellerRegister = RegisterRequestDto.builder()
                .fullName("Acme Seller")
                .email("seller@acme.com")
                .password("SecretSeller123!")
                .build();
        authService.register(sellerRegister);

        // 2. Create Category
        CreateCategoryRequestDto categoryDto = CreateCategoryRequestDto.builder()
                .name("Electronics")
                .slug("electronics")
                .level(0)
                .build();
        ResponseDto<CategoryResponseDto> catResponse = categoryService.createCategory(categoryDto);
        UUID categoryId = catResponse.getData().getId();
        assertNotNull(categoryId);

        // 3. Create Product
        CreateProductRequestDto productDto = CreateProductRequestDto.builder()
                .categoryId(categoryId)
                .title("Echo Dot Smart Speaker")
                .description("Smart speaker with Alexa")
                .basePrice(new BigDecimal("49.99"))
                .status(ProductStatus.ACTIVE)
                .build();
        ResponseDto<ProductResponseDto> prodResponse = productService.createProduct(productDto, "seller@acme.com");
        UUID productId = prodResponse.getData().getId();
        assertNotNull(productId);

        // 4. Create Variant
        CreateVariantRequestDto variantDto = CreateVariantRequestDto.builder()
                .asin("B09B8V1LZ3")
                .variantName("Charcoal Black")
                .variantAttributesJson("{\"color\": \"Charcoal\"}")
                .build();
        ResponseDto<ProductVariantResponseDto> variantResponse = productVariantService.createVariant(productId, variantDto);
        UUID variantId = variantResponse.getData().getId();
        assertNotNull(variantId);
        assertEquals("B09B8V1LZ3", variantResponse.getData().getAsin());

        // 5. Create Product Listing
        CreateListingRequestDto listingDto = CreateListingRequestDto.builder()
                .productVariantId(variantId)
                .sellerSku("ECHO-DOT-BLK-01")
                .price(new BigDecimal("44.99"))
                .stockQuantity(100)
                .fulfillmentType(FulfillmentType.FBA)
                .status(ListingStatus.ACTIVE)
                .build();
        ResponseDto<ProductListingResponseDto> listingResponse = productListingService.createListing(listingDto, "seller@acme.com");
        UUID listingId = listingResponse.getData().getId();
        assertNotNull(listingId);
        assertTrue(listingResponse.getData().getIsBuyboxWinner());
        assertEquals(100, listingResponse.getData().getStockQuantity());
    }

    @Test
    @DisplayName("Module 3 & 4 (Cart & Order): Add to Cart, update quantity, and Checkout into Order with stock deduction")
    void testCartAndCheckoutFlow() {
        // Setup Seller & Buyer
        authService.register(RegisterRequestDto.builder()
                .fullName("Store Owner")
                .email("store@owner.com")
                .password("Owner123!")
                .build());

        authService.register(RegisterRequestDto.builder()
                .fullName("Alice Buyer")
                .email("alice@buyer.com")
                .password("Buyer123!")
                .build());

        // Setup Catalog
        ResponseDto<CategoryResponseDto> cat = categoryService.createCategory(CreateCategoryRequestDto.builder()
                .name("Books")
                .slug("books")
                .build());

        ResponseDto<ProductResponseDto> prod = productService.createProduct(CreateProductRequestDto.builder()
                .categoryId(cat.getData().getId())
                .title("Clean Architecture Book")
                .basePrice(new BigDecimal("29.99"))
                .status(ProductStatus.ACTIVE)
                .build(), "store@owner.com");

        ResponseDto<ProductVariantResponseDto> variant = productVariantService.createVariant(prod.getData().getId(),
                CreateVariantRequestDto.builder()
                        .asin("B079DNL1B6")
                        .variantName("Paperback")
                        .build());

        ResponseDto<ProductListingResponseDto> listing = productListingService.createListing(CreateListingRequestDto.builder()
                .productVariantId(variant.getData().getId())
                .sellerSku("BOOK-CLEAN-ARCH-01")
                .price(new BigDecimal("35.00"))
                .stockQuantity(10)
                .fulfillmentType(FulfillmentType.FBM)
                .status(ListingStatus.ACTIVE)
                .build(), "store@owner.com");

        UUID listingId = listing.getData().getId();

        // 1. Add to Cart
        AddToCartRequestDto addDto = AddToCartRequestDto.builder()
                .listingId(listingId)
                .quantity(2)
                .build();
        ResponseDto<CartResponseDto> cartResponse = cartService.addToCart("alice@buyer.com", addDto);
        assertEquals(2, cartResponse.getData().getTotalActiveItems());
        assertEquals(new BigDecimal("70.00"), cartResponse.getData().getActiveSubtotal());

        // 2. Update Cart Item quantity
        UUID cartItemId = cartResponse.getData().getItems().get(0).getId();
        UpdateCartItemRequestDto updateDto = new UpdateCartItemRequestDto(3);
        ResponseDto<CartResponseDto> updatedCart = cartService.updateCartItem("alice@buyer.com", cartItemId, updateDto);
        assertEquals(3, updatedCart.getData().getTotalActiveItems());
        assertEquals(new BigDecimal("105.00"), updatedCart.getData().getActiveSubtotal());

        // 3. Checkout
        UUID shippingAddressId = UUID.randomUUID();
        CheckoutRequestDto checkoutDto = CheckoutRequestDto.builder()
                .shippingAddressId(shippingAddressId)
                .build();
        ResponseDto<OrderResponseDto> orderResponse = orderService.checkoutFromCart("alice@buyer.com", checkoutDto);

        assertNotNull(orderResponse.getData());
        assertNotNull(orderResponse.getData().getOrderNumber());
        assertTrue(orderResponse.getData().getOrderNumber().startsWith("AMZ-"));
        assertEquals(OrderStatus.PENDING, orderResponse.getData().getStatus());
        assertEquals(new BigDecimal("105.00"), orderResponse.getData().getTotalAmount());
        assertEquals(1, orderResponse.getData().getItems().size());
        assertEquals(3, orderResponse.getData().getItems().get(0).getQuantity());

        // 4. Verify Stock was deducted from 10 to 7
        ResponseDto<ProductListingResponseDto> updatedListing = productListingService.getListingById(listingId);
        assertEquals(7, updatedListing.getData().getStockQuantity());

        // 5. Verify Cart is now empty
        ResponseDto<CartResponseDto> cartAfterCheckout = cartService.getCartByUserEmail("alice@buyer.com");
        assertEquals(0, cartAfterCheckout.getData().getTotalActiveItems());

        // 6. Test Order Cancellation restores stock back from 7 to 10
        UUID orderId = orderResponse.getData().getId();
        ResponseDto<OrderResponseDto> cancelledOrder = orderService.cancelOrder(orderId, "alice@buyer.com");
        assertEquals(OrderStatus.CANCELLED, cancelledOrder.getData().getStatus());

        ResponseDto<ProductListingResponseDto> restoredListing = productListingService.getListingById(listingId);
        assertEquals(10, restoredListing.getData().getStockQuantity());
    }
}
