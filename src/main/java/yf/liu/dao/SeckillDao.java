package yf.liu.dao;

import org.apache.ibatis.annotations.Param;
import yf.liu.entity.Seckill;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/6/8.
 */
public interface SeckillDao {
    /**
     * 减少库存
     * @param seckillId
     * @param killTime
     * @return 如果更新行数大于1,表示更新的行数
     */
    int reduceNumber(@Param("seckillId") long seckillId, @Param("killTime") Date killTime);

    /**
     * 根据ID查询秒杀对象
     * @param seckillId
     * @return
     */
    Seckill queryById(long seckillId);

    /**
     * 根据偏移量查询秒杀商品列表
     * @param offset
     * @param limit
     * @return
     */
    List<Seckill> queryAll(@Param("offset") int offset,@Param("limit") int limit);

    /**
     * 使用存储过程执行秒杀
     * @param paramMap
     */
    void killByProcedure(Map<String,Object> paramMap);
}
