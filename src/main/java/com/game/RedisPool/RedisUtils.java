package com.game.RedisPool;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import com.game.resourcesload.LoadProp;
import com.game.systeminfrastructure.SingleChannel;
import com.game.util.Constant;
import com.game.util.DateUtil;
import com.game.util.StrUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 带有rdis连接池的工具
 */
public class RedisUtils {

	static JedisPool pool;

	/**
	 * 获取key-value键值对格式值
	 * @param key
	 * @return
	 */
	public static String get(String key) {
		Jedis localJedis = getJedis();
		String str = localJedis.get(key);
		returnClient(localJedis);
		return str;
	}

	/**
	 * 获取某个前缀key-value键值对格式值
	 * @param prefix
	 * @return
	 * @throws Exception
	 */
	public static Set<String> keys(String prefix) throws Exception {
		if (StrUtil.isNull(prefix)) {
			throw new Exception("redis error: prefix is empty");
		}
		Jedis jedis = getJedis();
		Set<String> set = jedis.keys(prefix + "*");
		returnClient(jedis);
		return set;
	}

	/**
	 * 将 key的值设为value
	 * 
	 * @param key
	 * @param value
	 * @throws Exception
	 * @author YXD
	 */
	public static void set(String key, String value) throws Exception {
		if (StrUtil.isNull(key))
			throw new Exception("redis error: key is empty");
		if (StrUtil.isNull(value))
			throw new Exception("redis error: value is empty");

		Jedis localJedis = getJedis();
		localJedis.set(key, value);
		returnClient(localJedis);
	}

	/**
	 * 将 key的值设为value
	 *
	 * @param key
	 * @param value
	 * @throws Exception
	 * @author YXD
	 */
	public static void accumulation(String key, String value) throws Exception {
		if (StrUtil.isNull(key))
			throw new Exception("redis error: key is empty");
		if (StrUtil.isNull(value))
			throw new Exception("redis error: value is empty");

		Jedis localJedis = getJedis();
		String oldValue = localJedis.get(key);
		if(StrUtil.isNotNull(oldValue)){
			value = Long.parseLong(value) + Long.parseLong(oldValue) +"";
		}
		localJedis.set(key, value);
		returnClient(localJedis);
	}

	/**
	 * redis中set集合添加元素
	 *
	 * @param key
	 * @param values
	 * @throws Exception
	 * @author YXD
	 */
	public static void sAdd(String key, Set values) throws Exception {
		if (StrUtil.isNull(key))
			throw new Exception("redis error: key is empty");
		if (null == values)
			throw new Exception("redis error: values is empty");
		Jedis localJedis = getJedis();
		String[] arr = new String[values.size()];
		values.toArray(arr);
		localJedis.sadd(key, arr);
		returnClient(localJedis);
	}

	/**
	 * redis中获取set集合元素
	 *
	 * @param key
	 * @throws Exception
	 * @author YXD
	 */
	public static Set<String> sMembers(String key) throws Exception {
		if (StrUtil.isNull(key))
			throw new Exception("redis error: key is empty");
		Jedis localJedis = getJedis();
		Set<String> setValues = localJedis.smembers(key);
		returnClient(localJedis);
		return setValues;
	}
	/**
	 * 将 key的值设为value，当且仅当key不存在<br>
	 * SET if Not eXists
	 * 
	 * @param key
	 * @param value
	 * @return
	 * @throws Exception
	 * @author YXD
	 */
	public static long setnx(String key, String value) throws Exception {
		if (StrUtil.isNull(key))
			throw new Exception("redis error: key is empty");
		if (StrUtil.isNull(value))
			throw new Exception("redis error: value is empty");

		Jedis localJedis = getJedis();
		long a = localJedis.setnx(key, value);
		returnClient(localJedis);
		return a;
	}

	/**
	 * 将 key的值设为value,并设置有效时间(单位:s)<br>
	 * 使用本地缓存暂不支持设置有效时间
	 * 
	 * @param key
	 * @param value
	 * @param 
	 * @throws Exception
	 * @author YXD
	 */
	public static void setTTL(String key, String value, int seconds) throws Exception {
		if (StrUtil.isNull(key))
			throw new Exception("redis error: key is empty");
		if (StrUtil.isNull(value))
			throw new Exception("redis error: value is empty");

		Jedis localJedis = getJedis();
		localJedis.set(key, value);
		localJedis.expire(key, seconds);
		returnClient(localJedis);
	}

