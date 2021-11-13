package experiment1;

import sun.java2d.pipe.RegionIterator;

import java.io.*;
import java.util.*;

/**
 * @description: 从磁盘读入给定数据，针对用户给出的不同的最小支持度， 用FP-growth算法找到频繁项集， 并保存到本地磁盘。
 * @author: hanxiao
 * @date: 2021/10/23
 **/

public class FPGrowth2 {
    //    static ArrayList<ArrayList<Integer>> tidList = new ArrayList<>();//不写new会怎么样呢？马上用treeSet来试试看
    static ArrayList<TableNode> headTable = new ArrayList<>();//项头表
    static HashMap<Integer, Integer> itemIdMap = new HashMap<>();//商品ID集合
    static Scanner scanner = new Scanner(System.in);//处理用户输入
    static int minSupport = 100;
//    static int minSupport=400;
    static HashSet<Integer> validItemIdSet = new HashSet<>();
    //    static HashSet<HashSet<Integer>> frequentItemSets=new HashSet<>();
    static HashSet<HashSet<Integer>> frequentItemSets = new HashSet<>();//用ArrayList是为了方便排序 后来发现没用
//    static HashMap<Integer, Integer> itemIdToHeadTableIndex = new HashMap<>();//其实不必要作为参数传递的 全局统一就行了

    //    static HashMap<Integer, Node> preSameIdNode = new HashMap<>();
    public static void main(String[] args) throws IOException {
        ArrayList<ArrayList<Integer>> tidList = getDataByDisk("data.txt");
        System.out.println("请输入最小相对支持度（输入 -1 代表结束程序）:");
//        Integer integer = new Integer(5);
//        int i=5;
//        Integer integer1 = new Integer(5);
//        System.out.println(integer.equals(i));
//        while (!(minRelativeSupport=scanner.nextLine()).equals("-1")){

//        minSupport = (int) (Double.parseDouble(minRelativeSupport) * tidList.size());
//        minSupport=1;
        //羡慕python的多返回值
//        HashSet<Integer> preItemIdSet = new HashSet<>();
        operateFP(tidList, null, null);//minSupport可以不传的 全局变量不会变的
        System.out.println("frequentItemSets = " + frequentItemSets);
        HashSet<HashSet<Integer>> tempSets = new HashSet<>();
        for (HashSet<Integer> frequentItemSet : frequentItemSets) {
            tempSets.addAll(getSubSets((new ArrayList<>(frequentItemSet))));
        }
        frequentItemSets.addAll(tempSets);
        System.out.println("frequentItemSets = " + frequentItemSets);
//        TreeSet<HashSet<Integer>> frequentItemTreeSets = new TreeSet<>((o1, o2) -> o1.size() - o2.size());
//        frequentItemTreeSets.addAll(frequentItemSets);
//        System.out.println("frequentItemTreeSets = " + frequentItemTreeSets);
        ArrayList<HashSet<Integer>> frequentItemsetsList = new ArrayList<>(frequentItemSets);
        frequentItemsetsList.sort((o1, o2) -> o1.size() - o2.size());
        System.out.println("frequentItemsetsList = " + frequentItemsetsList);
        System.out.println("frequentItemsetsList.size() = " + frequentItemsetsList.size());
        BufferedWriter out = new BufferedWriter(new FileWriter("FPGrowth.txt"));
        out.write(frequentItemsetsList.size() + "条\r\n");
        for (HashSet<Integer> frequentSet : frequentItemsetsList) {
            out.write(frequentSet + "\r\n");
        }
        out.close();

    }

    private static HashSet<HashSet<Integer>> getSubSets(ArrayList<Integer> set) {//用ArrayList方便取索引
        HashSet<HashSet<Integer>> subSets = new HashSet<>();
        int subSetsSize = 1 << set.size();
        for (int i = 1; i < subSetsSize; i++) {
            HashSet<Integer> subSet = new HashSet<>();
            int index = i;
            for (int j = set.size() - 1; j >= 0; j--) {
                if ((index & 1) == 1) {
                    subSet.add(set.get(j));
                }
                index = index >> 1;
            }
            subSets.add(subSet);
        }
        return subSets;
    }

