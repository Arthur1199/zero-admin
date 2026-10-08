package com.zero.utils.doMain;

import com.zero.utils.common.NumberHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2025/7/21 00:04
 * 说明: 域名检测工具
 */
public class DomainCheckUtil {

    private static Logger logger = LoggerFactory.getLogger(DomainCheckUtil.class);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2025/7/21 00:10
     * @param doMainName 域名
     * @param survivalRatio 存活比例
     * @return
     * 返回值:boolean
     * 返回值说明：域名是否存活，数据来源参考 https://tool.chinaz.com/dns
     */
    public static boolean isSurvivalDoMain(String doMainName,Double survivalRatio){
        long startTime = System.currentTimeMillis();
        Integer errorCount = 0;
        // 目标域名（可替换为需要查询的域名，建议先测试www.baidu.com确认功能）
//        String domain = "www.kbx678.com";
        // 修正URL格式：添加?domain=参数
        String url = "https://tool.chinaz.com/dns/" + doMainName;

        // 配置Chrome浏览器（关闭无头模式，便于调试）
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new"); // 调试时注释，让浏览器可见
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36");

        // 初始化WebDriver
        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15)); // 延长初始等待时间

        try {
            driver.get(url);
//            System.out.println("已打开页面：" + url);

            // 等待页面核心容器加载（根据实际结构调整，此处为示例）
            // 观察页面后，发现结果表格在class为"w1200 mt20"的容器中
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".toolResTabBox")));
//            System.out.println("页面核心容器已加载");

            // 等待结果表格的表头（“响应时间”列）加载
            WebDriverWait longerWait = new WebDriverWait(driver, Duration.ofSeconds(30));
//            longerWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//em[contains(text(), 'tool.chinaz.com')]")));
            // 模糊匹配 class 包含 "ipStr" 的 em 标签（应对多 class 情况）
            longerWait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//em[contains(@class, 'ipStr')]/a[contains(@href, 'tool.chinaz.com')]")
            ));
//            System.out.println("数据表格已加载");
            // 额外等待5秒确保渲染完成
            Thread.sleep(5000);
            // 获取页面源码并解析
            String pageSource = driver.getPageSource();
            Document doc = Jsoup.parse(pageSource);

            // 定位结果表格（根据实际class调整，示例为"tb_data"）
            Element resultTable = doc.selectFirst("#tbSort");
            if (resultTable == null) {
                logger.info("未找到数据表格，请检查选择器是否正确!");
                return false;
            }
            // 提取数据行
            Elements rows = resultTable.select("tr");
            List<DoMainCheckModdel> nodeInfoList = new ArrayList<>();
            // 跳过表头行（第0行），从第1行开始遍历
            for (int i = 1; i < rows.size(); i++) {
                Element row = rows.get(i);
                Elements cols = row.select("td");
                if (cols.size() >= 5) {
                    String location = cols.get(0).text().trim(); // 节点位置（如“北京 联通”）
//                    String ip = cols.get(1).text().trim();      // IP地址
//                    String recordType = cols.get(2).text().trim(); // 记录类型
                    String responseTime = cols.get(2).text().trim(); // 响应时间
                    String ttl = cols.get(3).text().trim();     // TTL值
//                    logger.info("位置：{},解析时间：{}，TTL：{}",location,responseTime,ttl);
                    System.out.printf("位置: %s,解析时间: %s, TTL: %s%n", location, responseTime, ttl);
                    if(responseTime.equals("--") || ttl.equals("--")){
//                        System.err.println(String.format("位置: %s,解析时间: %s, TTL: %s,解析失败！", location, responseTime, ttl));
                        //解析失败，新增失败次数
                        errorCount ++;
                        continue;
                    }
                    DoMainCheckModdel item = new DoMainCheckModdel();
                    item.setLocation(location);
                    item.setResponseTime(Long.parseLong(responseTime.replace("ms", "").replace("--", "")));
                    item.setTtl(Long.parseLong(ttl));
                    nodeInfoList.add(item);
//                    nodeInfoList.add(String.format(
//                            "位置: %s,解析时间: %s, TTL: %s",
//                            location, responseTime, ttl
//                    ));
                }
            }
            // 输出结果
//            logger.info("\n=== 域名节点超时情况 ===");
//            if (nodeInfoList.isEmpty()) {
//                logger.error("未提取到数据，请检查表格结构");
//            } else {
////                nodeInfoList.forEach(System.out::println);
//            }
            System.out.println("解析成功数量：" + nodeInfoList.size() + ",解析失败数量：" + errorCount);
            if(!nodeInfoList.isEmpty()){
                double div = NumberHelper.div(errorCount.doubleValue(), (nodeInfoList.size() + errorCount));
                System.out.println("失败百分百比例：" + div);
                if(div > survivalRatio){
                    return false;
                }else {
                    return true;
                }
            }else{
                return false;
            }
        } catch (TimeoutException e) {
            e.printStackTrace();
            logger.error("域名检测失败，域名无效：", e);
            return false;
        }catch (Exception e){
            e.printStackTrace();
            logger.error("域名检测失败，系统异常：", e);
            return false;
        }
        finally {
            // 关闭浏览器（调试时可注释，保留窗口观察）
            driver.quit();
            long endTime = System.currentTimeMillis();
            System.out.println("域名：" + doMainName + ",检测耗时：" +(endTime - startTime ) + ",毫秒!");
        }
    }

    public static void main(String[] args) {
        boolean survivalDoMain = DomainCheckUtil.isSurvivalDoMain("www.kbx67.com", 0.2d);
        System.out.println("当前域名是否存活：" + (survivalDoMain ? "是" : "否"));
    }
}
