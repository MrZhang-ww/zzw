package com.hand.activity.service;

import com.hand.activity.dto.ActivityDto;
import com.hand.activity.dto.ActivityFile;
import com.hand.activity.dto.ActivityStatement;
import com.hand.hap.core.IRequest;
import com.hand.hap.core.ProxySelf;
import com.hand.hap.system.dto.ResponseData;
import com.hand.hap.system.service.IBaseService;
import com.hand.activity.dto.ActivityZzw;

import java.io.InputStream;
import java.util.List;

public interface IActivityZzwService extends IBaseService<ActivityZzw>, ProxySelf<IActivityZzwService>{
    public List<ActivityZzw> selectAllAct(int page, int pageSize,ActivityZzw act);
//提交活动开启工作流
    public boolean submitAct(ActivityZzw act, IRequest requestContext);

    public List<ActivityZzw> selectActEnd(int page, int pageSize,ActivityZzw act);

    public boolean exportExcel(List<ActivityDto> list);

    public List<ActivityFile> selectFileById(int page, int pageSize, int activityId);

    public ActivityFile selectByPrimaryKey(IRequest var1, Long var2);

    ResponseData importExcel(InputStream is, String fileName,IRequest requestCtx) throws Exception;

    List<ActivityStatement> queryStatement(int page, int pageSize, ActivityZzw dto);
}