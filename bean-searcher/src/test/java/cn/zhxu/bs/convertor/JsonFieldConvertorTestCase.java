package cn.zhxu.bs.convertor;

import cn.zhxu.bs.BeanMeta;
import cn.zhxu.bs.FieldMeta;
import cn.zhxu.bs.bean.DbField;
import cn.zhxu.bs.bean.DbType;
import cn.zhxu.bs.implement.DefaultMetaResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class JsonFieldConvertorTestCase {

    final JsonFieldConvertor convertor = new JsonFieldConvertor();

    final BeanMeta<User> beanMeta = new DefaultMetaResolver().resolve(User.class);

    final FieldMeta roleMeta = beanMeta.requireFieldMeta("role");
    final FieldMeta rolesMeta = beanMeta.requireFieldMeta("roles");
    final FieldMeta kvsMeta = beanMeta.requireFieldMeta("kvs");
    final FieldMeta kvListsMeta = beanMeta.requireFieldMeta("kvLists");


    static class User {

        @DbField(type = DbType.JSON)
        private List<Role> roles;

        @DbField(type = DbType.JSON)
        private Role role;

        @DbField(type = DbType.JSON)
        private List<KV<String, Integer>> kvs;

        @DbField(type = DbType.JSON)
        private List<List<KV<String, Integer>>> kvLists;

    }

    static class KV<K, T> {
        private K k;
        private T v;
        public KV() {}
        public KV(K k, T v) {
            this.k = k;
            this.v = v;
        }
        public K getK() {
            return k;
        }
        public void setK(K k) {
            this.k = k;
        }
        public T getV() {
            return v;
        }
        public void setV(T v) {
            this.v = v;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            KV<?, ?> kv = (KV<?, ?>) o;
            return Objects.equals(k, kv.k) && Objects.equals(v, kv.v);
        }
    }

    static class Role {
        private int id;
        private String name;

        public void setId(int id) {
            this.id = id;
        }
        public int getId() {
            return id;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
    }

    @Test
    public void test_supports() {
        test_supports(roleMeta);
        test_supports(rolesMeta);
        test_supports(kvsMeta);
        test_supports(kvListsMeta);
        System.out.println("\ttest_support ok!");
    }

    public void test_supports(FieldMeta meta) {
        Assertions.assertTrue(convertor.supports(meta, String.class));
        Assertions.assertEquals(meta.getDbType(), DbType.JSON);
    }

    @Test
    public void test_convert_role() {
        String value = "{\"id\":1,\"name\":\"Jack\"}]";
        Object result = convertor.convert(roleMeta, value);
        Assertions.assertNotNull(result);
        assertRole(result, 1, "Jack");
        System.out.println("\ttest_convert_role ok!");
    }

    @Test
    public void test_convert_roles() {
        String value = "[{\"id\":1,\"name\":\"Jack\"},{\"id\":2,\"name\":\"Tom\"}]";
        Object results = convertor.convert(rolesMeta, value);
        Assertions.assertNotNull(results);
        Assertions.assertTrue(List.class.isAssignableFrom(results.getClass()));
        List<?> list = (List<?>) results;
        Assertions.assertEquals(2, list.size());
        assertRole(list.get(0), 1, "Jack");
        assertRole(list.get(1), 2, "Tom");
        System.out.println("\ttest_convert_roles ok!");
    }

    private void assertRole(Object data, int id, String name) {
        Assertions.assertNotNull(data);
        Assertions.assertTrue(Role.class.isAssignableFrom(data.getClass()));
        Role role = (Role) data;
        Assertions.assertEquals(id, role.getId());
        Assertions.assertEquals(name, role.getName());
    }

    @Test
    public void test_convert_kvs() {
        String value = "[{\"k\":\"id\",\"v\":1},{\"k\":\"age\",\"v\":20}]";
        Object results = convertor.convert(kvsMeta, value);
        Assertions.assertNotNull(results);
        Assertions.assertTrue(List.class.isAssignableFrom(results.getClass()));
        assertKvList(results, Arrays.asList(
                new KV<>("id", 1),
                new KV<>("age", 20)
        ));
        System.out.println("\ttest_convert_kvs ok!");
    }

    private void assertKvList(Object results, List<KV<String, Integer>> expects) {
        List<?> list = (List<?>) results;
        Assertions.assertEquals(expects.size(), list.size());
        for (int i = 0; i < expects.size(); i++) {
            Assertions.assertEquals(expects.get(i), list.get(i));
        }
    }

    @Test
    public void test_convert_kvLists() {
        String value = "[[{\"k\":\"id\",\"v\":1},{\"k\":\"age\",\"v\":20}],[{\"k\":\"idx\",\"v\":52}]]";
        Object results = convertor.convert(kvListsMeta, value);
        Assertions.assertNotNull(results);
        Assertions.assertTrue(List.class.isAssignableFrom(results.getClass()));
        List<?> list = (List<?>) results;
        Assertions.assertEquals(2, list.size());
        assertKvList(list.get(0), Arrays.asList(
                new KV<>("id", 1),
                new KV<>("age", 20)
        ));
        assertKvList(list.get(1), Collections.singletonList(new KV<>("idx", 52)));
        System.out.println("\ttest_convert_kvLists ok!");
    }

}
