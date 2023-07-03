package com.zjh.service.impl;

import com.zjh.constant.Constants;
import com.zjh.pojo.*;
import com.zjh.service.DirService;
import com.zjh.service.DiskService;
import com.zjh.service.FileService;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/**
 * @author �솴
 * @description: �ļ�����ʵ����
 */
public class FileServiceImpl implements FileService {
    private static final DirService dirService = new DirServiceImpl();
    private static final FileService fileService = new FileServiceImpl();
    private static final DiskService diskService = new DiskServiceImpl();
    private static final Scanner scanner = new Scanner(System.in);

    @Override
    /*�����ļ�**/
    public Boolean create(String fileName, String permission) {
        FCB curDir = Memory.getInstance().getCurDir();
        User user = Memory.getInstance().getCurUser();
        List<FCB> children = curDir.getChildren();
        //�п�
        if (Objects.isNull(fileName)) {
            System.out.println("[error]: �ļ�������Ϊ��");
            return false;
        }
        //�ж��ظ�
        fileName = fileName.trim(); //ȥ����β�ո�
        for (FCB child : children) {
            if (child.getFileName().equals(fileName)) {
                System.out.println("[error]: �ļ����ظ� ����������");
                return false;
            }
        }
        //���������ڵ� ����FCB �ļ���СΪ0 ���ļ�
        IndexNode indexNode = new IndexNode(permission, 0, -1, 0, user.getUserName(), new Date());
        FCB fcb = new FCB(fileName, 'N', indexNode, curDir, null);
        //���ļ����ƿ������̵�fcb����
        Disk.getINSTANCE().getFcbList().add(fcb);
        //�޸ĸ�Ŀ¼���ļ��� ���븸Ŀ¼���Ӽ���
        curDir.getIndexNode().addFcbNum();
        curDir.getChildren().add(fcb);
        System.out.println("[success]: �����ļ��ɹ���");
        return true;
    }

    @Override
    /**���ļ�**/
    public Boolean open(String filePath) {
        //ʹ��pathResolve����,�鿴�Ƿ���ڸ��ļ���Ŀ¼
        FCB fcb = dirService.pathResolve(filePath);
        //null ������
        if (Objects.isNull(fcb)) {
            System.out.println("[error]: Ŀ���ļ�������");
            return false;
        } else if (fcb.getType().equals('D')) {  //type D ������ͨ�ļ�
            System.out.println("[error]: �޷���Ŀ¼�ļ�");
            return false;
        } else {
            //type N ��ͨ�ļ�
            //�ж�Ȩ��
            int permission = fileService.checkPermission(fcb);
            if (permission == 0) {
                System.out.println("[error]: ��Ȩ��");
                return false;
            }
            //�ж��Ƿ��Ѿ���
            //�ж��Ƿ���openFileList��
            String fill_path = dirService.pwd(fcb);
            List<OpenFile> openFileList = Memory.getInstance().getOpenFileList();
            OpenFile toWriteFile = null;
            for (OpenFile openFile : openFileList) {
                if (openFile.getFilePath().equals(fill_path)) {
                    toWriteFile = openFile;
                }
            }
            if (Objects.nonNull(toWriteFile)) {
                System.out.println("[error]: �ļ��Ѵ򿪣�");
                return false;
            }
            //����openFileList��
            OpenFile openFile = new OpenFile(fcb, fill_path);
            Memory.getInstance().getOpenFileList().add(openFile);
            System.out.println("[success]: �򿪳ɹ���");
            return true;
        }
    }

    @Override
    /**��ʾ���ļ�**/
    public void show_open() {
        if (Memory.getInstance().getOpenFileList().size() == 0) {
            System.out.println("<�޴��ļ�>");
        }
        for (int i = 0; i < Memory.getInstance().getOpenFileList().size(); i++) {
            System.out.print(Memory.getInstance().getOpenFileList().get(i).getFcb().getFileName() + "\t");
        }
    }


