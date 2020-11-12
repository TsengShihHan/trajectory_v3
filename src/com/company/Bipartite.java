package com.company;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Bipartite {
    public HashMap<String, LinkedList<String>> trajectoryData;  //原始軌跡資料
    public HashMap<LinkedList<String>, ArrayList<String>> biT = new HashMap<>();  //scan建立的Bipartite
    public HashMap<LinkedList<String>, ArrayList<LinkedList<String>>> biCT = new HashMap<>();  //Bipartite對應投影的關聯
    public HashMap<LinkedList<String>, ArrayList<String>> new_biT;
    public HashMap<LinkedList<String>, ArrayList<LinkedList<String>>> new_biCT;
    public LinkedList<String> unifying;


    public Bipartite(HashMap<String, LinkedList<String>> trajectoryData) {
        this.trajectoryData = trajectoryData;
        build();
    }

    /*
     * 建立Bipartite & 關聯
     * */
    public void build() {
        this.trajectoryData.forEach((someOne, a_Trajectory) -> {
            LinkedList<String> aLocation = new LinkedList<>();
            LinkedList<String> bLocation = new LinkedList<>();
            ArrayList<String> personTA = new ArrayList<>();  //紀錄軌跡符合的人
            ArrayList<String> personTB = new ArrayList<>();  //紀錄軌跡符合的人
            ArrayList<LinkedList<String>> AC = new ArrayList<>();  //A的關聯
            ArrayList<LinkedList<String>> BC = new ArrayList<>();  //B的關聯

            //每個位置掃描
            a_Trajectory.forEach((location) -> {
                //判斷字首位置
                if (location.startsWith("a")) {
                    aLocation.add(location);
                } else {
                    bLocation.add(location);
                }
            });

            if (aLocation.size() != 0) {
                if (biT.containsKey(aLocation)) {
                    personTA = biT.get(aLocation);
                }
                personTA.add(someOne);
                biT.put(aLocation, personTA);
            }

            if (bLocation.size() != 0) {
                if (biT.containsKey(bLocation)) {
                    personTB = biT.get(bLocation);
                }
                personTB.add(someOne);
                biT.put(bLocation, personTB);
            }

            if (aLocation.size() != 0 && bLocation.size() != 0) {
                //建立a關聯
                if (this.biCT.containsKey(aLocation)) {
                    AC = this.biCT.get(aLocation);
                }
                if (!AC.contains(bLocation)) {
                    AC.add(bLocation);
                    this.biCT.put(aLocation, AC);
                }

                //建立b關聯
                if (this.biCT.containsKey(bLocation)) {
                    BC = this.biCT.get(bLocation);
                }
                if (!BC.contains(aLocation)) {
                    BC.add(aLocation);
                    this.biCT.put(bLocation, BC);
                }
            }
        });
    }

    /*
     * 更新重建biT、biCT
     * */
    public void update_biT_biCT(LinkedList<LinkedList<String>> checkPart) {
        this.new_biCT = new HashMap<>(this.biCT);
        this.new_biT = new HashMap<>(this.biT);
        this.unifying = new LinkedList<>(checkPart.get(0));
        LinkedList<String> with = new LinkedList<>(checkPart.get(1));

        if (!with.isEmpty()) {
            this.unifying.removeAll(with);  //取出差集項目，移除相同值，剩餘值為欲從原始軌跡中移除的項目
            ArrayList<String> tem_biT_unifyingList = new ArrayList<>(this.biT.get(checkPart.get(0))); //取出比較軌跡路徑包含的t(某個人) ex.[t5, t6, t7]
            ArrayList<LinkedList<String>> tem_biCT_unifyingList = new ArrayList<>(this.biCT.get(checkPart.get(0)));  //取出比較軌跡路徑所對應的投影位置 if[a3, a1]，對應是 [[b1], [b2]]
            ArrayList<String> tem_biT_value = new ArrayList<>(this.biT.get(checkPart.get(1)));
            ArrayList<LinkedList<String>> tem_biCT_value = new ArrayList<>(this.biCT.get(checkPart.get(1)));  //取出比較軌跡路徑所對應的投影位置

            //移除愈刪除的關聯項目
            this.new_biT.remove(checkPart.get(0));
            this.new_biCT.remove(checkPart.get(0));
            //加入移除的value項目給新的
            tem_biT_value.addAll(tem_biT_unifyingList);
            tem_biCT_unifyingList.forEach((biCT_Item) -> {
                if (!tem_biCT_value.contains(biCT_Item)) {
                    tem_biCT_value.add(biCT_Item);
                }
            });

            //更新進去
            this.new_biT.put(checkPart.get(1), tem_biT_value);
            this.new_biCT.put(checkPart.get(1), tem_biCT_value);

            //更新對應的連結
            tem_biCT_unifyingList.forEach((CT_part) -> {
                ArrayList<LinkedList<String>> a = new ArrayList<>(new_biCT.get(CT_part));  //a是暫存變數命名....不知道要命甚麼，取得關聯投影過去的位置// (if[a3, a1]，對應是 [[b1], [b2]]，要把b1、b2有連給[a3, a1]的項目移除)
                a.remove(checkPart.get(0));
                //有包含項目的話就跳過
                if (!a.contains(checkPart.get(1))) {
                    a.add(checkPart.get(1));
                }

                this.new_biCT.put(CT_part, a);

            });

        } else {
            ArrayList<LinkedList<String>> tem_biCT_unifyingList = new ArrayList<>(this.biCT.get(checkPart.get(0)));  //取出比較軌跡路徑所對應的投影位置

            this.new_biT.remove(checkPart.get(0));
            this.new_biCT.remove(checkPart.get(0));

            //更新對應的連結
            tem_biCT_unifyingList.forEach((CT_part) -> {
                ArrayList<LinkedList<String>> a = new ArrayList<>(new_biCT.get(CT_part));
                a.remove(checkPart.get(0));
                this.new_biCT.put(CT_part, a);

            });
        }
    }
}
