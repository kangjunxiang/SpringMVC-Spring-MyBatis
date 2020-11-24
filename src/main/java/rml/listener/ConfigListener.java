package rml.listener;

import org.antframework.configcenter.client.Config;
import org.antframework.configcenter.client.ConfigsContext;
import org.antframework.configcenter.client.core.ChangedProperty;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.List;

public class ConfigListener implements ServletContextListener {
    private static final Log log = LogFactory.getLog(ConfigListener.class);
    ConfigsContext configsContext = null;
    private static Config customerConfig = null;
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        // 当系统运行结束时，需关闭客户端释放相关资源
        configsContext.close();
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            String filePath = event.getServletContext().getRealPath("/") ;
            log.info("初始化数据库连接池配置文件，路径是：" + filePath);
            // 创建客户端
            configsContext = new ConfigsContext(
                    "customer",                 // 主体应用id
                    "dev",                      // 环境id
                    null,                       // 目标（用于标记客户端，可以为null）
                    "http://127.0.0.1:6220",  // 服务端地址
                    "D:\\var\\apps\\customer\\configcenter");  // 缓存文件夹路径

            // 获取会员系统的配置
            customerConfig = configsContext.getConfig("customer");
            // 现在就可以获取会员系统的所有配置项了（下面获取redis地址配置）
            String redisHost = customerConfig.getProperties().getProperty("redis.host");
            System.out.println(redisHost);

            // 将配置中心的属性定义与属性值加载到环境变量中，调整 <context:property-placeholder location="classpath:config.properties" system-properties-mode="OVERRIDE"/>
            // 设置 system-properties-mode="OVERRIDE" 用以覆盖 properties 中的值
            String[] propertyKeys = customerConfig.getProperties().getPropertyKeys();
            if( propertyKeys != null && propertyKeys.length > 0 ){
                 for(int i = 0;i < propertyKeys.length;i++ ){
                     System.setProperty(propertyKeys[i],customerConfig.getProperties().getProperty(propertyKeys[i]));
                     System.out.println(propertyKeys[i]);
                     System.out.println(customerConfig.getProperties().getProperty(propertyKeys[i]));
                 }
            }
            // 不仅可以获取会员系统的配置，还可以获取其他应用的配置，不过只能获取其他应用的公开配置，
            // 因为当前主体应用为会员系统，现在是以会员系统为视角获取其他应用的配置
            // 下面是获取账务系统的公开配置
         //   Config accountConfig = configsContext.getConfig("account");

            // 还可以注册配置变更监听器
            customerConfig.getListeners().addListener(new org.antframework.configcenter.client.ConfigListener() {
                @Override
                public void onChange(List<ChangedProperty> changedProperties) {
                    for (ChangedProperty changedProperty : changedProperties) {
                        log.info("监听到会员系统的配置有变更：");
                        log.info(changedProperty.getOldValue()+" ----》 "+changedProperty.getNewValue());
                    }
                }
            });
            // 开启接收服务端推送的配置变更通知
            configsContext.listenServer();

        }
        catch (Exception e) {
            log.error("加载数据库连接异常:" , e);
        }

    }

    public static String getProperty(String key){
        return customerConfig.getProperties().getProperty(key);
    }

}
