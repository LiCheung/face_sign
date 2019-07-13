package com.twelve.controller;

import com.twelve.model.login.Member;
import com.twelve.repository.MemberRepo;
import com.twelve.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Created by wang0 on 2016/9/13.
 */
@Controller
public class MemberCrol {

    private final ResourceLoader resourceLoader;

    @Autowired
    public MemberCrol(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Value("${web.upload-path}")
    private String path;



    @Autowired
    private MemberRepo memberRepo;

    public String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @RequestMapping("/member/setpwd")
    public String setPwd(@RequestParam String oldpwd,
                         @RequestParam String newpwd1,
                         @RequestParam String newpwd2) {

        String name = getCurrentUsername();
        if (oldpwd.equals(memberRepo.findPwdByName(name)) && newpwd1.equals(newpwd2)) {
            memberRepo.setPwd(newpwd1, name);
        }

        return "redirect:/signDetail";

    }


    @RequestMapping("/member/register")
    public String saveMember(@RequestParam String names,
                             @RequestParam String newpwd1,
                             @RequestParam String newpwd2,
                             @RequestParam Long stu_id,
                             @RequestParam Integer grade,
                             @RequestParam("fileName") MultipartFile file, Map<String, Object> map) {

        if (newpwd1.equals(newpwd2)) {
            String new_name = names + ".png";
            if(FileUtils.upload(file, path, new_name)) {
                Member member = new Member();
                member.setName(names);
                member.setPwd(newpwd1);
                member.setStuId(stu_id);
                member.setGrade(grade);
                member.setIsstart(0);
                member.setPicAddr(path + '/' +  new_name);
                memberRepo.save(member);
                return "redirect:/signDetail";
            }
        }
        map.put("msg", "保存失败");
        map.put("fileName", file.getOriginalFilename());
        return "/register";
    }
}
