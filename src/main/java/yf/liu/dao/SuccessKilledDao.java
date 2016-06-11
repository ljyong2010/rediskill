package yf.liu.dao;

import org.apache.ibatis.annotations.Param;
import yf.liu.entity.SuccessKilled;

/**
 * Created by Administrator on 2016/6/11.
 */
public interface SuccessKilledDao {
    /**
     * 插入购买明细,可过滤重复(数据库有联合主键)
     * @param seckillId
     * @param userPhone
     * @return
     */
    int insertSuccessKilled(@Param("seckilledId") long seckillId,@Param("userPhone") long userPhone);

    /**
     * 根据ID查询SuccessKilled并携带秒杀产品对象实体
     * @param seckilledId
     * @param userPhone
     * @return
     */
    SuccessKilled queryByIdWithSeckill(@Param("seckilledId") long seckilledId,@Param("userPhone") long userPhone);
}
