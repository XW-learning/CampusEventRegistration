//import com.seu.campus.event.registration.model.Event;
//import com.seu.campus.event.registration.service.EventService;
//import com.seu.campus.event.registration.service.impl.EventServiceImpl;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
///**
// * 活动发布功能测试类
// * 职能：模拟 Controller 调用 Service，测试业务逻辑和数据库保存是否正常。
// */
//public class TestEventPublish {
//    public static void main(String[] args) {
//        // 1. 实例化 Service 层（测试只针对 Service 及其下层）
//        EventService eventService = new EventServiceImpl();
//
//        // 2. 准备日期时间工具
//        // 注意：这里使用精确的日期格式进行转换。
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//        try {
//            // 3. 模拟一个活动对象
//            Event newEvent = new Event();
//
//            // 设置活动信息
//            newEvent.setTitle("校级编程交流大会");
//            newEvent.setCategory("学术");
//            newEvent.setLocation("九龙湖计算机学院");
//            newEvent.setDetail("欢迎全校师生参与，交流最新的编程技术和趋势。");
//
//            // 关键：设置发布者 ID
//            // !!! 警告：请确保你的 t_user 表中，存在 user_id=1 的用户，否则会违反外键约束而失败。
//            newEvent.setPublisherId(1);
//
//            // 设置时间（确保逻辑正确：开始时间 < 结束时间）
//            newEvent.setStartTime(sdf.parse("2026-03-10 09:00:00"));
//            newEvent.setEndTime(sdf.parse("2026-03-10 12:00:00"));
//            newEvent.setRegDeadline(sdf.parse("2026-03-05 23:59:59"));
//
//            // is_active 字段在 Service 层会设置为默认值 1，无需手动设置。
//
//            System.out.println("====== 开始测试活动发布 ======");
//
//            // 4. 调用发布业务
//            String result = eventService.publishEvent(newEvent);
//
//            // 5. 输出结果
//            System.out.println("活动发布结果: " + result);
//
//            if ("SUCCESS".equals(result)) {
//                System.out.println("✅ 测试通过！请去数据库表 t_event 查看是否多了一条数据。");
//            } else {
//                System.out.println("❌ 测试失败！原因: " + result);
//            }
//
//        } catch (ParseException e) {
//            System.err.println("❌ 测试失败！日期解析异常。请检查日期字符串和 SimpleDateFormat 格式是否匹配。");
//            e.printStackTrace();
//        } catch (Exception e) {
//            System.err.println("❌ 测试失败！数据库连接或反射等底层系统错误。");
//            e.printStackTrace();
//        }
//    }
//}