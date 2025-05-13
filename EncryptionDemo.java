// EncryptionDemo.java

/**
 * 加密组件接口：所有加密算法都实现这个接口
 */
interface Encryptor {
    /**
     * 对输入字符串进行加密
     * @param text 明文
     * @return 密文
     */
    String encrypt(String text);
}

/**
 * 最简单的移位加密（Caesar Cipher）— 具体组件
 */
class SimpleEncryptor implements Encryptor {
    private final int shift;  // 移位量

    public SimpleEncryptor(int shift) {
        this.shift = shift;
    }

    @Override
    public String encrypt(String text) {
        StringBuilder sb = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            if (Character.isUpperCase(c)) {
                sb.append((char) ('A' + (c - 'A' + shift) % 26));
            } else if (Character.isLowerCase(c)) {
                sb.append((char) ('a' + (c - 'a' + shift) % 26));
            } else {
                // 非字母字符不变
                sb.append(c);
            }
        }
        return sb.toString();
    }
}

/**
 * 装饰器抽象类：持有一个 Encryptor，被装饰对象
 */
abstract class EncryptDecorator implements Encryptor {
    protected final Encryptor wrappee;

    public EncryptDecorator(Encryptor wrappee) {
        this.wrappee = wrappee;
    }

    @Override
    public String encrypt(String text) {
        // 默认先调用被装饰者的 encrypt，再由子类追加增强
        return wrappee.encrypt(text);
    }
}

/**
 * 逆向输出加密：在上一步加密结果基础上，将字符串反转
 */
class ReverseEncryptDecorator extends EncryptDecorator {
    public ReverseEncryptDecorator(Encryptor wrappee) {
        super(wrappee);
    }

    @Override
    public String encrypt(String text) {
        String prev = super.encrypt(text);
        return new StringBuilder(prev).reverse().toString();
    }
}

/**
 * 求模加密：对上一步结果的每个字符做取模映射（这里以 mod=26，映射到大写字母）
 */
class ModuloEncryptDecorator extends EncryptDecorator {
    private final int mod;

    public ModuloEncryptDecorator(Encryptor wrappee, int mod) {
        super(wrappee);
        this.mod = mod;
    }

    @Override
    public String encrypt(String text) {
        String prev = super.encrypt(text);
        StringBuilder sb = new StringBuilder(prev.length());
        for (char c : prev.toCharArray()) {
            // 将字符的 Unicode 码取模，再映射到 'A'~'Z'
            char out = (char) ('A' + (c % mod));
            sb.append(out);
        }
        return sb.toString();
    }
}

/**
 * 客户端演示：任意组合多级加密
 * 保存文件名必须为 EncryptionDemo.java
 */
public class EncryptionDemo {
    public static void main(String[] args) {
        String plain = "Hello, Decorator!";

        // ① 仅做一次简单移位加密
        Encryptor caesar = new SimpleEncryptor(3);
        System.out.println("Simple    : " + caesar.encrypt(plain));

        // ② 在移位加密结果上做逆向输出
        Encryptor reverse = new ReverseEncryptDecorator(caesar);
        System.out.println("Reverse   : " + reverse.encrypt(plain));

        // ③ 在逆向输出结果上再做求模加密（mod=26）
        Encryptor modulo = new ModuloEncryptDecorator(reverse, 26);
        System.out.println("Modulo    : " + modulo.encrypt(plain));

        // ④ 链式三次加密：先移位(5)，再逆向，再求模(26)
        Encryptor chain = new ModuloEncryptDecorator(
                                new ReverseEncryptDecorator(
                                    new SimpleEncryptor(5)
                                ), 26);
        System.out.println("Chain(5)  : " + chain.encrypt(plain));
    }
}
