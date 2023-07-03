package com.zjh.service.impl;

import com.zjh.constant.Constants;
import com.zjh.pojo.FCB;
import com.zjh.pojo.IndexNode;
import com.zjh.pojo.User;
import com.zjh.pojo.Disk;
import com.zjh.pojo.Memory;
import com.zjh.service.DataService;
import com.zjh.service.UserService;

import java.util.*;

/**
 * @author �솴
 * @description: �û�����ʵ����
 */
public class UserServiceImpl implements UserService {
    Memory instance = Memory.getInstance();
    @Override
    public Boolean login(String userName, String password) {
        //�жϵ�ǰ�Ƿ��¼
        if(Objects.nonNull(instance.getCurUser())){
            System.out.println("[error]:�����˳���¼");
            return false;
        }
        //�û������������
        User user = instance.getUserMap().get(userName);
        if(Objects.isNull(user) || Objects.nonNull(user) && !user.getPassword().equals(password)){
            System.out.println("[error]:�û������������");
            return false;
        }
        //����memory��
         //�ҵ��û�Ŀ¼��FCB
        List<FCB> fcbList = Disk.getINSTANCE().getFcbList();
        for (int i = 0; i < fcbList.size(); i++) {
            if(fcbList.get(i).getFileName().equals(userName)){
                //�û�Ŀ¼FCB
                instance.setCurUser(user);
                instance.setCurDir(fcbList.get(i));
            }
        }
        System.out.println("[success]��¼�ɹ��� ��һ�ε�¼ʱ�䣺"+user.getLastLoginTime());
        user.setLastLoginTime(new Date());
        return true;
    }

    @Override
    public Boolean register(String userName, String password) {
        //�ж��Ƿ��¼
        if (Objects.nonNull(instance.getCurUser())) {
            System.out.println("[error]:�����˳���¼");
            return false;
        }
        Map<String, User> userMap = instance.getUserMap();
        //�ж��Ƿ��ظ�
        if(Objects.nonNull(userMap.get(userName))){
            System.out.println("[error]:�û����ظ�");
            return false;
        }
        //�Ž��û�����
        userMap.put(userName,new User(userName,password,null));
        //�½�һ���û�FCB��������� �����û���Ȩ��
        FCB rootDir = instance.getRootDir();
        IndexNode indexNode = new IndexNode("rwx---",0,-1,0,userName,new Date());
        FCB user_fcb = new FCB(userName,'D',indexNode, rootDir,new LinkedList<>());
        //�Ž�fcb����
        Disk.getINSTANCE().getFcbList().add(user_fcb);
        //�޸ĸ�Ŀ¼
        rootDir.getChildren().add(user_fcb);
        rootDir.getIndexNode().addFcbNum();
        System.out.println("[success]��ע��ɹ�");
        return true;
    }

    @Override
    public Boolean logout() {
        //�ж�
        if(Objects.isNull(instance.getCurUser())){
            System.out.println("[error]:���ȵ�¼");
            return false;
        }
        //�ж��Ƿ����ļ�δ�ر�
        if(Memory.getInstance().getOpenFileList().size() > 0){
            System.out.println("[error] ���ļ�δ�ر� ���ȹر�");
            return false;
        }
        instance.setCurUser(null);
        instance.setCurDir(instance.getRootDir());
        instance.getOpenFileList().clear();
        DataService dataService = new DataServiceImpl();
        dataService.saveData(Constants.SAVE_PATH);
        return true;
    }
}
