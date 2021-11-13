package experiment1;

import jdk.nashorn.internal.runtime.regexp.joni.constants.EncloseType;
import sun.plugin.javascript.navig.LinkArray;

import javax.sound.sampled.Line;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Target;
import java.time.chrono.ChronoLocalDate;
import java.util.*;

/**
 * @description: 从磁盘读入给定数据，针对用户给出的不同的最小支持度， 用FP-growth算法找到频繁项集， 并保存到本地磁盘。
 * @author: hanxiao
 * @date: 2021/10/23
 **/

public class FPGrowth {
    static ArrayList<ArrayList<Integer>> tidList = new ArrayList<>();//不写new会怎么样呢？马上用treeSet来试试看
    static ArrayList<TableNode> headTable = new ArrayList<>();//项头表
    static HashMap<Integer, Integer> itemIdMap = new HashMap<>();//商品ID集合
    static Scanner scanner = new Scanner(System.in);//处理用户输入
    static int minSupport;
    static HashSet<Integer> validItemIdSet = new HashSet<>();

    //    static HashMap<Integer, Node> preSameIdNode = new HashMap<>();
    public static void main(String[] args) throws IOException {
        getDataByDisk("data1.txt");
        System.out.println("请输入最小相对支持度（输入 -1 代表结束程序）:");
        String minRelativeSupport = "0.1";
//        Integer integer = new Integer(5);
//        int i=5;
//        Integer integer1 = new Integer(5);
//        System.out.println(integer.equals(i));
//        while (!(minRelativeSupport=scanner.nextLine()).equals("-1")){
        minSupport = (int) (Double.parseDouble(minRelativeSupport) * tidList.size());
        getHeadTable();
        trimTidList();
        //        if ()
//        System.out.println("tidList = " + tidList);
//        System.out.println("headTable = " + headTable);
        Node rootNode = getFPTree(tidList);
        showFPTree(rootNode);
        completeNodeLinkedListOfHeadTable(rootNode);
        operateFP(rootNode);
//        System.out.println("headTable = " + headTable);
//        }
    }

    private static void operateFP(Node rootNode) {

    }

//    private static ArrayList<ArrayList<Integer>> getNewTidList(ArrayList<TableNode> newHeadTable) {
//        //书上是反着写 我也跟着反着遍历
//        ArrayList<ArrayList<Integer>> newTidList = new ArrayList<>();
//        for (int i = newHeadTable.size() - 1; i >= 0; i--) {
//            for (Node node : newHeadTable.get(i).nodeLinkedList) {
//                int supportCount = node.supportCount;
//                LinkedList<Integer> list = new LinkedList<>();
//                while ((node=node.preNode)!=null){//构建一个新的事务集
////                    ArrayList<Integer> list = new ArrayList<>();
//                    list.addFirst(node.itemId);
//                }
//                ArrayList<Integer> newTid = new ArrayList<>(list);
//                for (int j = 0; j < supportCount; j++) {
//                    newTidList.add(newTid);
//                }
//            }
//        }
//    }

    private static void completeNodeLinkedListOfHeadTable(Node rootNode) {
        //这里用BFS来写的， 建树用的是DFS，所以建出来的节点链顺序和FP树创建的节点顺序不一样，不影响使用，懒得改了
        LinkedList<Node> list = new LinkedList<>();
        list.add(rootNode);
        while (!list.isEmpty()) {
            Node nowNode = list.getFirst();
            list.remove();//remove的只是一个引用， 对象没有销毁
            for (Node childNode : nowNode.child) {
                list.add(childNode);
                int indexOfIdInHeadTable = getIndexOfIdInHeadTable(childNode.itemId);
                headTable.get(indexOfIdInHeadTable).nodeLinkedList.add(childNode);
            }
        }
    }

    private static void showFPTree(Node rootNode) {//
        LinkedList<Node> list = new LinkedList<>();
        list.add(rootNode);
        System.out.println("rootNode.itemId = " + rootNode.itemId);
        while (!list.isEmpty()) {
            Node nowNode = list.getFirst();
            list.remove();
            for (Node childNode : nowNode.child) {
                list.add(childNode);
                System.out.println("childNode = " + childNode);
            }
        }
    }