	/**
	 * 设置redis的某个key的有效时间<br>
	 * 使用本地缓存暂不支持设置有效时间
	 * 
	 * @param key
	 * @param seconds
	 * @throws Exception
	 * @author YXD
	 */
	public static void expire(String key, int seconds) throws Exception {
		if (StrUtil.isNull(key))
			throw new Exception("redis error: key is empty");

		Jedis localJedis = getJedis();
		localJedis.expire(key, seconds);
		returnClient(localJedis);
	}

	/**
	 * 在redis中对某个key加锁一段时间<br>
	 * 使用本地缓存暂不支持加锁
	 * @param timeout
	 *            加锁时间
	 * @param key
	 *            key
	 * @return
	 * @author YXD
	 * @throws Exception
	 */
	public static boolean lock(long timeout, String key,String id) throws Exception {

		Random random = new Random();
		int expire = (int) (timeout / 1000 * 2);
		long nanoTime = DateUtil.getSystemTime();
		try {
			// 在timeout的时间范围内不断轮询锁
			while (DateUtil.getSystemTime() - nanoTime < timeout) {
				// 锁不存在的话，设置锁并设置锁过期时间，即加锁
				if (setnx(key, id) == 1) {
					if(expire<=0)
						expire = 1;
					expire(key, expire);// 设置锁过期时间是为了在没有释放
					// 锁的情况下锁过期后消失，不会造成永久阻塞
					return true;
				}
				// System.out.println("出现锁等待");
				// 短暂休眠，避免可能的活锁
				Thread.sleep(1, random.nextInt(1));
			}
		} catch (Exception e) {
			throw new RuntimeException("redis error: locking error", e);
		}
		return false;
	}

	/**
	 * 在redis中对某个key加锁一段时间
	 * 
	 * @param key
	 * @param timeout
	 * @return
	 * @author YXD
	 * @throws Exception
	 */
	public static boolean lock(String key, long timeout,String id) throws Exception {
		return lock(timeout, key,id);
	}

	/**
	 * 查看剩余时间
	 * @param key
	 * @return
	 * @author YXD
	 * @throws Exception 
	 */
	public static long ttl(String key) {
		Jedis redis = getJedis();
		long timeout = redis.ttl(key);
		returnClient(redis);
		return timeout;
	}
	
	/**
	 * 将key从缓存中删除
	 * @param key
	 * @author YXD
	 */
	public static void remove(String key) {
		if(StrUtil.isNull(key)){
			return;
		}

		Jedis localJedis = getJedis();
		localJedis.del(key);
		returnClient(localJedis);
	}


	/**
	 * 将keys从缓存中删除
	 * @param key
	 * @author YXD
	 */
	public static void removeKeys(String key) {
		if(StrUtil.isNull(key)){
			return;
		}
		Jedis localJedis = getJedis();
		Set<String> set = localJedis.keys(key + "*");
		Iterator<String> it = set.iterator();
		while(it.hasNext()){
			String keyStr = it.next();
			localJedis.del(keyStr);
		}
		returnClient(localJedis);
	}
	/**
	 * 删除带有某个前缀的所有缓存<br>
	 * <b>慎用</b>
	 * 
	 * @param prefix
	 * @author YXD
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static void removeAll(String prefix) throws Exception {
		Jedis jedis = getJedis();
		Set<String> set = keys(prefix);
		Iterator<String> it = set.iterator();
		while (it.hasNext()) {
			String keyStr = it.next();
			jedis.del(keyStr);
		}
		returnClient(jedis);
	}

	/**
	 * （同步版）从连接池里面获取redis客户端
	 * 
	 * @return
	 * @author YXD
	 */
	private static synchronized Jedis getJedis() {
		Object localObject = null;
		if (pool == null) {
			localObject = new JedisPoolConfig();
			((JedisPoolConfig) localObject).setMaxIdle(5);
			((JedisPoolConfig) localObject).setMaxTotal(500);
			((JedisPoolConfig) localObject).setTestOnBorrow(true);
			String str1 = SingleChannel.getConfig("redisIP");
			int i = Integer.parseInt(SingleChannel.getConfig("redisPort"));
			int j = Integer.parseInt(SingleChannel.getConfig("redisTimeout"));
			String str2 = SingleChannel.getConfig("redisAuth");
			pool = new JedisPool((GenericObjectPoolConfig) localObject, str1, i, j, str2);
		}
		localObject = pool.getResource();
		return ((Jedis) localObject);
	}

	/**
	 * 返回redis客户端给连接池
	 * 
	 * @param jedis
	 *            redis连接客户端
	 * @author YXD
	 */
	private static void returnClient(Jedis jedis) {
		pool.returnResourceObject(jedis);
	}

}
