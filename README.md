# elastic-job-spring-boot-starter

Elastic-Job Spring Boot 自动集成，只需要一个注解即可发布Job。

Elastic-Job官方提供了基于Spring和Java代码2种方式的任务配置，刚开始用Elastic-Job的时候我比较喜欢用Spring XML文件的方式来配置任务。

这种方式能够很直观的看到所有的任务信息，结构比较清晰。当Spring Boot全面普及后，于是我们慢慢淡忘了XML配置。

当我发表了一篇名为[《房价网是怎么使用分布式作业框架elastic-job》](http://cxytiandi.com/blog/detail/12107)的文章，后面我的个人网站[猿天地](http://cxytiandi.com/)还推出了Elastic-Job的技术视频后，有很多人问我能不能用注解的方式来配置任务，都觉得注解比XML要简洁，方便。

由于官方没有提供Elastic-Job的Spring Boot Starter,于是我抽时间写了一个Starter,目的当然是使用注解简化XML的配置，下面我们就来看看怎么使用吧：

## 增加elastic-job-spring-boot-starter的Maven依赖

由于目前刚开发完成，也不知道会有多少人需要使用这个Starter，Jar包暂时不传到Maven中央仓库，需要体验的同学直接下载源码编译即可依赖，等后面Star的数量多了起来之后，有比较多的人关注了这个Starter，后面我会传到Maven中央仓库供大家更方便的使用。

```
<dependency>
			<groupId>com.cxytiandi</groupId>
			<artifactId>elastic-job-spring-boot-starter</artifactId>
			<version>1.0.0</version>
</dependency>
```

## 开启Elastic-Job自动配置

开启自动配置只需要在Spring Boot的启动类上增加@EnableElasticJob注解

```
import java.util.concurrent.CountDownLatch;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import com.cxytiandi.elasticjob.annotation.EnableElasticJob;

/**
 * ElasticJob Spring Boot集成案例
 * 
 * @author yinjihuan
 * 
 * @about http://cxytiandi.com/about
 *
 */
@SpringBootApplication
@EnableElasticJob
public class JobApplication {
	
	public static void main(String[] args) {
		new SpringApplicationBuilder().sources(JobApplication.class).web(false).run(args);
		try {
			new CountDownLatch(1).await();
		} catch (InterruptedException e) {
		}
	}
	
}
```
