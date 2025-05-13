// ProductDemo.java

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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
 * 数据操作接口：定义查询方法
 */
interface ProductDao {
    Product getProductById(int id);
}

/**
 * 真实的数据访问实现（模拟从数据库或远程服务获取）
 */
class ProductDaoImpl implements ProductDao {
    @Override
    public Product getProductById(int id) {
        // 模拟延迟或远程调用
        System.out.println("[DB] Fetching product id=" + id);
        // 返回一个示例 Product
        return new Product(id, "商品#" + id);
    }
}

/**
 * 缓存代理的 InvocationHandler
 * 将对 getProductById 的调用结果缓存在内存 Map 中
 */
class CacheInvocationHandler implements InvocationHandler {
    private final Object target;
    private final Map<Integer, Product> cache = new HashMap<>();

    public CacheInvocationHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 仅对 getProductById 方法进行缓存，其他方法直接调用
        if ("getProductById".equals(method.getName())
                && args != null
                && args.length == 1
                && args[0] instanceof Integer) {
            Integer id = (Integer) args[0];
            if (cache.containsKey(id)) {
                System.out.println("[CACHE] Return cached product id=" + id);
                return cache.get(id);
            }
            // 调用真实对象获取
            Product result = (Product) method.invoke(target, args);
            // 缓存并返回
            cache.put(id, result);
            System.out.println("[CACHE] Caching product id=" + id);
            return result;
        }
        // 其它方法或签名，直接委托
        return method.invoke(target, args);
    }
}

/**
 * 客户端演示：创建动态代理并进行多次查询
 */
public class ProductDemo {
    public static void main(String[] args) {
        // 1. 创建真实的 DAO
        ProductDao realDao = new ProductDaoImpl();

        // 2. 为 realDao 创建一个缓存代理
        ProductDao cachedDao = (ProductDao) Proxy.newProxyInstance(
            ProductDao.class.getClassLoader(),
            new Class<?>[]{ProductDao.class},
            new CacheInvocationHandler(realDao)
        );

        // 3. 第一次查询：应走“数据库”，并缓存结果
        System.out.println("Result: " + cachedDao.getProductById(42));
        System.out.println();

        // 4. 第二次查询相同 ID：应直接从缓存返回
        System.out.println("Result: " + cachedDao.getProductById(42));
        System.out.println();

        // 5. 查询不同 ID：又走“数据库”
        System.out.println("Result: " + cachedDao.getProductById(7));
        System.out.println();

        // 6. 再次查询 ID=7：走缓存
        System.out.println("Result: " + cachedDao.getProductById(7));
    }
}
