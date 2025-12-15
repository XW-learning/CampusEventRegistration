//import com.seu.campus.event.registration.model.User;
//import com.seu.campus.event.registration.service.UserService;
//import com.seu.campus.event.registration.service.impl.UserServiceImpl;
//
///**
// * 测试注册功能
// */
//public class TestRegister {
//    public static void main(String[] args) {
//        // 1. 模拟 Service 对象 (在 Servlet 中也是这样调用的)
//        UserService userService = new UserServiceImpl();
//
//        // 2. 模拟一个前端传来的 User 对象
//        User newUser = new User();
//        // 建议每次运行修改一下学号，避免主键冲突
//        newUser.setUsername("21319001");
//        newUser.setPassword("123456");
//        newUser.setRealName("张三");
//        newUser.setRole("student");
//        newUser.setEmail("zhangsan@seu.edu.cn");
//        newUser.setPhone("13800138000");
//
//        System.out.println("====== 开始测试注册 ======");
//
//        // 3. 调用注册业务
//        String result = userService.register(newUser);
//
//        // 4. 输出结果
//        System.out.println("注册结果: " + result);
//
//        if ("SUCCESS".equals(result)) {
//            System.out.println("✅ 测试通过！请去数据库表 t_user 查看是否多了一条数据。");
//        } else {
//            System.out.println("❌ 测试失败！原因: " + result);
//        }
//    }
//}