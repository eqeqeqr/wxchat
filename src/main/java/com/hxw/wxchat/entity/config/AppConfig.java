package com.hxw.wxchat.entity.config;

import com.hxw.wxchat.utils.StringTools;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppConfig {
    @Value("${ws.port}")
    private Integer wsPort;

    @Value("${project.folder}")
    private String projectFolder;

    @Value("${admin.emails}")
    private String adminEmails;


    public Integer getWsPort() {return wsPort;}

    public void setWsPort(Integer wsPort) {
        this.wsPort = wsPort;
    }

    public String getProjectFolder() {
        if (StringTools.isEmpty(projectFolder)&&!projectFolder.endsWith("/")){
            projectFolder=projectFolder+"/";
        }
        return projectFolder;
    }

    public void setProjectFolder(String projectFolder) {
        this.projectFolder = projectFolder;
    }

    public String getAdminEmails() {
        return adminEmails;
    }

    public void setAdminEmails(String adminEmails) {
        this.adminEmails = adminEmails;
    }
}