    @Override
    public Boolean read(String filePath) {
        //�ж��Ƿ����
        FCB fcb = dirService.pathResolve(filePath);
        if (Objects.isNull(fcb)) {
            System.out.println("[error]: Ŀ���ļ�������");
            return false;
        } else if (fcb.getType().equals('D')) {
            //type D ������ͨ�ļ�
            System.out.println("[error]: �޷�дĿ¼�ļ�");
            return false;
        } else {
            //type N ��ͨ�ļ�
            //�ж��ļ�Ȩ��
            int permission = fileService.checkPermission(fcb);
            if (permission == 0) {
                System.out.println("[error]: ��Ȩ��");
                return false;
            }
            //�ж��Ƿ���openFileList��
            String fill_path = dirService.pwd(fcb);
            List<OpenFile> openFileList = Memory.getInstance().getOpenFileList();
            OpenFile toWriteFile = null;
            for (OpenFile openFile : openFileList) {
                if (openFile.getFilePath().equals(fill_path)) {
                    toWriteFile = openFile;
                }
            }
            if (Objects.nonNull(toWriteFile)) {
                FAT[] fats = Memory.getInstance().getFat();
                Block[] disk = Disk.getINSTANCE().getDisk();
                //�Ӵ��̶�ȡ
                System.out.println("--------BEGIN--------");
                if (fcb.getIndexNode().getSize() == 0) {
                    System.out.println("<!!!EMPTY FILE!!!>");
                    System.out.println("---------END---------");
                    return false;
                }
                FAT temp = fats[fcb.getIndexNode().getFirst_block()];
                while (temp.getNextId() != -1) {
                    //�������
                    System.out.print(disk[temp.getId()].getContent());
                    temp = fats[temp.getNextId()];
                }
                System.out.print(disk[temp.getId()].getContent());
                System.out.println();
                System.out.println("---------END---------");
            } else {
                System.out.println("[error]: �ļ�δ�� ���ȴ򿪣�");
                return false;
            }
        }
        return true;
    }

    @Override
    /**д���ļ�**/
    public Boolean write(String filePath) {
        //�ж��Ƿ����
        FCB fcb = dirService.pathResolve(filePath);
        if (Objects.isNull(fcb)) {
            System.out.println("[error]: Ŀ���ļ�������");
            return false;
        } else if (fcb.getType().equals('D')) {
            //type D ������ͨ�ļ�
            System.out.println("[error]: �޷�дĿ¼�ļ�");
            return false;
        } else {
            //type N ��ͨ�ļ�
            //�ж��ļ�Ȩ��
            int permission = fileService.checkPermission(fcb);
            if (permission == 0) {
                System.out.println("[error]: ��Ȩ��");
                return false;
            } else if (permission == 4) {
                System.out.println("[error]: ���ļ���ֻ���ļ�");
                return false;
            } else {
                //��д
                //�ж��Ƿ���openFileList��
                String fill_path = dirService.pwd(fcb);
                List<OpenFile> openFileList = Memory.getInstance().getOpenFileList();
                OpenFile toWriteFile = null;
                for (OpenFile openFile : openFileList) {
                    if (openFile.getFilePath().equals(fill_path)) {
                        toWriteFile = openFile;
                    }
                }
                if (Objects.nonNull(toWriteFile)) {
                    StringBuilder content = new StringBuilder();
                    System.out.println("������Ҫд������ݣ���$$��β��:");
                    //��ȡ�û����� ����$$����
                    while (true) {
                        String nextLine = scanner.nextLine();
                        if (nextLine.endsWith("$$")) {
                            content.append(nextLine, 0, nextLine.length() - 2);
                            break;
                        } else {
                            content.append(nextLine);
                            content.append("\n");
                        }
                    }
                    String choice = null;
                    if (fcb.getIndexNode().getSize() == 0) {
                        //���ļ� Ĭ�ϸ���
                        choice = "1";
                    } else {
                        //������ ���û�ѡ��д��ģʽ
                        while (true) {
                            System.out.println("ԭ�ļ������� ��ѡ�񸲸�д��1��/ ׷��д��2��:");
                            choice = scanner.nextLine();
                            if (choice.equals("1") || choice.equals("2")) {
                                break;
                            }
                        }
                    }
                    FAT[] fats = Memory.getInstance().getFat();
                    int size = content.toString().toCharArray().length;
                    if (choice.equals("1")) {
                        //����д��
                        //1.������ǿ��ļ� �����֮ǰռ�ݵ��̿�
                        if (fcb.getIndexNode().getSize() != 0) {
                            diskService.freeFile(fcb);
                            fcb.getFather().getIndexNode().subFcbNum();
                        }
                        //2.����д��
                        int first = diskService.writeToDisk(content.toString());
                        //3.���ļ�ָ���һ��
                        fcb.getIndexNode().setFirst_block(first);
                        //4.�޸���������С
                        fcb.getIndexNode().setSize(size);
                        //�޸ĸ�Ŀ¼�� �Լ�һֱ�ݹ��޸ĸ�Ŀ¼�Ĵ�С
                        dirService.updateSize(fcb, true, -1);
                    } else {
                        //׷��д��
                        //1.�ӵ�һ��������  ֱ��-1�Ŀ�Ŀ��
                        FAT temp = fats[fcb.getIndexNode().getFirst_block()];
                        while (temp.getNextId() != -1) {
                            temp = fats[temp.getNextId()];
                        }
                        //2.д��Ҫ׷�ӵ�����
                        content.insert(0, '\n');
                        int append_begin = diskService.writeToDisk(content.toString());
                        //3.�޸����һ��ָ���µ�����
                        temp.setNextId(append_begin);
                        //4.�޸���������С ����ԭ����
                        int size_origin = fcb.getIndexNode().getSize();
                        fcb.getIndexNode().setSize(size + size_origin);
                        //�޸ĸ�Ŀ¼�� �Լ�һֱ�ݹ��޸ĸ�Ŀ¼�Ĵ�С
                        dirService.updateSize(fcb, true, size);
                    }
                    System.out.println("[success]: д��ɹ���");
                    return true;
                } else {
                    System.out.println("[error]: �ļ�δ�� ���ȴ򿪣�");
                    return false;
                }
            }
        }
    }