    private static ArrayList<Integer> operateFP(ArrayList<ArrayList<Integer>> tidList, Integer preItemId, HashMap<Integer, Integer> itemIdToHeadTableIndex) {
        HashMap<Integer, Integer> itemIdMap = getItemIdMap(tidList, minSupport);//得到
//        HashSet<Integer> validItemIdSet=getValidItemIdSet(itemIdMap, minSupport);
        trimTidList(tidList, itemIdMap);//tidList作为引用传递进来 修改一般是没问题的
        System.out.println("tidList = " + tidList);
        ArrayList<TableNode> headTable = getHeadTable(itemIdMap, itemIdToHeadTableIndex);//HeadTable可以单独封装为一个类
        itemIdToHeadTableIndex = getItemIdToHeadTableIndex(headTable);
        System.out.println("headTable = " + headTable);
        Node rootNode = getFPTree(tidList);
        showFPTree(rootNode);
        completeNodeLinkedListOfHeadTable(rootNode, headTable);
        System.out.println("headTable = " + headTable);
        ArrayList<Integer> frequentPatternList = new ArrayList<>();
//        if (headTable.get(headTable.size()-1).nodeLinkedList.size()==1){
//            frequentPatternList = getConditionalPatternBase(nodeLinkedList.get(0));
//            frequentPatternList.add(headTable.get(i).itemId);//能不能简化一下 get(i)单独设置一个变量
//            if (preItemId != null) frequentPatternList.add(preItemId);
//            System.out.println("preItemId = " + preItemId);
//            System.out.println("conditionalPatternBase = " + frequentPatternList);
//            if (!frequentPatternList.isEmpty()) frequentItemSets.add(new HashSet<>(frequentPatternList));
//            return frequentPatternList;
//        }
        for (int i = headTable.size() - 1; i >= 0; i--) {
            HashSet<Integer> set = new HashSet<>();
            set.add(headTable.get(i).itemId);
            if (preItemId==null) frequentItemSets.add(set);
            LinkedList<Node> nodeLinkedList = headTable.get(i).nodeLinkedList;
            frequentPatternList.clear();//这个语句有没有用呀
//            if (tidList.isEmpty()) {
//
//            }
            if (nodeLinkedList.size() == 1 /*&& nodeLinkedList.getFirst().child.size() == 0*/) {
                frequentPatternList = getConditionalPatternBase(nodeLinkedList.get(0));
                frequentPatternList.add(headTable.get(i).itemId);//能不能简化一下 get(i)单独设置一个变量
                if (preItemId != null) frequentPatternList.add(preItemId);
                System.out.println("preItemId = " + preItemId);
                System.out.println("conditionalPatternBase = " + frequentPatternList);
                if (!frequentPatternList.isEmpty()) frequentItemSets.add(new HashSet<>(frequentPatternList));
                if (preItemId!=null) return frequentPatternList;
            } else {
//                preItemIdSet.add(headTable.get(i).itemId);
                frequentPatternList = operateFP(getNewTidList(nodeLinkedList), headTable.get(i).itemId, itemIdToHeadTableIndex);
                if (preItemId != null) frequentPatternList.add(preItemId);
//                frequentPatternList.addAll(preItemIdSet);
//                frequentPatternList.add()
                System.out.println("preItemId = " + preItemId);
                System.out.println("frequentPatternList = " + frequentPatternList);
                if (!frequentPatternList.isEmpty()) frequentItemSets.add(new HashSet<>(frequentPatternList));
            }
        }
        return frequentPatternList;
    }

    private static HashMap<Integer, Integer> getItemIdToHeadTableIndex(ArrayList<TableNode> headTable) {
        HashMap<Integer, Integer> itemIdToHeadTableIndex = new HashMap<>();
        for (int i = 0; i < headTable.size(); i++) {
            itemIdToHeadTableIndex.put(headTable.get(i).itemId, i);
        }
        return itemIdToHeadTableIndex;
    }

    private static ArrayList<ArrayList<Integer>> getNewTidList(LinkedList<Node> nodeLinkedList) {
        ArrayList<ArrayList<Integer>> newTidList = new ArrayList<>();
        for (int i = 0; i < nodeLinkedList.size(); i++) {
            Node node = nodeLinkedList.get(i);
            ArrayList conditionalPatternBase = getConditionalPatternBase(node);
            if (!conditionalPatternBase.isEmpty()) {
                for (int j = 0; j < node.supportCount; j++) {
                    newTidList.add(conditionalPatternBase);
                }
            }
        }
        return newTidList;
    }

    private static ArrayList<Integer> getConditionalPatternBase(Node node) {//用HashSet好还是用ArrayList好呢
        LinkedList<Integer> conditionalPatternBase = new LinkedList<>();
        while (node.preNode.itemId != -1) {//根节点用-1标志
            node = node.preNode;
            conditionalPatternBase.addFirst(node.itemId);
        }
        return new ArrayList<>(conditionalPatternBase);
    }

//    private static HashSet<Integer> getValidItemIdSet(HashMap<Integer, Integer> itemIdMap, int minSupport) {
//        for (Map.Entry<Integer, Integer> entry : itemIdMap.entrySet()) {
//            entry.ge
//        }
//    }