    private static Node getFPTree(ArrayList<ArrayList<Integer>> tidList) {
        Node rootNode = new Node(-1, -1, new ArrayList<>(),null);
        for (ArrayList<Integer> tid : FPGrowth.tidList) {
            Node preNode = rootNode;
            for (Integer itemId : tid) {
                int indexOfIdInChild;
                //如果有多少个tid就搞多少个分支这样做不知道行不行
                //按照书上的方法来编码 过程略微复杂点
                if ((indexOfIdInChild = preNode.getIndexOfIdInChild(itemId)) != -1) {
                    Node newChildNode = preNode.child.get(indexOfIdInChild);
                    newChildNode.supportCount++;
                    preNode.child.set(indexOfIdInChild, newChildNode);
                } else {
                    preNode.child.add(new Node(itemId, 1, new ArrayList<>(), preNode));
                    indexOfIdInChild = preNode.child.size() - 1;
                }
                preNode = preNode.child.get(indexOfIdInChild);//java没有指针还挺不习惯的
            }
        }
        return rootNode;
    }

    private static int getIndexOfIdInHeadTable(Integer itemId) {
        for (int i = 0; i < headTable.size(); i++) {
            if (itemId.equals(headTable.get(i).itemId)) return i;
        }
        return -1;
    }


    private static void trimTidList() {
        for (int i = 0; i < tidList.size(); i++) {
            ArrayList<Integer> tid = tidList.get(i);
            Iterator<Integer> iterator = tid.iterator();
            while (iterator.hasNext()) {
                Integer itemId = iterator.next();
                if (!validItemIdSet.contains(itemId)) iterator.remove();
            }
            tid.sort((o1, o2) -> itemIdMap.get(o2) - itemIdMap.get(o1));//ArrayList的sort是不是比LinkedList的sort要快？
        }
//        for (ArrayList<Integer> tid : tidList) {
//        }
    }

    private static void getHeadTable() {
        for (Map.Entry<Integer, Integer> entry : itemIdMap.entrySet()) {
            int itemIdCount = entry.getValue();
            if (itemIdCount >= minSupport) {
                System.out.println("yes！entry.getKey() = " + entry.getKey() + "  entry.getValue()= " + entry.getValue());
                int itemId = entry.getKey();
                validItemIdSet.add(itemId);
                headTable.add(new TableNode(itemId, itemIdCount, new LinkedList<>()));
            }
        }
        headTable.sort((o1, o2) -> o2.supportCount - o1.supportCount);
//        System.out.println("now headTable = " + headTable);
    }


    private static void getDataByDisk(String s) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(s));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            ArrayList<Integer> tempList = new ArrayList<>();
            String[] itemIds = line.split("  ");
            for (String itemId : itemIds) {
                int realItemId = Integer.parseInt(itemId);
                tempList.add(realItemId);
                int nowIdCount = itemIdMap.containsKey(realItemId) ? itemIdMap.get(realItemId) : 0;
                itemIdMap.put(realItemId, nowIdCount + 1);
            }
            tidList.add(tempList);
        }
        bufferedReader.close();
    }

    static class TableNode {
        int itemId;
        int supportCount;
        //        Node headNode;
        LinkedList<Node> nodeLinkedList;

        public TableNode(int itemId, int supportCount, LinkedList<Node> nodeLinkedList) {
            this.itemId = itemId;
            this.supportCount = supportCount;
            this.nodeLinkedList = nodeLinkedList;
        }

        @Override
        public String toString() {
            return "TableNode{" +
                    "itemId=" + itemId +
                    ", supportCount=" + supportCount +
                    ", nodeLinkedList=" + nodeLinkedList +
                    '}';
        }
    }

    static class Node {
        int itemId;
        int supportCount;
        ArrayList<Node> child;
        Node preNode;

        public Node(int itemId, int supportCount, ArrayList<Node> child, Node preNode) {
            this.itemId = itemId;
            this.supportCount = supportCount;
            this.child = child;
            this.preNode=preNode;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "itemId=" + itemId +
                    ", supportCount=" + supportCount +
                    '}';
        }

        //        public boolean hasChildWithId(Integer itemId) {
//            for (Node node : child) {
//                if (itemId.equals(itemId)) return true;//包装类和基本类型可以用== 但害怕后面代码改动 所以用equal保险一点
//            }
//            return false;
//        }

        public int getIndexOfIdInChild(Integer itemId) {
            for (int i = 0; i < child.size(); i++) {
                if (itemId.equals(child.get(i).itemId)) return i;//这里绕的有点复杂了 get(i)得到的是Node
            }
            return -1;
        }
    }
}
