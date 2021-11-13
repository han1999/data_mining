package experiment1;

import java.io.*;
import java.util.*;

/**
 * @description: 对给定的数据， 找出频繁项集， 对用户的不同输入(相对支持度)，分别计算结果集，并保存到磁盘中。
 * @author: hanxiao
 * @date: 2021/10/21
 **/

public class Apriori {
    static ArrayList<HashSet<Integer>> tid = new ArrayList<>();//有可能不同的顾客 买了完全相同的东西 所以不能用set  要用ArrayList
    static HashSet<HashSet<Integer>> c = new HashSet<>();//候选项集
    static HashSet<HashSet<Integer>> originC = new HashSet<>();//初始候选集， 方便用户更换支持度
    static HashSet<HashSet<Integer>> l = new HashSet<>();//频繁项集
    static int min_sup;//最小支持度（绝对的）
    static String minSupRelative;//用户输入的相对最小支持度
    static int k;//迭代的轮数
    static Scanner scanner = new Scanner(System.in);
    static int count = 0;//代表用户有几次输入， 方便后续存盘编号

    public static void main(String[] args) throws IOException {
        getDataByDisk("data.txt");//读取磁盘文件
        System.out.println("\r\n请输入最小支持度（输入 -1 代表结束程序）：");
        while (!((minSupRelative = scanner.nextLine()).equals("-1"))) {
            l.clear();
            c.clear();
            c.addAll(originC);//每一轮用户输入不同数值 重新开始迭代
            min_sup = (int) (Double.parseDouble(minSupRelative) * tid.size());//转换为绝对支持度
//            min_sup=100;
            k = 1;
            while (!c.isEmpty()) {
                cToL();//从候选项集中找到频繁项集
                lToNextC();//对频繁项集进行连接，产生下一轮的候选项集
                trimC();//对候选项集进行剪枝操作
                k++;//下一轮
            }
            count++;
            saveResultToDisk(l);//将结果存入磁盘
            System.out.println("\r\n请输入最小支持度（输入 -1 代表结束程序）：");
        }
        System.out.println("再见！");
    }

    /**
     * 存入结果到磁盘中
     *
     * @param l 结果集
     * @throws IOException
     */
    private static void saveResultToDisk(HashSet<HashSet<Integer>> l) throws IOException {
        ArrayList<HashSet<Integer>> list = new ArrayList<>(l);
        list.sort(Comparator.comparingInt(HashSet::size));//对结果集进行排序 美观
        BufferedWriter out = new BufferedWriter(new FileWriter("resultData" + count + ".txt"));
        out.write("总共有数据：" + tid.size() + "条\r\n");
        out.write("最小相对支持度：" + minSupRelative + "    最小绝对支持度：" + min_sup + "\r\n");
        out.write("频繁项集共有：" + list.size() + " 条，如下：\r\n");
        for (HashSet<Integer> set : list) {
            out.write(set + "\r\n");
        }
        System.out.println("结果已经保存在 resultData" + count + ".txt 文件中， 请注意查看！");
        out.close();
    }

    /**
     * 从候选项集中找到频繁项集
     */
    private static void cToL() {
        for (HashSet<Integer> set : c) {
            int count = 0;
            for (HashSet<Integer> user : tid) {
                if (user.containsAll(set)) count++;//如果tid里面包含候选项 则count+1
            }
            if (count >= min_sup) {
                l.add(set);//找到了一个频繁项集 假如结果集中
            }
        }
        c.clear();//清空候选项集 方便下一轮操作
    }

    /**
     * 对频繁项集进行连接，产生下一轮的候选项集
     */
    private static void lToNextC() {
        //紧扣定义来写
        HashSet<HashSet<Integer>> tempL1 = new HashSet<>(l);
        HashSet<HashSet<Integer>> tempL2 = new HashSet<>(l);
        for (HashSet<Integer> set1 : tempL1) {
            if (set1.size() == k) {//只连接两个含有k个元素的频繁项集
                ArrayList<Integer> list1 = new ArrayList<>(set1);
                for (HashSet<Integer> set2 : tempL2) {
                    if (set2.size() == k) {
                        ArrayList<Integer> list2 = new ArrayList<>(set2);//如果一开始就用ArrayList，或许简单一些
                        if (canConnect(list1, list2)) {
                            HashSet<Integer> setTemp = new HashSet<>(list1);
                            setTemp.add(list2.get(list2.size() - 1));
                            c.add(setTemp);
                        }
                    }
                }
            }
        }
    }

    /**
     * 对候选项集进行剪枝操作
     */
    private static void trimC() {
        Iterator<HashSet<Integer>> iterator = c.iterator();
        while (iterator.hasNext()) {
            HashSet<Integer> set = iterator.next();
            if (needTrim(set)) {//用迭代器进行剪枝
                iterator.remove();
            }
        }
    }

    /**
     * 判断候选项集 是否需要进行剪枝
     *
     * @param set 某个候选项集
     * @return 需要剪枝则返回true，否则返回false
     */
    private static boolean needTrim(HashSet<Integer> set) {
        HashSet<HashSet<Integer>> subsets = getSubsetsWithNumbersOneLess(set);
        for (HashSet<Integer> subset : subsets) {
            if (!l.contains(subset)) {//如果子集不在已有的结果集中 代表这个候选项集需要删除掉
                return true;
            }
        }
        return false;
    }

    /**
     * 得到一个集合的所有元素个数少一的子集合
     *
     * @param set 原集合
     * @return 原集合所有元素个数少一的子集合的集合
     */
    private static HashSet<HashSet<Integer>> getSubsetsWithNumbersOneLess(HashSet<Integer> set) {
        HashSet<HashSet<Integer>> hashSets = new HashSet<>();
        ArrayList<Integer> list = new ArrayList<>(set);//转换为List更好操作
        for (int i = 0; i < list.size(); i++) {
            ArrayList<Integer> tempList = new ArrayList<>(list);
            tempList.remove(i);//每次轮流去掉一个元素
            hashSets.add(new HashSet<>(tempList));
        }
        return hashSets;
    }

    /**
     * 判断两个list是否可以连接
     *
     * @param list1
     * @param list2
     * @return 如果满足连接条件， 则返回true， 否则返回false
     */
    private static boolean canConnect(ArrayList<Integer> list1, ArrayList<Integer> list2) {
        list1.sort(Comparator.comparingInt(o -> o));
        list2.sort(Comparator.comparingInt(o -> o));//先对list进行排序
        for (int i = 0; i < list1.size() - 1; i++) {
            if (list1.get(i) != list2.get(i)) return false;//除了最后一个元素以外的其他元素必须全都相等
        }
        if (list1.get(list1.size() - 1) < list2.get(list2.size() - 1)) return true; //如果其他元素都相等， 最后一个元素不相等， 则可以连接
        return false;//其余情况， 均不能连接
    }

    /**
     * 从本地磁盘中读取数据
     *
     * @param s 要读取的文件的文件名
     * @throws IOException
     */
    private static void getDataByDisk(String s) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(s));
        String line;
        while ((line = br.readLine()) != null) {
            String[] goodsIds = line.split("  ");//这里分割起来容易出问题， 因为不是一个空格， 是两个空格
            HashSet<Integer> user = new HashSet<>();
            for (String goodsId : goodsIds) {
                int str2Int = Integer.parseInt(goodsId);
                user.add(str2Int);
                HashSet<Integer> originSet = new HashSet<>();//初始C1的情况
                originSet.add(str2Int);
                originC.add(originSet);
            }
            tid.add(user);
        }
        br.close();
    }
}
