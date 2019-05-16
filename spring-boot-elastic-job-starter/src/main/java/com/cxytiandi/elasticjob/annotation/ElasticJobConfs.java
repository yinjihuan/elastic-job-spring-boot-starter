package com.cxytiandi.elasticjob.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author heyc
 * @date 2019/5/16 15:30
 */
@Component
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ElasticJobConfs {

    /**
     * 任务调度调度配置
     * @return
     */
    ElasticJobConf[] value();

}