    @Override
    /**�ر��ļ�**/
    public Boolean close(String filePath) {
        //�ж��Ƿ����
        FCB fcb = dirService.pathResolve(filePath);
        if (Objects.isNull(fcb)) {
            System.out.println("[error]: Ŀ���ļ�������");
            return false;
        } else if (fcb.getType().equals('D')) {
            //type D ������ͨ�ļ�
            System.out.println("[error]: �޷��ر�Ŀ¼�ļ�");
            return false;
        } else {
            //type N ��ͨ�ļ�
            //�ж��Ƿ���openFileList��
            String fill_path = dirService.pwd(fcb);
            List<OpenFile> openFileList = Memory.getInstance().getOpenFileList();
            for (OpenFile openFile : openFileList) {
                if (openFile.getFilePath().equals(fill_path)) {
                    //�޸�fcb��updateTime
                    fcb.getIndexNode().setUpdateTime(new Date());
                    //��openFileList���Ƴ�
                    openFileList.remove(openFile);
                    System.out.println("[success]: �رճɹ���");
                    return true;

                }
            }
            System.out.println("[error]: �ļ�δ�� ����ر�");
            return false;
        }
    }

    @Override
    /**ɾ���ļ�**/
    public Boolean delete(String filePath) {
        //�ж��Ƿ����
        FCB fcb = dirService.pathResolve(filePath);
        if (Objects.isNull(fcb)) {
            System.out.println("[error]: Ŀ���ļ�������");
            return false;
        }
        //�ж�Ȩ�� ��Ҫ���ļ��о���rwxȨ�� ���ļ�����rwȨ��
        int per_father = fileService.checkPermission(fcb.getFather());
        int permission = fileService.checkPermission(fcb);
        if (!(per_father == 7 && (permission == 7 || permission == 6))) {
            System.out.println("[error]: ��Ȩ��");
            return false;
        }
        //�ж��Ƿ�� ��Ҫ�ȹر�
        //�ж��Ƿ���openFileList��
        String fill_path = dirService.pwd(fcb);
        List<OpenFile> openFileList = Memory.getInstance().getOpenFileList();
        OpenFile toWriteFile = null;
        for (OpenFile openFile : openFileList) {
            if (openFile.getFilePath().equals(fill_path)) {
                toWriteFile = openFile;
            }
        }
        if (Objects.nonNull(toWriteFile)) {
            System.out.println("[error]: �ļ����� ���ȹر�");
            return false;
        }
        //�ظ�ȷ��
        String choice = null;
        while (true) {
            System.out.println("ȷ��ɾ�����ļ�����Y/N��");
            choice = scanner.nextLine();
            if (choice.equals("Y")) break;
            if (choice.equals("N")) {
                System.out.println("[success]: ��ȡ��ɾ����");
                return false;
            }
        }
        //���ļ��ж�
        if (fcb.getIndexNode().getSize() != 0 || fcb.getIndexNode().getFcbNum() != 0) {
            if (fcb.getType().equals('D')) {  //type D Ŀ¼
                //todo ����ջɾ��Ŀ¼
//                diskService.freeDir(fcb);
                System.out.println("[error]: �ļ��зǿ� �޷�ɾ��");
                return false;
            } else {  //��ͨ�ļ� N
                //��մ���
                diskService.freeFile(fcb);
            }
        }
        //����ǿ�Ŀ¼ �������ǵ�ǰĿ¼
        if (fcb == Memory.getInstance().getCurDir()) {
            System.out.println("[error]: �޷�ɾ����ǰĿ¼ �����˳���ǰĿ¼��");
        }
        //��FCB������ȥ�� �޸ĸ�Ŀ¼�ļ��� �޸ĸ�Ŀ¼���ӽ��
        Disk.getINSTANCE().getFcbList().remove(fcb);
        fcb.getFather().getIndexNode().subFcbNum();
        fcb.getFather().getChildren().remove(fcb);
        //�ݹ��޸ĸ�Ŀ¼�ļ���С
        dirService.updateSize(fcb, false, -1);
        System.out.println("[success]: ɾ���ɹ�");
        return true;
    }

