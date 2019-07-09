package com.twelve.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twelve.model.sign.SignRecords;
import com.twelve.repository.SignRecordsRepo;
import com.twelve.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * Created by wang0 on 2016/9/28.
 */
@Controller
@RequestMapping("/signDetail")
public class SignDetailCrol {

    @Autowired
    private SignRecordsRepo signRecordsRepo;

    public String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @RequestMapping("")
    public String showpage(){
        return "signDetail";
    }

    @RequestMapping("/getData")
    @ResponseBody
    public List<SignRecords> getData(@RequestBody String string) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> map = (Map<String, String>) mapper.readValue(string, Map.class);
        String starts = map.get("starts");
        String ends = map.get("ends");
        String name = getCurrentUsername();
        System.out.println(name);
        long start = new SimpleDateFormat("yyyy-MM-dd").parse(starts).getTime();
        long end = new SimpleDateFormat("yyyy-MM-dd").parse(ends).getTime();

        List<SignRecords> signRecordses = signRecordsRepo.queryByNameTimeDetail(name, new Timestamp(start), new Timestamp(end));
        System.out.println(signRecordses.size());
        if (signRecordses.size() != 0 && signRecordses != null) {
            String total = String.valueOf(DateUtil.formatdate(signRecordsRepo.queryByNameTime(name, new Timestamp(start), new Timestamp(end))));
            SignRecords sign = new SignRecords();
            sign.setStrTime(total);
            signRecordses.add(sign);
        }

        return signRecordses;
    }

}
