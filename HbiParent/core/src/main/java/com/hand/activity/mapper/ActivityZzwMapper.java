package com.hand.activity.mapper;

import com.hand.activity.dto.ActivityFile;
import com.hand.activity.dto.ActivityStatement;
import com.hand.hap.mybatis.common.Mapper;
import com.hand.activity.dto.ActivityZzw;

import java.util.List;

public interface ActivityZzwMapper extends Mapper<ActivityZzw>{
    public List<ActivityZzw> selectAllAct(ActivityZzw act);

    public boolean upActStatus(ActivityZzw act);

    //修改活动状态为发布中
    public void upActRelease();

    //修改活动状态为结束
    public void upActEnd();

    public List<ActivityZzw> selectActEnd(ActivityZzw act);

    public List<ActivityFile> selectFileById(int activityId);

    public ActivityFile selectByFiled(Long filed);

    public List<String> selectRoleById(Long activityId);

    public List<String> selectImgsById(Long activityId);
    public ActivityZzw selectReleaseAct(Long activityId);

    public List<Long> selectIdByStatus();

    public List<ActivityZzw> selectReleaseActs();

    public List<ActivityStatement> selectStatement();
}