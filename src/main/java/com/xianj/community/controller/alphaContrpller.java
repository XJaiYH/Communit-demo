package com.xianj.community.controller;

import com.xianj.community.service.AlphaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

// 与controller功能等价的注解，该注解就是将Bean加载到IoC容器，主要用于处理请求的组件
// 开发业务组件，使用Service注解
// 开发数据库访问组件，使用Repository注解
// Service是通过Component实现的，所以Component注解也可以，如果是通用组件
@Controller
@RequestMapping("/alpha")
public class alphaContrpller {

    @Autowired
    AlphaService alphaService;

    @RequestMapping("/time")
    @ResponseBody
    public String printInfo(){
        //alphaService.printInfo();
        return "ok";
    }

    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello(){
        return "Hello Spring Boot.";
    }

    @RequestMapping("/data")
    @ResponseBody
    public String getData(){
        return alphaService.find();
    }

    @RequestMapping("/http")// 声明请求路径
    public void http(HttpServletRequest request, HttpServletResponse response){
        // 获取请求数据
        System.out.println(request.getMethod());
        System.out.println(request.getServletPath());
        Enumeration<String> enumeration = request.getHeaderNames();// 该类型迭代器，很老了
        while(enumeration.hasMoreElements()){
            String name = enumeration.nextElement();
            String value = request.getHeader(name);
            System.out.println(name + ": " + value);
        }
        System.out.println(request.getParameter("code"));// 获取浏览器传入的参数，参数名为code

        // response 用于向浏览器返回响应数据
        response.setContentType("text/html;charset=utf-8"); //返回类型
        try (
                PrintWriter printWriter = response.getWriter();// 该类必须有实现close方法，才可以在try后的小括号中创建，
                                                               // 编译时会自动在最后加上finally来销毁对象
        ) {
            printWriter.write("<h1>xianjun</h1>");// 向浏览器打印
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // GET请求处理，一般用于从浏览器获取数据
    // /students?current=1&limit=20   路径中，“?”之后带的是参数，后端可以获取这些参数
    @RequestMapping(path="/students", method = RequestMethod.GET)// 用method限制可以处理请求的类型（即从浏览器获取请求的类型）
    @ResponseBody //用于响应
    public String getStudents(
            @RequestParam(name = "current", required = false, defaultValue = "1") int current,
            @RequestParam(name = "limit", required = false, defaultValue = "5") int limit) {
        // 如此，可以获取到请求中附带的参数，要求参数名保持一致。
        // 如果路径中没有参数呢？可以加上@RequestParam注解，指定传入的参数名、默认值、是否需要被传入
        // 表示request中名为current的参数赋值给current形参，required说该参数可以不传，若不传则默认值为"1"
        System.out.println(current);
        System.out.println(limit);
        return "students";
    }

    // 如果参数成为了路径的一部分，如何获取，如 /student/123
    @RequestMapping(path = "/student/{id}", method = RequestMethod.GET) //其中，{}为参数名，注意，这里不是student路径，而是/student/{id}
    @ResponseBody
    public String getStudent(@PathVariable("id") int id){ // 用@PathVariable注解指定传入的参数名，并传给形参id
        System.out.println(id);
        return "a student";
    }

    // POST请求，浏览器在网页中填写表单后提交
    // 为什么不用GET？ 一是GET方法传参会将参数写入路径，其次路径长度有限，没法传许多参数
    @RequestMapping(path = "/student", method = RequestMethod.POST)
    @ResponseBody// 不加该注解默认返回html
    public String saveStudent(String name, int age){// 需要形参名和表单中的名字一致，可以传入参数。也可通过@RequestParam注解
        System.out.println(name);
        System.out.println(age);
        return "success";
    }

    // 响应HTML数据，使用thymeleaf模板
    @RequestMapping(path = "/teacher", method = RequestMethod.GET)
    public ModelAndView getTeacher(){
        ModelAndView mav = new ModelAndView();
        mav.addObject("name", "Lisi");
        mav.addObject("age", 30);
        mav.setViewName("/demo/view");// 后缀不用写，并且这是保存在templates文件夹下，templates不用写
        // 但是要先将/demo/view模板写好，才能渲染该模板用于返回
        return mav;
    }

    @RequestMapping(path = "/school", method = RequestMethod.GET)
    public String getSchool(Model model){// 调用代码时，DispatchServlet查看有Model对象，就会自动实例化该对象并传入
        model.addAttribute("name", "pku");// 网对象中存储数据
        model.addAttribute("age", 120);// 上一个方法中，将Model和View的数据都装入了对象
        // 该方法将Model装入形参中，将View路径返回给DispatchServlet，且DispatchServlet仍持有model的引用，所以可以将数据渲染到view中
        return "/demo/view"; // 返回视图路径
    }

    // 响应JSON数据（异步请求），如当前网页不刷新，但已经访问过数据库并返回了结果
    // JAVA对象 -> JSON字符串 -> JS对象，JSON在跨语言情况下很常用
    @RequestMapping(path = "/emp", method = RequestMethod.GET)
    @ResponseBody // 返回JSON
    public Map<String, Object> getEmp(){
        Map<String, Object> emp = new HashMap<>();
        emp.put("name", "Lisi");
        emp.put("age", 23);
        emp.put("salary", 8000);
        return emp;
    }

    @RequestMapping(path = "/emps", method = RequestMethod.GET)
    @ResponseBody // 返回JSON
    public List<Map<String, Object>> getEmps(){
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> emp = new HashMap<>();
        emp.put("name", "Lisi");
        emp.put("age", 23);
        emp.put("salary", 8000);
        list.add(emp);

        emp = new HashMap<>();
        emp.put("name", "Lisa");
        emp.put("age", 25);
        emp.put("salary", 8000);
        list.add(emp);

        emp = new HashMap<>();
        emp.put("name", "Shy");
        emp.put("age", 20);
        emp.put("salary", 10000);
        list.add(emp);
        return list;
    }
}
