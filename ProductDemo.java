// ProductDemo.java

import java.util.HashMap;
import java.util.Map;

/**
 * 商品实体
 */
class Product {
    private final int id;
    private final String name;

    public Product(int id, String name) {
        this.id   = id;
        this.name = name;
    }

    public int getId()   { return id; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return "Product{id=" + id + ", name='" + name + "'}";
    }
}

/**
 * Subject 接口：定义查询方法
 */
interface ProductDao {
    Product getProductById(int id);
}

/**
 * RealSubject：真实的数据访问实现（模拟从数据库或远程服务获取）
 */
class ProductDaoImpl implements ProductDao {
    @Override
    public Product getProductById(int id) {
        // 模拟延迟或远程调用
        System.out.println("[DB] Fetching product id=" + id);
        return new Product(id, "Product#" + id);
    }
}

/**
 * Proxy：缓存代理，实现 ProductDao，内部持有一个真正的 ProductDaoImpl
 */
class ProductDaoCacheProxy implements ProductDao {
    private final ProductDao realDao;
    private final Map<Integer, Product> cache = new HashMap<>();

    public ProductDaoCacheProxy(ProductDao realDao) {
        this.realDao = realDao;
    }

    @Override
    public Product getProductById(int id) {
        // 先从缓存取
        if (cache.containsKey(id)) {
            System.out.println("[CACHE] Return cached product id=" + id);
            return cache.get(id);
        }
        // 缓存未命中，调用真实对象，并缓存结果
        Product p = realDao.getProductById(id);
        cache.put(id, p);
        System.out.println("[CACHE] Caching product id=" + id);
        return p;
    }
}

/**
 * Client：演示使用静态代理进行多次查询
 */
public class ProductDemo {
    public static void main(String[] args) {
        // 1. 真实的 DAO
        ProductDao realDao = new ProductDaoImpl();

        // 2. 用缓存代理包装
        ProductDao cachedDao = new ProductDaoCacheProxy(realDao);

        // 3. 第一次查询：走“数据库”，并缓存
        System.out.println("Result: " + cachedDao.getProductById(42));
        System.out.println();

        // 4. 第二次查询相同 ID：直接走缓存
        System.out.println("Result: " + cachedDao.getProductById(42));
        System.out.println();

        // 5. 查询不同 ID：走“数据库”
        System.out.println("Result: " + cachedDao.getProductById(7));
        System.out.println();

        // 6. 再次查询 ID=7：走缓存
        System.out.println("Result: " + cachedDao.getProductById(7));
    }
}