    @Override
    /**
     * ���ҵ�ǰ�û��Ը��ļ���Ȩ��  0��ʾ��Ȩ�� r=4,w=2,x=1 rwx=7 rw-=6 r--=4
     */
    public int checkPermission(FCB fcb) {
        int permission = 0;
        //�鿴�Ƿ��Ǵ�����
        String per = null;
        if (Memory.getInstance().getCurUser().getUserName().equals(fcb.getIndexNode().getCreator())) {
            //ǰ��λ
            per = fcb.getIndexNode().getPermission().substring(0, 3);
        } else {
            //����λ
            per = fcb.getIndexNode().getPermission().substring(3);
        }
        char[] chars = per.toCharArray();
        for (char c : chars) {
            if (c == 'r') {
                permission += Constants.READ;
            } else if (c == 'w') {
                permission += Constants.WRITE;
            } else if (c == 'x') {
                permission += Constants.EXECUTION;
            }
        }
        return permission;
    }

    @Override
    /**�ļ�������**/
    public Boolean rename(String filePath, String newName) {
        //�ж��Ƿ����
        FCB fcb = dirService.pathResolve(filePath);
        if (Objects.isNull(fcb)) {
            System.out.println("[error]: Ŀ���ļ�������");
            return false;
        }
        //�ж��ļ�Ȩ��
        int permission = fileService.checkPermission(fcb);
        if (permission == 0 || permission == 4) {
            System.out.println("[error]: ��Ȩ��");
            return false;
        }
        //�������ͨ�ļ� �ж��Ƿ��
        if (fcb.getType().equals('N')) {
            String fill_path = dirService.pwd(fcb);
            List<OpenFile> openFileList = Memory.getInstance().getOpenFileList();
            OpenFile toWriteFile = null;
            for (OpenFile openFile : openFileList) {
                if (openFile.getFilePath().equals(fill_path)) {
                    toWriteFile = openFile;
                }
            }
            if (Objects.nonNull(toWriteFile)) {
                System.out.println("[error]: �ļ����� ���ȹرգ�");
                return false;
            }
        }
        //�ж��Ƿ�����
        //�ж��ظ�
        newName = newName.trim(); //ȥ����β�ո�
        List<FCB> children = Memory.getInstance().getCurDir().getChildren();
        for (FCB child : children) {
            if (child.getFileName().equals(newName)) {
                System.out.println("[error]: �ļ����ظ� ����������");
                return false;
            }
        }
        //����������
        fcb.setFileName(newName);
        System.out.println("[success]: �޸ĳɹ���");
        return true;
    }
}