    private static HashMap<Integer, Integer> getItemIdMap(ArrayList<ArrayList<Integer>> tidList, int minSupport) {
        HashMap<Integer, Integer> itemIdMap = new HashMap<>();
        for (ArrayList<Integer> tid : tidList) {
            for (Integer itemId : tid) {
                int nowIdCount = itemIdMap.containsKey(itemId) ? itemIdMap.get(itemId) : 0;
                itemIdMap.put(itemId, nowIdCount + 1);
            }
        }
        Iterator<Map.Entry<Integer, Integer>> iterator = itemIdMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Integer> entry = iterator.next();
            if (entry.getValue() < minSupport) iterator.remove();
        }
        return itemIdMap;
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

    private static void completeNodeLinkedListOfHeadTable(Node rootNode, ArrayList<TableNode> headTable) {
        //这里用BFS来写的， 建树用的是DFS，所以建出来的节点链顺序和FP树创建的节点顺序不一样，不影响使用，懒得改了
        LinkedList<Node> list = new LinkedList<>();
        list.add(rootNode);
        while (!list.isEmpty()) {
            Node nowNode = list.getFirst();
            list.remove();//remove的只是一个引用， 对象没有销毁
            for (Node childNode : nowNode.child) {
                list.add(childNode);
                int indexOfIdInHeadTable = getIndexOfIdInHeadTable(headTable, childNode.itemId);//其实可以维护一个map快速查找 太麻烦不搞了
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
                System.out.println(childNode);
            }
        }
    }

    private static Node getFPTree(ArrayList<ArrayList<Integer>> tidList) {
        Node rootNode = new Node(-1, -1, new ArrayList<>(), null);
        for (ArrayList<Integer> tid : tidList) {
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

    private static int getIndexOfIdInHeadTable(ArrayList<TableNode> headTable, Integer itemId) {
        for (int i = 0; i < headTable.size(); i++) {
            if (itemId.equals(headTable.get(i).itemId)) return i;
        }
        return -1;
    }


    private static void trimTidList(ArrayList<ArrayList<Integer>> tidList, HashMap<Integer, Integer> itemIdMap) {
        for (int i = 0; i < tidList.size(); i++) {
            ArrayList<Integer> tid = tidList.get(i);
            Iterator<Integer> iterator = tid.iterator();
            while (iterator.hasNext()) {
                Integer itemId = iterator.next();
                if (!itemIdMap.containsKey(itemId)) iterator.remove();
            }
            tid.sort((o1, o2) -> itemIdMap.get(o2) - itemIdMap.get(o1));//ArrayList的sort是不是比LinkedList的sort要快？
        }
    }

    private static ArrayList<TableNode> getHeadTable(HashMap<Integer, Integer> itemIdMap, HashMap<Integer, Integer> itemIdToHeadTableIndex) {
        ArrayList<TableNode> headTable = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : itemIdMap.entrySet()) {
//            System.out.println("yes！entry.getKey() = " + entry.getKey() + "  entry.getValue()= " + entry.getValue());
//            validItemIdSet.add(itemId);
            headTable.add(new TableNode(entry.getKey(), entry.getValue(), new LinkedList<>()));
        }
        if (itemIdToHeadTableIndex != null) {
            headTable.sort((o1, o2) -> {
                if (o1.supportCount != o2.supportCount) {
                    return o2.supportCount - o1.supportCount;
                } else {
                    return itemIdToHeadTableIndex.get(o1.itemId) - itemIdToHeadTableIndex.get(o2.itemId);
                }
            });//这个好像是不稳定的
        } else {
            headTable.sort((o1, o2) -> o2.supportCount - o1.supportCount);
        }
        return headTable;
    }


    private static ArrayList<ArrayList<Integer>> getDataByDisk(String s) throws IOException {
        ArrayList<ArrayList<Integer>> tidList = new ArrayList<>();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(s));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            ArrayList<Integer> tid = new ArrayList<>();
            String[] itemIds = line.split("  ");
            for (String itemId : itemIds) {
//                int realItemId = Integer.parseInt(itemId);
                tid.add(Integer.parseInt(itemId));
//                int nowIdCount = itemIdMap.containsKey(realItemId) ? itemIdMap.get(realItemId) : 0;
//                itemIdMap.put(realItemId, nowIdCount + 1);
            }
            tidList.add(tid);
        }
        bufferedReader.close();
        return tidList;
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
            this.preNode = preNode;
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
