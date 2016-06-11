package yf.liu.service.impl;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import yf.liu.dao.SeckillDao;
import yf.liu.dao.SuccessKilledDao;
import yf.liu.dao.cache.RedisDao;
import yf.liu.dto.Exposer;
import yf.liu.dto.SeckillExecution;
import yf.liu.entity.Seckill;
import yf.liu.entity.SuccessKilled;
import yf.liu.enums.SeckillStatEnum;
import yf.liu.exception.RepeatKillException;
import yf.liu.exception.SeckillCloseException;
import yf.liu.exception.SeckillException;
import yf.liu.service.SeckillService;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/6/8.
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    private Log LOG = LogFactory.getLog(this.getClass());

    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private RedisDao redisDao;
    @Autowired
    private SuccessKilledDao successKilledDao;

    private final String slat = "asdfasd2341242@#$@#$%$%%#@$%#@%^%^";


    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0,1000);
    }

    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    public Exposer exportSeckillUrl(long seckillId) {
        Seckill seckill = redisDao.getSeckill(seckillId);
        if (seckill == null){
            seckill = getById(seckillId);
            if (seckill == null){
                return new Exposer(false,seckillId);
            }else {
                redisDao.putSeckill(seckill);
            }
        }
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();

        Date nowTime = new Date();

        if (nowTime.getTime() > endTime.getTime() || nowTime.getTime() < startTime.getTime()){
            return new Exposer(false,seckillId,nowTime.getTime(),startTime.getTime(),endTime.getTime());
        }
        String md5 = getMD5(seckillId);
        return new Exposer(true,md5,seckillId);
    }

    @Transactional
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, RepeatKillException, SeckillCloseException {
        if (StringUtils.isEmpty(md5) || !md5.equals(getMD5(seckillId))){
            throw new SeckillException(SeckillStatEnum.DATA_REWRITE.getStateInfo());
        }
        Date nowTime = new Date();
        try {
            int inserCount = successKilledDao.insertSuccessKilled(seckillId,userPhone);
            if (inserCount <= 0){
                throw new RepeatKillException(SeckillStatEnum.REPEAT_KILL.getStateInfo());
            }else {
                int updateCount = seckillDao.reduceNumber(seckillId,nowTime);
                if (updateCount <= 0){
                    throw new SeckillCloseException(SeckillStatEnum.END.getStateInfo());
                }else {
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId,userPhone);
                    return new SeckillExecution(seckillId,SeckillStatEnum.SUCCESS,successKilled);
                }
            }
        }catch (SeckillCloseException e1){
            throw e1;
        }catch (RepeatKillException e2){
            throw e2;
        }catch (Exception e){
            LOG.error(e.getMessage());
            throw new SeckillException("seckill inner error: "+e.getMessage());
        }

    }

    public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5) {
        if (StringUtils.isEmpty(md5) || !md5.equals(getMD5(seckillId))){
            throw new SeckillException(SeckillStatEnum.DATA_REWRITE.getStateInfo());
        }
        Date killTime = new Date();
        Map<String,Object> map = new HashMap<String, Object>();

        map.put("seckillId",seckillId);
        map.put("phone",userPhone);
        map.put("killTime",killTime);
        map.put("result",null);
        try {
            seckillDao.killByProcedure(map);
            int result = MapUtils.getInteger(map,"result",-2);
            if (result == 1){
                SuccessKilled sk = successKilledDao.queryByIdWithSeckill(seckillId,userPhone);
                return new SeckillExecution(seckillId,SeckillStatEnum.SUCCESS,sk);
            }else {
                return new SeckillExecution(seckillId,SeckillStatEnum.stateOf(result));
            }
        }catch (Exception e){
            LOG.error(e.getMessage());
            return new SeckillExecution(seckillId,SeckillStatEnum.INNER_ERROR);
        }
    }

    private String getMD5(long seckillId) {
        String base = seckillId + "/" + slat;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        LOG.info("_________________________________md5: " + md5);
        return md5;
    }
}
