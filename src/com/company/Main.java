package com.company;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        //下面三行為記憶體計算
        /* Get the Java runtime */
        Runtime runtime = Runtime.getRuntime();
        /* Run the garbage collector */
        runtime.gc();

        final float Pbr = (float) 0.5;  //所設定的Pbr閥值
        int checkPartPoint = 0;  //判定checkPartList是否為空變數，若是空的則=1，之後比對加入空集合
        int gain = 0;  //計算gain次數(待check檢查總次數)
        ArrayList<LinkedList<LinkedList<String>>> checkPartList;

        String inputFileName = "400.txt";  //輸入測試檔案名稱
        final HashMap<String, LinkedList<String>> trajectoryData = getTrajectoryData(inputFileName);  //取得軌跡資料檔案(txt) EX:{t4=[a2, a3, b1], t5=[a3, a1, b1], t6=[a3, a1, b1], t7=[a3, b2, a1], t8=[a3, b2, b3], t1=[a1, b2, b3], t2=[b1, a2, b2, a3], t3=[a2, b3, a3]}

        long createBipartiteGraphStartTime = System.currentTimeMillis();   //獲取建立bipartite graph的開始時間
        Bipartite bipartiteData = new Bipartite(trajectoryData);  //一、取得建立bipartiteData的分群資料以及投影關聯
        long createBipartiteGraphEndTime = System.currentTimeMillis();   //獲取建立bipartite graph的結束時間

        while (true) {
            FindPP findOrgPP = new FindPP(bipartiteData.biT, bipartiteData.biCT, Pbr, 0);  //二-1、掃描異常項目 check：0初始化/1loop計算

            if (findOrgPP.problematicTotal.equals(0)) {
                break;

            }  //終止條件設定

            if (checkPartPoint == 0) {
                checkPartList = findOrgPP.combinationCheckWithoutEmpty(findOrgPP.problematicDenominatorKey, bipartiteData.biT.keySet());  //二-2、組合代檢查項目

                if (checkPartList.size() == 0) {
                    checkPartPoint = 1;
                    checkPartList = findOrgPP.combinationCheckWithEmpty(findOrgPP.problematicDenominatorKey, bipartiteData.biT.keySet());  //二-2、組合代檢查項目

                }
            } else {
                checkPartList = findOrgPP.combinationCheckWithEmpty(findOrgPP.problematicDenominatorKey, bipartiteData.biT.keySet());  //二-2、組合代檢查項目

            }
            gain += checkPartList.size();  //計算gain次數(待check檢查總次數)

            findMaxUgainPart(checkPartList, trajectoryData, bipartiteData, findOrgPP, Pbr);  //三、掃描全部組合找出最大Ugain項目，並且更新原始表格

            bipartiteData = new Bipartite(trajectoryData);  //一、取得建立bipartiteData的分群資料以及投影關聯
        }

        long programEndTime = System.currentTimeMillis();   //獲取程式最結束終時間

        /* Calculate the used memory */
        long memory = runtime.totalMemory() - runtime.freeMemory();
//        long end_memory = runtime.totalMemory() - runtime.freeMemory();

        System.out.println("0.執行版本(v3)：到最後才加入空集合 + 不跟原先的計算項目數值做檢查是否包含的版本");
        System.out.println("1.建立bipartite graph的時間：" + (createBipartiteGraphEndTime - createBipartiteGraphStartTime) + "ms");
        System.out.println("2.建完bipartite graph後，一直到最終結束所花的時間：" + (programEndTime - createBipartiteGraphEndTime) + "ms");
        System.out.println("3.計算gain的總次數：" + gain + "次");
//        System.out.println("4.原始 #trajectories：\t\t" + TrajectoriesCount(getTrajectoryData(inputFileName)) + "個；" + getTrajectoryData(inputFileName));
        System.out.println("4.原始 #trajectories：\t\t" + TrajectoriesCount(getTrajectoryData(inputFileName)) + "個；");
//        System.out.println("5.最後有多少 #trajectories：\t" + TrajectoriesCount(trajectoryData) + "個；" + trajectoryData);
        System.out.println("5.最後有多少 #trajectories：\t" + TrajectoriesCount(trajectoryData) + "個；");
//        System.out.println("6.原始 #locations：\t\t\t" + locationsCount(new Bipartite(getTrajectoryData(inputFileName)).biT) + "個；" + new Bipartite(getTrajectoryData(inputFileName)).biT);
        System.out.println("6.原始 #locations：\t\t\t" + locationsCount(new Bipartite(getTrajectoryData(inputFileName)).biT) + "個；");
