import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class WebServer {
    
    public static class Item {
        String name;
        int weight;
        int value;
        public Item(String n, int w, int v) { name=n; weight=w; value=v; }
    }

    private static List<Item> items = new ArrayList<>();
    private static int capacity = 0;
    private static String lastResultHtml = "";

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new RouteHandler());
        server.createContext("/style.css", new StaticHandler("style.css", "text/css"));
        server.setExecutor(null); 
        server.start();
        System.out.println("No Apache Tomcat Needed!");
        System.out.println("Java Web Server successfully started at: http://localhost:8080");
        System.out.println("Leave this terminal running, and open the URL in your browser.");
    }

    static class StaticHandler implements HttpHandler {
        String file;
        String contentType;
        public StaticHandler(String f, String c) { file = f; contentType = c; }
        
        @Override
        public void handle(HttpExchange t) throws IOException {
            File tempFile = new File(file);
            if (!tempFile.exists()) {
                String response = "404 Not Found";
                t.sendResponseHeaders(404, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }
            t.getResponseHeaders().set("Content-Type", contentType);
            t.sendResponseHeaders(200, tempFile.length());
            OutputStream os = t.getResponseBody();
            Files.copy(tempFile.toPath(), os);
            os.close();
        }
    }

    static class RouteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if ("POST".equalsIgnoreCase(t.getRequestMethod())) {
                InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String formData = br.readLine();
                Map<String, String> params = parseFormData(formData);
                
                String action = params.get("action");
                if ("add".equals(action)) {
                    String name = params.get("name");
                    try {
                        int w = Integer.parseInt(params.get("weight"));
                        int v = Integer.parseInt(params.get("value"));
                        String capStr = params.get("capacity");
                        if (capStr != null && !capStr.isEmpty()) {
                            capacity = Integer.parseInt(capStr);
                        }
                        if (name != null && !name.trim().isEmpty() && w > 0 && v > 0) {
                            items.add(new Item(name.trim(), w, v));
                        }
                    } catch (Exception e) {}
                    lastResultHtml = "";
                } else if ("delete".equals(action)) {
                    try {
                        int idx = Integer.parseInt(params.get("index"));
                        if (idx >= 0 && idx < items.size()) {
                            items.remove(idx);
                        }
                        String capStr = params.get("capacity");
                        if (capStr != null && !capStr.isEmpty()) {
                            capacity = Integer.parseInt(capStr);
                        }
                    } catch (Exception e) {}
                    lastResultHtml = "";
                } else if ("optimize".equals(action)) {
                    try {
                        capacity = Integer.parseInt(params.get("capacity"));
                    } catch(Exception e) {}
                    runOptimization();
                } else if ("new".equals(action)) {
                    items.clear();
                    capacity = 0;
                    lastResultHtml = "";
                }
                
                t.getResponseHeaders().set("Location", "/");
                t.sendResponseHeaders(302, -1);
                return;
            }

            String html = new String(Files.readAllBytes(Paths.get("index.html")));
            html = html.replace("{{CAPACITY}}", capacity > 0 ? String.valueOf(capacity) : "");
            html = html.replace("{{TOTAL_ITEMS}}", String.valueOf(items.size()));
            
            StringBuilder itemsHtml = new StringBuilder();
            if (items.isEmpty()) {
                itemsHtml.append("<div class=\"Empty-State\" id=\"items-empty\"><p class=\"no-items\">No items added</p></div>");
            } else {
                itemsHtml.append("<div class=\"items-table-wrapper\"><div class=\"table-header\">");
                itemsHtml.append("<span class=\"col-name\"><strong>Name</strong></span>");
                itemsHtml.append("<span class=\"col-weight\"><strong>Weight</strong></span>");
                itemsHtml.append("<span class=\"col-value\"><strong>Value</strong></span>");
                itemsHtml.append("<span class=\"col-action\"></span></div><div id=\"items-list\">");
                
                for (int i = 0; i < items.size(); i++) {
                    Item it = items.get(i);
                    itemsHtml.append("<div class=\"table-row\">");
                    itemsHtml.append("<span class=\"col-name\">").append(escapeHtml(it.name)).append("</span>");
                    itemsHtml.append("<span class=\"col-weight\">").append(it.weight).append(" kg</span>");
                    itemsHtml.append("<span class=\"col-value\">$").append(it.value).append("</span>");
                    itemsHtml.append("<span class=\"col-action\">");
                    itemsHtml.append("<form action=\"/\" method=\"post\" style=\"display:inline;\">");
                    itemsHtml.append("<input type=\"hidden\" name=\"action\" value=\"delete\">");
                    itemsHtml.append("<input type=\"hidden\" name=\"index\" value=\"").append(i).append("\">");
                    itemsHtml.append("<button type=\"submit\" class=\"btn-delete\">Delete</button>");
                    itemsHtml.append("</form></span></div>");
                }
                itemsHtml.append("</div></div>");
            }
            html = html.replace("{{ITEMS_TABLE}}", itemsHtml.toString());
            html = html.replace("{{RESULTS_SECTION}}", lastResultHtml);

            if (!lastResultHtml.isEmpty()) {
                html = html.replace("id=\"config-view\"", "id=\"config-view\" style=\"display:none;\"");
            }

            t.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            byte[] response = html.getBytes();
            t.sendResponseHeaders(200, response.length);
            OutputStream os = t.getResponseBody();
            os.write(response);
            os.close();
        }
        
        private void runOptimization() {
            if (items.isEmpty() || capacity <= 0) return;
            
            int n = items.size();
            int[] W = new int[n];
            int[] V = new int[n];
            for (int i=0; i<n; i++) {
                W[i] = items.get(i).weight;
                V[i] = items.get(i).value;
            }

            int[][] dp = new int[n + 1][capacity + 1];
            for (int i = 1; i <= n; i++) {
                for (int sz = 1; sz <= capacity; sz++) {
                    dp[i][sz] = dp[i - 1][sz];
                    if (sz >= W[i - 1]) {
                        dp[i][sz] = Math.max(dp[i][sz], dp[i - 1][sz - W[i - 1]] + V[i - 1]);
                    }
                }
            }

            int maxValue = dp[n][capacity];
            List<Item> selectedItems = new ArrayList<>();
            int sz = capacity;
            int weightUsed = 0;

            for (int i = n; i > 0; i--) {
                if (dp[i][sz] != dp[i - 1][sz]) {
                    Item idx = items.get(i - 1);
                    selectedItems.add(idx);
                    weightUsed += idx.weight;
                    sz -= W[i - 1];
                }
            }
            Collections.reverse(selectedItems);
            
            String pctStr = String.format("%.1f", ((double)weightUsed / capacity) * 100);
            
            StringBuilder res = new StringBuilder();
            res.append("<div class=\"container slide-in\" id=\"results-view\">");
            res.append("<h1>Optimization Results</h1>");
            res.append("<div class=\"stats-row\">");
            res.append("<div class=\"stat\"><span class=\"stat-label\">Max Value</span><span class=\"stat-value\">$").append(maxValue).append("</span></div>");
            res.append("<div class=\"stat\"><span class=\"stat-label\">Weight Used</span><span class=\"stat-value\">").append(weightUsed).append(" kg</span></div>");
            res.append("<div class=\"stat\"><span class=\"stat-label\">Items Selected</span><span class=\"stat-value\">").append(selectedItems.size()).append("</span></div>");
            res.append("</div>");
            res.append("<div class=\"capacity-bar-section\"><span class=\"capacity-text\">Capacity: ").append(pctStr).append("% used</span>");
            res.append("<div class=\"progress-bar-bg\"><div class=\"progress-bar-fill\" style=\"width: ").append(pctStr).append("%\"></div></div></div>");
            
            res.append("<h2>Selected Items</h2><div class=\"items-table-wrapper\">");
            res.append("<div class=\"table-header result-header\"><span class=\"col-name\"><strong>Name</strong></span><span class=\"col-weight\"><strong>Weight</strong></span><span class=\"col-value\"><strong>Value</strong></span></div>");
            res.append("<div id=\"selected-items-list\">");
            for (Item it : selectedItems) {
                res.append("<div class=\"table-row result-row\">");
                res.append("<span class=\"col-name\">").append(escapeHtml(it.name)).append("</span>");
                res.append("<span class=\"col-weight\">").append(it.weight).append(" kg</span>");
                res.append("<span class=\"col-value\">$").append(it.value).append("</span>");
                res.append("</div>");
            }
            res.append("</div></div>");
            res.append("<div class=\"final-button\"><form action=\"/\" method=\"post\"><input type=\"hidden\" name=\"action\" value=\"new\"><button type=\"submit\" class=\"final-btn btn-new\">New Optimization</button></form></div>");
            res.append("</div>");
            
            lastResultHtml = res.toString();
        }

        private Map<String, String> parseFormData(String formData) {
            Map<String, String> map = new HashMap<>();
            if (formData == null) return map;
            String[] pairs = formData.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (kv.length > 1) {
                    try {
                        map.put(kv[0], java.net.URLDecoder.decode(kv[1], "UTF-8"));
                    } catch (Exception e) {}
                }
            }
            return map;
        }

        private String escapeHtml(String in) {
            if (in == null) return "";
            return in.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
        }
    }
}
