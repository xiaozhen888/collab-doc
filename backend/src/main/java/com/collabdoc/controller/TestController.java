package com.collabdoc.controller;

import com.collabdoc.entity.Document;
import com.collabdoc.mapper.DocumentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/test") //给这个类的所有接口加一个统一的前缀/api/test,那么这个类中所有@GetMapping的路径都会变成/api/test/xxx
public class TestController {

    @Autowired
    private DocumentMapper documentMapper;//让Spring自动把DocumentMapper的实现类注入进来，不需要new，直接用

    @Autowired
    private StringRedisTemplate redisTemplate;//让Spring注入Redis操作工具，redis中存的键值对都是String类型，所以对应使用Redis中的StringRedisTemplate方法


    /**
     * 测试接口：返回服务状态
     */
    @GetMapping("/ping")
    public Map<String,Object> ping(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("status","ok");
        result.put("message","pong");
        result.put("timestamp", LocalDateTime.now());

        return result;
    }


    /**
     * 测试数据库：插入一条文档记录
     */
    @GetMapping("/db/insert")
    public Map<String,Object> testDbInsert(){
        Map<String, Object> result = new HashMap<>();

        Document doc = new Document();
//        doc.setId(UUID.randomUUID().toString()); //UUID.randomUUID().toString()生成类似于”abc-123-def"的唯一ID
        doc.setId("002");
        doc.setTitle("测试文档");
        doc.setContent("这是测试内容");
        doc.setOwnerId("test-user");
        doc.setCreateTime(LocalDateTime.now());
        doc.setUpdateTime(LocalDateTime.now());

        //调用MyBatis-Plus的insert方法，把doc这个java对象，作为一行数据，插入到数据库的document表里
        //documentMapper是MyBatis-Plus自动生成的数据库操作工具，专门操作doucment表
        //.insert()是MyBatis-Plus提供的一个方法，作用是把一个对象插入到数据库
        //MyBatis-Plus会自动做映射，然后执行SQL
        //SQL不需要写是因为MyBatis-Plus 在底层自动生成了 INSERT SQL。这就是它的便利性：约定大于配置。
        int rows = documentMapper.insert(doc); //受影响的行数

        result.put("success",rows>0);
        result.put("rows",rows);
        result.put("insertedId",doc.getId());

        return result;
    }


    /**
     * 测试数据库：查询所有文档
     */
    @GetMapping("/db/query")
    public Map<String,Object> testDbQuery(){
        Map<String, Object> result = new HashMap<>();

        List<Document> list = documentMapper.selectList(null); //MyBatis-Plus方法，查询所有记录（参数null表示无条件）
        result.put("success",true);
        result.put("count",list.size());
        result.put("documents",list);

        return result;
    }


    /**
     * 测试Redis:存入键值对
     * 需要带参数访问，否则页面返回400
     */
    @GetMapping("/redis/set")
    //@RequestParam:表示从访问的地址URL参数中取值
    //localhost:8080/api/test/redis/set?key=hello&value=world
    public Map<String,Object> testRedisSet(@RequestParam String key,@RequestParam String value){
        Map<String, Object> result = new HashMap<>();

        redisTemplate.opsForValue() //拿到Redis的字符串操作工具
                .set(key,value);    //执行Redis的SET命令

        result.put("success",true);
        result.put("key",key);
        result.put("value",value);
        result.put("message","Redis写入成功");

        return result;
    }


    /**
     * 测试Redis:读取键值对
     * 需要带参数访问，否则页面返回400
     */
    @GetMapping("/redis/get")
    public Map<String,Object> testRedisGet(@RequestParam String key){
        Map<String, Object> result = new HashMap<>();

        //需要添加局部变量！！因为这是根据key值返回value值，需要一个变量来存放value值！！
        String value = redisTemplate.opsForValue()
                .get(key);      //执行Redis的GET命令     //key不存在则value为null

        result.put("success",true);
        result.put("key",key);
        result.put("value",value);

        return result;
    }
}
