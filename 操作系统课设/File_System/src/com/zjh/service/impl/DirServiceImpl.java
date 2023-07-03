package com.zjh.service.impl;

import com.zjh.constant.Constants;
import com.zjh.pojo.*;
import com.zjh.service.DirService;
import com.zjh.service.FileService;
import com.zjh.utils.Utility;
import com.zjh.view.View;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author �솴
 * @description: Ŀ¼����ʵ����
 */
public class DirServiceImpl implements DirService {
    private static final DirService dirService = new DirServiceImpl();
    private static final FileService fileService = new FileServiceImpl();
    public static void main(String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.insert(0,'a');
        sb.insert(0,'b');
        sb.insert(0,'c');
        System.out.println(sb.toString());
    }
    /**��ʾ��ǰĿ¼�µ������ļ��
     * ��ɫ ��ͨ�ļ�
     * ��ɫ ��ִ���ļ�
     * ��ɫ ѹ���ļ�
     * ��ɫ Ŀ¼�ļ�
     * **/
    @Override
    public void dir() {
        Memory memory = Memory.getInstance();
        List<FCB> children = memory.getCurDir().getChildren();
        View view = new View();
        System.out.println("Ŀ¼Ȩ��\t�ļ�����\t������\t�ļ���С\t\t �޸�ʱ��\t\t\t\t\t\t�ļ���");
        for (int i = 0; i < children.size(); i++) {
            FCB fcb = children.get(i);
            if(fcb.getType().equals('N')){  //fcb�д������ͨ�ļ�
                if(Utility.getSuffix(fcb.getFileName()).equals("exe")){
                    //��ɫ
                    view.showFcb(fcb,36);
                }else if(Utility.getSuffix(fcb.getFileName()).equals("tar") || Utility.getSuffix(fcb.getFileName()).equals("zip") || Utility.getSuffix(fcb.getFileName()).equals("zip") || Utility.getSuffix(fcb.getFileName()).equals("rar")){
                    view.showFcb(fcb,31);
                }else {
                    //��ͨ�ļ�
                    view.showFcb(fcb,-1);
                }
            }else {
                //��ɫ
                view.showFcb(fcb,34);
            }

        }
    }

    /**����Ŀ¼**/
    @Override
    public Boolean mkdir(String dirName,String permission) {
        FCB curDir = Memory.getInstance().getCurDir();
        User user = Memory.getInstance().getCurUser();
        List<FCB> children = curDir.getChildren();
        //�п�
        if(Objects.isNull(dirName)){
            System.out.println("[error]: Ŀ¼������Ϊ��");
            return false;
        }
        //�ж��ظ�
        dirName = dirName.trim(); //ȥ����β�ո�
        for (FCB child : children) {
            if(child.getFileName().equals(dirName)){
                System.out.println("[error]: Ŀ¼���ظ� ����������");
                return false;
            }
        }
        //���������ڵ� ����FCB �ļ���СΪ0 ���ļ�
        IndexNode indexNode = new IndexNode(permission, 0, -1, 0, user.getUserName(), new Date());
        FCB fcb = new FCB(dirName, 'D', indexNode, curDir, new LinkedList<>());
        //���ļ����ƿ������̵�fcb����
        Disk.getINSTANCE().getFcbList().add(fcb);
        //�޸ĸ�Ŀ¼���ļ��� ���븸Ŀ¼���Ӽ���
        curDir.getIndexNode().addFcbNum();
        curDir.getChildren().add(fcb);
        System.out.println("[success]: ����Ŀ¼�ɹ���");
        return true;
    }

    /**�л�Ŀ¼**/
    @Override
    public Boolean cd(String path) {
        //�ж��Ƿ�ֱ���л�����Ŀ¼
        if("/".equals(path.trim())){
            Memory.getInstance().setCurDir(Memory.getInstance().getRootDir());
            return true;
        }
        //�ж��ǲ���..  ../
        if("..".equals(path.trim()) || "../".equals(path.trim())){
            FCB curDir = Memory.getInstance().getCurDir();
            //�ж��ǲ����Ѿ��ڸ�Ŀ¼
            if(curDir != Memory.getInstance().getRootDir()){
                //�ı䵱ǰĿ¼Ϊ��Ŀ¼
                Memory.getInstance().setCurDir(curDir.getFather());
            }
            return true;

        }
        //����·��
        FCB fcb = dirService.pathResolve(path);
        //null ������
        if(Objects.isNull(fcb)){
            System.out.println("[error]: Ŀ��Ŀ¼������");
            return false;
        }else if(fcb.getType().equals('N')){
            //type N ����Ŀ¼�ļ�
            System.out.println("[error]: �޷�������ͨ�ļ�");
            return false;
        }else {
            //type D �л�����ӦĿ¼
            //�ж�Ȩ��
            int permission = fileService.checkPermission(fcb);
            if(permission == 0){
                System.out.println("[error]: ��Ȩ��");
                return false;
            }
            Memory.getInstance().setCurDir(fcb);//���õ�ǰĿ¼Ϊ��Ҫ��ת�����ļ���fcb��Ϊǰ����Ҫ��ת���ļ�
        }
        return null;
    }

