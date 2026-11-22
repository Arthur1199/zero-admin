package com.zero.web.controller.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import com.zero.common.annotation.RequiresPermissions;
import com.zero.common.annotation.SystemModule;
import com.zero.common.enums.SystemModuleEnum;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zero.common.constant.CacheConstants;
import com.zero.common.core.domain.AjaxResult;
import com.zero.common.utils.StringUtils;
import com.zero.system.domain.SysCache;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 缓存监控
 */
@RestController
@RequestMapping("/monitor/cache")
@SystemModule(SystemModuleEnum.MONITOR)
public class CacheController
{
    /**
     * RedisTemplate字段
     */
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    /**
     * caches字段
     */

    private final static List<SysCache> caches = new ArrayList<SysCache>();
    {
        caches.add(new SysCache(CacheConstants.LOGIN_TOKEN_KEY, "用户信息"));
        caches.add(new SysCache(CacheConstants.SYS_CONFIG_KEY, "配置信息"));
        caches.add(new SysCache(CacheConstants.SYS_DICT_KEY, "数据字典"));
        caches.add(new SysCache(CacheConstants.CAPTCHA_CODE_KEY, "验证码"));
        caches.add(new SysCache(CacheConstants.REPEAT_SUBMIT_KEY, "防重提交"));
        caches.add(new SysCache(CacheConstants.RATE_LIMIT_KEY, "限流处理"));
        caches.add(new SysCache(CacheConstants.PWD_ERR_CNT_KEY, "密码错误次数"));
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取信息
     * @return
     * 返回值: AjaxResult
     * 返回值说明：获取信息
     */

    @RequiresPermissions("monitor:cache:list")
    @GetMapping()
    public AjaxResult getInfo() throws Exception
    {
        Properties info = (Properties) redisTemplate.execute((RedisCallback<Object>) connection -> connection.info());
        Properties commandStats = (Properties) redisTemplate.execute((RedisCallback<Object>) connection -> connection.info("commandstats"));
        Object dbSize = redisTemplate.execute((RedisCallback<Object>) connection -> connection.dbSize());

        Map<String, Object> result = new HashMap<>(3);
        result.put("info", info);
        result.put("dbSize", dbSize);

        List<Map<String, String>> pieList = new ArrayList<>();
        commandStats.stringPropertyNames().forEach(key -> {
            Map<String, String> data = new HashMap<>(2);
            String property = commandStats.getProperty(key);
            data.put("name", StringUtils.removeStart(key, "cmdstat_"));
            data.put("value", StringUtils.substringBetween(property, "calls=", ",usec"));
            pieList.add(data);
        });
        result.put("commandStats", pieList);
        return AjaxResult.success(result);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 缓存
     * @return
     * 返回值: AjaxResult
     * 返回值说明：缓存
     */

    @RequiresPermissions("monitor:cache:list")
    @GetMapping("/getNames")
    public AjaxResult cache()
    {
        return AjaxResult.success(caches);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取缓存Keys
     * @param cacheName 缓存名称参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明：获取缓存Keys
     */

    @RequiresPermissions("monitor:cache:list")
    @GetMapping("/getKeys/{cacheName}")
    public AjaxResult getCacheKeys(@PathVariable String cacheName)
    {
        Set<String> cacheKeys = redisTemplate.keys(cacheName + "*");
        return AjaxResult.success(new TreeSet<>(cacheKeys));
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取缓存值
     * @param cacheName 缓存名称参数
     * @param cacheKey 缓存键名参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明：获取缓存值
     */

    @RequiresPermissions("monitor:cache:list")
    @GetMapping("/getValue/{cacheName}/{cacheKey}")
    public AjaxResult getCacheValue(@PathVariable String cacheName, @PathVariable String cacheKey)
    {
        String cacheValue = redisTemplate.opsForValue().get(cacheKey);
        SysCache sysCache = new SysCache(cacheName, cacheKey, cacheValue);
        return AjaxResult.success(sysCache);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 清空缓存名称
     * @param cacheName 缓存名称参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明：清空缓存名称
     */

    @RequiresPermissions("monitor:cache:list")
    @DeleteMapping("/clearCacheName/{cacheName}")
    public AjaxResult clearCacheName(@PathVariable String cacheName)
    {
        Collection<String> cacheKeys = redisTemplate.keys(cacheName + "*");
        redisTemplate.delete(cacheKeys);
        return AjaxResult.success();
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 清空缓存键名
     * @param cacheKey 缓存键名参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明：清空缓存键名
     */

    @RequiresPermissions("monitor:cache:list")
    @DeleteMapping("/clearCacheKey/{cacheKey}")
    public AjaxResult clearCacheKey(@PathVariable String cacheKey)
    {
        redisTemplate.delete(cacheKey);
        return AjaxResult.success();
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 清空缓存All
     * @return
     * 返回值: AjaxResult
     * 返回值说明：清空缓存All
     */

    @RequiresPermissions("monitor:cache:list")
    @DeleteMapping("/clearCacheAll")
    public AjaxResult clearCacheAll()
    {
        Collection<String> cacheKeys = redisTemplate.keys("*");
        redisTemplate.delete(cacheKeys);
        return AjaxResult.success();
    }
}
