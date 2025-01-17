package enhance;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;

import java.util.HashMap;
import java.util.Map;

public class Example {
    public static void main(String[] args) {
        Map<String, Object> beans = new HashMap<>();
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(Example.class);
        enhancer.setCallback((MethodInterceptor) (obj, method, arguments, proxy) -> {
            String name = method.getName();
            if (!beans.containsKey(name)) {
                Object bean = proxy.invokeSuper(obj, arguments);
                beans.put(name, bean);
            }
            return beans.get(name);
        });
        Example config = (Example) enhancer.create();
        System.out.println(config.getClass());
        config.foo().doSomething();
        config.foo().doSomething();
    }
    public Bar bar() {
        System.out.println("Config.bar");
        return new Bar();
    }
    public Foo foo() {
        System.out.println("Config.foo");
        return new Foo(bar());
    }
    public static class Foo {
        private final Bar bar;
        public Foo(Bar bar) {
            this.bar = bar;
        }

        public void doSomething() {
            System.out.println(this.toString() + "#" + bar.toString());
        }
    }
    public static class Bar {
    }
}
