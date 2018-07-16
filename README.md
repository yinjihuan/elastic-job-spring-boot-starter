# elastic-job-spring-boot-starter介绍

Elastic-Job Spring Boot 自动集成，只需要一个注解即可发布Job。

Elastic-Job官方提供了基于Spring和Java代码2种方式的任务配置，刚开始用Elastic-Job的时候我比较喜欢用Spring XML文件的方式来配置任务。

这种方式能够很直观的看到所有的任务信息，结构比较清晰。当Spring Boot全面普及后，于是我们慢慢淡忘了XML配置。

当我发表了一篇名为[《房价网是怎么使用分布式作业框架elastic-job》](http://cxytiandi.com/blog/detail/12107)的文章，后面我的个人网站[猿天地](http://cxytiandi.com/)还推出了Elastic-Job的技术视频后，有很多人问我能不能用注解的方式来配置任务，都觉得注解比XML要简洁，方便。

由于官方没有提供Elastic-Job的Spring Boot Starter,于是我抽时间写了一个Starter,目的当然是使用注解简化XML的配置，下面我们就来看看怎么使用吧：

## 增加elastic-job-spring-boot-starter的Maven依赖

目前最新版本1.0.1

第一步添加仓库地址：
```
<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
</repositories>
```
第二步添加依赖：
```
<dependency>
	    <groupId>com.github.yinjihuan</groupId>
	    <artifactId>elastic-job-spring-boot-starter</artifactId>
	    <version>1.0.1</version>
</dependency>
```

## 增加Zookeeper注册中心的配置

```
elasticJob.zk.serverLists=192.168.10.47:2181
elasticJob.zk.namespace=cxytiandi_job2
```
Zookeeper配置的前缀是elasticJob.zk，详细的属性配置请查看[ZookeeperProperties](https://github.com/yinjihuan/elastic-job-spring-boot-starter/blob/master/spring-boot-elastic-job-starter/src/main/java/com/cxytiandi/elasticjob/autoconfigure/ZookeeperProperties.java)

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

## 配置任务

```
@ElasticJobConf(name = "MySimpleJob", cron = "0/10 * * * * ?", 
	shardingItemParameters = "0=0,1=1", description = "简单任务")
public class MySimpleJob implements SimpleJob {

	public void execute(ShardingContext context) {
		System.out.println(2/0);
		String shardParamter = context.getShardingParameter();
		System.out.println("分片参数："+shardParamter);
		int value = Integer.parseInt(shardParamter);
		for (int i = 0; i < 1000000; i++) {
			if (i % 2 == value) {
				String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
				System.out.println(time + ":开始执行简单任务" + i);
			}
		}
	}

}
```
任务的配置只需要在任务类上增加一个ElasticJobConf注解，注解中有很多属性，这些属性都是任务的配置，详细的属性配置请查看[ElasticJobConf](https://github.com/yinjihuan/elastic-job-spring-boot-starter/blob/master/spring-boot-elastic-job-starter/src/main/java/com/cxytiandi/elasticjob/annotation/ElasticJobConf.java)
		
到此为止，我们就快速的使用注解发布了一个任务，DataflowJob和ScriptJob的使用方式一样。

使用示列参考：[elastic-job-spring-boot-example](https://github.com/yinjihuan/elastic-job-spring-boot-example)

## 事件追踪功能使用

事件追踪功能在注解中也只需要配置eventTraceRdbDataSource=你的数据源 就可以使用了，数据源用什么连接池无限制，唯一需要注意的一点是你的数据源必须在spring-boot-elastic-job-starter之前创建，因为spring-boot-elastic-job-starter中依赖了你的数据源，下面我以druid作为连接池来进行讲解。

引入druid的Spring Boot Starter,GitHub地址：[druid-spring-boot-starter](https://github.com/alibaba/druid/tree/master/druid-spring-boot-starter)

```
<dependency>
	<groupId>com.alibaba</groupId>
	<artifactId>druid-spring-boot-starter</artifactId>
	<version>1.1.2</version>
</dependency>
```

配置连接池属性：

```
spring.datasource.druid.log.url=jdbc:mysql://localhost:3306/event_log
spring.datasource.druid.log.username=root
spring.datasource.druid.log.password=123456
spring.datasource.druid.log.driver-class-name=com.mysql.jdbc.Driver
```

然后在项目中定义一个配置类，配置连接池，手动配置的原因是连接池可以在elastic-job-starter之前被初始化。

```
@Configuration
public class BeanConfig {
	
	/**
	 * 任务执行事件数据源
	 * @return
	 */
	@Bean("datasource")
	@ConfigurationProperties("spring.datasource.druid.log")
	public DataSource dataSourceTwo(){
	    return DruidDataSourceBuilder.create().build();
	}
	
}

```

然后在注解中增加数据源的配置即可：

```
@ElasticJobConf(name = "MySimpleJob", cron = "0/10 * * * * ?", 
	shardingItemParameters = "0=0,1=1", description = "简单任务", eventTraceRdbDataSource = "datasource")
```

## application.properties中配置任务信息

使用注解是比较方便，但很多时候我们需要不同的环境使用不同的配置，测试环境跟生产环境的配置肯定是不一样的，当然你也可以在发布之前将注解中的配置调整好然后发布。

为了能够让任务的配置区分环境，还可以在属性文件中配置任务的信息，当属性文件中配置了任务的信息，优先级就比注解中的高。

首先还是在任务类上加@ElasticJobConf(name = "MySimpleJob")注解，只需要增加一个name即可，任务名是唯一的。

剩下的配置都可以在属性文件中进行配置，格式为elasticJob.任务名.配置属性=属性值

```
elasticJob.MySimpleJob.cron=0/10 * * * * ?
elasticJob.MySimpleJob.overwrite=true
elasticJob.MySimpleJob.shardingTotalCount=1
elasticJob.MySimpleJob.shardingItemParameters=0=0,1=1
elasticJob.MySimpleJob.jobParameter=test
elasticJob.MySimpleJob.failover=true
elasticJob.MySimpleJob.misfire=true
elasticJob.MySimpleJob.description=simple job
elasticJob.MySimpleJob.monitorExecution=false
elasticJob.MySimpleJob.listener=com.cxytiandi.job.core.MessageElasticJobListener
elasticJob.MySimpleJob.jobExceptionHandler=com.cxytiandi.job.core.CustomJobExceptionHandler
elasticJob.MySimpleJob.disabled=true
```

## Script任务使用说明
由于Script任务的执行逻辑是在具体的脚本中，是通过scriptCommandLine来指定执行脚本的路径。我这边为了统一的去发现项目中的任务列表，还是需要建一个脚本的Java类，加上ElasticJobConf注解，只是不需要写逻辑而已，示例如下：

```
/**
 * 脚本任务不需要写逻辑，逻辑在被执行的脚本中，这边只是定义一个任务而已
 * @author yinjihuan
 *
 */
@ElasticJobConf(name = "MyScriptJob")
public class MyScriptJob implements ScriptJob {

	public void execute(ShardingContext context) {
		
	}

}

```

配置：

```
elasticJob.MyScriptJob.cron=0/10 * * * * ?
elasticJob.MyScriptJob.overwrite=true
elasticJob.MyScriptJob.scriptCommandLine=D:\\apache-tomcat-addrepo-allcity\\bin\\startup.bat
```

##  Spring XML配置代码示例

[https://github.com/yinjihuan/spring-cloud/tree/master/fangjia-job](https://github.com/yinjihuan/spring-cloud/tree/master/fangjia-job)


# 作者
- 尹吉欢 1304489315@qq.com
- 博客 http://cxytiandi.com/blogs/yinjihuan

更多技术分享请关注微信公众号：猿天地

![image.png](http://upload-images.jianshu.io/upload_images/2685774-da01a73d0cfc3f35.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
