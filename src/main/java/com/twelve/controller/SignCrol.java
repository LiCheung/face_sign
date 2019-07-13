package com.twelve.controller;

import com.alibaba.fastjson.JSON;
import com.baidu.aip.face.AipFace;
import com.baidu.aip.face.MatchRequest;
import com.baidu.aip.util.Base64Util;
import com.google.common.collect.Lists;
import com.twelve.model.login.Member;
import com.twelve.model.sign.Data;
import com.twelve.model.sign.SignRecords;
import com.twelve.repository.MemberRepo;
import com.twelve.repository.SignRecordsRepo;
import com.twelve.utils.DateUtil;
import com.twelve.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

@Controller
@Slf4j
@RequestMapping("/sign")
public class SignCrol {

    @Autowired
    private MemberRepo signMemberRepo;

    @Autowired
    private SignRecordsRepo signRecordsRepo;

//    public static List<Map<String, Object>> WARN_MAP = new LinkedList<>();

    private Jedis jedis = RedisUtil.getJedis();

    @RequestMapping("/name")
    @ResponseBody
    public List<Map<String, Object>> show() {

//        List<SignMember> members = memberRepo.findAll();
        /**
         * 使用HQL进行某个表的多字段查询
         */

        List<Object[]> members = signMemberRepo.findNamesAndIsstart();
        List<Map<String, Object>> returnInfos = new LinkedList<>();
        for (Object[] member : members) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", member[0]);
            map.put("isstart", member[1]);
            returnInfos.add(map);
        }

