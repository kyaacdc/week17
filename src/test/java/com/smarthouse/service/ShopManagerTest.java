package com.smarthouse.service;

import com.smarthouse.repository.*;
import com.smarthouse.pojo.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.TransactionSystemException;
import javax.persistence.NoResultException;
import java.util.*;

import static com.smarthouse.service.util.enums.EnumProductSorter.*;
import static com.smarthouse.service.util.enums.EnumSearcher.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ShopManagerTest {

    @Autowired
    private ShopManager shopManager;
    @Autowired
    private ProductCardRepository productCardRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private OrderMainRepository orderMainRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private AttributeValueRepository attributeValueRepository;
    @Autowired
    private AttributeNameRepository attributeNameRepository;
    @Autowired
    private VisualizationRepository visualizationRepository;

    @After
    public void after(){
        attributeValueRepository.deleteAll();
        attributeNameRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderMainRepository.deleteAll();
        customerRepository.deleteAll();
        visualizationRepository.deleteAll();
        productCardRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    public void mustShowCorrectProductAvailability() throws Exception {
        Category category = new Category("desc", "name", null);
        category = categoryRepository.save(category);

        ProductCard productCard = new ProductCard("888", "2name", 2222, 34, 45, 4, "xxx", category);

        //Save to DB
        productCard = productCardRepository.save(productCard);
        assertTrue(shopManager.isProductAvailable(productCard.getSku()));
    }

    @Test(expected = TransactionSystemException.class)
    public void shouldThrowExceptionWhenMakeIncorrectOrder() throws Exception {
        Category category = categoryRepository.save(new Category("desc", "catname", null));
        productCardRepository.save(new ProductCard("bell", "bell signal", 1234, 100, 1, 1, "bell desc", category));

        shopManager.createOrder("kya@bk.ru", "Y1uriy", "0503337178", "my address", 3, "bell");
    }

    @Test
    public void shouldMakeCorrectOrder() {
        Category category = categoryRepository.save(new Category("desc", "catname", null));
        productCardRepository.save(new ProductCard("bell", "bell signal", 1234, 100, 1, 1, "bell desc", category));

        shopManager.createOrder("kya@bk.ru", "Yuriy", "0503337178", "my address", 3, "bell");

        assertThat(productCardRepository.findOne("bell").getAmount(), is(equalTo(100)));

        orderItemRepository.deleteAll();
        orderMainRepository.deleteAll();
        customerRepository.deleteAll();
        productCardRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test(expected = NoResultException.class)
    public void shouldThrowExceptionWhenMakeOrder() {
        Category category = categoryRepository.save(new Category("desc", "catname", null));
        productCardRepository.save(new ProductCard("bell", "bell signal", 1234, 100, 1, 1, "bell desc", category));

        shopManager.createOrder("kya@bk.ru", "Yuriy", "0503337178", "my address", 103, "bell");
    }

    @Test
    public void shouldCheckCorrectionOfCompleteOrder() {

        Category category = categoryRepository.save(new Category("desc", "catname", null));
        productCardRepository.save(new ProductCard("bell", "bell signal", 1234, 100, 1, 1, "bell desc", category));
        productCardRepository.save(new ProductCard("ring", "ring signal", 1234, 50, 1, 1, "bell desc", category));
        productCardRepository.save(new ProductCard("lord", "lord signal", 1234, 10, 1, 1, "bell desc", category));
        shopManager.createOrder("kya@bk.ru", "Yuriy", "0503337178", "my address", 3, "bell");
        shopManager.createOrder("kya@bk.ru", "Yuriy", "0503337178", "my address", 3, "ring");
        shopManager.createOrder("kya@bk.ru", "Yuriy", "0503337178", "my address", 3, "lord");

        List<OrderMain> orderMains = orderMainRepository.findByCustomer(customerRepository.findOne("kya@bk.ru"));

        for (OrderMain om : orderMains) {
            assertThat(om.getStatus() == 1, is(true));
            List<OrderItem> orderItems = orderItemRepository.findByOrderMain(om);
            for (OrderItem oi : orderItems)
                assertThat(productCardRepository.findOne(oi.getProductCard().getSku()).getAmount(), oneOf(100, 50, 10));
        }

        shopManager.submitOrder("kya@bk.ru");

        orderMains = orderMainRepository.findByCustomer(customerRepository.findOne("kya@bk.ru"));

        for (OrderMain om : orderMains) {
            assertThat(om.getStatus() != 1, is(true));
            assertThat(om.getCustomer().getEmail(), is(equalTo("kya@bk.ru")));
            List<OrderItem> orderItems = orderItemRepository.findByOrderMain(om);
            for (OrderItem oi : orderItems)
                assertThat(productCardRepository.findOne(oi.getProductCard().getSku()).getAmount(), oneOf(97, 47, 7));
        }
    }

    @Test
    public void shouldCheckRightValidationOfOrderCreation() {
        Category category = categoryRepository.save(new Category("desc", "catname", null));
        productCardRepository.save(new ProductCard("bell", "bell signal", 1234, 100, 1, 1, "bell desc", category));

        shopManager.createOrder("kya@bk.ru", "Yuriy", "0503337178", "my address", 3, "bell");

        assertThat(productCardRepository.findOne("bell").getAmount(), is(equalTo(100)));
        assertThat(shopManager.validateOrder("kya@bk.ru"), is(true));
    }

    @Test
    public void shouldGetCorrectListOrderedByCustomer() {
        Customer customer = new Customer("anniya@bk.ru", "Yuriy", false, "7585885");
        customerRepository.save(customer);

        OrderMain orderMain1 = new OrderMain("1OrderAddress", 1, customer);
        OrderMain orderMain2 = new OrderMain("2OrderAddress", 1, customer);
        OrderMain orderMain3 = new OrderMain("3OrderAddress", 1, customer);

        //Save to DB
        orderMainRepository.save(orderMain1);
        orderMainRepository.save(orderMain2);
        orderMainRepository.save(orderMain3);

        List<OrderMain> ordersMain = orderMainRepository.findByCustomer(customer);

        for (OrderMain o : ordersMain) {
            assertThat(o, is(notNullValue()));
            assertThat(o, is(anything()));
            assertThat(o.getAddress(), oneOf("1OrderAddress", "2OrderAddress", "3OrderAddress"));
            assertThat(o, isA(OrderMain.class));
        }
    }

    @Test
    public void shouldGetRealItemOrdersByOrderMain() {
        Category category = categoryRepository.save(new Category("desc", "name", null));
        ProductCard productCard = productCardRepository.save(new ProductCard("111", "name", 123, 1, 1, 1, "decs", category));
        Customer customer = customerRepository.save(new Customer("anniya@bk.ru", "Yuriy", false, "7585885"));
        OrderMain orderMain = orderMainRepository.save(new OrderMain("OrderAddress", 1, customer));
        OrderItem orderItem1 = orderItemRepository.save(new OrderItem(5, 555, productCard, orderMain));
        OrderItem orderItem2 = orderItemRepository.save(new OrderItem(6, 555, productCard, orderMain));
        OrderItem orderItem3 = orderItemRepository.save(new OrderItem(7, 555, productCard, orderMain));

        //Save to DB
        orderItemRepository.save(orderItem1);
        orderItemRepository.save(orderItem2);
        orderItemRepository.save(orderItem3);

        List<OrderItem> orderItems = orderItemRepository.findByOrderMain(orderMain);

        for (OrderItem o : orderItems) {
            assertThat(o.getAmount(), oneOf(5, 6, 7));
            assertThat(orderItems, is(notNullValue()));
            assertThat(orderItems, is(anything()));
            assertThat(orderItems.get(0), isA(OrderItem.class));
        }
    }

    @Test
    public void shouldGetRightItemOrdersByProdCard() {
        Category category = categoryRepository.save(new Category("desc", "name", null));
        ProductCard productCard = productCardRepository.save(new ProductCard("111", "name", 123, 1, 1, 1, "decs", category));
        Customer customer = customerRepository.save(new Customer("anniya@bk.ru", "Yuriy", false, "7585885"));
        OrderMain orderMain = orderMainRepository.save(new OrderMain("OrderAddress", 1, customer));
        OrderItem orderItem1 = orderItemRepository.save(new OrderItem(5, 555, productCard, orderMain));
        OrderItem orderItem2 = orderItemRepository.save(new OrderItem(6, 555, productCard, orderMain));
        OrderItem orderItem3 = orderItemRepository.save(new OrderItem(7, 555, productCard, orderMain));

        //Save to DB
        orderItemRepository.save(orderItem1);
        orderItemRepository.save(orderItem2);
        orderItemRepository.save(orderItem3);

        List<OrderItem> orderItems = orderItemRepository.findByProductCard(productCard);

        for (OrderItem o : orderItems) {
            assertThat(o.getAmount(), oneOf(5, 6, 7));
            assertThat(orderItems, is(notNullValue()));
            assertThat(orderItems, is(anything()));
            assertThat(orderItems.get(0), isA(OrderItem.class));
        }
    }

    @Test
    public void shouldCorrectGetItemOrdersByProdCard() {
        Category category = categoryRepository.save(new Category("desc", "name", null));
        ProductCard productCard = productCardRepository.save(new ProductCard("111", "name", 123, 1, 1, 1, "decs", category));
        Customer customer = customerRepository.save(new Customer("anniya@bk.ru", "Yuriy", false, "7585885"));
        OrderMain orderMain = orderMainRepository.save(new OrderMain("OrderAddress", 1, customer));
        OrderItem orderItem1 = orderItemRepository.save(new OrderItem(5, 555, productCard, orderMain));
        OrderItem orderItem2 = orderItemRepository.save(new OrderItem(6, 555, productCard, orderMain));
        OrderItem orderItem3 = orderItemRepository.save(new OrderItem(7, 555, productCard, orderMain));

        //Save to DB
        orderItemRepository.save(orderItem1);
        orderItemRepository.save(orderItem2);
        orderItemRepository.save(orderItem3);

        List<OrderItem> orderItems = orderItemRepository.findByProductCard(productCard);

        for (OrderItem o : orderItems) {
            assertThat(o.getAmount(), oneOf(5, 6, 7));
            assertThat(orderItems, is(notNullValue()));
            assertThat(orderItems, is(anything()));
            assertThat(orderItems.get(0), isA(OrderItem.class));
        }
    }

    @Test
    public void shouldCheckCorrectionOfSearchAllProductsByDifferCriteria() throws Exception {

        Category category = new Category("desc", "name", null);
        category = categoryRepository.save(category);

        ProductCard productCard1 = new ProductCard("888", "name111", 2222, 34, 45, 4, "xxx", category);
        ProductCard productCard2 = new ProductCard("999", "name111", 2222, 34, 45, 4, "xxx", category);

        //Save to DB
        productCardRepository.save(productCard1);
        productCardRepository.save(productCard2);

        Set<ProductCard> allProducts = shopManager.findAllProductsByCriteria("Name111");
        assertThat(allProducts.size(), is(equalTo(2)));

        allProducts = shopManager.findAllProductsByCriteria("Desc");
        assertThat(allProducts.size(), is(equalTo(2)));

        allProducts = shopManager.findAllProductsByCriteria("nAme");
        assertThat(allProducts.size(), is(equalTo(2)));
    }

    @Test
    public void shouldCheckCorrectionOfSearchWithFindByName() throws Exception {

        Category category = new Category("desc", "name", null);
        category = categoryRepository.save(category);

        ProductCard productCard1 = new ProductCard("888", "name111", 2222, 34, 45, 4, "xxx", category);
        ProductCard productCard2 = new ProductCard("999", "name111", 2222, 34, 45, 4, "xxx", category);

        //Save in DB
        productCardRepository.save(productCard1);
        productCardRepository.save(productCard2);

        Set<ProductCard> allProducts = shopManager.findProductsIn("naMe111", FIND_IN_NAME);
        assertThat(allProducts.size(), is(equalTo(2)));
    }

    @Test
    public void shouldCheckCorrectionOfSearchByProductDescription() throws Exception {

        Category category = new Category("desc", "name", null);
        category = categoryRepository.save(category);

        ProductCard productCard1 = new ProductCard("888", "name111", 2222, 34, 45, 4, "xxx", category);
        ProductCard productCard2 = new ProductCard("999", "name111", 2222, 34, 45, 4, "xxx", category);

        //Save in DB
        productCardRepository.save(productCard1);
        productCardRepository.save(productCard2);

        Set<ProductCard> allProducts = shopManager.findProductsIn("xXx", FIND_IN_PROD_DESC);
        assertThat(allProducts.size(), is(equalTo(2)));
    }

    @Test
    public void shouldCheckCorrectionOfSearchByCategoryDescription() throws Exception {

        Category category = new Category("desc", "name", null);
        category = categoryRepository.save(category);

        ProductCard productCard1 = new ProductCard("888", "name111", 2222, 34, 45, 4, "xxx", category);
        ProductCard productCard2 = new ProductCard("999", "name111", 2222, 34, 45, 4, "xxx", category);

        //Save in DB
        productCardRepository.save(productCard1);
        productCardRepository.save(productCard2);

        Set<ProductCard> allProducts = shopManager.findProductsIn("DESC", FIND_IN_CATEGORY_DESC);
        assertThat(allProducts.size(), is(equalTo(2)));
    }

    @Test
    public void shouldCheckCorrectionOfSearchAllProductsByCriteriaAndSituatedPlace() throws Exception {

        Category category = new Category("desc", "name", null);
        category = categoryRepository.save(category);

        ProductCard productCard1 = new ProductCard("888", "name111", 2222, 34, 45, 4, "xxx", category);
        ProductCard productCard2 = new ProductCard("999", "name111", 2222, 34, 45, 4, "xxx", category);

        //Save in DB
        productCardRepository.save(productCard1);
        productCardRepository.save(productCard2);

        Set<ProductCard> allProducts = shopManager.findProductsIn("naMe111", FIND_IN_NAME);
        assertThat(allProducts.size(), is(equalTo(2)));
        allProducts = shopManager.findProductsIn("xXx", FIND_IN_PROD_DESC);
        assertThat(allProducts.size(), is(equalTo(2)));
        allProducts = shopManager.findProductsIn("DESC", FIND_IN_CATEGORY_DESC);
        assertThat(allProducts.size(), is(equalTo(2)));
        allProducts = shopManager.findProductsIn("naMe", FIND_IN_CATEGORY_NAME);
        assertThat(allProducts.size(), is(equalTo(2)));
    }

    @Test
    public void shouldGetCorrectRootCategory() throws Exception {

        Category category = new Category("desc", "name", null);

        categoryRepository.save(category);

        List<Category> list = shopManager.getRootCategory();
        assertThat(list.get(0).getName(), is(equalTo("name")));
    }

    @Test
    public void shouldGetExistedSubCategories() throws Exception {

        Category category1 = new Category("desc", "name", null);
        category1 = categoryRepository.save(category1);
        Category category2 = new Category("desc", "name", category1);
        categoryRepository.save(category2);

        //List<Category> list = shopManager.getSubCategories(categoryRepository.findOne(category1.getId()));
        List<Category> list = shopManager.getSubCategories(category1.getId());
        assertThat(list.get(0).getName(), oneOf("catname1", "catname31", "name"));
        assertThat(list.get(0).getId(), isA(Integer.class));
    }

    @Test
    public void shouldGetCorrectProductCardsByCategory() throws Exception {
        Category category = new Category("desc", "name", null);
        category = categoryRepository.save(category);

        ProductCard productCard1 = new ProductCard("888", "2name", 2222, 34, 45, 4, "xxx", category);
        ProductCard productCard2 = new ProductCard("999", "3name", 3333, 34, 45, 4, "xxx", category);
        ProductCard productCard3 = new ProductCard("000", "4name", 444, 34, 45, 4, "xxx", category);

        //Save in DB
        productCard1 = productCardRepository.save(productCard1);
        productCard2 = productCardRepository.save(productCard2);
        productCard3 = productCardRepository.save(productCard3);

        List<ProductCard> productCards = shopManager.getProductCardsByCategory(category.getId());

        assertThat(productCard1.getName(), is (equalTo("2name")));
        assertThat(productCard2.getName(), is (equalTo("3name")));
        assertThat(productCard3.getName(), is (equalTo("4name")));
        assertThat(productCards, is(notNullValue()));
        assertThat(productCards, is(anything()));
        assertThat(productCards.get(1), isA(ProductCard.class));
    }

    @Test
    public void shouldCheckOfGetVisualListByProduct() throws Exception {
        Category category = new Category("desc", "name", null);
        category = categoryRepository.save(category);
        ProductCard productCard = new ProductCard("222", "2name", 2222, 34, 45, 4, "xxx", category);
        productCard = productCardRepository.save(productCard);

        //Save in DB
        Visualization visualization1 = new Visualization(2, "1url", productCard);
        Visualization visualization2 = new Visualization(4, "2url", productCard);
        Visualization visualization3 = new Visualization(6, "3url", productCard);

        visualization1 = visualizationRepository.save(visualization1);
        visualization2 = visualizationRepository.save(visualization2);
        visualization3 = visualizationRepository.save(visualization3);

        List<Visualization> visualizations = shopManager.getVisualListByProduct(productCard.getSku());
        //List<Visualization> visualizations = visualizationRepository.findByProductCard(productCard.getSku());

        assertThat(visualizations, is(notNullValue()));
        assertThat(visualizations, is(anything()));
        assertThat(visualizations.get(0), isA(Visualization.class));
        assertThat(visualization1.getUrl(), is(equalTo("1url")));
        assertThat(visualization2.getType(), is(equalTo(4)));
        assertThat(visualization3.getUrl(), is(equalTo("3url")));
    }

    @Test
    public void mustCorrectGetAttrValuesByProduct() throws Exception {
        Category category = new Category("desc", "name", null);
        category = categoryRepository.save(category);
        ProductCard productCard = new ProductCard("222", "2name", 2222, 34, 45, 4, "xxx", category);
        productCard = productCardRepository.save(productCard);
        AttributeName attributeName = new AttributeName("color");
        attributeNameRepository.save(attributeName);
        AttributeValue attributeValue = new AttributeValue("1", attributeName, productCard);
        attributeValueRepository.save(attributeValue);
        attributeValue = new AttributeValue("2", attributeName, productCard);
        attributeValueRepository.save(attributeValue);
        attributeValue = new AttributeValue("3", attributeName, productCard);
        attributeValueRepository.save(attributeValue);

        List<AttributeValue> list = shopManager.getAttrValuesByProduct("222");

        list.forEach(a -> {
            assertThat(a.getValue(), oneOf("1", "2", "3"));
            assertThat(a, is(notNullValue()));
            assertThat(a.getValue(), isA(String.class));
            assertThat(a, isA(AttributeValue.class));
        });
    }

    @Test
    public void shouldGetRealAttrValuesByName() throws Exception {
        Category category = new Category("desc", "name", null);
        category = categoryRepository.save(category);
        ProductCard productCard = new ProductCard("222", "2name", 2222, 34, 45, 4, "xxx", category);
        productCard = productCardRepository.save(productCard);
        AttributeName attributeName = new AttributeName("color");
        attributeNameRepository.save(attributeName);
        AttributeValue attributeValue = new AttributeValue("1", attributeName, productCard);
        attributeValueRepository.save(attributeValue);
        attributeValue = new AttributeValue("2", attributeName, productCard);
        attributeValueRepository.save(attributeValue);
        attributeValue = new AttributeValue("3", attributeName, productCard);
        attributeValueRepository.save(attributeValue);

        List<AttributeValue> list = shopManager.getAttributeValuesByName(attributeNameRepository.save(attributeName).getName());

        list.forEach(a -> {
            assertThat(a.getValue(), oneOf("1", "2", "3"));
            assertThat(a, is(notNullValue()));
            assertThat(a.getValue(), isA(String.class));
            assertThat(a, isA(AttributeValue.class));
        });
    }

    @Test
    public void shouldRightSortProductsByPopularity() throws Exception {

        productCardRepository.deleteAll();

        Category category = new Category("desc", "name", null);
        category = categoryRepository.save(category);

        Category subCategory = new Category("desc", "name", category);
        subCategory = categoryRepository.save(subCategory);

        ProductCard productCard = new ProductCard("111", "1name", 2222, 34, 1, 1, "xxx", category);
        productCardRepository.save(productCard);
        productCard = new ProductCard("333", "3name", 2222, 34, 3, 3, "xxx", category);
        productCardRepository.save(productCard);
        productCard = new ProductCard("222", "2name", 2222, 34, 2, 2, "xxx", category);
        productCardRepository.save(productCard);

        productCard = new ProductCard("444", "4name", 4444, 34, 4, 4, "xxx", subCategory);
        productCardRepository.save(productCard);

        List<ProductCard> productCards = shopManager.sortProductCardBy(0, SORT_BY_POPULARITY);
        assertThat(productCards.get(3).getLikes(), is(equalTo(4)));
        for(ProductCard p: productCards)
            assertThat(p.getLikes(), oneOf(1,2,3,4));
    }

    @Test
    public void shouldRightSortProductsByPopularityFromCategory() throws Exception {

        Category category = new Category("desc", "name", null);
        category = categoryRepository.save(category);

        Category subCategory = new Category("desc", "name", category);
        subCategory = categoryRepository.save(subCategory);

        ProductCard productCard = new ProductCard("111", "1name", 2222, 34, 1, 1, "xxx", category);
        productCardRepository.save(productCard);
        productCard = new ProductCard("333", "3name", 2222, 34, 3, 3, "xxx", category);
        productCardRepository.save(productCard);
        productCard = new ProductCard("222", "2name", 2222, 34, 2, 2, "xxx", category);
        productCardRepository.save(productCard);

        productCard = new ProductCard("444", "4name", 4444, 34, 4, 4, "xxx", subCategory);
        productCardRepository.save(productCard);

        List<ProductCard> productCards = shopManager.sortProductCardBy(category.getId(), SORT_BY_POPULARITY);
        assertThat(productCards.get(2).getLikes(), is(equalTo(3)));
        for(ProductCard p: productCards)
            assertThat(p.getLikes(), oneOf(1,2,3));
    }

    @Test
    public void shouldRightSortProductsByUnpopularity() throws Exception {

        Category category = new Category("desc", "name", null);
        category = categoryRepository.save(category);

        Category subCategory = new Category("desc", "name", category);
        subCategory = categoryRepository.save(subCategory);

        ProductCard productCard = new ProductCard("111", "1name", 2222, 34, 1, 1, "xxx", category);
        productCardRepository.save(productCard);
        productCard = new ProductCard("333", "3name", 2222, 34, 3, 3, "xxx", category);
        productCardRepository.save(productCard);
        productCard = new ProductCard("222", "2name", 2222, 34, 2, 2, "xxx", category);
        productCardRepository.save(productCard);

        productCard = new ProductCard("444", "4name", 4444, 34, 4, 4, "xxx", subCategory);
        productCardRepository.save(productCard);

        List<ProductCard> productCards = shopManager.sortProductCardBy(0, SORT_BY_UNPOPULARITY);
        assertThat(productCards.get(1).getDislikes(), is(equalTo(2)));
        for(ProductCard p: productCards)
            assertThat(p.getDislikes(), oneOf(1,2,3,4));
    }

    @Test
    public void shouldRightSortProductsByUnpopularityFromCategory() throws Exception {

        Category category = new Category("desc", "name", null);
        category = categoryRepository.save(category);

        Category subCategory = new Category("desc", "name", category);
        subCategory = categoryRepository.save(subCategory);

        ProductCard productCard = new ProductCard("111", "1name", 2222, 34, 1, 1, "xxx", category);
        productCardRepository.save(productCard);
        productCard = new ProductCard("333", "3name", 2222, 34, 3, 3, "xxx", category);
        productCardRepository.save(productCard);
        productCard = new ProductCard("222", "2name", 2222, 34, 2, 2, "xxx", category);
        productCardRepository.save(productCard);

        productCard = new ProductCard("444", "4name", 4444, 34, 4, 4, "xxx", subCategory);
        productCardRepository.save(productCard);

        List<ProductCard> productCards = shopManager.sortProductCardBy(category.getId(), SORT_BY_UNPOPULARITY);
        assertThat(productCards.get(2).getDislikes(), is(equalTo(3)));
        for(ProductCard p: productCards)
            assertThat(p.getDislikes(), oneOf(1,2,3));
    }

    @Test
    public void shouldCorrectSortAllProductsByName() throws Exception {

        Category category = new Category("desc", "name", null);
        category = categoryRepository.save(category);

        Category subCategory = new Category("desc", "name", category);
        subCategory = categoryRepository.save(subCategory);

        ProductCard productCard = new ProductCard("111", "1name", 2222, 34, 1, 1, "xxx", category);
        productCardRepository.save(productCard);
        productCard = new ProductCard("333", "3name", 2222, 34, 3, 3, "xxx", category);
        productCardRepository.save(productCard);
        productCard = new ProductCard("222", "2name", 2222, 34, 2, 2, "xxx", category);
        productCardRepository.save(productCard);

        productCard = new ProductCard("444", "4name", 4444, 34, 4, 4, "xxx", subCategory);
        productCardRepository.save(productCard);

        List<ProductCard> productCards = shopManager.sortProductCardBy(0, SORT_BY_NAME);
        assertThat(productCards.get(1).getName(), is(equalTo("2name")));
    }

    @Test
    public void shouldCorrectSortProductsByNameWithCategory() throws Exception {

        Category category = new Category("desc", "name", null);
        category = categoryRepository.save(category);

        Category subCategory = new Category("desc", "name", category);
        subCategory = categoryRepository.save(subCategory);

        ProductCard productCard = new ProductCard("333", "3name", 2222, 34, 3, 3, "xxx", category);
        productCardRepository.save(productCard);

        List<ProductCard> productCards = shopManager.sortProductCardBy(category.getId(), SORT_BY_NAME);
        assertEquals(productCards.get(0).getName(), "3name");
    }

    @Test
    public void shouldCorrectSortProductsByNameReversedWithCategory() throws Exception {

        Category category = new Category("desc", "name", null);
        category = categoryRepository.save(category);

        Category subCategory = new Category("desc", "name", category);
        subCategory = categoryRepository.save(subCategory);

        ProductCard productCard = new ProductCard("111", "1name", 2222, 34, 1, 1, "xxx", category);
        productCardRepository.save(productCard);
        productCard = new ProductCard("333", "3name", 2222, 34, 3, 3, "xxx", category);
        productCardRepository.save(productCard);
        productCard = new ProductCard("222", "2name", 2222, 34, 2, 2, "xxx", category);
        productCardRepository.save(productCard);

        productCard = new ProductCard("444", "4name", 4444, 34, 4, 4, "xxx", subCategory);
        productCardRepository.save(productCard);


        List<ProductCard> productCards = shopManager.sortProductCardBy(category.getId(), SORT_BY_NAME_REVERSED);
        assertThat(productCards.get(1).getName(), is(equalTo("2name")));
    }

    @Test
    public void shouldCorrectSortAllProductsByNameReversed() throws Exception {

        Category category = new Category("desc", "name", null);
        category = categoryRepository.save(category);

        Category subCategory = new Category("desc", "name", category);
        subCategory = categoryRepository.save(subCategory);

        ProductCard productCard = new ProductCard("111", "1name", 2222, 34, 1, 1, "xxx", category);
        productCardRepository.save(productCard);
        productCard = new ProductCard("333", "3name", 2222, 34, 3, 3, "xxx", category);
        productCardRepository.save(productCard);
        productCard = new ProductCard("222", "2name", 2222, 34, 2, 2, "xxx", category);
        productCardRepository.save(productCard);

        productCard = new ProductCard("444", "4name", 4444, 34, 4, 4, "xxx", subCategory);
        productCardRepository.save(productCard);


        List<ProductCard> productCards = shopManager.sortProductCardBy(0, SORT_BY_NAME_REVERSED);
        assertThat(productCards.get(1).getName(), is(equalTo("3name")));
    }

    @Test
    public void mustCorrectAllSortProductsByPrice() throws Exception {

        Category category = new Category("desc", "name", null);
        category = categoryRepository.save(category);

        Category subCategory = new Category("desc", "name", category);
        subCategory = categoryRepository.save(subCategory);

        ProductCard productCard = new ProductCard("111", "1name", 11111, 34, 1, 1, "xxx", category);
        productCardRepository.save(productCard);
        productCard = new ProductCard("333", "3name", 33333, 34, 3, 3, "xxx", category);
        productCardRepository.save(productCard);
        productCard = new ProductCard("222", "2name", 22222, 34, 2, 2, "xxx", category);
        productCardRepository.save(productCard);

        productCard = new ProductCard("444", "4name", 44444, 34, 4, 4, "xxx", subCategory);
        productCardRepository.save(productCard);

        List<ProductCard> productCards = shopManager.sortProductCardBy(0, SORT_BY_HIGH_PRICE);
        assertThat(productCards.get(0).getPrice(), is(equalTo(44444)));
    }

    @Test
    public void mustCorrectSortAllProductsByLowPrice() throws Exception {

        Category category = new Category("desc", "name", null);
        category = categoryRepository.save(category);

        Category subCategory = new Category("desc", "name", category);
        subCategory = categoryRepository.save(subCategory);

        ProductCard productCard = new ProductCard("111", "1name", 11111, 34, 1, 1, "xxx", category);
        productCardRepository.save(productCard);
        productCard = new ProductCard("333", "3name", 33333, 34, 3, 3, "xxx", category);
        productCardRepository.save(productCard);
        productCard = new ProductCard("222", "2name", 22222, 34, 2, 2, "xxx", category);
        productCardRepository.save(productCard);

        productCard = new ProductCard("444", "4name", 44444, 34, 4, 4, "xxx", subCategory);
        productCardRepository.save(productCard);

        List<ProductCard> productCards =  shopManager.sortProductCardBy(0, SORT_BY_LOW_PRICE);
        assertThat(productCards.get(0).getPrice(), is(equalTo(11111)));
    }

    @Test
    public void mustCorrectSortProductsByHighPriceWithCategory() throws Exception {

        Category category = new Category("desc", "name", null);
        category = categoryRepository.save(category);

        Category subCategory = new Category("desc", "name", category);
        subCategory = categoryRepository.save(subCategory);

        ProductCard productCard = new ProductCard("111", "1name", 11111, 34, 1, 1, "xxx", category);
        productCardRepository.save(productCard);
        productCard = new ProductCard("333", "3name", 33333, 34, 3, 3, "xxx", category);
        productCardRepository.save(productCard);
        productCard = new ProductCard("222", "2name", 22222, 34, 2, 2, "xxx", category);
        productCardRepository.save(productCard);

        productCard = new ProductCard("444", "4name", 44444, 34, 4, 4, "xxx", subCategory);
        productCardRepository.save(productCard);

        List<ProductCard> productCards = shopManager.sortProductCardBy(category.getId(), SORT_BY_HIGH_PRICE);
        assertThat(productCards.get(1).getPrice(), is(equalTo(22222)));
    }

    @Test
    public void mustCorrectSortProductsByLowPriceWithCategory() throws Exception {

        Category category = new Category("desc", "name", null);
        category = categoryRepository.save(category);

        Category subCategory = new Category("desc", "name", category);
        subCategory = categoryRepository.save(subCategory);

        ProductCard productCard = new ProductCard("111", "1name", 11111, 34, 1, 1, "xxx", category);
        productCardRepository.save(productCard);
        productCard = new ProductCard("333", "3name", 33333, 34, 3, 3, "xxx", category);
        productCardRepository.save(productCard);
        productCard = new ProductCard("222", "2name", 22222, 34, 2, 2, "xxx", category);
        productCardRepository.save(productCard);

        productCard = new ProductCard("444", "4name", 44444, 34, 4, 4, "xxx", subCategory);
        productCardRepository.save(productCard);

        List<ProductCard> productCards = shopManager.sortProductCardBy(category.getId(), SORT_BY_LOW_PRICE);
        assertThat(productCards.get(1).getPrice(), is(equalTo(22222)));
    }

}
