import java.util.*;

public class main {

    public static int knapsack(int capacity, int[] W, int[] V) {
        int n = W.length;
        int[][] dp = new int[n + 1][capacity + 1];

        for (int i = 1; i <= n; i++) {
            for (int sz = 1; sz <= capacity; sz++) {
                dp[i][sz] = dp[i - 1][sz];

                if (sz >= W[i - 1]) {
                    dp[i][sz] = Math.max(dp[i][sz],
                            dp[i - 1][sz - W[i - 1]] + V[i - 1]);
                }
            }
        }

        return dp[n][capacity];
    }

    
    public static List<Integer> getSelectedItems(int capacity, int[] W, int[] V) {
        int n = W.length;
        int[][] dp = new int[n + 1][capacity + 1];

        for (int i = 1; i <= n; i++) {
            for (int sz = 1; sz <= capacity; sz++) {
                dp[i][sz] = dp[i - 1][sz];

                if (sz >= W[i - 1]) {
                    dp[i][sz] = Math.max(dp[i][sz],
                            dp[i - 1][sz - W[i - 1]] + V[i - 1]);
                }
            }
        }

        
        List<Integer> items = new ArrayList<>();
        int sz = capacity;

        for (int i = n; i > 0; i--) {
            if (dp[i][sz] != dp[i - 1][sz]) {
                items.add(i - 1);
                sz -= W[i - 1];
            }
        }

        Collections.reverse(items);
        return items;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of items: ");
        int n = sc.nextInt();

        int[] W = new int[n];
        int[] V = new int[n];

        System.out.println("Enter weights:");
        for (int i = 0; i < n; i++) W[i] = sc.nextInt();

        System.out.println("Enter values:");
        for (int i = 0; i < n; i++) V[i] = sc.nextInt();

        System.out.print("Enter capacity: ");
        int capacity = sc.nextInt();

        int maxValue = knapsack(capacity, W, V);
        List<Integer> selected = getSelectedItems(capacity, W, V);

        System.out.println("\nMaximum Value: " + maxValue);
        System.out.println("Selected Items (0-based index): " + selected);
    }
}