        return returnInfos;
    }

    @RequestMapping("")
    public String showpage(HttpServletRequest request) {
        //设置会话超时时间为1天
        request.getSession().setMaxInactiveInterval(24 * 60 * 60);
        return "signWork";
    }

    @RequestMapping(value = "/start", produces = "application/text")
    @ResponseBody
    public String sendStart(@RequestBody Data data) {

        System.out.println("aaaaac");
        /*System.out.println(data.getName());

        System.out.println(data.getData());*/
        String name = data.getName();
        Member member = signMemberRepo.findByName(name);

        final String APP_ID = "16751944";
        final String API_KEY = "5W53rSvUNgbFjQeIDGULsf2K";
        final String SECRET_KEY = "HmQToe8eVzNGxuFSOlZeeXArHG4rUjLI";

        AipFace client = new AipFace(APP_ID, API_KEY, SECRET_KEY);

        String imgAddr = member.getPicAddr();
        System.out.println("aaaaaaaaa"+imgAddr);
        byte[] b1 = new byte[0];
        try {
            b1 = FileUtils.readFileToByteArray(new File(imgAddr));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String s1 = Base64Util.encode(b1);
        String s2 = data.getData().split(",")[1];

        System.out.println("bbbbbbbbbb"+s1.getBytes());
        MatchRequest req1 = new MatchRequest(s1, "BASE64");
        MatchRequest req2 = new MatchRequest(s2, "BASE64");


        List<MatchRequest> list = Lists.newArrayList();
        list.add(req1);
        list.add(req2);

       /* JSONObject rs = client.match(list);*/

        JSONObject rs = client.match(list);
        System.out.println(rs);
        if(rs.getString("error_msg").equals("SUCCESS") == true) {
            JSONObject result = rs.getJSONObject("result");
            Double score = result.getDouble("score");
            if (score >= 80.0) {
                SignRecords signRecords = new SignRecords(name);
                if (signRecordsRepo.save(signRecords) != null) {
                    signMemberRepo.setIsStart(name);
                    return name + "签到成功" + "  匹配度为:" + score;
                } else {
                    log.error(name + "签到失败");
                    return name + "签到失败，请重试";
                }
            } else {
                log.error(name + "签到失败");
                return "不是本人，无法签到" + "  匹配度为:" + score;
            }
        }
        else if(rs.getString("error_msg").equals("pic not has face")){
            return "没有识别到人脸";
        }
        else{
            return "其他识别问题";
        }
    }

    @RequestMapping("/getWarn")
    @ResponseBody
    public Object getWarn() {

        List<Map<String, Object>> warnMap = new LinkedList<>();

        long now = System.currentTimeMillis();
        List<String> names = signMemberRepo.findNamesStart();

        if(jedis != null){
            jedis.del("warnMap");
        }

        System.out.println(jedis.set("haha", "haha"));
        for (String name : names) {
            Timestamp cometime = signRecordsRepo.selectDescZCometime(name);
            if (cometime != null) {
//            如果大于四小时进行提示
                if (now - cometime.getTime() > 14400000) {
                    Map<String, Object> map = new HashMap();
                    map.put("name", name);
                    map.put("time", DateUtil.formatdate(now - cometime.getTime()));
                    log.info("name" + name + "and : time " + DateUtil.formatdate(now - cometime.getTime()));
                    warnMap.add(map);
                }
            }
        }
        if (warnMap.size() > 0) {

            String jsonString = JSON.toJSONString(warnMap);
            System.out.println(jsonString);
            jedis.set("warnMap", jsonString);

            return warnMap;
        } else {
            return "";
        }
    }

    @RequestMapping(value = "/onceEnd", produces = "application/text")
    @ResponseBody
    public String onceEnd() {
//        List<Map<String, Object>> infos = (List<Map<String, Object>>) getWarn();

        String warnMap = jedis.get("warnMap");

        List<HashMap> hashMaps = JSON.parseArray(warnMap, HashMap.class);
        //超过六小时被删除的
        StringBuffer deletedInfo = new StringBuffer("");
        if (hashMaps.size() > 0) {
            List<HashMap> outTimePerson = hashMaps;
            try {
                for (Map<String, Object> map : outTimePerson) {
                    String name = (String) map.get("name");
//                    Long id = signRecordsRepo.selectDesc(name).get(0).getId();
                    Long id = signRecordsRepo.selectDescZId(name);
                    Long cometime = signRecordsRepo.selectComeTime(id).getTime();
                    Timestamp leaveTimeStamp = new Timestamp(System.currentTimeMillis());
                    Long totalTime = leaveTimeStamp.getTime() - cometime;
                    if (totalTime > 6 * 60 * 60 * 1000) {
                        signRecordsRepo.delete(id);
                        signMemberRepo.setIsEnd(name);
                        deletedInfo.append(name + ",");
                    } else {
                        String str_total = String.valueOf(DateUtil.formatdate(totalTime));
                        if (signRecordsRepo.setSendEnd(leaveTimeStamp, totalTime, str_total, id) == 1) {
                            signMemberRepo.setIsEnd(name);
                        }
                        SignRecords signRecords = new SignRecords(name);
                        if (signRecordsRepo.save(signRecords) != null) {
                            signMemberRepo.setIsStart(name);
                        } else {
                            throw new Exception("一键重签失败");
                        }
                    }
                }
            } catch (Exception e) {
                log.error("一键重签失败,请重试");
                return "一键重签失败,请重试";
            }
            jedis.del("warnMap");
        }
        if (!deletedInfo.toString().equals("")) {
            String string = deletedInfo.deleteCharAt(deletedInfo.length() - 1).append("超过六小时，此次签到无效").toString();
            log.error(string);
            return string;
        }
        return "一键重签成功";
    }

    @RequestMapping(value = "/end", produces = "application/text")
    @ResponseBody
    public String sendend(@RequestBody String name) {

//        Long id = signRecordsRepo.selectDesc(name).get(0).getId();
        Long id = signRecordsRepo.selectDescZId(name);

        Long cometime = signRecordsRepo.selectComeTime(id).getTime();
        Timestamp leaveTimeStamp = new Timestamp(System.currentTimeMillis());

        Long totalTime = leaveTimeStamp.getTime() - cometime;
        if (totalTime > 6 * 60 * 60 * 1000) {
            signRecordsRepo.delete(id);
            signMemberRepo.setIsEnd(name);
            log.error(name + "签到超过6小时，签到无效");

            return name + "签到超过6小时，签到无效";
        } else {
            String str_total = String.valueOf(DateUtil.formatdate(totalTime));
            if (signRecordsRepo.setSendEnd(leaveTimeStamp, totalTime, str_total, id) == 1) {
                signMemberRepo.setIsEnd(name);
                return name + "签退成功";
            } else {
                log.error(name + "签退失败，请重试");
                return name + "签退失败，请重试";
            }
        }

    }

}
