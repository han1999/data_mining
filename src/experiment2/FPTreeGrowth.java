package experiment2;
import java.io.*;
import java.util.*;


public class FPTreeGrowth {
    public static int allCount=0;//频繁项集的个数
    public static int  support = 100; //最小支持度
    public Map<String,Integer> stringIntegerHashMap =new HashMap<String,Integer>();//第一次的次序

    public static void main(String[] args) throws IOException {

        FPTreeGrowth fpTreeGrowth=new FPTreeGrowth();
        LinkedList<LinkedList<String>> dataByDisk=fpTreeGrowth.getDataByDisk("data.txt");
        LinkedList<TreeNode> treeNodes=fpTreeGrowth.buildHeaderLink(dataByDisk);//第一次遍历的表头节点
        allCount+=treeNodes.size();
        fpTreeGrowth.order(treeNodes);
        fpTreeGrowth.fpgrowth(dataByDisk,null);//进行递归
        System.out.println("allCount = " + allCount);
    }

    /**
     * 从磁盘中读取数据
     * @return
     * @throws IOException
     */
    public LinkedList<LinkedList<String>> getDataByDisk(String fileName) throws IOException {
        LinkedList<LinkedList<String>> records=new LinkedList<LinkedList<String>>();
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            if(line.length()==0||"".equals(line))continue;
            String[] str=line.split("  ");
            LinkedList<String> litm=new LinkedList<String>();
            for(int i=0;i<str.length;i++){
                litm.add(str[i].trim());
            }
            records.add(litm);
        }
        br.close();
        return records;
    }

    //创建表头链
    public LinkedList<TreeNode> buildHeaderLink(LinkedList<LinkedList<String>> records){
        LinkedList<TreeNode> header=null;
        if(records.size()>0){
            header=new LinkedList<TreeNode>();
        }else{
            return null;
        }
        Map<String, TreeNode> map = new HashMap<String, TreeNode>();
        for(LinkedList<String> items:records){

            for(String item:items){
                //如果存在数量增1，不存在则新增
                if(map.containsKey(item)){
                    map.get(item).Sum(1);
                }else{
                    TreeNode node=new TreeNode();
                    node.setName(item);
                    node.setCount(1);
                    map.put(item, node);
                }
            }
        }
        // 把支持度大于（或等于）minSup的项加入到F1中
        Set<String> names = map.keySet();
        for (String name : names) {
            TreeNode tnode = map.get(name);
            if (tnode.getCount() >= support) {
                header.add(tnode);
            }
        }
        sort(header);

        String test="ddd";
//        allCount+=header.size();
        return header;
    }
    //选择法排序,如果次数相等，则按名字排序,字典顺序,先小写后大写
    public List<TreeNode> sort(List<TreeNode> list){
        int len=list.size();
        for(int i=0;i<len;i++){

            for(int j=i+1;j<len;j++){
                TreeNode node1=list.get(i);
                TreeNode node2=list.get(j);
                if(node1.getCount()<node2.getCount()){
                    TreeNode tmp=new TreeNode();
                    tmp=node2;
                    list.remove(j);
                    //list指定位置插入，原来的>=j元素都会往下移，不会删除,所以插入前要删除掉原来的元素
                    list.add(j,node1);
                    list.remove(i);
                    list.add(i,tmp);
                }
                //如果次数相等，则按名字排序,字典顺序,先小写后大写
                if(node1.getCount()==node2.getCount()){
                    String name1=node1.getName();
                    String name2=node2.getName();
                    int flag=name1.compareTo(name2);
                    if(flag>0){
                        TreeNode tmp=new TreeNode();
                        tmp=node2;
                        list.remove(j);
                        //list指定位置插入，原来的>=j元素都会往下移，不会删除,所以插入前要删除掉原来的元素
                        list.add(j,node1);
                        list.remove(i);
                        list.add(i,tmp);
                    }


                }
            }
        }

        return list;
    }
    //选择法排序，降序,如果同名按L 中的次序排序
    public   List<String> itemsort(LinkedList<String> lis,List<TreeNode> header){
        //List<String> list=new ArrayList<String>();
        //选择法排序
        int len=lis.size();
        for(int i=0;i<len;i++){
            for(int j=i+1;j<len;j++){
                String key1=lis.get(i);
                String key2=lis.get(j);
                Integer value1=findcountByname(key1,header);
                if(value1==-1)continue;
                Integer value2=findcountByname(key2,header);
                if(value2==-1)continue;
                if(value1<value2){
                    String tmp=key2;
                    lis.remove(j);
                    lis.add(j,key1);
                    lis.remove(i);
                    lis.add(i,tmp);
                }
                if(value1==value2){
                    int v1= stringIntegerHashMap.get(key1);
                    int v2= stringIntegerHashMap.get(key2);
                    if(v1>v2){
                        String tmp=key2;
                        lis.remove(j);
                        lis.add(j,key1);
                        lis.remove(i);
                        lis.add(i,tmp);
                    }
                }
            }
        }
        return lis;
    }

    public Integer findcountByname(String itemname,List<TreeNode> header){
        Integer count=-1;
        for(TreeNode node:header){
            if(node.getName().equals(itemname)){
                count= node.getCount();
            }
        }
        return count;
    }
    /**
     *
     * @param records 构建树的记录,如I1,I2,I3
     * @param header 韩书中介绍的表头
     * @return 返回构建好的树
     */
    public TreeNode builderFpTree(LinkedList<LinkedList<String>> records, List<TreeNode> header){

        TreeNode root;
        if(records.size()<=0){
            return null;
        }
        root=new TreeNode();
        for(LinkedList<String> items:records){
            itemsort(items,header);
            addNode(root,items,header);
        }
        String dd="dd";
        String test=dd;
        return root;
    }
    //当已经有分枝存在的时候，判断新来的节点是否属于该分枝的某个节点，或全部重合，递归
    public TreeNode addNode(TreeNode root, LinkedList<String> items, List<TreeNode> header){
        if(items.size()<=0)return null;
        String item=items.poll();
        //当前节点的孩子节点不包含该节点，那么另外创建一支分支。
        TreeNode node=root.findChild(item);
        if(node==null){
            node=new TreeNode();
            node.setName(item);
            node.setCount(1);
            node.setParent(root);
            root.addChild(node);

            //加将各个节点加到链头中
            for(TreeNode head:header){
                if(head.getName().equals(item)){
                    while(head.getNextHomonym()!=null){
                        head=head.getNextHomonym();
                    }
                    head.setNextHomonym(node);
                    break;
                }
            }
            //加将各个节点加到链头中
        }else{
            node.setCount(node.getCount()+1);
        }

        addNode(node,items,header);
        return root;
    }
    //从叶子找到根节点，递归之
    public void toroot(TreeNode node, LinkedList<String> newrecord){
        if(node.getParent()==null)return;
        String name=node.getName();
        newrecord.add(name);
        toroot(node.getParent(),newrecord);
    }
    //对条件FP-tree树进行组合，以求出频繁项集
    public void combineItem(TreeNode node, LinkedList<String> newrecord, String Item){
        if(node.getParent()==null)return;
        String name=node.getName();
        newrecord.add(name);
        toroot(node.getParent(),newrecord);
    }
    //fp-growth
    public void fpgrowth(LinkedList<LinkedList<String>> records,String item){
        //保存新的条件模式基的各个记录，以重新构造FP-tree
        LinkedList<LinkedList<String>> newrecords=new LinkedList<LinkedList<String>>();
        //构建链头
        LinkedList<TreeNode> header=buildHeaderLink(records);
        //创建FP-Tree
        TreeNode fptree= builderFpTree(records,header);
        //结束递归的条件
        if(header.size()<=0||fptree==null){
//            System.out.println("-----------------");
            return;
        }
        //打印结果,输出频繁项集
        if(item!=null){
            //寻找条件模式基,从链尾开始
            for(int i=header.size()-1;i>=0;i--){
                TreeNode head=header.get(i);
                String itemname=head.getName();
                Integer count=0;
                while(head.getNextHomonym()!=null){
                    head=head.getNextHomonym();
                    //叶子count等于多少，就算多少条记录
                    count=count+head.getCount();

                }
                //打印频繁项集
                System.out.println(head.getName()+","+item+"\t"+count);
                allCount++;
            }
        }
        //寻找条件模式基,从链尾开始
        for(int i=header.size()-1;i>=0;i--){
            TreeNode head=header.get(i);
            String itemname;
            //再组合
            if(item==null){
                itemname=head.getName();
            }else{
                itemname=head.getName()+","+item;
            }

            while(head.getNextHomonym()!=null){
                head=head.getNextHomonym();
                //叶子count等于多少，就算多少条记录
                Integer count=head.getCount();
                for(int n=0;n<count;n++){
                    LinkedList<String> record=new LinkedList<String>();
                    toroot(head.getParent(),record);
                    newrecords.add(record);
                }
            }
            //System.out.println("-----------------");
            //递归之,以求子FP-Tree
            fpgrowth(newrecords,itemname);
        }
    }
    //保存次序，此步也可以省略，为了减少再加工结果的麻烦而加
    public void order(LinkedList<TreeNode> orderheader){
        for(int i=0;i<orderheader.size();i++){
            TreeNode node=orderheader.get(i);
            stringIntegerHashMap.put(node.getName(), i);
        }

    }


    public static class TreeNode implements Comparable<TreeNode>{

        private String name; // 节点名称
        private Integer count; // 计数
        private TreeNode parent; // 父节点
        private List<TreeNode> children; // 子节点
        private TreeNode nextHomonym; // 下一个同名节点

        public TreeNode() {

        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }
        public void Sum(Integer count) {
            this.count =this.count+count;
        }
        public TreeNode getParent() {
            return parent;
        }

        public void setParent(TreeNode parent) {
            this.parent = parent;
        }

        public List<TreeNode> getChildren() {
            return children;
        }

        public void setChildren(List<TreeNode> children) {
            this.children = children;
        }

        public TreeNode getNextHomonym() {
            return nextHomonym;
        }

        public void setNextHomonym(TreeNode nextHomonym) {
            this.nextHomonym = nextHomonym;
        }
        /**
         * 添加一个节点
         * @param child
         */
        public void addChild(TreeNode child) {
            if (this.getChildren() == null) {
                List<TreeNode> list = new ArrayList<TreeNode>();
                list.add(child);
                this.setChildren(list);
            } else {
                this.getChildren().add(child);
            }
        }
        /**
         *  是否存在着该节点,存在返回该节点，不存在返回空
         * @param name
         * @return
         */
        public TreeNode findChild(String name) {
            List<TreeNode> children = this.getChildren();
            if (children != null) {
                for (TreeNode child : children) {
                    if (child.getName().equals(name)) {
                        return child;
                    }
                }
            }
            return null;
        }

        @Override
        public int compareTo(TreeNode arg0) {
            // TODO Auto-generated method stub
            int count0 = arg0.getCount();
            // 跟默认的比较大小相反，导致调用Arrays.sort()时是按降序排列
            return count0 - this.count;
        }
    }
}
