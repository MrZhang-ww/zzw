package com.hand.activity.controllers;

import com.hand.activity.dto.ActivityDto;
import com.hand.hap.attachment.dto.SysFile;
import com.hand.hap.attachment.exception.AttachmentException;
import com.hand.hap.security.TokenUtils;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import com.hand.hap.system.controllers.BaseController;
import com.hand.hap.core.IRequest;
import com.hand.hap.system.dto.ResponseData;
import com.hand.activity.dto.ActivityZzw;
import com.hand.activity.dto.ActivityFile;
import com.hand.activity.service.IActivityZzwService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Controller
    public class ActivityZzwController extends BaseController{
    private static final Integer BUFFER_SIZE = 1024;

    @Autowired
    private IActivityZzwService service;


    @RequestMapping(value = "/rent/activity/zzw/query")
    @ResponseBody
    public ResponseData query(ActivityZzw dto, @RequestParam(defaultValue = DEFAULT_PAGE) int page,
        @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize, HttpServletRequest request) {
        IRequest requestContext = createRequestContext(request);
        return new ResponseData(service.selectAllAct(page,pageSize,dto));
    }

    @RequestMapping(value = "/rent/activity/zzw/submit")
    @ResponseBody
    public ResponseData update(@RequestBody List<ActivityZzw> dto, BindingResult result, HttpServletRequest request){
        getValidator().validate(dto, result);
        if (result.hasErrors()) {
        ResponseData responseData = new ResponseData(false);
        responseData.setMessage(getErrorMessage(result, request));
        return responseData;
        }
        IRequest requestCtx = createRequestContext(request);
        return new ResponseData(service.batchUpdate(requestCtx, dto));
    }

    @RequestMapping(value = "/rent/activity/zzw/remove")
    @ResponseBody
    public ResponseData delete(HttpServletRequest request,@RequestBody List<ActivityZzw> dto){
        service.batchDelete(dto);
        return new ResponseData();
    }
    @InitBinder
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        simpleDateFormat.setLenient(false);
        CustomDateEditor dateEditor = new CustomDateEditor(simpleDateFormat, true);
        binder.registerCustomEditor(Date.class, dateEditor);
    }
        @RequestMapping(value = "/rent/activity/zzw/submitAndAct")
        @ResponseBody
        public ResponseData submitAndAct(@RequestBody List<ActivityZzw> list, HttpServletRequest request) {
            IRequest requestContext = createRequestContext(request);
            service.submitAct(list.get(0),requestContext);
            return new ResponseData();
        }
        @RequestMapping(value = "/rent/activity/zzw/queryEnd")
        @ResponseBody
        public ResponseData queryEnd(ActivityZzw dto, @RequestParam(defaultValue = DEFAULT_PAGE) int page,
                                  @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize, HttpServletRequest request) {
            IRequest requestContext = createRequestContext(request);
            return new ResponseData(service.selectActEnd(page,pageSize,dto));
        }
        @RequestMapping(value = "/rent/activity/zzw/exportExcel")
        @ResponseBody
        public ResponseData exportExcel(@RequestBody List<ActivityDto> list, HttpServletRequest request) {
            IRequest requestContext = createRequestContext(request);
            return new ResponseData(service.exportExcel(list));
        }
        @RequestMapping(value = "/rent/activity/zzw/queryFile")
        @ResponseBody
        public ResponseData queryFile(@RequestParam int activityId, @RequestParam(defaultValue = DEFAULT_PAGE) int page,
                                  @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize, HttpServletRequest request) {
            IRequest requestContext = createRequestContext(request);
            return new ResponseData(service.selectFileById(page,pageSize,activityId));
        }
        @RequestMapping(value="/rent/activity/zzw/download")
        public void detail(HttpServletRequest request, HttpServletResponse response, String fileId, String token) throws Exception {
            IRequest requestContext = this.createRequestContext(request);
            ActivityFile actFile = this.service.selectByPrimaryKey(requestContext, Long.valueOf(fileId));
            actFile.set_token(token);
            TokenUtils.checkToken(request.getSession(false), actFile);
            File file = new File(actFile.getFilePath());
            if (file.exists()) {
                response.addHeader("Content-Disposition", "attachment;filename=\"" + URLEncoder.encode(actFile.getFileName(), "UTF-8") + "\"");
                response.setContentType(actFile.getFileType() + ";charset=" + "UTF-8");
                response.setHeader("Accept-Ranges", "bytes");
                int fileLength = (int)file.length();
                response.setContentLength(fileLength);
                if (fileLength > 0) {
                    this.writeFileToResp(response, file);
                }

            } else {
                throw new AttachmentException("msg.warning.download.file.error", "msg.warning.download.file.error", new Object[0]);
            }
        }
        //查询活动报表
        @RequestMapping(value = "/rent/activity/zzw/queryStatement")
        @ResponseBody
        public ResponseData queryStatement(ActivityZzw dto, @RequestParam(defaultValue = DEFAULT_PAGE) int page,
                                  @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize, HttpServletRequest request) {
            IRequest requestContext = createRequestContext(request);
            return new ResponseData(service.queryStatement(page,pageSize,dto));
        }
        /***自定义导入*****/
        @RequestMapping(
                value = {"/wht/ora/20796/org/access/export/excel/import/custom"},
                method = {RequestMethod.POST}
        )
        public ResponseData uploadExcel(HttpServletRequest request, Locale locale, String
                contextPath) throws Exception {
            ResponseData rd = new ResponseData();
            IRequest requestCtx = createRequestContext(request);
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)
                    request;
            MultipartFile file = multipartRequest.getFile("upfile");
            if (file == null || file.isEmpty()) {
                rd.setSuccess(false);
                rd.setMessage("File is empty!");
                return rd;
            }
            InputStream in = file.getInputStream();
            //importExcel()方法在下面的IOra20796OrgAccessService定义
            return service.importExcel(in, file.getOriginalFilename(),requestCtx);
        }

        private void writeFileToResp(HttpServletResponse response, File file) throws Exception {
            byte[] buf = new byte[BUFFER_SIZE];
            InputStream inStream = new FileInputStream(file);
            Throwable var5 = null;

            try {
                ServletOutputStream outputStream = response.getOutputStream();
                Throwable var7 = null;

                try {
                    int readLength;
                    while((readLength = inStream.read(buf)) != -1) {
                        outputStream.write(buf, 0, readLength);
                    }

                    outputStream.flush();
                } catch (Throwable var30) {
                    var7 = var30;
                    throw var30;
                } finally {
                    if (outputStream != null) {
                        if (var7 != null) {
                            try {
                                outputStream.close();
                            } catch (Throwable var29) {
                                var7.addSuppressed(var29);
                            }
                        } else {
                            outputStream.close();
                        }
                    }

                }
            } catch (Throwable var32) {
                var5 = var32;
                throw var32;
            } finally {
                if (inStream != null) {
                    if (var5 != null) {
                        try {
                            inStream.close();
                        } catch (Throwable var28) {
                            var5.addSuppressed(var28);
                        }
                    } else {
                        inStream.close();
                    }
                }

            }

        }
    }