package com.luopc.platform.clients.mongodb;


import com.luopc.platform.clients.mongodb.config.MongoConfig;
import com.luopc.platform.clients.mongodb.entity.Product;
import com.luopc.platform.clients.mongodb.entity.User;
import com.luopc.platform.clients.mongodb.repository.ProductRepository;
import com.luopc.platform.clients.mongodb.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(
        classes =  {MongoDBApplication.class, MongoConfig.class},
        properties = {
                "spring.config.location=classpath:application-mongodb.yml",
                "spring.profiles.active=test"
        }
)
@TestPropertySource(locations = "classpath:application-mongodb.yml")
@Slf4j
public class MongoDBComprehensiveTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private User testUser;
    private List<Product> testProducts;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        userRepository.deleteAll();
        productRepository.deleteAll();

        // 准备测试用户数据
        testUser = createUser("testuser", "test@example.com", "Test User", 25, true);
        userRepository.save(testUser);

        // 准备测试产品数据
        testProducts = Arrays.asList(
                createProduct("Laptop", "高性能笔记本电脑", "Electronics", new BigDecimal("5999.99"), 50, true),
                createProduct("Phone", "智能手机", "Electronics", new BigDecimal("2999.99"), 100, true),
                createProduct("Book", "Java编程指南", "Books", new BigDecimal("89.99"), 200, true),
                createProduct("Desk", "办公桌", "Furniture", new BigDecimal("899.99"), 30, false)
        );
        productRepository.saveAll(testProducts);
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据
        userRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("测试基本CRUD操作 - 用户")
    void testUserCrudOperations() {
        // Create
        User newUser = createUser("john_doe", "john@example.com", "John Doe", 30, true);
        User savedUser = userRepository.save(newUser);
        assertNotNull(savedUser.getId());
        log.info("创建用户成功: {}", savedUser.getUsername());

        // Read
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertTrue(foundUser.isPresent());
        assertEquals("john_doe", foundUser.get().getUsername());
        log.info("查询用户成功: {}", foundUser.get().getUsername());

        // Update
        User updateUser = foundUser.get();
        updateUser.setAge(31);
        updateUser.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(updateUser);
        assertEquals(31, updatedUser.getAge());
        log.info("更新用户成功: 年龄={}", updatedUser.getAge());

        // Delete
        userRepository.deleteById(savedUser.getId());
        Optional<User> deletedUser = userRepository.findById(savedUser.getId());
        assertFalse(deletedUser.isPresent());
        log.info("删除用户成功");
    }

    @Test
    @DisplayName("测试基本CRUD操作 - 产品")
    void testProductCrudOperations() {
        // Create
        Product newProduct = createProduct("Tablet", "平板电脑", "Electronics", new BigDecimal("1999.99"), 75, true);
        Product savedProduct = productRepository.save(newProduct);
        assertNotNull(savedProduct.getId());
        log.info("创建产品成功: {}", savedProduct.getName());

        // Read
        Optional<Product> foundProduct = productRepository.findById(savedProduct.getId());
        assertTrue(foundProduct.isPresent());
        assertEquals("Tablet", foundProduct.get().getName());
        log.info("查询产品成功: {}", foundProduct.get().getName());

        // Update
        Product updateProduct = foundProduct.get();
        updateProduct.setPrice(new BigDecimal("1799.99"));
        updateProduct.setStock(80);
        updateProduct.setUpdatedAt(LocalDateTime.now());
        Product updatedProduct = productRepository.save(updateProduct);
        assertEquals(new BigDecimal("1799.99"), updatedProduct.getPrice());
        log.info("更新产品成功: 价格={}", updatedProduct.getPrice());

        // Delete
        productRepository.deleteById(savedProduct.getId());
        Optional<Product> deletedProduct = productRepository.findById(savedProduct.getId());
        assertFalse(deletedProduct.isPresent());
        log.info("删除产品成功");
    }

    @Test
    @DisplayName("测试查询方法 - 用户")
    void testUserQueryMethods() {
        // 测试根据用户名查询
        Optional<User> userByUsername = userRepository.findByUsername("testuser");
        assertTrue(userByUsername.isPresent());
        assertEquals("testuser", userByUsername.get().getUsername());

        // 测试根据年龄大于某个值查询
        List<User> usersAbove20 = userRepository.findByAgeGreaterThan(20);
        assertFalse(usersAbove20.isEmpty());
        assertTrue(usersAbove20.stream().allMatch(user -> user.getAge() > 20));

        // 测试根据激活状态查询
        List<User> activeUsers = userRepository.findByActiveTrue();
        assertFalse(activeUsers.isEmpty());
        assertTrue(activeUsers.stream().allMatch(User::getActive));

        // 测试根据姓名模糊查询
        List<User> usersWithName = userRepository.findByFullNameContainingIgnoreCase("test");
        assertFalse(usersWithName.isEmpty());

        // 测试自定义查询 - 年龄范围
        List<User> usersInAgeRange = userRepository.findByAgeBetween(20, 30);
        assertFalse(usersInAgeRange.isEmpty());

        // 测试统计查询
        long activeUserCount = userRepository.countByActive(true);
        assertTrue(activeUserCount > 0);
    }

    @Test
    @DisplayName("测试查询方法 - 产品")
    void testProductQueryMethods() {
        // 测试根据分类查询
        List<Product> electronics = productRepository.findByCategory("Electronics");
        assertEquals(2, electronics.size());
        assertTrue(electronics.stream().allMatch(p -> "Electronics".equals(p.getCategory())));

        // 测试价格小于某个值查询
        List<Product> cheapProducts = productRepository.findByPriceLessThan(new BigDecimal("1000"));
        assertFalse(cheapProducts.isEmpty());
        assertTrue(cheapProducts.stream().allMatch(p -> p.getPrice().compareTo(new BigDecimal("1000")) < 0));

        // 测试可用产品按价格升序排列
        List<Product> availableProducts = productRepository.findByAvailableTrueOrderByPriceAsc();
        assertTrue(availableProducts.size() >= 3);
        for (int i = 0; i < availableProducts.size() - 1; i++) {
            assertTrue(availableProducts.get(i).getPrice()
                    .compareTo(availableProducts.get(i + 1).getPrice()) <= 0);
        }

        // 测试分页查询
        Page<Product> productPage = productRepository.findByCategoryAndAvailableTrue("Electronics",
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "price")));
        assertNotNull(productPage);
        assertTrue(productPage.getContent().size() > 0);

        // 测试名称模糊查询
        List<Product> productsWithName = productRepository.findByNameContainingIgnoreCase("book");
        assertEquals(1, productsWithName.size());
        assertEquals("Book", productsWithName.get(0).getName());

        // 测试复合条件查询
        List<Product> electronicsInRange = productRepository.findByCategoryAndPriceBetween(
                "Electronics", new BigDecimal("2000"), new BigDecimal("7000"));
        assertEquals(2, electronicsInRange.size());

        // 测试统计查询
        long electronicsCount = productRepository.countByCategory("Electronics");
        assertEquals(2, electronicsCount);
    }

    @Test
    @DisplayName("测试MongoTemplate原生操作")
    void testMongoTemplateOperations() {
        // 使用MongoTemplate进行复杂查询
        Query query = new Query();
        query.addCriteria(Criteria.where("category").is("Electronics")
                .and("price").gt(new BigDecimal("2000")));

        List<Product> expensiveElectronics = mongoTemplate.find(query, Product.class);
        assertFalse(expensiveElectronics.isEmpty());
        log.info("MongoTemplate查询结果数量: {}", expensiveElectronics.size());

        // 测试更新操作
        Query updateQuery = new Query(Criteria.where("username").is("testuser"));
        org.springframework.data.mongodb.core.query.Update update =
                new org.springframework.data.mongodb.core.query.Update()
                        .set("age", 26)
                        .set("updatedAt", LocalDateTime.now());

        mongoTemplate.updateFirst(updateQuery, update, User.class);

        Optional<User> updatedUser = userRepository.findByUsername("testuser");
        assertTrue(updatedUser.isPresent());
        assertEquals(26, updatedUser.get().getAge());
        log.info("MongoTemplate更新操作成功");

        // 测试聚合操作示例
        long totalProducts = mongoTemplate.count(new Query(), Product.class);
        assertTrue(totalProducts > 0);
        log.info("MongoTemplate计数操作成功: {}", totalProducts);
    }

    @Test
    @DisplayName("测试批量操作")
    void testBatchOperations() {
        // 批量插入用户
        List<User> batchUsers = Arrays.asList(
                createUser("batch1", "batch1@example.com", "Batch User 1", 28, true),
                createUser("batch2", "batch2@example.com", "Batch User 2", 32, true),
                createUser("batch3", "batch3@example.com", "Batch User 3", 25, false)
        );

        List<User> savedBatchUsers = userRepository.saveAll(batchUsers);
        assertEquals(3, savedBatchUsers.size());
        log.info("批量插入用户成功: {}个", savedBatchUsers.size());

        // 批量查询验证
        List<User> activeBatchUsers = userRepository.findByActiveTrue();
        assertTrue(activeBatchUsers.size() >= 2); // 包含原有的testuser

        // 批量删除
        userRepository.deleteAll(savedBatchUsers);
        List<User> remainingUsers = userRepository.findAll();
        assertEquals(1, remainingUsers.size()); // 只剩下原来的testuser
        log.info("批量删除操作成功");
    }

    @Test
    @DisplayName("测试索引和性能")
    void testIndexesAndPerformance() {
        // 测试唯一索引约束
        User duplicateUser = createUser("testuser", "duplicate@example.com", "Duplicate User", 30, true);
        assertThrows(Exception.class, () -> userRepository.save(duplicateUser));
        log.info("唯一索引约束测试通过");

        // 测试复合索引查询性能
        long startTime = System.currentTimeMillis();
        List<Product> indexedQueryResult = productRepository.findByCategoryAndPriceBetween(
                "Electronics", new BigDecimal("1000"), new BigDecimal("10000"));
        long endTime = System.currentTimeMillis();

        log.info("复合索引查询耗时: {} ms, 结果数量: {}",
                endTime - startTime, indexedQueryResult.size());
        assertTrue(endTime - startTime < 1000); // 应该在1秒内完成
    }

    // 辅助方法
    private User createUser(String username, String email, String fullName, Integer age, Boolean active) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setAge(age);
        user.setActive(active);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    private Product createProduct(String name, String description, String category,
                                  BigDecimal price, Integer stock, Boolean available) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setCategory(category);
        product.setPrice(price);
        product.setStock(stock);
        product.setAvailable(available);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }
}
