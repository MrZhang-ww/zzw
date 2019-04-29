package com.hand.activity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.hibernate.validator.constraints.Length;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.util.Date;

public class ActivityDto {
    @Id
    @GeneratedValue
    private Long activityId;


    @Length(max = 50)
    private String name; //活动名称

    private Date createData; //创建日期


    @Length(max = 50)
    private String people; //创建人
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    private Date startReleaseData; //发布日期从
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    private Date endReleaseData; //发布日期到


    @Length(max = 50)
    private String status; //状态

    @Length(max = 65535)
    private String content; //活动内容

    @Length(max = 50)
    private String roleName; //角色名称


    @Length(max = 50)
    private String activityEmployee; //负责管理员工

    @NotNull
    private Double money; //活动金额


    public void setActivityId(Long activityId){
        this.activityId = activityId;
    }

    public Long getActivityId(){
        return activityId;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public void setCreateData(Date createData){
        this.createData = createData;
    }

    public Date getCreateData(){
        return createData;
    }

    public void setPeople(String people){
        this.people = people;
    }

    public String getPeople(){
        return people;
    }

    public void setStartReleaseData(Date startReleaseData){
        this.startReleaseData = startReleaseData;
    }

    public Date getStartReleaseData(){
        return startReleaseData;
    }

    public void setEndReleaseData(Date endReleaseData){
        this.endReleaseData = endReleaseData;
    }

    public Date getEndReleaseData(){
        return endReleaseData;
    }

    public void setStatus(String status){
        this.status = status;
    }

    public String getStatus(){
        return status;
    }

    public void setContent(String content){
        this.content = content;
    }

    public String getContent(){
        return content;
    }

    public void setRoleName(String roleName){
        this.roleName = roleName;
    }

    public String getRoleName(){
        return roleName;
    }

    public void setActivityEmployee(String activityEmployee){
        this.activityEmployee = activityEmployee;
    }

    public String getActivityEmployee(){
        return activityEmployee;
    }

    public void setMoney(Double money){
        this.money = money;
    }

    public Double getMoney(){
        return money;
    }
}
