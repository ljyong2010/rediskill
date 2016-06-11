package yf.liu.dao.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import redis.clients.jedis.JedisPool;

/**
 * Created by Administrator on 2016/6/8.
 */
public class RedisDao {
    private final Log LOG = LogFactory.getLog(this.getClass());

    private final JedisPool jedisPool;
    private RuntimeSch
}
