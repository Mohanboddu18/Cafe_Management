package com.cafe.management.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class RootController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String rootHtml() {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Cafe Management Backend API</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
                    body { background: #0f172a; color: #f8fafc; display: flex; align-items: center; justify-content: center; min-height: 100vh; padding: 20px; }
                    .card { background: #1e293b; border-radius: 16px; padding: 40px; max-width: 600px; width: 100%; box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.5); border: 1px solid #334155; }
                    .badge { display: inline-block; background: #10b981; color: #022c22; font-weight: 700; padding: 4px 12px; border-radius: 9999px; font-size: 13px; margin-bottom: 16px; }
                    h1 { font-size: 26px; font-weight: 700; margin-bottom: 12px; color: #ffffff; }
                    p { color: #94a3b8; line-height: 1.6; margin-bottom: 24px; font-size: 15px; }
                    .stats-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 24px; }
                    .stat-box { background: #0f172a; padding: 14px; border-radius: 8px; border: 1px solid #334155; }
                    .stat-label { font-size: 12px; color: #64748b; text-transform: uppercase; font-weight: 600; }
                    .stat-val { font-size: 14px; color: #38bdf8; font-weight: 600; margin-top: 4px; }
                    .links { display: flex; gap: 12px; flex-wrap: wrap; }
                    .btn { background: #3b82f6; color: #fff; padding: 10px 18px; border-radius: 8px; text-decoration: none; font-size: 14px; font-weight: 600; transition: 0.2s; }
                    .btn:hover { background: #2563eb; }
                    .btn-secondary { background: #334155; }
                    .btn-secondary:hover { background: #475569; }
                </style>
            </head>
            <body>
                <div class="card">
                    <span class="badge">● ONLINE & HEALTHY</span>
                    <h1>Artisanal Cafe Backend API</h1>
                    <p>Spring Boot REST API is running and connected to the cloud online database.</p>
                    
                    <div class="stats-grid">
                        <div class="stat-box">
                            <div class="stat-label">Database Host</div>
                            <div class="stat-val">sql12.freesqldatabase.com</div>
                        </div>
                        <div class="stat-box">
                            <div class="stat-label">Database Name</div>
                            <div class="stat-val">sql12834862</div>
                        </div>
                        <div class="stat-box">
                            <div class="stat-label">API Status</div>
                            <div class="stat-val">HTTP 200 OK</div>
                        </div>
                        <div class="stat-box">
                            <div class="stat-label">Total Tables</div>
                            <div class="stat-val">21 Active Tables</div>
                        </div>
                    </div>

                    <div class="links">
                        <a href="/swagger-ui/index.html" class="btn">Explore Swagger Docs</a>
                        <a href="/api/customer/menu/categories" class="btn btn-secondary">Customer Menu API</a>
                        <a href="/api/health" class="btn btn-secondary">Health Endpoint</a>
                    </div>
                </div>
            </body>
            </html>
            """;
    }

    @GetMapping(value = "/api/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "UP");
        resp.put("service", "cafe-backend");
        resp.put("database", "Online MySQL (sql12834862)");
        resp.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("favicon.ico")
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.noContent().build();
    }
}
