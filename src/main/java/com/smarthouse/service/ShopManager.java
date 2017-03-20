package com.smarthouse.service;

import com.smarthouse.repository.*;
import com.smarthouse.pojo.*;
import com.smarthouse.service.util.validators.EmailValidator;
import com.smarthouse.service.util.enums.EnumProductSorter;
import com.smarthouse.service.util.enums.EnumSearcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.NoResultException;
import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@RestController
public class ShopManager {

    private ProductCardRepository productCardRepository;
    private CategoryRepository categoryRepository;
    private CustomerRepository customerRepository;
    private OrderMainRepository orderMainRepository;
    private OrderItemRepository orderItemRepository;
    private VisualizationRepository visualizationRepository;
    private AttributeValueRepository attributeValueRepository;

    @Autowired
    public void setProductCardRepository(ProductCardRepository productCardRepository) {
        this.productCardRepository = productCardRepository;
    }
    @Autowired
    public void setCategoryRepository(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    @Autowired
    public void setCustomerRepository(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
    @Autowired
    public void setOrderMainRepository(OrderMainRepository orderMainRepository) {
        this.orderMainRepository = orderMainRepository;
    }
    @Autowired
    public void setOrderItemRepository(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }
    @Autowired
    public void setVisualizationRepository(VisualizationRepository visualizationRepository) {
        this.visualizationRepository = visualizationRepository;
    }
    @Autowired
    public void setAttributeValueRepository(AttributeValueRepository attributeValueRepository) {
        this.attributeValueRepository = attributeValueRepository;
    }

    public ShopManager() {
    }

    public ShopManager(ProductCardRepository productCardRepository, CategoryRepository categoryRepository,
                       CustomerRepository customerRepository, OrderMainRepository orderMainRepository,
                       OrderItemRepository orderItemRepository, VisualizationRepository visualizationRepository,
                       AttributeValueRepository attributeValueRepository) {
        this.productCardRepository = productCardRepository;
        this.categoryRepository = categoryRepository;
        this.customerRepository = customerRepository;
        this.orderMainRepository = orderMainRepository;
        this.orderItemRepository = orderItemRepository;
        this.visualizationRepository = visualizationRepository;
        this.attributeValueRepository = attributeValueRepository;
    }

    /**
     * Method createOrder is add or update new Customer into database
     * and also add order info in DB with compute total price
     *
     * @param email   user email address for identy each user by primary key
     * @param name    name of user (optional)
     * @param phone   phone number of user (optional)
     * @param address address for receive order
     * @param amount  amount of products in order
     * @param sku     unique id of each product
     * @throws NoResultException   if amount of products in our order
     *                             less than on warehouse
     * @throws ValidationException if email is not valid
     */
    @Transactional
    @RequestMapping("/createOrder")
    public OrderMain createOrder(@RequestParam(value="email") String email,
                                 @RequestParam(value="name") String name,
                                 @RequestParam(value="phone") String phone,
                                 @RequestParam(value="address") String address,
                                 @RequestParam(value="amount" , defaultValue="1") int amount,
                                 @RequestParam(value="sku") String sku) {

        EmailValidator emailValidator = new EmailValidator();

        if (!emailValidator.validate(email))
            throw new ValidationException("Email not valid");

        if (!isRequiredAmountOfProductCardAvailable(sku, amount))
            throw new NoResultException("This amount of products not exist on our warehouse");

        ProductCard productCard = productCardRepository.findOne(sku);
        int totalPrice = productCard.getPrice() * amount;
        Customer customer = new Customer(email, name, true, phone);
        customer = customerRepository.save(customer);
        OrderMain orderMain = new OrderMain(address, 1, customer);
        orderMain = orderMainRepository.save(orderMain);
        orderItemRepository.save(new OrderItem(amount, totalPrice, productCard, orderMain));
        return orderMain;
    }

    /**
     * Method submitOrder need for update amount of ProductCard
     * on warehouse and update status of order in OrderMain in tables
     *
     * @param email is  a user email for making changes
     * @return void type
     * @throws NoResultException if amount of products in our order
     *                           less than on warehouse
     */
    @Transactional
    @RequestMapping("/submitOrder")
    public List<OrderMain> submitOrder(@RequestParam(value="email") String email) {

        if (validateOrder(email) && customerRepository.exists(email)) {
            Customer customer = customerRepository.findOne(email);
            List<OrderMain> ordersByCustomer = getOrdersByCustomer(customer);
            List<OrderMain> resultList = new ArrayList<>();

            for (OrderMain om : ordersByCustomer) {

                if (om.getStatus() != 1)
                    continue;

                List<OrderItem> orderItemsByOrderMain = getItemOrdersByOrderMain(om);
                for (OrderItem oi : orderItemsByOrderMain) {
                    ProductCard productCard = productCardRepository.findOne(oi.getProductCard().getSku());
                    int newAmount = productCard.getAmount() - oi.getAmount();
                    productCard.setAmount(newAmount);
                    productCardRepository.save(productCard);
                }

                om.setStatus(2);
                resultList.add(orderMainRepository.save(om));
            }
            return resultList;
        }

        throw new NoResultException("Error of submit order");
    }

    /**
     * Method findProductsByCriteriaInAllPlaces need for find any ProductCard
     * on warehouse by String criteria in all Db     *
     *
     * @param criteria String is  a string for the find
     * @return Set<ProductCard> type with found set of products
     */
    @RequestMapping("/findAllProductsByCriteria")
    public Set<ProductCard> findAllProductsByCriteria(@RequestParam(value="criteria") String criteria) {
        Set<ProductCard> result = new LinkedHashSet<>();

        ProductCard productCard = productCardRepository.findOne(criteria);
        if (productCard != null)
            result.add(productCard);

        List<ProductCard> list = productCardRepository.findByNameIgnoreCase(criteria);
        if (list.size() > 0)
            result.addAll(list);

        list = productCardRepository.findByProductDescriptionIgnoreCase(criteria);
        if (list.size() > 0)
            result.addAll(list);

        result.addAll(getProductsByCategoryDescription(criteria));
        result.addAll(getProductsByCategoryName(criteria));

        return result;
    }

    /**
     * Method findProductsInColumn need for find any ProductCard
     * on warehouse by String criteria, in custom place.
     *
     * @param criteria     String is  a string for the find
     * @param placeForFind enumeration for choose sort criteria:
     *                     FIND_ALL,
     *                     FIND_BY_NAME,
     *                     FIND_IN_PROD_DESC,
     *                     FIND_IN_CATEGORY_NAME,
     *                     FIND_IN_CATEGORY_DESC;
     * @return Set<ProductCard> found results of products
     */
    public Set<ProductCard> findProductsInColumn(String criteria, EnumSearcher placeForFind) {

        Set<ProductCard> result = new LinkedHashSet<>();
        List<Category> categoryList;

        switch (placeForFind) {
            case FIND_BY_NAME:
                result.addAll(productCardRepository.findByNameIgnoreCase(criteria));
                return result;
            case FIND_IN_PROD_DESC:
                result.addAll(productCardRepository.findByProductDescriptionIgnoreCase(criteria));
                return result;
            case FIND_IN_CATEGORY_NAME:
                return getProductsByCategoryName(criteria);
            case FIND_IN_CATEGORY_DESC:
                return getProductsByCategoryDescription(criteria);
            default:
                throw new NoResultException();
        }
    }

// Methods for getting lists of various items

    public List<Category> getRootCategory() {
        return categoryRepository.findByCategory(null);
    }


    public List<Category> getSubCategories(Category category) {
        return categoryRepository.findByCategory(category);
    }

    public List<ProductCard> getProductCardsByCategory(Category category) {
        Sort sort = new Sort(new Sort.Order(Sort.Direction.ASC, "name"));
        return productCardRepository.findByCategory(category, sort);
    }

    public List<Visualization> getVisualListByProduct(ProductCard productCard) {
        return visualizationRepository.findByProductCard(productCard);
    }

    public List<AttributeValue> getAttrValuesByProduct(ProductCard productCard) {
        return attributeValueRepository.findByProductCard(productCard);
    }

    public List<AttributeValue> getAttrValuesByName(AttributeName attributeName) {
        return attributeValueRepository.findByAttributeName(attributeName);
    }

    public List<ProductCard> sortProductCard(Category category, EnumProductSorter criteria) {

        Sort sort;

        switch (criteria) {
            case SORT_BY_NAME:
                sort = new Sort(new Sort.Order(Sort.Direction.ASC, "name")); break;
            case SORT_BY_NAME_REVERSED:
                sort = new Sort(new Sort.Order(Sort.Direction.DESC, "name")); break;
            case SORT_BY_LOW_PRICE:
                sort = new Sort(new Sort.Order(Sort.Direction.ASC, "price")); break;
            case SORT_BY_HIGH_PRICE:
                sort = new Sort(new Sort.Order(Sort.Direction.DESC, "price")); break;
            case SORT_BY_POPULARITY:
                sort = new Sort(new Sort.Order(Sort.Direction.ASC, "likes")); break;
            case SORT_BY_UNPOPULARITY:
                sort = new Sort(new Sort.Order(Sort.Direction.ASC, "dislikes")); break;
            default:
                throw new NoResultException();
        }

        return category == null ? productCardRepository.findAllBy(sort) : productCardRepository.findByCategory(category, sort);
    }

    //Return product availabitity in storehouse by amount
    public boolean isRequiredAmountOfProductCardAvailable(String sku, int amount) {

        ProductCard productCard = productCardRepository.findOne(sku);
        int productCardAmount = productCard.getAmount();

        return amount <= productCardAmount;
    }

    //Return product availabitity in storehouse
    public boolean isProductAvailable(String sku) {
        return productCardRepository.exists(sku);
    }

    /**
     * Method validateOrder need for check amount of ProductCard
     * on warehouse.
     *
     * @param email is  a user email for making changes
     * @return boolean type. True if amount in order >= amount on
     * warehouse
     */
    public boolean validateOrder(String email) {
        boolean isExist = true;
        Customer customer = customerRepository.findOne(email);
        List<OrderMain> ordersByCustomer = getOrdersByCustomer(customer);
        l1:
        for (OrderMain om : ordersByCustomer) {
            List<OrderItem> itemOrdersByOrderMain = getItemOrdersByOrderMain(om);
            for (OrderItem oi : itemOrdersByOrderMain) {
                if (!isProductAvailable(oi.getProductCard().getSku())) {
                    isExist = false;
                    break l1;
                }
            }
        }
        return isExist;
    }


    public List<OrderMain> getOrdersByCustomer(Customer customer) {
        return orderMainRepository.findByCustomer(customer);
    }

    public List<OrderItem> getItemOrdersByOrderMain(OrderMain orderMain) {
        return orderItemRepository.findByOrderMain(orderMain);
    }

    public List<OrderItem> getItemOrdersByProdCard(ProductCard productCard) {
        return orderItemRepository.findByProductCard(productCard);
    }

    //Private helpful methods

    private Set<ProductCard> getProductsByCategoryDescription(String criteria) {

        Set<ProductCard> result = new LinkedHashSet<>();

        List<Category> categoryList = categoryRepository.findByDescriptionIgnoreCase(criteria);
        if (categoryList.size() > 0) {
            for (Category c : categoryList) {
                Sort sort = new Sort(new Sort.Order(Sort.Direction.ASC, "name"));
                List<ProductCard> list = productCardRepository.findByCategory(c, sort);
                result.addAll(list);
            }
        }

        return result;
    }

    private Set<ProductCard> getProductsByCategoryName(String criteria) {

        Set<ProductCard> result = new LinkedHashSet<>();

        List<Category> categoryList = categoryRepository.findByNameIgnoreCase(criteria);
        if (categoryList.size() > 0) {
            for (Category c : categoryList) {
                Sort sort = new Sort(new Sort.Order(Sort.Direction.ASC, "name"));
                List<ProductCard> list = productCardRepository.findByCategory(c, sort);
                result.addAll(list);
            }
        }

        return result;
    }
}
