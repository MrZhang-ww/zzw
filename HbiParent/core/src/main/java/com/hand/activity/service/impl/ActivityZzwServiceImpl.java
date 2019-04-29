package com.hand.activity.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import cn.hutool.poi.excel.StyleSet;
import com.github.pagehelper.PageHelper;
import com.hand.activiti.dto.ActivitiDto;
import com.hand.activiti.service.BillReturnService;
import com.hand.activity.dto.ActivityDto;
import com.hand.activity.dto.ActivityFile;
import com.hand.activity.dto.ActivityStatement;
import com.hand.activity.mapper.ActivityZzwMapper;
import com.hand.hap.core.IRequest;
import com.hand.hap.system.dto.ResponseData;
import com.hand.hap.system.service.impl.BaseServiceImpl;
import org.activiti.rest.service.api.engine.variable.RestVariable;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hand.activity.dto.ActivityZzw;
import com.hand.activity.service.IActivityZzwService;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class ActivityZzwServiceImpl extends BaseServiceImpl<ActivityZzw> implements IActivityZzwService{
    @Autowired
    ActivityZzwMapper actMapper;
    @Autowired
    BillReturnService billReturnService;
    private final static String excel2003L =".xls";    //2003- 版本的excel
    private final static String excel2007U =".xlsx";
    @Override
    public List<ActivityZzw> selectAllAct(int page, int pageSize,ActivityZzw act) {
        PageHelper.startPage(page,pageSize);
        return actMapper.selectAllAct(act);
    }

    @Override
    public boolean submitAct(ActivityZzw act,IRequest requestContext) {
        List<ActivityZzw> activityZzws = selectAllAct(1, 10, act);
        if(null!=act.getActivityId()){
            ActivitiDto dto1 =new ActivitiDto();
            createParams(dto1,act);
            billReturnService.startBill(requestContext,dto1);
        }
        return true ;
    }

    @Override
    public List<ActivityZzw> selectActEnd(int page, int pageSize, ActivityZzw act) {
        PageHelper.startPage(page,pageSize);
        return actMapper.selectActEnd(act);
    }

    @Override
    public boolean exportExcel(List<ActivityDto> list) {
        DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        for (ActivityDto activityZzw : list) {

            if(activityZzw.getStatus().equals("NEWACTIVITY")){
                activityZzw.setStatus("新建");
            }else if(activityZzw.getStatus().equals("AUDITING")){
                activityZzw.setStatus("审核中");
            }else if(activityZzw.getStatus().equals("VERIFIED")){
                activityZzw.setStatus("审核通过");
            }else if(activityZzw.getStatus().equals("REFUSE")){
                activityZzw.setStatus("审核拒绝");
            }else if(activityZzw.getStatus().equals("RELEASEING")){
                activityZzw.setStatus("发布中");
            }else{
                activityZzw.setStatus("结束");
            }
        }
        ExcelWriter writer= ExcelUtil.getWriter("d:/ActivityExcel/activity"+System.currentTimeMillis()+".xlsx");
        writer.addHeaderAlias("activityId","活动ID");
        writer.addHeaderAlias("name","活动名称");
        writer.addHeaderAlias("createData","创建时间");
        writer.addHeaderAlias("people","创建人");
        writer.addHeaderAlias("startReleaseData","发布时间");
        writer.addHeaderAlias("endReleaseData","结束时间");
        writer.addHeaderAlias("status","活动状态");
        writer.addHeaderAlias("content","活动内容");
        writer.addHeaderAlias("roleName","角色名称");
        writer.addHeaderAlias("activityEmployee","管理人");
        writer.addHeaderAlias("money","活动金额");
        for (int i=0;i<11;i++){
            writer.setColumnWidth(i,20);
        }

        /*writer.setColumnWidth(1, 30);
        writer.setColumnWidth(2,25);*/

        writer.write(list);
        writer.close();
        return true;
    }

    @Override
    public List<ActivityFile> selectFileById(int page, int pageSize, int activityId) {
        PageHelper.startPage(page,pageSize);
        return actMapper.selectFileById(activityId);
    }

    @Override
    public ActivityFile selectByPrimaryKey(IRequest var1, Long fileId) {
        return this.actMapper.selectByFiled(fileId);
    }

    @Override
    public ResponseData importExcel(InputStream is, String fileName,IRequest requestCtx) throws Exception {
        ResponseData rd = new ResponseData();
        List<List<Object>> list = new ArrayList<List<Object>>();
        Workbook workbook = null;
        String fileType = fileName.substring(fileName.lastIndexOf("."));

        if (excel2003L.equals(fileType)) {
            workbook = new HSSFWorkbook(is); //2003-
        } else if (excel2007U.equals(fileType)) {
            workbook = new XSSFWorkbook(is); //2007+
        } else {
            throw new Exception("解析的⽂文件格式有误！");
        }
        Sheet worksheet = null;
        Row row = null;
        Cell cell = null;
//遍历Excel中所有的sheet
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            worksheet = workbook.getSheetAt(i);
            if (worksheet == null) {
                continue;
            }
//遍历当前sheet中的所有⾏行行
            for (int j = worksheet.getFirstRowNum(); j < worksheet.getLastRowNum() + 1; j++) {
                    row = worksheet.getRow(j);
// 跳过空⾏行行和标题⾏行行（第⼀一⾏行行）
                if (row == null || row.getFirstCellNum() == j) {
                    continue;
                }
//遍历所有的列列
                List<Object> li = new ArrayList<Object>();
                for (int y = row.getFirstCellNum(); y < row.getLastCellNum(); y++) {
                    cell = row.getCell(y);
                    Object cellValue = this.getCellValue(cell);
                    if(cellValue.equals("")){
                        rd.setMessage(worksheet.getRow(0).getCell(y)+"为空!!请完善表格后导入");
                        return rd;
                    }
                    li.add(cellValue);
                }
                list.add(li);
            }
        }
        workbook.close();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < list.size(); i++) {
            List<Object> excelRow = list.get(i);
            ActivityZzw dto = new ActivityZzw();
            dto.setName(excelRow.get(1).toString());
           // dto.setCreateData(sdf.parse(new Date().toString()));
            dto.setPeople(excelRow.get(3).toString());
            dto.setStartReleaseData(sdf.parse(excelRow.get(4).toString()));
            dto.setEndReleaseData(sdf.parse(excelRow.get(5).toString()));
            dto.setContent(excelRow.get(7).toString());
            dto.setRoleName(excelRow.get(8).toString());
            dto.setActivityEmployee(excelRow.get(9).toString());
            dto.setMoney(Double.parseDouble(excelRow.get(10).toString()));
            dto.setCreatedBy(requestCtx.getUserId());
            dto.setCreateData(new Date());
            dto.setObjectVersionNumber((long)1);
            dto.setLastUpdatedBy(requestCtx.getUserId());
            dto.setLastUpdateDate(new Date());
            actMapper.insertSelective(dto);
        }
        rd.setSuccess(true);
        rd.setMessage("Import successfully!");
        return rd;
    }

    @Override
    public List<ActivityStatement> queryStatement(int page, int pageSize, ActivityZzw dto) {
        PageHelper.startPage(page,pageSize);
        return actMapper.selectStatement();
    }

    private Object getCellValue(Cell cell) {
        Object value = null;
        DecimalFormat df = new DecimalFormat("0"); //格式化number String字符
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd"); //⽇日期格式化
        DecimalFormat df2 = new DecimalFormat("0.00"); //格式化数字
        short format = cell.getCellStyle().getDataFormat();
        if (cell != null) {
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_STRING:
                    value = cell.getRichStringCellValue().getString();
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    if ("General".equals(cell.getCellStyle().getDataFormatString())) {
                        value = df.format(cell.getNumericCellValue());
                    } else if ("m/d/yy h:mm".equals(cell.getCellStyle().getDataFormatString())||format == 14 || format == 31 || format == 57 || format == 58) {
                        value = sdf.format(cell.getDateCellValue());
                    } else {
                        value = df2.format(cell.getNumericCellValue());
                    }
                    break;
                case Cell.CELL_TYPE_BOOLEAN:
                    value = cell.getBooleanCellValue();
                    break;
                case Cell.CELL_TYPE_BLANK:
                    value = "";
                    break;
                default:
                    break;
            }
        } else {
            value = "";
        }
        return value;
    }
    private void createParams(ActivitiDto dto,ActivityZzw act){

        dto.setActivitiCode("activity");
        dto.setBusinessKey(act.getActivityId()+"");//
        List<RestVariable> variables = new ArrayList<>();

        RestVariable actCode = new RestVariable();
        actCode.setName("actCode");
        actCode.setType("string");
        actCode.setValue("activity");

       /* RestVariable businessKey = new RestVariable();
        businessKey.setName("businessKey");
        businessKey.setType("string");
        businessKey.setValue(act.getActivityId()+"");*/

        RestVariable actRole = new RestVariable();
        if(act.getMoney()<=10000){
            actRole.setName("actRole");
            actRole.setType("string");
            actRole.setValue("组长");
            variables.add(actRole);
        }else{
            actRole.setName("actRole");
            actRole.setType("string");
            actRole.setValue("经理");
            variables.add(actRole);
        }

        /*
        RestVariable gender = new RestVariable();
        gender.setName("gender");
        gender.setType("string");
        gender.setValue("F");*/

        RestVariable amount = new RestVariable();
        amount.setName("amount");
        amount.setType("double");
        amount.setValue(act.getMoney());

        variables.add(actCode);
       // variables.add(businessKey);

       // variables.add(gender);
        variables.add(amount);
        dto.setVariable(variables);

    }
}