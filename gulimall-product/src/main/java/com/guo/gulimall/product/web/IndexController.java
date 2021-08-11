package com.guo.gulimall.product.web;

import com.guo.gulimall.product.entity.CategoryEntity;
import com.guo.gulimall.product.service.CategoryService;
import com.guo.gulimall.product.vo.Catelog2Vo;
import lombok.RequiredArgsConstructor;
import org.redisson.api.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final CategoryService categoryService;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate redisTemplate;

    @GetMapping({"/","/index.html"})
    public String indexPage(Model model) {

        List<CategoryEntity> list = categoryService.getFirstLevelCategory();
        model.addAttribute("category", list);
        return "index";
    }

    @GetMapping("index/catalog.json")
    @ResponseBody
    public Map<String, List<Catelog2Vo>> getCateLogJson() {
        return categoryService.getCateLogJson();
    }

    // redisson分布式锁
    @GetMapping("/redissonHello")
    @ResponseBody
    public String redissonHello() {
        // 1、获取一把锁。 只要锁的名字一样，就是同一把锁
        RLock myLock = redissonClient.getLock("myLock");
        // 2、加锁
        // 阻塞式等待，默认加锁的时间30s
        // 在不设置加锁时间的情况下，看门狗会给锁自动续期，如果业务超长，运行期间自动给锁续上新的30s。
        // 加锁的业务只要运行完成，锁即不会续期，即使不手动解锁，锁在默认时间后也会自动删除
        myLock.lock();
        try {
            Thread.sleep(20000);

        } catch (Exception e) {

        }finally {
            myLock.unlock();
        }
        return "hello";
    }

    /***********************读写锁*********************/
    /* 改数据加写锁，读数据加读锁
     * 写锁是一个排他锁（互斥锁），读锁是一个共享锁
     * 读 + 读： 相当于无锁，并发读
     * 写 + 读：等待写锁释放
     * 写 + 写：阻塞方式 
     * 读 + 写：有读锁。写也需要等待
     * 只要有写的存在，都必须等待
     */
    @ResponseBody
    @GetMapping("/write")
    public String writeValue() {

        RReadWriteLock lock = redissonClient.getReadWriteLock("rwLock");
        String s = "";
        RLock rLock = lock.writeLock();
        rLock.lock();
        try {
            System.out.println("写锁加锁成功"+ Thread.currentThread().getId());
            s = UUID.randomUUID().toString();
            redisTemplate.opsForValue().set("writeValue", s);
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            rLock.unlock();
            System.out.println("写锁释放"+ Thread.currentThread().getId());
        }
        return s;
    }

    @ResponseBody
    @GetMapping("/read")
    public String readValue() {
        String s = "";
        RReadWriteLock lock = redissonClient.getReadWriteLock("rwLock");
        RLock rLock = lock.readLock();
        rLock.lock();
        try {
            System.out.println("读锁加锁成功"+ Thread.currentThread().getId());
            s= redisTemplate.opsForValue().get("writeValue");
            Thread.sleep(30000);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            rLock.unlock();
            System.out.println("读锁释放"+ Thread.currentThread().getId());
        }
        return s;
    }

    /**
     * 信号量
     */
    @ResponseBody
    @GetMapping("/park")
    public String park() throws InterruptedException {
        RSemaphore park = redissonClient.getSemaphore("park");
        // 获取一个信号量
        park.acquire();
        return "ok";
    }

    @ResponseBody
    @GetMapping("/go")
    public String go() {
        RSemaphore park = redissonClient.getSemaphore("park");
        // 获取一个信号量
        park.release();
        return "ok";
    }

    /************闭锁***********/
    @ResponseBody
    @GetMapping("/lockDoor")
    public String lockDoor() throws InterruptedException {
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.trySetCount(5);
        door.await();
        return "放年假了";
    }

    @ResponseBody
    @GetMapping("/gogogo{id}")
    public String gogogo(@PathVariable("id") Long id) {
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        // 计数减一
       door.countDown();
        return id + "班的人都走了";
    }



}