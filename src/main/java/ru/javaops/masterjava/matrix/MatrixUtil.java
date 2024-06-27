package ru.javaops.masterjava.matrix;

import java.util.Random;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;

/**
 * gkislin
 * 03.07.2016
 */
public class MatrixUtil {

    public static int[][] concurrentMultiply(int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException, ExecutionException {

        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        class ColumnMultipleResult {
            private final int col;
            private final int[] columnC;

            private ColumnMultipleResult(int col, int[] columnC) {
                this.col = col;
                this.columnC = columnC;
            }
        }

        final CompletionService<ColumnMultipleResult> completionService = new ExecutorCompletionService<>(executor);

        for (int j = 0; j < matrixSize; j++) {
            final int col = j;
            final int[] columnB = new int[matrixSize];
            for (int k = 0; k < matrixSize; k++) {
                columnB[k] = matrixB[k][col];
            }
            completionService.submit(() -> {
                final int[] columnC = new int[matrixSize];

                for (int row = 0; row < matrixSize; row++) {
                    final int[] rowA = matrixA[row];
                    int sum = 0;
                    for (int k = 0; k < matrixSize; k++) {
                        sum += rowA[k] * columnB[k];
                    }
                    columnC[row] = sum;
                }
                return new ColumnMultipleResult(col, columnC);
            });
        }

        for (int i = 0; i < matrixSize; i++) {
            ColumnMultipleResult res = completionService.take().get();
            for (int k = 0; k < matrixSize; k++) {
                matrixC[k][res.col] = res.columnC[k];
            }
        }
        return matrixC;

        //this algorithm in 3 times slower than the single-threaded implementation
//        final int[] columnB = new int[matrixSize];
//        Future<?>[] futures = new Future<?>[matrixSize * matrixSize];
//
//        for (int columns = 0; columns < matrixSize; columns++) {
//            for (int j = 0; j < matrixSize; j++) {
//                columnB[j] = matrixB[j][columns];
//            }
//            for (int row = 0; row < matrixSize; row++) {
//                int finalRow = row;
//                int finalColumns = columns;
//                futures[row * matrixSize + columns] = executor.submit(new Callable<Void>() {
//
//                    final int sum = 0;
//
//                    final int[] rowA = matrixA[finalRow];
//
//                    @Override
//                    public Void call() {
//                        int sum = 0;
//                        final int[] rowA = matrixA[finalRow];
//                        matrixC[finalRow][finalColumns] = 0;
//                        for (int k = 0; k < matrixSize; k++) {
//                            sum += rowA[k] * columnB[k];
//                        }
//                        matrixC[finalRow][finalColumns] = sum;
//                        return null;
//                    }
//                });
//            }
//        }
//        for (Future<?> future : futures) {
//            future.get();
//        }
//        return matrixC;
    }

    public static int[][] singleThreadMultiply(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];
        final int[] columnB = new int[matrixSize];

        for (int columns = 0; columns < matrixSize; columns++) {
            for (int j = 0; j < matrixSize; j++) {
                columnB[j] = matrixB[j][columns];
            }

            for (int row = 0; row < matrixSize; row++) {
                int sum = 0;
                final int[] rowA = matrixA[row];
                for (int k = 0; k < matrixSize; k++) {
                    sum += rowA[k] * columnB[k];
                }
                matrixC[row][columns] = sum;
            }
        }

        return matrixC;
    }

    public static int[][] create(int size) {
        int[][] matrix = new int[size][size];
        Random rn = new Random();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = rn.nextInt(10);
            }
        }
        return matrix;
    }

    public static boolean compare(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                if (matrixA[i][j] != matrixB[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
}
