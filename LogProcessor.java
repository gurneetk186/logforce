import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class LogProcessor {

    static class LogTask implements Callable<Map<String, Object>> {

        private List<String> lines;

        public LogTask(List<String> lines) {
            this.lines = lines;
        }

        @Override
        public Map<String, Object> call() {

            int errorCount = 0;
            int warningCount = 0;
            Set<String> uniqueIPs = new HashSet<>();
            Map<String, Integer> statusCodes = new HashMap<>();

            for (String line : lines) {

                String[] parts = line.split(" ");
                if (parts.length == 0) continue;

                uniqueIPs.add(parts[0]);

                if (line.contains("[ERROR]")) errorCount++;
                if (line.contains("[WARNING]")) warningCount++;

                String statusCode = parts[parts.length - 1];
                statusCodes.put(statusCode,
                        statusCodes.getOrDefault(statusCode, 0) + 1);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("errors", errorCount);
            result.put("warnings", warningCount);
            result.put("ips", uniqueIPs);
            result.put("status", statusCodes);

            return result;
        }
    }

    public static void main(String[] args) throws Exception {

        File file = new File("sample.log");
        List<String> allLines = new ArrayList<>();

        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            allLines.add(scanner.nextLine());
        }
        scanner.close();

        int numThreads = 4;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        int chunkSize = allLines.size() / numThreads;
        List<Future<Map<String, Object>>> futures = new ArrayList<>();

        long startTime = System.nanoTime();

        for (int i = 0; i < numThreads; i++) {
            int start = i * chunkSize;
            int end = (i == numThreads - 1)
                    ? allLines.size()
                    : (i + 1) * chunkSize;

            List<String> subList = allLines.subList(start, end);
            futures.add(executor.submit(new LogTask(subList)));
        }

        int totalErrors = 0;
        int totalWarnings = 0;
        Set<String> combinedIPs = new HashSet<>();
        Map<String, Integer> combinedStatus = new HashMap<>();

        for (Future<Map<String, Object>> future : futures) {

            Map<String, Object> result = future.get();

            totalErrors += (int) result.get("errors");
            totalWarnings += (int) result.get("warnings");

            combinedIPs.addAll((Set<String>) result.get("ips"));

            Map<String, Integer> status =
                    (Map<String, Integer>) result.get("status");

            for (String key : status.keySet()) {
                combinedStatus.put(key,
                        combinedStatus.getOrDefault(key, 0) + status.get(key));
            }
        }

        executor.shutdown();

        long endTime = System.nanoTime();

        System.out.println("Errors: " + totalErrors);
        System.out.println("Warnings: " + totalWarnings);
        System.out.println("Unique IPs: " + combinedIPs.size());
        System.out.println("Status Codes: " + combinedStatus);
        System.out.println("Processing Time (ms): " +
                (endTime - startTime) / 1_000_000);
    }
}
