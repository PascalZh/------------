import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class FirstOrderModelSolver {
    private static int nPop = 100; // 种群大小
    private static int nCrossOver = Math.round(nPop * 0.5f); // 交叉个体数
    private static int nMutation = Math.round(nPop * 1); // 变异个体数
    private static double[][] W;
    private static double[] D;
    private static int numNodes = 36; // 节点总数
    private static double pm = 1/numNodes; // 变异概率
    private static boolean doFixNode = true; // 是否固定某几个节点为加氯点，这些点为水源点，具体哪个点需要自己找到这个变量的相关代码修改
    private static int[] fixedNodes = {0, 25}; // 固定的节点
    private static int NF = 2; // 固定加氯点个数
    private static int NS = 3; // 最大加氯点个数（不包括固定加氯点）
    private static int N_iter = 100; // 迭代次数

    public static void main(String[] args) {
        // 读取W.csv和D.csv文件
        try {
            W = readCSV("W.csv");
            D = readCSV("D.csv")[0];
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        double[][] pop = new double[nPop][numNodes+1];

        int optim_vars = 1;
        while (optim_vars <= nPop) {
            pop[optim_vars - 1] = threshold(rand(numNodes+1), NS / (double) numNodes);
            if (doFixNode) {
                for (int i = 0; i < NF; i++) {
                    pop[optim_vars - 1][fixedNodes[i]] = 1;
                }
            }
            if (isValid(pop[optim_vars - 1])) {
                optim_vars++;
            }
        }

        for (optim_vars = 1; optim_vars <= N_iter; optim_vars++) {
            double[][] popm = new double[nMutation][numNodes+1];
            for (int j = 0; j < nMutation; j++) {
                while (true) {
                    double[] m = binaryMutate(pop[j]);
                    if (isValid(m)) {
                        popm[j] = m;
                        break;
                    }
                }
            }

            double[][] popc = new double[nCrossOver][numNodes+1];
            for (int j = 0; j < nCrossOver; j++) {
                while (true) {
                    int p1 = (int) Math.round(rand(1)[0] * (numNodes-1));
                    int p2 = (int) Math.round(rand(1)[0] * (numNodes-1));
                    double[][] c = simpleXover(pop[p1], pop[p2]);
                    double[] c1 = c[0];
                    double[] c2 = c[1];
                    if (isValid(c1)) {
                        popc[j] = c1;
                        break;
                    }
                    if (isValid(c2)) {
                        popc[j] = c2;
                        break;
                    }
                }
            }

            double[][] allPop = calculateFit(concatenate(concatenate(pop, popc), popm));
            pop = roulette(allPop, nPop);
        }

        // 展示结果
        // find max fitness in the pop
        int[] maxidx = find(pop, max(pop, numNodes));
        // print results
        System.out.println("最优解：");
        System.out.println("最优解的适应度值：" + pop[maxidx[0]][pop[maxidx[0]].length - 1]);
        System.out.println("最优解的加氯点：");
        for (int i = 0; i < numNodes; i++) {
            if (pop[maxidx[0]][i] == 1) {
                System.out.print((i + 1) + " ");
            }
        }
        System.out.println();
    }

    private static double[][] readCSV(String filename) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(filename));
        int row = 0;
        while (scanner.hasNextLine()) {
            scanner.nextLine();
            row = row + 1;
        }
        scanner.close();

        double[][] data = new double[row][];
        scanner = new Scanner(new File(filename));
        for (int i = 0; i < row; i++) {
            String[] line = scanner.nextLine().split(",");
            data[i] = new double[line.length];
            for (int j = 0; j < line.length; j++) {
                data[i][j] = Double.parseDouble(line[j]);
            }
        }
        scanner.close();
        return data;
    }

    private static double[] rand(int n) {
        double[] r = new double[n];
        Random random = new Random();
        for (int i = 0; i < n; i++) {
            r[i] = random.nextDouble();
        }
        return r;
    }

    private static double[] threshold(double[] arr, double thres) {
        double[] r = new double[arr.length];
        for (int i = 0; i < arr.length; i++) {
            r[i] = arr[i] < thres ? 1. : 0.;
        }
        return r;
    }

    public static double[] binaryMutate(double[] parent) {
        // Basic bit mutation changes each bit in the parent individual based on the
        // mutation probability
        int numVar = parent.length - 1; // Get the number of variables
        // Randomly select a number between 1 and the number of variables to perform
        // mutation on that bit
        double[] rN = new double[numVar];
        for (int i = 0; i < numVar; i++) {
            rN[i] = Math.random() < pm ? 1 : 0;
        }
        double[] mutatedParent = new double[parent.length];
        for (int i = 0; i < numVar; i++) {
            mutatedParent[i] = Math.abs(parent[i] - rN[i]);
        }
        mutatedParent[numVar] = parent[numVar];

        if (doFixNode) {
            for (int i = 0; i < NF; i++) {
                mutatedParent[fixedNodes[i]] = 1;
            }
        }
        return mutatedParent;
    }

    public static double[][] simpleXover(double[] p1, double[] p2) {
        // Selects two individuals P1 and P2 from the parent population and performs
        // single-point crossover
        int numVar = p1.length - 1; // Get the number of variables
        // Randomly select a number between 1 and the number of variables as the
        // crossover point
        int cPoint = (int) Math.round(Math.random() * (numVar - 2)) + 1;

        double[] c1 = new double[p1.length];
        double[] c2 = new double[p2.length];
        for (int i = 0; i < numVar; i++) {
            if (i < cPoint) {
                c1[i] = p1[i];
                c2[i] = p2[i];
            } else {
                c1[i] = p2[i];
                c2[i] = p1[i];
            }
        }
        c1[numVar] = p1[numVar];
        c2[numVar] = p2[numVar];

        if (doFixNode) {
            for (int i = 0; i < NF; i++) {
                c1[fixedNodes[i]] = 1;
                c2[fixedNodes[i]] = 1;
            }
        }

        return new double[][] { c1, c2 };
    }

    private static boolean isValid(double[] x) {
        double sum = 0;
        for (int i = 0; i < x.length - 1; i++) {
            sum += x[i];
        }
        return doFixNode ? sum <= NS+NF : sum <= NS;
    }

    public static double[][] calculateFit(double[][] pop) {
        double[][] pop_with_fit = new double[pop.length][pop[0].length];
        for (int i = 0; i < pop_with_fit.length; i++) {
            double[] x = Arrays.copyOfRange(pop[i], 0, pop[i].length - 1);
            double[] y = new double[W.length];
            for (int j = 0; j < W.length; j++) {
                y[j] = dotProduct(W[j], x) >= 1 ? 1 : 0;
            }
            double[] D_ = new double[D.length];
            for (int j = 0; j < D.length; j++) {
                D_[j] = Math.max(0, D[j]);
            }
            pop_with_fit[i] = Arrays.copyOf(pop[i], pop[i].length);
            pop_with_fit[i][pop_with_fit[i].length - 1] = dotProduct(D_, y);
        }
        return pop_with_fit;
    }

    public static double dotProduct(double[] a, double[] b) {
        double result = 0;
        for (int i = 0; i < a.length; i++) {
            result += a[i] * b[i];
        }
        return result;
    }

    public static double[][] roulette(double[][] oldPop, int numSols) {
        // Roulette wheel selection is a typical proportional selection function, where
        // the survival probability of individual i
        // is equal to the ratio of its fitness to the sum of the fitnesses of all
        // individuals.

        // Generate relative selection probabilities
        double minFit = 0;
        double totalFit = 0;
        for (int i = 0; i < oldPop.length; i++) {
            totalFit += oldPop[i][oldPop[i].length - 1] - minFit + 1;
        }
        double[] prob = new double[oldPop.length];
        for (int i = 0; i < oldPop.length; i++) {
            prob[i] = (oldPop[i][oldPop[i].length - 1] - minFit + 1) / totalFit;
        }
        for (int i = 1; i < prob.length; i++) {
            prob[i] += prob[i - 1];
        }

        // Generate random numbers and select individuals to pass on to the next
        // generation
        double[] rNums = new double[numSols];
        for (int i = 0; i < numSols; i++) {
            rNums[i] = Math.random();
        }
        Arrays.sort(rNums);

        double[][] newPop = new double[numSols][oldPop[0].length];
        int fitIn = 0, newIn = 0;
        while (newIn < numSols) {
            if (rNums[newIn] < prob[fitIn]) {
                newPop[newIn] = Arrays.copyOf(oldPop[fitIn], oldPop[0].length);
                newIn++;
            } else {
                fitIn++;
            }
        }
        return newPop;
    }

    private static int[] find(double[][] arr, double value) {
        int[] idx = new int[arr.length];
        int j = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i][arr[i].length - 1] == value) {
                idx[j++] = i;
            }
        }
        int[] r = new int[j];
        for (int i = 0; i < j; i++) {
            r[i] = idx[i];
        }
        return r;
    }

    private static double[][] concatenate(double[][] arr1, double[][] arr2) {
        double[][] r = new double[arr1.length + arr2.length][];
        for (int i = 0; i < arr1.length; i++) {
            r[i] = arr1[i];
        }
        for (int i = 0; i < arr2.length; i++) {
            r[arr1.length + i] = arr2[i];
        }
        return r;
    }

    private static double max(double[][] arr, int n) {
        double max = arr[0][n];
        for (int i = 1; i < arr.length; i++) {
            if (arr[i][n] > max) {
                max = arr[i][n];
            }
        }
        return max;
    }
}