//        System.out.println("7.最後有多少 #locations：\t" + locationsCount(bipartiteData.biT) + "個；" + bipartiteData.biT);
        System.out.println("7.最後有多少 #locations：\t" + locationsCount(bipartiteData.biT) + "個；");
        System.out.println("8.所需記憶體空間：" + memory + "bytes");
    }


    /*
     * 讀取軌跡檔案
     * */
    private static HashMap<String, LinkedList<String>> getTrajectoryData(String inputFileName) {
        final HashMap<String, LinkedList<String>> trajectoryTale = new HashMap<>();
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream("./input/Oden/avg_len5/" + inputFileName), StandardCharsets.UTF_8)); // 指定讀取文件的編碼格式，以免出現中文亂碼
            String str;

            while ((str = reader.readLine()) != null) {
                trajectoryTale.put(str.split(" ")[0], new LinkedList<>(Arrays.asList(Arrays.copyOfRange(str.split(" "), 1, str.split(" ").length))));  //根據空白進行切割，第0個當作key，1~全部當作value

            }
        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            try {
                assert reader != null;
                reader.close();

            } catch (IOException e) {
                e.printStackTrace();

            }
        }

        return trajectoryTale;
    }

    /*
     * 掃描待檢查組合項目找出最大Ugain數值
     * */
    private static void findMaxUgainPart(ArrayList<LinkedList<LinkedList<String>>> checkPartList, HashMap<String, LinkedList<String>> trajectoryData, Bipartite bipartiteData, FindPP findOrgPP, float Pbr) {
        LinkedList<LinkedList<String>> maxCheckPart = new LinkedList<>();
        float maxU_gain = -1;

        for (LinkedList<LinkedList<String>> checkPart : checkPartList) {
            float DenominatorData = (float) 0;  //公式中分母的計算累加值
            float U_gain;

            bipartiteData.update_biT_biCT(checkPart);  //將每個愈計算的checkPart進行計算，重建bipartite以及關聯表
            FindPP findLoopPP = new FindPP(bipartiteData.new_biT, bipartiteData.new_biCT, Pbr, 1);  //二-1、掃描異常項目 check：0初始化/1loop計算

            for (String T_unifyingPerson_t : bipartiteData.biT.get(checkPart.get(0))) {
                float denominatorMolecular = trajectoryData.get(T_unifyingPerson_t).size() - bipartiteData.unifying.size();  //公式中分母裡面的分子項目，為比較項目中t所包含的軌跡項目數量 扣除 移除的軌跡數
                float denominatorDenominator = trajectoryData.get(T_unifyingPerson_t).size();
                DenominatorData += (1 - ((denominatorMolecular * (denominatorMolecular - 1)) / (denominatorDenominator * (denominatorDenominator - 1))));
            }
            U_gain = ((float) (findOrgPP.problematicTotal - findLoopPP.problematicTotal) / findOrgPP.problematicTotal) * ((float) 1 / DenominatorData);

            if (maxU_gain < U_gain) {
                maxU_gain = U_gain;
                maxCheckPart = checkPart;
            }
        }
        bipartiteData.unifying = new LinkedList<>(maxCheckPart.get(0));
        bipartiteData.unifying.removeAll(maxCheckPart.get(1));
        //更新原始軌跡表 (若看不懂，參見v2 到最後才加入空集合 + 不跟原先的計算項目數值做檢查是否包含的版本 中的456行)
        bipartiteData.biT.get(maxCheckPart.get(0)).forEach((updateTrajectoriesTaleKey) -> bipartiteData.unifying.forEach((removeItem) -> trajectoryData.get(updateTrajectoriesTaleKey).removeIf(s -> s.equals(removeItem))));

    }

    /*
     * 計算 #trajectories
     * */
    private static Integer TrajectoriesCount(HashMap<String, LinkedList<String>> trajectoryData) {
        int TrajectoriesCount = 0;
        //如果計算#trajectories的value不等於0，進行加總
        for (String strings : trajectoryData.keySet()) {
            if (trajectoryData.get(strings).size() != 0) {
                TrajectoriesCount += 1;

            }
        }

        return TrajectoriesCount;
    }

    /*
     * 計算 #locations
     * */
    private static Integer locationsCount(HashMap<LinkedList<String>, ArrayList<String>> biT) {
        Iterator<LinkedList<String>> it = biT.keySet().iterator();
        int TrajectoriesCount = 0;

        while (it.hasNext()) {
            LinkedList<String> locationsKey = it.next();
            List<String> locationsValue = biT.get(locationsKey);
            TrajectoriesCount += locationsValue.size() * locationsKey.size();

        }

        return TrajectoriesCount;
    }
}