    /**����·��**/
    @Override
    public FCB pathResolve(String path) {
        path = path.trim();
        FCB curDir = Memory.getInstance().getCurDir();
        FCB rootDir = Memory.getInstance().getRootDir();
        //�ж��ǲ���/��ͷ ���Ǿ��ǵ�ǰĿ¼��
        if(!path.startsWith("/")){
            for (FCB child : curDir.getChildren()) {
                if(child.getFileName().equals(path)){
                    return child;
                }
            }
        }else {
            //��/��ͷ �Ӹ�Ŀ¼���������
            path = path.substring(1);
            String[] splitDir = path.split("/");
            FCB temp = rootDir;
            for (int i = 0; i < splitDir.length - 1; i++) {
                //�ҵ�Ŀ���ļ�����Ŀ¼
                for (FCB child : temp.getChildren()) {
                    if(child.getFileName().equals(splitDir[i])){
                        temp = child;
                        continue;
                    }
                }
            }
            //�ڸ�Ŀ¼����
            for (FCB child : temp.getChildren()) {
                if(child.getFileName().equals(splitDir[splitDir.length - 1])){
                    return child;
                }
            }
        }
        return null;
    }

    /**����Ŀ¼��С**/
    @Override
    public void updateSize(FCB fcb, Boolean isAdd, int new_add) {
        FCB temp = fcb.getFather();
        while (temp != Memory.getInstance().getRootDir()){
            //�ݹ��޸ĸ�Ŀ¼�Ĵ�С
            int size = temp.getIndexNode().getSize();
            if(isAdd){
                if(new_add == -1){
                    //����Ŀ¼��С
                    temp.getIndexNode().setSize(size + fcb.getIndexNode().getSize());
                }else {
                    temp.getIndexNode().setSize(size + new_add);
                }
            }else {
                temp.getIndexNode().setSize(size - fcb.getIndexNode().getSize());
            }
            temp = temp.getFather();
        }
    }

    /**��ʾ��ǰĿ¼�µ������ļ��ļ���**/
    @Override
    public void ls() {
        Memory memory = Memory.getInstance();
        List<FCB> children = memory.getCurDir().getChildren();
        for (int i = 0; i < children.size(); i++) {
            FCB fcb = children.get(i);
            if(fcb.getType().equals('N')){
                if(Utility.getSuffix(fcb.getFileName()).equals("exe")){
                    System.out.print(Utility.getFormatLogString(fcb.getFileName(),36,0) + " ");
                }else if(Utility.getSuffix(fcb.getFileName()).equals("tar") || Utility.getSuffix(fcb.getFileName()).equals("zip") || Utility.getSuffix(fcb.getFileName()).equals("zip") || Utility.getSuffix(fcb.getFileName()).equals("rar")){
                    System.out.print(Utility.getFormatLogString(fcb.getFileName(),31,0) + " ");
                }else {
                    System.out.print(fcb.getFileName() + " ");
                }
            }else {
                System.out.print(Utility.getFormatLogString(fcb.getFileName(),34,0) + " ");
            }
        }
    }

    /**��ʾȫ·��**/
    @Override
    public String pwd(FCB fcb) {
        Memory memory = Memory.getInstance();
        StringBuilder sb = new StringBuilder();
        FCB temp = fcb;
        while (temp != memory.getRootDir()){
            //��û��ӡ����Ŀ¼
            sb.insert(0,temp.getFileName());
            sb.insert(0,'/');
            temp = temp.getFather();
        }
        return sb.toString();
    }

    /**��������ʱ��ʾ��ǰĿ¼**/
    @Override
    public void showPath() {
        Memory memory = Memory.getInstance();
        StringBuilder sb = new StringBuilder();
        sb.append("\n[");
        sb.append(memory.getCurUser().getUserName() + "@");
        sb.append(" ");
        sb.append(memory.getCurDir().equals(memory.getRootDir()) ? "/" : memory.getCurDir().getFileName());
        sb.append("]");
        System.out.print(sb);
    }

    /**��ʾλʾͼ**/
    @Override
    public void bitmap() {
        FAT[] fats = Memory.getInstance().getFat();
        for (int i = 0; i < fats.length; i++) {
            System.out.print(fats[i].getBitmap() + " ");
            if((i+1) % Constants.WORD_SIZE == 0){
                System.out.println();
            }
        }
    }
}
