import org.apache.commons.math3.optim.linear.*;
import org.apache.commons.math3.optim.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class SecondOrderModelSolver {

    public static void main(String[] args) {
        // read a matrix A from a csv file
        double[][] A = new double[36][];
        int N_vars = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader("A.csv"));
            String line;
            int row = 0;
            while ((line = reader.readLine()) != null) {
                String[] cols = line.split(",");
                A[row] = new double[cols.length];
                for (int col = 0; col < cols.length; col++) {
                    if (row == 0) {
                        N_vars = cols.length;
                    }
                    A[row][col] = Double.parseDouble(cols[col]);
                }
                row++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // print A, N_vars
        System.out.println("A:");
        for (int i = 0; i < 36; i++) {
            for (int j = 0; j < N_vars; j++) {
                // format float with .3f
                System.out.printf("%.3f ", A[i][j]);
            }
            System.out.println();
        }
        System.out.println("N_vars = " + N_vars);

        double[] coeff_obj = new double[N_vars];
        for (int i = 0; i < N_vars; i++) {
            coeff_obj[i] = 1;
        }

        LinearObjectiveFunction f = new LinearObjectiveFunction(coeff_obj, 0);

        // create constraints: c_min <= A*x <= c_max, where A is loaded from csv file
        double c_min = 0.2;
        double c_max = 4;
        Collection<LinearConstraint> constraints = new ArrayList<>();
        for (int i = 0; i < 36; i++) {
            if (i == 0 || i == 25) {
                constraints.add(new LinearConstraint(A[i], Relationship.GEQ, c_min));
                constraints.add(new LinearConstraint(A[i], Relationship.LEQ, c_max));
                continue;
            }
            constraints.add(new LinearConstraint(A[i], Relationship.GEQ, c_min));
            constraints.add(new LinearConstraint(A[i], Relationship.LEQ, c_max));
        }

        for (int i = 0; i < N_vars; i++) {
            double[] coeff = new double[N_vars];
            coeff[i] = 1;
            constraints.add(new LinearConstraint(coeff, Relationship.GEQ, 0));
        }

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(new MaxIter(100), f, new LinearConstraintSet(constraints), GoalType.MINIMIZE);

        double[] solutionValues = solution.getPoint();
        for (int i = 0; i < solutionValues.length; i++) {
            System.out.println("x" + (i + 1) + " = " + solutionValues[i]);
        }
        System.out.println("Minimum value of z = " + (solution.getValue()));
    }
}
